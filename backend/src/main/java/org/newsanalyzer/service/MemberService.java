package org.newsanalyzer.service;

import org.newsanalyzer.exception.ResourceNotFoundException;
import org.newsanalyzer.model.Person;
import org.newsanalyzer.model.Person.Chamber;
import org.newsanalyzer.repository.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for member (Person) lookup operations.
 *
 * Provides business logic for the MemberController.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Service
@Transactional(readOnly = true)
public class MemberService {

    private static final Logger log = LoggerFactory.getLogger(MemberService.class);

    private final PersonRepository personRepository;

    public MemberService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    /**
     * Find all members with pagination.
     */
    public Page<Person> findAll(Pageable pageable) {
        return personRepository.findAll(pageable);
    }

    /**
     * Find member by ID.
     */
    public Optional<Person> findById(UUID id) {
        return personRepository.findById(id);
    }

    /**
     * Find member by BioGuide ID.
     */
    public Optional<Person> findByBioguideId(String bioguideId) {
        return personRepository.findByBioguideId(bioguideId);
    }

    /**
     * Find member by BioGuide ID or throw exception.
     */
    public Person getByBioguideId(String bioguideId) {
        return personRepository.findByBioguideId(bioguideId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with bioguideId: " + bioguideId));
    }

    /**
     * Search members by name.
     */
    public Page<Person> searchByName(String name, Pageable pageable) {
        return personRepository.searchByName(name, pageable);
    }

    /**
     * Search members by name (returns list).
     */
    public List<Person> searchByName(String name) {
        return personRepository.searchByName(name);
    }

    /**
     * Find members by state.
     */
    public Page<Person> findByState(String state, Pageable pageable) {
        return personRepository.findByState(state.toUpperCase(), pageable);
    }

    /**
     * Find members by state (returns list).
     */
    public List<Person> findByState(String state) {
        return personRepository.findByState(state.toUpperCase());
    }

    /**
     * Find members by chamber.
     */
    public Page<Person> findByChamber(Chamber chamber, Pageable pageable) {
        return personRepository.findByChamber(chamber, pageable);
    }

    /**
     * Find members by chamber (returns list).
     */
    public List<Person> findByChamber(Chamber chamber) {
        return personRepository.findByChamber(chamber);
    }

    /**
     * Find members by party.
     */
    public Page<Person> findByParty(String party, Pageable pageable) {
        return personRepository.findByParty(party, pageable);
    }

    /**
     * Get total member count.
     */
    public long count() {
        return personRepository.count();
    }

    /**
     * Get count by chamber.
     */
    public long countByChamber(Chamber chamber) {
        return personRepository.countByChamber(chamber);
    }

    /**
     * Get party distribution.
     */
    public List<Object[]> getPartyDistribution() {
        return personRepository.getPartyDistribution();
    }

    /**
     * Get state distribution.
     */
    public List<Object[]> getStateDistribution() {
        return personRepository.getStateDistribution();
    }

    /**
     * Find member by external ID (FEC, GovTrack, OpenSecrets, VoteSmart).
     *
     * @param type The ID type: fec, govtrack, opensecrets, votesmart
     * @param id The ID value
     * @return Optional containing the member if found
     * @throws IllegalArgumentException if type is not supported
     */
    public Optional<Person> findByExternalId(String type, String id) {
        if (type == null || id == null) {
            return Optional.empty();
        }

        String normalizedType = type.toLowerCase().trim();
        log.debug("Looking up member by external ID: type={}, id={}", normalizedType, id);

        return switch (normalizedType) {
            case "fec" -> personRepository.findByFecId(id);
            case "govtrack" -> {
                try {
                    yield personRepository.findByGovtrackId(Integer.parseInt(id));
                } catch (NumberFormatException e) {
                    log.warn("Invalid GovTrack ID (not an integer): {}", id);
                    yield Optional.empty();
                }
            }
            case "opensecrets" -> personRepository.findByOpensecretsId(id);
            case "votesmart" -> {
                try {
                    yield personRepository.findByVotesmartId(Integer.parseInt(id));
                } catch (NumberFormatException e) {
                    log.warn("Invalid VoteSmart ID (not an integer): {}", id);
                    yield Optional.empty();
                }
            }
            default -> {
                log.warn("Unsupported external ID type: {}", normalizedType);
                yield Optional.empty();
            }
        };
    }
}
