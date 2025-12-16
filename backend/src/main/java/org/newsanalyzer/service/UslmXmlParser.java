package org.newsanalyzer.service;

import org.newsanalyzer.dto.ParsedStatuteSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * StAX-based streaming parser for USLM (United States Legislative Markup) XML files.
 * Uses streaming to handle large XML files (50-100MB) with minimal memory footprint.
 *
 * USLM Structure:
 * <pre>
 * {@code
 * <uslm>
 *   <main>
 *     <title identifier="/us/usc/t5">
 *       <num>5</num>
 *       <heading>GOVERNMENT ORGANIZATION AND EMPLOYEES</heading>
 *       <chapter identifier="/us/usc/t5/ch1">
 *         <num>CHAPTER 1</num>
 *         <heading>ORGANIZATION</heading>
 *         <section identifier="/us/usc/t5/s101">
 *           <num>§ 101</num>
 *           <heading>Executive departments</heading>
 *           <content>...</content>
 *           <sourceCredit>...</sourceCredit>
 *         </section>
 *       </chapter>
 *     </title>
 *   </main>
 * </uslm>
 * }
 * </pre>
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Service
public class UslmXmlParser {

    private static final Logger log = LoggerFactory.getLogger(UslmXmlParser.class);

    private static final String ELEMENT_SECTION = "section";
    private static final String ELEMENT_NUM = "num";
    private static final String ELEMENT_HEADING = "heading";
    private static final String ELEMENT_CONTENT = "content";
    private static final String ELEMENT_SOURCE_CREDIT = "sourceCredit";
    private static final String ELEMENT_TITLE = "title";
    private static final String ELEMENT_CHAPTER = "chapter";
    private static final String ATTR_IDENTIFIER = "identifier";

    // Pattern to extract title number from identifier: /us/usc/t5/s101 -> 5
    private static final Pattern TITLE_PATTERN = Pattern.compile("/us/usc/t(\\d+)");
    // Pattern to clean section number: "§ 101" -> "101"
    private static final Pattern SECTION_NUM_PATTERN = Pattern.compile("§\\s*([\\w\\-]+)");

    private final XMLInputFactory xmlInputFactory;

    public UslmXmlParser() {
        this.xmlInputFactory = XMLInputFactory.newInstance();
        // Security: Disable external entities to prevent XXE attacks
        this.xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        this.xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
    }

    /**
     * Parse USLM XML from an InputStream and return all sections.
     * Collects all sections in memory - use parseStream() for large files.
     *
     * @param inputStream XML input stream
     * @return List of parsed sections
     * @throws XMLStreamException if XML parsing fails
     */
    public List<ParsedStatuteSection> parse(InputStream inputStream) throws XMLStreamException {
        List<ParsedStatuteSection> sections = new ArrayList<>();
        parseStream(inputStream, sections::add);
        return sections;
    }

    /**
     * Stream-parse USLM XML and invoke callback for each section.
     * Memory-efficient for large files - sections are processed one at a time.
     *
     * @param inputStream XML input stream
     * @param sectionConsumer Callback invoked for each parsed section
     * @throws XMLStreamException if XML parsing fails
     */
    public void parseStream(InputStream inputStream, Consumer<ParsedStatuteSection> sectionConsumer)
            throws XMLStreamException {
        XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(inputStream);

        try {
            ParserContext context = new ParserContext();
            parseDocument(reader, context, sectionConsumer);
        } finally {
            reader.close();
        }
    }

    /**
     * Parse the document and extract sections.
     */
    private void parseDocument(XMLStreamReader reader, ParserContext context,
                               Consumer<ParsedStatuteSection> sectionConsumer) throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();

            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    handleStartElement(reader, context);
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    handleEndElement(reader, context, sectionConsumer);
                    break;
            }
        }

        log.info("Parsed {} sections from USLM XML", context.sectionCount);
    }

    /**
     * Handle start element events.
     */
    private void handleStartElement(XMLStreamReader reader, ParserContext context) throws XMLStreamException {
        String localName = reader.getLocalName();

        switch (localName) {
            case ELEMENT_TITLE:
                context.currentTitleId = reader.getAttributeValue(null, ATTR_IDENTIFIER);
                context.inTitle = true;
                break;

            case ELEMENT_CHAPTER:
                context.currentChapterId = reader.getAttributeValue(null, ATTR_IDENTIFIER);
                context.inChapter = true;
                break;

            case ELEMENT_SECTION:
                context.currentSectionId = reader.getAttributeValue(null, ATTR_IDENTIFIER);
                context.inSection = true;
                context.sectionXmlBuilder = new StringBuilder();
                context.sectionXmlBuilder.append("<section identifier=\"")
                        .append(context.currentSectionId)
                        .append("\">");
                break;

            case ELEMENT_NUM:
                if (context.inSection) {
                    context.sectionNum = readElementText(reader);
                    context.sectionXmlBuilder.append("<num>")
                            .append(escapeXml(context.sectionNum))
                            .append("</num>");
                } else if (context.inChapter && !context.inSection) {
                    context.currentChapterNum = readElementText(reader);
                } else if (context.inTitle && !context.inChapter && !context.inSection) {
                    context.currentTitleNum = readElementText(reader);
                }
                break;

            case ELEMENT_HEADING:
                if (context.inSection) {
                    context.sectionHeading = readElementText(reader);
                    context.sectionXmlBuilder.append("<heading>")
                            .append(escapeXml(context.sectionHeading))
                            .append("</heading>");
                } else if (context.inChapter && !context.inSection && context.currentChapterName == null) {
                    // Only capture first heading at chapter level (skip notes/amendments headings)
                    context.currentChapterName = readElementText(reader);
                } else if (context.inTitle && !context.inChapter && !context.inSection && context.currentTitleName == null) {
                    // Only capture first heading at title level (skip notes headings)
                    context.currentTitleName = readElementText(reader);
                }
                break;

            case ELEMENT_CONTENT:
                if (context.inSection) {
                    String contentXml = readElementXml(reader);
                    context.sectionContentText = extractTextFromXml(contentXml);
                    context.sectionXmlBuilder.append("<content>")
                            .append(contentXml)
                            .append("</content>");
                }
                break;

            case ELEMENT_SOURCE_CREDIT:
                if (context.inSection) {
                    context.sectionSourceCredit = readElementText(reader);
                    context.sectionXmlBuilder.append("<sourceCredit>")
                            .append(escapeXml(context.sectionSourceCredit))
                            .append("</sourceCredit>");
                }
                break;
        }
    }

    /**
     * Handle end element events.
     */
    private void handleEndElement(XMLStreamReader reader, ParserContext context,
                                  Consumer<ParsedStatuteSection> sectionConsumer) {
        String localName = reader.getLocalName();

        switch (localName) {
            case ELEMENT_TITLE:
                context.inTitle = false;
                context.currentTitleId = null;
                context.currentTitleNum = null;
                context.currentTitleName = null;
                break;

            case ELEMENT_CHAPTER:
                context.inChapter = false;
                context.currentChapterId = null;
                context.currentChapterNum = null;
                context.currentChapterName = null;
                break;

            case ELEMENT_SECTION:
                if (context.inSection && context.currentSectionId != null) {
                    context.sectionXmlBuilder.append("</section>");
                    ParsedStatuteSection section = buildSection(context);
                    context.sectionCount++;
                    sectionConsumer.accept(section);
                }
                context.inSection = false;
                context.resetSection();
                break;
        }
    }

    /**
     * Build a ParsedStatuteSection from current context.
     */
    private ParsedStatuteSection buildSection(ParserContext context) {
        Integer titleNumber = extractTitleNumber(context.currentSectionId);
        String sectionNumber = cleanSectionNumber(context.sectionNum);

        return ParsedStatuteSection.builder()
                .uscIdentifier(context.currentSectionId)
                .titleNumber(titleNumber)
                .titleName(context.currentTitleName)
                .chapterNumber(extractChapterNumber(context.currentChapterNum))
                .chapterName(context.currentChapterName)
                .sectionNumber(sectionNumber)
                .heading(context.sectionHeading)
                .contentText(context.sectionContentText)
                .contentXml(context.sectionXmlBuilder.toString())
                .sourceCredit(context.sectionSourceCredit)
                .hasParseErrors(false)
                .build();
    }

    /**
     * Read element text content.
     */
    private String readElementText(XMLStreamReader reader) throws XMLStreamException {
        StringBuilder sb = new StringBuilder();
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.CHARACTERS || event == XMLStreamConstants.CDATA) {
                sb.append(reader.getText());
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                break;
            } else if (event == XMLStreamConstants.START_ELEMENT) {
                // Nested element - read recursively
                sb.append(readElementText(reader));
            }
        }
        return sb.toString().trim();
    }

    /**
     * Read element including nested XML as string.
     */
    private String readElementXml(XMLStreamReader reader) throws XMLStreamException {
        StringBuilder sb = new StringBuilder();
        int depth = 1;

        while (reader.hasNext() && depth > 0) {
            int event = reader.next();

            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    depth++;
                    sb.append("<").append(reader.getLocalName());
                    for (int i = 0; i < reader.getAttributeCount(); i++) {
                        sb.append(" ")
                          .append(reader.getAttributeLocalName(i))
                          .append("=\"")
                          .append(escapeXml(reader.getAttributeValue(i)))
                          .append("\"");
                    }
                    sb.append(">");
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    depth--;
                    if (depth > 0) {
                        sb.append("</").append(reader.getLocalName()).append(">");
                    }
                    break;

                case XMLStreamConstants.CHARACTERS:
                case XMLStreamConstants.CDATA:
                    sb.append(escapeXml(reader.getText()));
                    break;
            }
        }

        return sb.toString();
    }

    /**
     * Extract plain text from XML content.
     */
    private String extractTextFromXml(String xml) {
        if (xml == null || xml.isEmpty()) {
            return "";
        }
        // Remove XML tags and normalize whitespace
        return xml.replaceAll("<[^>]+>", " ")
                  .replaceAll("\\s+", " ")
                  .trim();
    }

    /**
     * Extract title number from USC identifier.
     * Example: "/us/usc/t5/s101" -> 5
     */
    private Integer extractTitleNumber(String identifier) {
        if (identifier == null) {
            return null;
        }
        Matcher matcher = TITLE_PATTERN.matcher(identifier);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                log.warn("Failed to parse title number from identifier: {}", identifier);
            }
        }
        return null;
    }

    /**
     * Clean section number by removing section symbol and whitespace.
     * Example: "§ 101" -> "101"
     */
    private String cleanSectionNumber(String rawNum) {
        if (rawNum == null) {
            return null;
        }
        Matcher matcher = SECTION_NUM_PATTERN.matcher(rawNum);
        if (matcher.find()) {
            return matcher.group(1);
        }
        // Return as-is if no section symbol found
        return rawNum.trim();
    }

    /**
     * Extract chapter number from raw chapter num text.
     * Example: "CHAPTER 1" -> "1"
     */
    private String extractChapterNumber(String rawChapterNum) {
        if (rawChapterNum == null) {
            return null;
        }
        // Remove "CHAPTER" prefix and clean
        return rawChapterNum.replaceAll("(?i)chapter\\s*", "").trim();
    }

    /**
     * Escape special XML characters.
     */
    private String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }

    /**
     * Parse a single section from XML string (for testing).
     *
     * @param sectionXml XML string containing a single section element
     * @return Parsed section or null if parsing fails
     */
    public ParsedStatuteSection parseSection(String sectionXml) {
        try {
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(
                    sectionXml.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            List<ParsedStatuteSection> sections = parse(bais);
            return sections.isEmpty() ? null : sections.get(0);
        } catch (XMLStreamException e) {
            log.error("Failed to parse section XML: {}", e.getMessage());
            return ParsedStatuteSection.builder()
                    .hasParseErrors(true)
                    .parseErrorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * Internal context class to track parser state.
     */
    private static class ParserContext {
        // Title context
        boolean inTitle = false;
        String currentTitleId;
        String currentTitleNum;
        String currentTitleName;

        // Chapter context
        boolean inChapter = false;
        String currentChapterId;
        String currentChapterNum;
        String currentChapterName;

        // Section context
        boolean inSection = false;
        String currentSectionId;
        String sectionNum;
        String sectionHeading;
        String sectionContentText;
        String sectionSourceCredit;
        StringBuilder sectionXmlBuilder;

        // Statistics
        int sectionCount = 0;

        void resetSection() {
            currentSectionId = null;
            sectionNum = null;
            sectionHeading = null;
            sectionContentText = null;
            sectionSourceCredit = null;
            sectionXmlBuilder = null;
        }
    }
}
