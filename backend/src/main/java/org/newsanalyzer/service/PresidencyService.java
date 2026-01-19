package org.newsanalyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.newsanalyzer.dto.ExecutiveOrderDTO;
import org.newsanalyzer.dto.PresidencyAdministrationDTO;
import org.newsanalyzer.dto.PresidencyAdministrationDTO.CabinetMemberDTO;
import org.newsanalyzer.dto.PresidencyAdministrationDTO.OfficeholderDTO;
import org.newsanalyzer.dto.PresidencyDTO;
import org.newsanalyzer.dto.PresidencyDTO.VicePresidentDTO;
import org.newsanalyzer.model.*;
import org.newsanalyzer.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for presidency data access and business logic.
 *
 * Part of ARCH-1.7: Updated to use Individual instead of Person.
 *
 * @author James (Dev Agent)
 * @since 3.0.0
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PresidencyService {

    private static final String VP_POSITION_TITLE = "Vice President of the United States";
    private static final String COS_POSITION_TITLE = "White House Chief of Staff";

    private final PresidencyRepository presidencyRepository;
    private final IndividualRepository individualRepository;
    private final ExecutiveOrderRepository executiveOrderRepository;
    private final PositionHoldingRepository positionHoldingRepository;
    private final GovernmentPositionRepository positionRepository;
    private final GovernmentOrganizationRepository organizationRepository;

    // =====================================================================
    // List Presidencies
    // =====================================================================

    /**
     * Get paginated list of presidencies, most recent first.
     *
     * @param page page number (0-based)
     * @param size page size
     * @return page of PresidencyDTO
     */
    public Page<PresidencyDTO> listPresidencies(int page, int size) {
        log.debug("Listing presidencies - page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "number"));
        Page<Presidency> presidencies = presidencyRepository.findAll(pageable);

        // Batch load individuals for efficiency
        Set<UUID> individualIds = presidencies.getContent().stream()
                .map(Presidency::getIndividualId)
                .collect(Collectors.toSet());
        Map<UUID, Individual> individualMap = loadIndividualMap(individualIds);

        // Batch load EO counts
        Map<UUID, Long> eoCounts = loadEoCounts(presidencies.getContent().stream()
                .map(Presidency::getId)
                .collect(Collectors.toSet()));

        return presidencies.map(p -> PresidencyDTO.from(
                p,
                individualMap.get(p.getIndividualId()),
                eoCounts.getOrDefault(p.getId(), 0L).intValue(),
                null  // VPs loaded separately for detail view
        ));
    }

    // =====================================================================
    // Get Single Presidency
    // =====================================================================

    /**
     * Get presidency by ID with full details.
     *
     * @param id presidency UUID
     * @return Optional containing PresidencyDTO if found
     */
    public Optional<PresidencyDTO> getPresidencyById(UUID id) {
        log.debug("Getting presidency by id={}", id);
        return presidencyRepository.findById(id)
                .map(this::toDetailDTO);
    }

    /**
     * Get presidency by number (1-47).
     *
     * @param number presidency number
     * @return Optional containing PresidencyDTO if found
     */
    public Optional<PresidencyDTO> getPresidencyByNumber(Integer number) {
        log.debug("Getting presidency by number={}", number);
        return presidencyRepository.findByNumber(number)
                .map(this::toDetailDTO);
    }

    /**
     * Get the current presidency (no end date).
     *
     * @return Optional containing current PresidencyDTO
     */
    public Optional<PresidencyDTO> getCurrentPresidency() {
        log.debug("Getting current presidency");
        return presidencyRepository.findFirstByEndDateIsNullOrderByNumberDesc()
                .map(this::toDetailDTO);
    }

    // =====================================================================
    // Executive Orders
    // =====================================================================

    /**
     * Get paginated list of executive orders for a presidency.
     *
     * @param presidencyId presidency UUID
     * @param page page number (0-based)
     * @param size page size
     * @return page of ExecutiveOrderDTO
     */
    public Page<ExecutiveOrderDTO> getExecutiveOrders(UUID presidencyId, int page, int size) {
        log.debug("Getting executive orders for presidency={}, page={}, size={}", presidencyId, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "signingDate"));
        return executiveOrderRepository.findByPresidencyId(presidencyId, pageable)
                .map(ExecutiveOrderDTO::from);
    }

    // =====================================================================
    // Administration (VP, CoS, Cabinet)
    // =====================================================================

    /**
     * Get administration data (VP, CoS, Cabinet) for a presidency.
     *
     * @param presidencyId presidency UUID
     * @return Optional containing PresidencyAdministrationDTO if presidency exists
     */
    public Optional<PresidencyAdministrationDTO> getAdministration(UUID presidencyId) {
        log.debug("Getting administration for presidency={}", presidencyId);

        return presidencyRepository.findById(presidencyId).map(presidency -> {
            // Get VP position holdings
            List<OfficeholderDTO> vps = getPositionHoldersByTitle(presidencyId, VP_POSITION_TITLE);

            // Get CoS position holdings
            List<OfficeholderDTO> chiefs = getPositionHoldersByTitle(presidencyId, COS_POSITION_TITLE);

            // Get Cabinet members (placeholder - can be expanded)
            List<CabinetMemberDTO> cabinet = getCabinetMembers(presidencyId);

            return PresidencyAdministrationDTO.builder()
                    .presidencyId(presidencyId)
                    .presidencyNumber(presidency.getNumber())
                    .presidencyLabel(presidency.getOrdinalLabel() + " Presidency")
                    .vicePresidents(vps)
                    .chiefsOfStaff(chiefs)
                    .cabinetMembers(cabinet)
                    .build();
        });
    }

    // =====================================================================
    // Helper Methods
    // =====================================================================

    /**
     * Convert Presidency to detailed DTO with VP list.
     */
    private PresidencyDTO toDetailDTO(Presidency presidency) {
        Individual individual = individualRepository.findById(presidency.getIndividualId()).orElse(null);
        int eoCount = (int) executiveOrderRepository.countByPresidencyId(presidency.getId());
        List<VicePresidentDTO> vps = getVicePresidents(presidency.getId());

        return PresidencyDTO.from(presidency, individual, eoCount, vps);
    }

    /**
     * Get Vice Presidents for a presidency.
     */
    private List<VicePresidentDTO> getVicePresidents(UUID presidencyId) {
        // Find VP position
        Optional<GovernmentPosition> vpPosition = positionRepository.findByTitle(VP_POSITION_TITLE);
        if (vpPosition.isEmpty()) {
            return Collections.emptyList();
        }

        // Find holdings linked to this presidency for VP position
        List<PositionHolding> holdings = positionHoldingRepository
                .findByPositionId(vpPosition.get().getId())
                .stream()
                .filter(h -> presidencyId.equals(h.getPresidencyId()))
                .sorted(Comparator.comparing(PositionHolding::getStartDate))
                .collect(Collectors.toList());

        // Load individuals
        Set<UUID> individualIds = holdings.stream()
                .map(PositionHolding::getIndividualId)
                .collect(Collectors.toSet());
        Map<UUID, Individual> individualMap = loadIndividualMap(individualIds);

        return holdings.stream()
                .map(h -> {
                    Individual ind = individualMap.get(h.getIndividualId());
                    String termLabel = formatTermLabel(h.getStartDate(), h.getEndDate());
                    return VicePresidentDTO.builder()
                            .individualId(h.getIndividualId())
                            .fullName(ind != null ? ind.getFullName() : "Unknown")
                            .firstName(ind != null ? ind.getFirstName() : null)
                            .lastName(ind != null ? ind.getLastName() : null)
                            .startDate(h.getStartDate())
                            .endDate(h.getEndDate())
                            .termLabel(termLabel)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Get officeholders by position title for a presidency.
     */
    private List<OfficeholderDTO> getPositionHoldersByTitle(UUID presidencyId, String positionTitle) {
        Optional<GovernmentPosition> position = positionRepository.findByTitle(positionTitle);
        if (position.isEmpty()) {
            return Collections.emptyList();
        }

        List<PositionHolding> holdings = positionHoldingRepository
                .findByPositionId(position.get().getId())
                .stream()
                .filter(h -> presidencyId.equals(h.getPresidencyId()))
                .sorted(Comparator.comparing(PositionHolding::getStartDate))
                .collect(Collectors.toList());

        Set<UUID> individualIds = holdings.stream()
                .map(PositionHolding::getIndividualId)
                .collect(Collectors.toSet());
        Map<UUID, Individual> individualMap = loadIndividualMap(individualIds);

        return holdings.stream()
                .map(h -> {
                    Individual ind = individualMap.get(h.getIndividualId());
                    return OfficeholderDTO.builder()
                            .holdingId(h.getId())
                            .individualId(h.getIndividualId())
                            .fullName(ind != null ? ind.getFullName() : "Unknown")
                            .firstName(ind != null ? ind.getFirstName() : null)
                            .lastName(ind != null ? ind.getLastName() : null)
                            .positionTitle(positionTitle)
                            .startDate(h.getStartDate())
                            .endDate(h.getEndDate())
                            .termLabel(formatTermLabel(h.getStartDate(), h.getEndDate()))
                            .imageUrl(ind != null ? ind.getImageUrl() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Get Cabinet members for a presidency (placeholder - returns empty for now).
     * Can be expanded to query PAS positions linked to the presidency.
     */
    private List<CabinetMemberDTO> getCabinetMembers(UUID presidencyId) {
        // TODO: Implement Cabinet member lookup when PAS data is linked to presidencies
        return Collections.emptyList();
    }

    /**
     * Batch load individuals by IDs into a map.
     */
    private Map<UUID, Individual> loadIndividualMap(Set<UUID> individualIds) {
        if (individualIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return individualRepository.findAllById(individualIds).stream()
                .collect(Collectors.toMap(Individual::getId, i -> i));
    }

    /**
     * Batch load EO counts by presidency IDs.
     */
    private Map<UUID, Long> loadEoCounts(Set<UUID> presidencyIds) {
        if (presidencyIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<UUID, Long> counts = new HashMap<>();
        for (UUID id : presidencyIds) {
            counts.put(id, executiveOrderRepository.countByPresidencyId(id));
        }
        return counts;
    }

    /**
     * Format term label from dates.
     */
    private String formatTermLabel(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) return "Unknown";
        if (endDate != null) {
            return String.format("%d-%d", startDate.getYear(), endDate.getYear());
        } else {
            return String.format("%d-present", startDate.getYear());
        }
    }
}
