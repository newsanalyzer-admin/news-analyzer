package org.newsanalyzer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Service for downloading US Code XML files from uscode.house.gov.
 * Handles ZIP file downloads, extraction, and streaming of XML content.
 *
 * Download URL pattern:
 * - Individual titles: https://uscode.house.gov/download/releasepoints/us/pl/119/22/xml_usc{TT}@119-22.zip
 * - Release point format: @{Congress}-{Release}
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 * @see <a href="https://uscode.house.gov/download/download.shtml">US Code Download Page</a>
 */
@Service
public class UsCodeDownloadService {

    private static final Logger log = LoggerFactory.getLogger(UsCodeDownloadService.class);

    /**
     * Base URL for US Code downloads.
     */
    private static final String BASE_URL = "https://uscode.house.gov/download/releasepoints/us/pl";

    /**
     * Default timeout for HTTP connections (30 seconds).
     */
    private static final int CONNECTION_TIMEOUT_MS = 30_000;

    /**
     * Default timeout for reading data (5 minutes for large files).
     */
    private static final int READ_TIMEOUT_MS = 300_000;

    /**
     * Default release point (current as of implementation).
     * Check https://uscode.house.gov/download/download.shtml for latest.
     */
    private static final String DEFAULT_RELEASE_POINT = "119-46";

    /**
     * Download and extract a specific US Code title.
     *
     * @param titleNumber Title number (1-54)
     * @param releasePoint Release point (e.g., "119-22"), or null for default
     * @return InputStream for the extracted XML file
     * @throws IOException if download or extraction fails
     */
    public InputStream downloadTitle(int titleNumber, String releasePoint) throws IOException {
        String effectiveReleasePoint = releasePoint != null ? releasePoint : DEFAULT_RELEASE_POINT;
        String[] releaseParts = effectiveReleasePoint.split("-");
        if (releaseParts.length != 2) {
            throw new IllegalArgumentException("Invalid release point format: " + effectiveReleasePoint);
        }

        String congress = releaseParts[0];
        String release = releaseParts[1];

        // Build download URL
        // Format: https://uscode.house.gov/download/releasepoints/us/pl/119/22/xml_usc05@119-22.zip
        String titleFormatted = String.format("%02d", titleNumber);
        String fileName = String.format("xml_usc%s@%s.zip", titleFormatted, effectiveReleasePoint);
        String downloadUrl = String.format("%s/%s/%s/%s", BASE_URL, congress, release, fileName);

        log.info("Downloading US Code Title {} from: {}", titleNumber, downloadUrl);

        InputStream zipStream = downloadFromUrl(downloadUrl);
        return extractXmlFromZip(zipStream, titleNumber);
    }

    /**
     * Get list of available titles with download info.
     * Note: US Code has 54 titles (with Title 53 reserved).
     *
     * @return List of title numbers (1-54)
     */
    public List<Integer> getAvailableTitles() {
        List<Integer> titles = new ArrayList<>();
        for (int i = 1; i <= 54; i++) {
            titles.add(i);
        }
        return titles;
    }

    /**
     * Build the download URL for a specific title.
     *
     * @param titleNumber Title number
     * @param releasePoint Release point (e.g., "119-22")
     * @return Full download URL
     */
    public String buildDownloadUrl(int titleNumber, String releasePoint) {
        String effectiveReleasePoint = releasePoint != null ? releasePoint : DEFAULT_RELEASE_POINT;
        String[] releaseParts = effectiveReleasePoint.split("-");

        String congress = releaseParts[0];
        String release = releaseParts[1];
        String titleFormatted = String.format("%02d", titleNumber);
        String fileName = String.format("xml_usc%s@%s.zip", titleFormatted, effectiveReleasePoint);

        return String.format("%s/%s/%s/%s", BASE_URL, congress, release, fileName);
    }

    /**
     * Check if a specific title is available for download.
     *
     * @param titleNumber Title number
     * @param releasePoint Release point
     * @return true if the title is available
     */
    public boolean isTitleAvailable(int titleNumber, String releasePoint) {
        String url = buildDownloadUrl(titleNumber, releasePoint);

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(CONNECTION_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);

            int responseCode = connection.getResponseCode();
            connection.disconnect();

            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            log.warn("Failed to check availability for Title {}: {}", titleNumber, e.getMessage());
            return false;
        }
    }

    /**
     * Download content from a URL.
     *
     * @param urlString URL to download from
     * @return InputStream for the downloaded content
     * @throws IOException if download fails
     */
    private InputStream downloadFromUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(CONNECTION_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setRequestProperty("User-Agent", "NewsAnalyzer/2.0 (Government Data Aggregator)");

        int responseCode = connection.getResponseCode();

        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException(String.format(
                    "Failed to download from %s: HTTP %d %s",
                    urlString, responseCode, connection.getResponseMessage()));
        }

        log.info("Download started, content length: {} bytes",
                connection.getContentLength() > 0 ? connection.getContentLength() : "unknown");

        return new BufferedInputStream(connection.getInputStream());
    }

    /**
     * ZIP file magic bytes (PK header).
     */
    private static final byte[] ZIP_MAGIC = {0x50, 0x4B, 0x03, 0x04};

    /**
     * Extract XML file from a ZIP input stream.
     * The ZIP typically contains a single XML file for the title.
     *
     * @param zipStream ZIP input stream
     * @param titleNumber Expected title number (for validation)
     * @return InputStream for the extracted XML
     * @throws IOException if extraction fails
     */
    private InputStream extractXmlFromZip(InputStream zipStream, int titleNumber) throws IOException {
        // Buffer the stream to allow magic byte checking
        BufferedInputStream bufferedStream = new BufferedInputStream(zipStream);
        bufferedStream.mark(4);

        // Check for ZIP magic bytes (PK header)
        byte[] header = new byte[4];
        int bytesRead = bufferedStream.read(header);
        bufferedStream.reset();

        if (bytesRead < 4 || !isZipFile(header)) {
            // Read first bytes to check if it's an HTML error page
            bufferedStream.mark(1024);
            byte[] preview = new byte[256];
            bufferedStream.read(preview);
            String previewStr = new String(preview).toLowerCase();
            bufferedStream.close();

            if (previewStr.contains("<!doctype html") || previewStr.contains("<html")) {
                throw new IOException("Server returned HTML error page instead of ZIP file for Title " + titleNumber +
                        ". The release point may be outdated - check https://uscode.house.gov/download/download.shtml");
            }
            throw new IOException("Invalid ZIP file format for Title " + titleNumber);
        }

        ZipInputStream zis = new ZipInputStream(bufferedStream);
        ZipEntry entry;

        while ((entry = zis.getNextEntry()) != null) {
            String entryName = entry.getName();

            // Look for the main XML file (e.g., usc05.xml)
            if (entryName.endsWith(".xml") && !entry.isDirectory()) {
                log.info("Extracting XML file: {} (size: {} bytes)",
                        entryName, entry.getSize() > 0 ? entry.getSize() : "unknown");

                // Copy to temp file to allow multiple reads
                Path tempFile = Files.createTempFile("usc_" + titleNumber + "_", ".xml");
                tempFile.toFile().deleteOnExit();

                Files.copy(zis, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                zis.close();

                log.info("Extracted to temp file: {}", tempFile);
                return Files.newInputStream(tempFile);
            }
        }

        zis.close();
        throw new IOException("No XML file found in ZIP archive for Title " + titleNumber);
    }

    /**
     * Check if byte array starts with ZIP magic bytes.
     */
    private boolean isZipFile(byte[] header) {
        return header.length >= 4 &&
                header[0] == ZIP_MAGIC[0] &&
                header[1] == ZIP_MAGIC[1] &&
                header[2] == ZIP_MAGIC[2] &&
                header[3] == ZIP_MAGIC[3];
    }

    /**
     * Download a title directly to a file.
     * Useful for batch processing or caching.
     *
     * @param titleNumber Title number
     * @param releasePoint Release point
     * @param targetPath Path to save the extracted XML
     * @throws IOException if download or extraction fails
     */
    public void downloadTitleToFile(int titleNumber, String releasePoint, Path targetPath) throws IOException {
        try (InputStream xmlStream = downloadTitle(titleNumber, releasePoint)) {
            Files.copy(xmlStream, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            log.info("Saved Title {} to: {}", titleNumber, targetPath);
        }
    }

    /**
     * Get the default release point.
     *
     * @return Default release point string
     */
    public String getDefaultReleasePoint() {
        return DEFAULT_RELEASE_POINT;
    }
}
