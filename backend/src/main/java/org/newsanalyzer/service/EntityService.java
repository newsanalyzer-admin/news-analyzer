package org.newsanalyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.newsanalyzer.dto.CreateEntityRequest;
import org.newsanalyzer.dto.EntityDTO;
import org.newsanalyzer.model.Entity;
import org.newsanalyzer.model.EntityType;
import org.newsanalyzer.repository.EntityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for Entity operations.
 *
 * Handles:
 * - CRUD operations
 * - Schema.org mapping (via SchemaOrgMapper)
 * - Entity search and filtering
 * - Data validation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EntityService {

    private final EntityRepository entityRepository;
    private final SchemaOrgMapper schemaOrgMapper;

    /**
     * Create a new entity with automatic Schema.org mapping
     */
    @Transactional
    public EntityDTO createEntity(CreateEntityRequest request) {
        log.info("Creating entity: type={}, name={}", request.getEntityType(), request.getName());

        Entity entity = new Entity();
        entity.setEntityType(request.getEntityType());
        entity.setName(request.getName());
        entity.setProperties(request.getProperties() != null ? request.getProperties() : Map.of());
        entity.setSource(request.getSource());
        entity.setConfidenceScore(request.getConfidenceScore() != null ? request.getConfidenceScore() : 1.0f);
        entity.setVerified(request.getVerified() != null ? request.getVerified() : false);

        // Auto-generate Schema.org type if not provided
        if (request.getSchemaOrgType() == null) {
            String schemaOrgType = schemaOrgMapper.getSchemaOrgType(request.getEntityType());
            entity.setSchemaOrgType(schemaOrgType);
            log.debug("Auto-mapped entity type {} to Schema.org type {}", request.getEntityType(), schemaOrgType);
        } else {
            entity.setSchemaOrgType(request.getSchemaOrgType());
        }

        // Auto-generate Schema.org JSON-LD if not provided
        if (request.getSchemaOrgData() == null || request.getSchemaOrgData().isEmpty()) {
            Map<String, Object> jsonLd = schemaOrgMapper.generateJsonLd(entity);
            entity.setSchemaOrgData(jsonLd);
            log.debug("Auto-generated Schema.org JSON-LD for entity: {}", entity.getName());
        } else {
            // Enrich provided data
            Map<String, Object> enriched = schemaOrgMapper.enrichSchemaOrgData(
                request.getSchemaOrgData(),
                entity
            );
            entity.setSchemaOrgData(enriched);
        }

        Entity saved = entityRepository.save(entity);
        log.info("Created entity: id={}, name={}", saved.getId(), saved.getName());

        return toDTO(saved);
    }

    /**
     * Get entity by ID
     */
    @Transactional(readOnly = true)
    public EntityDTO getEntityById(UUID id) {
        log.debug("Fetching entity by id: {}", id);
        Entity entity = entityRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Entity not found: " + id));
        return toDTO(entity);
    }

    /**
     * Get all entities
     */
    @Transactional(readOnly = true)
    public List<EntityDTO> getAllEntities() {
        log.debug("Fetching all entities");
        return entityRepository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get entities by type
     */
    @Transactional(readOnly = true)
    public List<EntityDTO> getEntitiesByType(EntityType entityType) {
        log.debug("Fetching entities by type: {}", entityType);
        return entityRepository.findByEntityType(entityType).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get entities by Schema.org type
     */
    @Transactional(readOnly = true)
    public List<EntityDTO> getEntitiesBySchemaOrgType(String schemaOrgType) {
        log.debug("Fetching entities by Schema.org type: {}", schemaOrgType);
        return entityRepository.findBySchemaOrgType(schemaOrgType).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Search entities by name
     */
    @Transactional(readOnly = true)
    public List<EntityDTO> searchEntitiesByName(String nameFragment) {
        log.debug("Searching entities by name: {}", nameFragment);
        return entityRepository.findByNameContainingIgnoreCase(nameFragment).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Full-text search
     */
    @Transactional(readOnly = true)
    public List<EntityDTO> fullTextSearch(String searchText, int limit) {
        log.debug("Full-text search: text={}, limit={}", searchText, limit);
        return entityRepository.fullTextSearch(searchText, limit).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get recent entities
     */
    @Transactional(readOnly = true)
    public List<EntityDTO> getRecentEntities(int days) {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(days);
        log.debug("Fetching entities created after: {}", fromDate);
        return entityRepository.findRecentEntities(fromDate).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Update entity
     */
    @Transactional
    public EntityDTO updateEntity(UUID id, CreateEntityRequest request) {
        log.info("Updating entity: id={}", id);

        Entity entity = entityRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Entity not found: " + id));

        // Update fields
        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getEntityType() != null) {
            entity.setEntityType(request.getEntityType());
            // Regenerate Schema.org type
            entity.setSchemaOrgType(schemaOrgMapper.getSchemaOrgType(request.getEntityType()));
        }
        if (request.getProperties() != null) {
            entity.setProperties(request.getProperties());
        }
        if (request.getVerified() != null) {
            entity.setVerified(request.getVerified());
        }

        // Regenerate Schema.org JSON-LD
        Map<String, Object> jsonLd = schemaOrgMapper.generateJsonLd(entity);
        entity.setSchemaOrgData(jsonLd);

        Entity updated = entityRepository.save(entity);
        log.info("Updated entity: id={}, name={}", updated.getId(), updated.getName());

        return toDTO(updated);
    }

    /**
     * Delete entity
     */
    @Transactional
    public void deleteEntity(UUID id) {
        log.info("Deleting entity: id={}", id);
        entityRepository.deleteById(id);
    }

    /**
     * Verify entity
     */
    @Transactional
    public EntityDTO verifyEntity(UUID id) {
        log.info("Verifying entity: id={}", id);
        Entity entity = entityRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Entity not found: " + id));

        entity.setVerified(true);
        Entity saved = entityRepository.save(entity);

        return toDTO(saved);
    }

    /**
     * Convert Entity to EntityDTO
     */
    private EntityDTO toDTO(Entity entity) {
        EntityDTO dto = new EntityDTO();
        dto.setId(entity.getId());
        dto.setEntityType(entity.getEntityType());
        dto.setName(entity.getName());
        dto.setProperties(entity.getProperties());
        dto.setSchemaOrgType(entity.getSchemaOrgType());
        dto.setSchemaOrgData(entity.getSchemaOrgData());
        dto.setSource(entity.getSource());
        dto.setConfidenceScore(entity.getConfidenceScore());
        dto.setVerified(entity.getVerified());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
