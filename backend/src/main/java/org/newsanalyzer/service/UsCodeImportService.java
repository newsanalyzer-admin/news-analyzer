package org.newsanalyzer.service;

import org.newsanalyzer.dto.ParsedStatuteSection;
import org.newsanalyzer.dto.UsCodeImportResult;
import org.newsanalyzer.model.Statute;
import org.newsanalyzer.repository.StatuteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for importing US Code data from uscode.house.gov.
 * Orchestrates download, parsing, and database persistence.
 *
 * Features:
 * - Batch inserts for performance (100 records per batch)
 * - Upsert logic on usc_identifier for idempotent imports
 * - Progress tracking and statistics
 * - Error handling with partial import support
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Service
public class UsCodeImportService {

    private static final Logger log = LoggerFactory.getLogger(UsCodeImportService.class);

    /**
     * Batch size for database inserts.
     */
    private static final int BATCH_SIZE = 100;

    /**
     * Import source identifier.
     */
    private static final String IMPORT_SOURCE = "USCODE";

    private final UsCodeDownloadService downloadService;
    private final UslmXmlParser xmlParser;
    private final StatuteRepository statuteRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public UsCodeImportService(UsCodeDownloadService downloadService,
                               UslmXmlParser xmlParser,
                               StatuteRepository statuteRepository) {
        this.downloadService = downloadService;
        this.xmlParser = xmlParser;
        this.statuteRepository = statuteRepository;
    }

    /**
     * Import a single US Code title.
     *
     * @param titleNumber Title number to import (1-54)
     * @param releasePoint Release point (null for default)
     * @return Import result with statistics
     */
    public UsCodeImportResult importTitle(int titleNumber, String releasePoint) {
        String effectiveReleasePoint = releasePoint != null ? releasePoint : downloadService.getDefaultReleasePoint();
        UsCodeImportResult result = UsCodeImportResult.forTitle(titleNumber, effectiveReleasePoint);

        log.info("Starting import of US Code Title {} (release point: {})", titleNumber, effectiveReleasePoint);

        try (InputStream xmlStream = downloadService.downloadTitle(titleNumber, effectiveReleasePoint)) {
            importFromStream(xmlStream, effectiveReleasePoint, result);
            result.markSuccess();

            log.info("Completed import of Title {}: {} inserted, {} updated, {} failed",
                    titleNumber, result.getSectionsInserted(), result.getSectionsUpdated(), result.getSectionsFailed());

        } catch (IOException e) {
            log.error("Failed to download Title {}: {}", titleNumber, e.getMessage());
            result.markFailed("Download failed: " + e.getMessage());
        } catch (XMLStreamException e) {
            log.error("Failed to parse Title {}: {}", titleNumber, e.getMessage());
            result.markFailed("Parse failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error importing Title {}: {}", titleNumber, e.getMessage(), e);
            result.markFailed("Unexpected error: " + e.getMessage());
        }

        return result;
    }

    /**
     * Import all US Code titles.
     * This is a long-running operation.
     *
     * @param releasePoint Release point (null for default)
     * @return Aggregated import result
     */
    public UsCodeImportResult importAllTitles(String releasePoint) {
        String effectiveReleasePoint = releasePoint != null ? releasePoint : downloadService.getDefaultReleasePoint();
        UsCodeImportResult aggregateResult = UsCodeImportResult.forFullImport(effectiveReleasePoint);

        log.info("Starting full US Code import (release point: {})", effectiveReleasePoint);

        List<Integer> titles = downloadService.getAvailableTitles();
        int successCount = 0;
        int failCount = 0;

        for (int titleNumber : titles) {
            log.info("Importing Title {} of {}...", titleNumber, titles.size());

            UsCodeImportResult titleResult = importTitle(titleNumber, effectiveReleasePoint);

            aggregateResult.setSectionsInserted(
                    aggregateResult.getSectionsInserted() + titleResult.getSectionsInserted());
            aggregateResult.setSectionsUpdated(
                    aggregateResult.getSectionsUpdated() + titleResult.getSectionsUpdated());
            aggregateResult.setSectionsFailed(
                    aggregateResult.getSectionsFailed() + titleResult.getSectionsFailed());
            aggregateResult.setTotalProcessed(
                    aggregateResult.getTotalProcessed() + titleResult.getTotalProcessed());

            if (titleResult.isSuccess()) {
                successCount++;
            } else {
                failCount++;
                aggregateResult.addError("Title " + titleNumber + ": " + titleResult.getErrorMessage());
            }
        }

        if (failCount == 0) {
            aggregateResult.markSuccess();
            log.info("Full import completed successfully: {} titles, {} sections",
                    successCount, aggregateResult.getTotalProcessed());
        } else {
            aggregateResult.markFailed(String.format("%d of %d titles failed", failCount, titles.size()));
            log.warn("Full import completed with errors: {} successful, {} failed",
                    successCount, failCount);
        }

        return aggregateResult;
    }

    /**
     * Import from an XML input stream.
     * Used internally and for testing with pre-downloaded files.
     *
     * Note: No @Transactional here - each statute is saved in its own transaction
     * to prevent cascade failures when one record has issues.
     *
     * @param xmlStream XML input stream
     * @param releasePoint Release point for metadata
     * @param result Result object to update with statistics
     */
    public void importFromStream(InputStream xmlStream, String releasePoint, UsCodeImportResult result)
            throws XMLStreamException {

        List<Statute> batch = new ArrayList<>(BATCH_SIZE);

        xmlParser.parseStream(xmlStream, section -> {
            try {
                Statute statute = convertToEntity(section, releasePoint);
                batch.add(statute);
                result.setTotalProcessed(result.getTotalProcessed() + 1);

                if (batch.size() >= BATCH_SIZE) {
                    saveBatch(batch, result);
                    batch.clear();

                    if (result.getTotalProcessed() % 1000 == 0) {
                        log.info("Progress: {} sections processed", result.getTotalProcessed());
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to process section {}: {}", section.getUscIdentifier(), e.getMessage());
                result.addError(section.getUscIdentifier() + ": " + e.getMessage());
            }
        });

        // Save remaining batch
        if (!batch.isEmpty()) {
            saveBatch(batch, result);
        }
    }

    /**
     * Save a batch of statutes with upsert logic.
     * Each statute is saved individually to prevent transaction cascade failures.
     */
    private void saveBatch(List<Statute> batch, UsCodeImportResult result) {
        for (Statute statute : batch) {
            saveStatuteIndividually(statute, result);
        }
    }

    /**
     * Save a single statute with error recovery.
     * Clears EntityManager on failure to prevent transaction cascade.
     */
    @Transactional
    public void saveStatuteIndividually(Statute statute, UsCodeImportResult result) {
        try {
            Optional<Statute> existing = statuteRepository.findByUscIdentifier(statute.getUscIdentifier());

            if (existing.isPresent()) {
                // Update existing record
                Statute toUpdate = existing.get();
                updateExistingStatute(toUpdate, statute);
                statuteRepository.saveAndFlush(toUpdate);
                result.setSectionsUpdated(result.getSectionsUpdated() + 1);
            } else {
                // Insert new record
                statuteRepository.saveAndFlush(statute);
                result.setSectionsInserted(result.getSectionsInserted() + 1);
            }
        } catch (Exception e) {
            log.warn("Failed to save statute {}: {}", statute.getUscIdentifier(), e.getMessage());
            result.addError(statute.getUscIdentifier() + ": " + e.getMessage());
            result.setSectionsFailed(result.getSectionsFailed() + 1);
            // Clear persistence context to recover from failed transaction state
            entityManager.clear();
        }
    }

    /**
     * Update an existing statute with new data.
     */
    private void updateExistingStatute(Statute existing, Statute updated) {
        existing.setTitleNumber(updated.getTitleNumber());
        existing.setTitleName(updated.getTitleName());
        existing.setChapterNumber(updated.getChapterNumber());
        existing.setChapterName(updated.getChapterName());
        existing.setSectionNumber(updated.getSectionNumber());
        existing.setHeading(updated.getHeading());
        existing.setContentText(updated.getContentText());
        existing.setContentXml(updated.getContentXml());
        existing.setSourceCredit(updated.getSourceCredit());
        existing.setSourceUrl(updated.getSourceUrl());
        existing.setReleasePoint(updated.getReleasePoint());
        existing.setImportSource(updated.getImportSource());
        // Note: createdAt is not updated, updatedAt is handled by @PreUpdate
    }

    /**
     * Convert a parsed section to a Statute entity.
     */
    private Statute convertToEntity(ParsedStatuteSection section, String releasePoint) {
        String sourceUrl = buildSourceUrl(section.getUscIdentifier());

        return Statute.builder()
                .uscIdentifier(section.getUscIdentifier())
                .titleNumber(section.getTitleNumber())
                .titleName(section.getTitleName())
                .chapterNumber(section.getChapterNumber())
                .chapterName(section.getChapterName())
                .sectionNumber(section.getSectionNumber())
                .heading(section.getHeading())
                .contentText(section.getContentText())
                .contentXml(section.getContentXml())
                .sourceCredit(section.getSourceCredit())
                .sourceUrl(sourceUrl)
                .releasePoint(releasePoint)
                .importSource(IMPORT_SOURCE)
                .build();
    }

    /**
     * Build the official source URL for a section.
     * Example: https://uscode.house.gov/view.xhtml?req=granuleid:USC-prelim-title5-section101
     */
    private String buildSourceUrl(String uscIdentifier) {
        if (uscIdentifier == null) {
            return null;
        }
        // Convert /us/usc/t5/s101 to USC-prelim-title5-section101
        String granuleId = uscIdentifier
                .replaceAll("^/us/usc/t(\\d+)/s(.+)$", "USC-prelim-title$1-section$2");
        return "https://uscode.house.gov/view.xhtml?req=granuleid:" + granuleId;
    }

    /**
     * Get import statistics for the database.
     *
     * @return Map of statistics
     */
    public long getTotalStatuteCount() {
        return statuteRepository.countAll();
    }

    /**
     * Get count of statutes by import source.
     */
    public long getUsCodeCount() {
        return statuteRepository.countByImportSource(IMPORT_SOURCE);
    }

    /**
     * Delete all imported statutes (for re-import).
     * Use with caution!
     *
     * @return Number of records deleted
     */
    @Transactional
    public long deleteAllImportedStatutes() {
        long count = statuteRepository.countByImportSource(IMPORT_SOURCE);
        log.warn("Deleting {} imported statutes", count);
        statuteRepository.deleteAll(statuteRepository.findByImportSource(IMPORT_SOURCE,
                org.springframework.data.domain.Pageable.unpaged()).getContent());
        return count;
    }
}
