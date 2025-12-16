package org.newsanalyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.newsanalyzer.dto.AppointeeDTO;
import org.newsanalyzer.dto.ExecutivePositionDTO;
import org.newsanalyzer.model.*;
import org.newsanalyzer.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for executive branch appointee lookup operations.
 *
 * Provides business logic for querying appointees, positions, and Cabinet members.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppointeeService {

    private final PersonRepository personRepository;
    private final GovernmentPositionRepository positionRepository;
    private final PositionHoldingRepository holdingRepository;
    private final GovernmentOrganizationRepository orgRepository;

    /**
     * Cabinet-level position title patterns.
     * These are used to identify Cabinet members.
     */
    private static final List<String> CABINET_TITLE_PATTERNS = Arrays.asList(
            "Secretary of State",
            "Secretary of the Treasury",
            "Secretary of Defense",
            "Attorney General",
            "Secretary of the Interior",
            "Secretary of Agriculture",
            "Secretary of Commerce",
            "Secretary of Labor",
            "Secretary of Health and Human Services",
            "Secretary of Housing and Urban Development",
            "Secretary of Transportation",
            "Secretary of Energy",
            "Secretary of Education",
            "Secretary of Veterans Affairs",
            "Secretary of Homeland Security",
            "Vice President",
            "White House Chief of Staff",
            "Administrator of the Environmental Protection Agency",
            "Director of the Office of Management and Budget",
            "United States Trade Representative",
            "Ambassador to the United Nations",
            "Chair of the Council of Economic Advisers",
            "Administrator of the Small Business Administration"
    );

    /**
     * Get all current appointees (paginated).
     */
    public Page<AppointeeDTO> getAllAppointees(Pageable pageable) {
        Page<PositionHolding> holdings = holdingRepository.findAllCurrentExecutiveHoldings(pageable);
        List<AppointeeDTO> dtos = holdings.getContent().stream()
                .map(this::convertToAppointeeDTO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, holdings.getTotalElements());
    }

    /**
     * Get appointee by person ID.
     */
    public Optional<AppointeeDTO> getAppointeeById(UUID personId) {
        return personRepository.findById(personId)
                .flatMap(person -> {
                    // Find their current executive holding
                    List<PositionHolding> currentHoldings = holdingRepository
                            .findByPersonIdOrderByStartDateDesc(personId)
                            .stream()
                            .filter(PositionHolding::isCurrent)
                            .collect(Collectors.toList());

                    if (currentHoldings.isEmpty()) {
                        // Return person info without active position
                        return Optional.of(AppointeeDTO.builder()
                                .id(person.getId())
                                .firstName(person.getFirstName())
                                .lastName(person.getLastName())
                                .fullName(person.getFullName())
                                .current(false)
                                .status("No active position")
                                .build());
                    }

                    // Get the most recent holding
                    PositionHolding holding = currentHoldings.get(0);
                    return Optional.ofNullable(convertToAppointeeDTO(holding));
                });
    }

    /**
     * Search appointees by name or title.
     */
    public List<AppointeeDTO> searchAppointees(String query, int limit) {
        if (query == null || query.trim().length() < 2) {
            return Collections.emptyList();
        }

        Set<AppointeeDTO> results = new LinkedHashSet<>();

        // Search by person name
        List<Person> personMatches = personRepository.searchByName(query.trim());
        for (Person person : personMatches) {
            if (results.size() >= limit) break;

            List<PositionHolding> holdings = holdingRepository
                    .findByPersonIdOrderByStartDateDesc(person.getId())
                    .stream()
                    .filter(PositionHolding::isCurrent)
                    .collect(Collectors.toList());

            if (!holdings.isEmpty()) {
                AppointeeDTO dto = convertToAppointeeDTO(holdings.get(0));
                if (dto != null) {
                    results.add(dto);
                }
            }
        }

        // Search by position title
        if (results.size() < limit) {
            List<GovernmentPosition> positionMatches = positionRepository
                    .searchExecutivePositionsByTitle(query.trim());

            for (GovernmentPosition position : positionMatches) {
                if (results.size() >= limit) break;

                List<PositionHolding> currentHolders = holdingRepository
                        .findCurrentHoldersByPositionId(position.getId());

                for (PositionHolding holding : currentHolders) {
                    if (results.size() >= limit) break;
                    AppointeeDTO dto = convertToAppointeeDTO(holding);
                    if (dto != null) {
                        results.add(dto);
                    }
                }
            }
        }

        return new ArrayList<>(results);
    }

    /**
     * Get appointees by agency/organization.
     */
    public List<AppointeeDTO> getAppointeesByAgency(UUID orgId) {
        List<PositionHolding> holdings = holdingRepository
                .findCurrentExecutiveHoldingsByOrganizationId(orgId);

        return holdings.stream()
                .map(this::convertToAppointeeDTO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Get appointees by appointment type.
     */
    public List<AppointeeDTO> getAppointeesByType(AppointmentType type) {
        List<PositionHolding> holdings = holdingRepository
                .findCurrentExecutiveHoldingsByAppointmentType(type);

        return holdings.stream()
                .map(this::convertToAppointeeDTO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Get current Cabinet members.
     */
    public List<AppointeeDTO> getCabinetMembers() {
        List<AppointeeDTO> cabinetMembers = new ArrayList<>();

        // Get all Cabinet-level positions
        List<GovernmentPosition> cabinetPositions = positionRepository.findCabinetLevelPositions();

        // Filter to only true Cabinet positions
        for (GovernmentPosition position : cabinetPositions) {
            if (isCabinetPosition(position.getTitle())) {
                List<PositionHolding> currentHolders = holdingRepository
                        .findCurrentHoldersByPositionId(position.getId());

                for (PositionHolding holding : currentHolders) {
                    AppointeeDTO dto = convertToAppointeeDTO(holding);
                    if (dto != null) {
                        cabinetMembers.add(dto);
                    }
                }
            }
        }

        // Sort by position title for consistent ordering
        cabinetMembers.sort(Comparator.comparing(AppointeeDTO::getPositionTitle));

        return cabinetMembers;
    }

    /**
     * Get all executive positions (paginated).
     */
    public Page<ExecutivePositionDTO> getAllExecutivePositions(Pageable pageable) {
        Page<GovernmentPosition> positions = positionRepository.findAllExecutivePositions(pageable);

        List<ExecutivePositionDTO> dtos = positions.getContent().stream()
                .map(this::convertToExecutivePositionDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, positions.getTotalElements());
    }

    /**
     * Get vacant executive positions.
     */
    public List<ExecutivePositionDTO> getVacantPositions() {
        List<GovernmentPosition> allPositions = positionRepository.findAllExecutivePositions();

        return allPositions.stream()
                .filter(pos -> !holdingRepository.hasCurrentHolder(pos.getId()))
                .map(pos -> ExecutivePositionDTO.forVacant(pos, getOrganization(pos.getOrganizationId())))
                .collect(Collectors.toList());
    }

    /**
     * Check if a position title matches Cabinet-level patterns.
     */
    private boolean isCabinetPosition(String title) {
        if (title == null) return false;
        String lowerTitle = title.toLowerCase();

        for (String pattern : CABINET_TITLE_PATTERNS) {
            if (lowerTitle.contains(pattern.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convert PositionHolding to AppointeeDTO.
     */
    private AppointeeDTO convertToAppointeeDTO(PositionHolding holding) {
        if (holding == null) return null;

        Person person = personRepository.findById(holding.getPersonId()).orElse(null);
        GovernmentPosition position = positionRepository.findById(holding.getPositionId()).orElse(null);
        GovernmentOrganization org = null;

        if (position != null && position.getOrganizationId() != null) {
            org = orgRepository.findById(position.getOrganizationId()).orElse(null);
        }

        return AppointeeDTO.from(person, position, holding, org);
    }

    /**
     * Convert GovernmentPosition to ExecutivePositionDTO.
     */
    private ExecutivePositionDTO convertToExecutivePositionDTO(GovernmentPosition position) {
        GovernmentOrganization org = getOrganization(position.getOrganizationId());

        // Find current holder
        String currentHolderName = null;
        List<PositionHolding> currentHolders = holdingRepository
                .findCurrentHoldersByPositionId(position.getId());

        if (!currentHolders.isEmpty()) {
            UUID personId = currentHolders.get(0).getPersonId();
            Person person = personRepository.findById(personId).orElse(null);
            if (person != null) {
                currentHolderName = person.getFullName();
            }
        }

        return ExecutivePositionDTO.from(position, org, currentHolderName);
    }

    /**
     * Get organization by ID (with null handling).
     */
    private GovernmentOrganization getOrganization(UUID orgId) {
        if (orgId == null) return null;
        return orgRepository.findById(orgId).orElse(null);
    }
}
