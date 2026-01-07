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
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PresidencyService {

    private static final String VP_POSITION_TITLE = "Vice President of the United States";
    private static final String COS_POSITION_TITLE = "White House Chief of Staff";

    private final PresidencyRepository presidencyRepository;
    private final PersonRepository personRepository;
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

        // Batch load persons for efficiency
        Set<UUID> personIds = presidencies.getContent().stream()
                .map(Presidency::getPersonId)
                .collect(Collectors.toSet());
        Map<UUID, Person> personMap = loadPersonMap(personIds);

        // Batch load EO counts
        Map<UUID, Long> eoCounts = loadEoCounts(presidencies.getContent().stream()
                .map(Presidency::getId)
                .collect(Collectors.toSet()));

        return presidencies.map(p -> PresidencyDTO.from(
                p,
                personMap.get(p.getPersonId()),
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
        Person person = personRepository.findById(presidency.getPersonId()).orElse(null);
        int eoCount = (int) executiveOrderRepository.countByPresidencyId(presidency.getId());
        List<VicePresidentDTO> vps = getVicePresidents(presidency.getId());

        return PresidencyDTO.from(presidency, person, eoCount, vps);
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

        // Load persons
        Set<UUID> personIds = holdings.stream()
                .map(PositionHolding::getPersonId)
                .collect(Collectors.toSet());
        Map<UUID, Person> personMap = loadPersonMap(personIds);

        return holdings.stream()
                .map(h -> {
                    Person p = personMap.get(h.getPersonId());
                    String termLabel = formatTermLabel(h.getStartDate(), h.getEndDate());
                    return VicePresidentDTO.builder()
                            .personId(h.getPersonId())
                            .fullName(p != null ? buildFullName(p) : "Unknown")
                            .firstName(p != null ? p.getFirstName() : null)
                            .lastName(p != null ? p.getLastName() : null)
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

        Set<UUID> personIds = holdings.stream()
                .map(PositionHolding::getPersonId)
                .collect(Collectors.toSet());
        Map<UUID, Person> personMap = loadPersonMap(personIds);

        return holdings.stream()
                .map(h -> {
                    Person p = personMap.get(h.getPersonId());
                    return OfficeholderDTO.builder()
                            .holdingId(h.getId())
                            .personId(h.getPersonId())
                            .fullName(p != null ? buildFullName(p) : "Unknown")
                            .firstName(p != null ? p.getFirstName() : null)
                            .lastName(p != null ? p.getLastName() : null)
                            .positionTitle(positionTitle)
                            .startDate(h.getStartDate())
                            .endDate(h.getEndDate())
                            .termLabel(formatTermLabel(h.getStartDate(), h.getEndDate()))
                            .imageUrl(p != null ? p.getImageUrl() : null)
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
     * Batch load persons by IDs into a map.
     */
    private Map<UUID, Person> loadPersonMap(Set<UUID> personIds) {
        if (personIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return personRepository.findAllById(personIds).stream()
                .collect(Collectors.toMap(Person::getId, p -> p));
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
     * Build full name from Person entity.
     */
    private String buildFullName(Person person) {
        if (person == null) return null;

        StringBuilder sb = new StringBuilder();
        sb.append(person.getFirstName());

        if (person.getMiddleName() != null && !person.getMiddleName().isEmpty()) {
            sb.append(" ").append(person.getMiddleName());
        }

        sb.append(" ").append(person.getLastName());

        if (person.getSuffix() != null && !person.getSuffix().isEmpty()) {
            sb.append(" ").append(person.getSuffix());
        }

        return sb.toString();
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
