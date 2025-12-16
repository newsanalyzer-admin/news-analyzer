package org.newsanalyzer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.dto.ParsedStatuteSection;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UslmXmlParser.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
class UslmXmlParserTest {

    private UslmXmlParser parser;

    @BeforeEach
    void setUp() {
        parser = new UslmXmlParser();
    }

    // =====================================================================
    // Valid Section Parsing Tests
    // =====================================================================

    @Test
    void parse_validSection_extractsAllFields() throws XMLStreamException {
        // Given
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <uslm>
              <main>
                <title identifier="/us/usc/t5">
                  <num>5</num>
                  <heading>GOVERNMENT ORGANIZATION AND EMPLOYEES</heading>
                  <chapter identifier="/us/usc/t5/ch1">
                    <num>CHAPTER 1</num>
                    <heading>ORGANIZATION</heading>
                    <section identifier="/us/usc/t5/s101">
                      <num>§ 101</num>
                      <heading>Executive departments</heading>
                      <content>
                        <p>The Executive departments are:</p>
                        <list>
                          <item><num>(1)</num><p>The Department of State.</p></item>
                          <item><num>(2)</num><p>The Department of the Treasury.</p></item>
                        </list>
                      </content>
                      <sourceCredit>(Pub. L. 89–554, Sept. 6, 1966, 80 Stat. 378.)</sourceCredit>
                    </section>
                  </chapter>
                </title>
              </main>
            </uslm>
            """;

        // When
        List<ParsedStatuteSection> sections = parseXml(xml);

        // Then
        assertEquals(1, sections.size());
        ParsedStatuteSection section = sections.get(0);

        assertEquals("/us/usc/t5/s101", section.getUscIdentifier());
        assertEquals(5, section.getTitleNumber());
        assertEquals("GOVERNMENT ORGANIZATION AND EMPLOYEES", section.getTitleName());
        assertEquals("1", section.getChapterNumber());
        assertEquals("ORGANIZATION", section.getChapterName());
        assertEquals("101", section.getSectionNumber());
        assertEquals("Executive departments", section.getHeading());
        assertNotNull(section.getContentText());
        assertTrue(section.getContentText().contains("Executive departments"));
        assertTrue(section.getContentText().contains("Department of State"));
        assertEquals("(Pub. L. 89–554, Sept. 6, 1966, 80 Stat. 378.)", section.getSourceCredit());
        assertNotNull(section.getContentXml());
        assertFalse(section.isHasParseErrors());
    }

    @Test
    void parse_multipleSections_extractsAll() throws XMLStreamException {
        // Given
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <uslm>
              <main>
                <title identifier="/us/usc/t5">
                  <num>5</num>
                  <heading>GOVERNMENT ORGANIZATION</heading>
                  <chapter identifier="/us/usc/t5/ch1">
                    <num>CHAPTER 1</num>
                    <heading>ORGANIZATION</heading>
                    <section identifier="/us/usc/t5/s101">
                      <num>§ 101</num>
                      <heading>First section</heading>
                      <content><p>Content one</p></content>
                    </section>
                    <section identifier="/us/usc/t5/s102">
                      <num>§ 102</num>
                      <heading>Second section</heading>
                      <content><p>Content two</p></content>
                    </section>
                  </chapter>
                </title>
              </main>
            </uslm>
            """;

        // When
        List<ParsedStatuteSection> sections = parseXml(xml);

        // Then
        assertEquals(2, sections.size());
        assertEquals("/us/usc/t5/s101", sections.get(0).getUscIdentifier());
        assertEquals("/us/usc/t5/s102", sections.get(1).getUscIdentifier());
    }

    // =====================================================================
    // Missing Field Handling Tests
    // =====================================================================

    @Test
    void parse_missingHeading_handlesGracefully() throws XMLStreamException {
        // Given
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <uslm>
              <main>
                <title identifier="/us/usc/t5">
                  <num>5</num>
                  <section identifier="/us/usc/t5/s101">
                    <num>§ 101</num>
                    <content><p>Content without heading</p></content>
                  </section>
                </title>
              </main>
            </uslm>
            """;

        // When
        List<ParsedStatuteSection> sections = parseXml(xml);

        // Then
        assertEquals(1, sections.size());
        assertNull(sections.get(0).getHeading());
        assertNotNull(sections.get(0).getContentText());
    }

    @Test
    void parse_missingSourceCredit_handlesGracefully() throws XMLStreamException {
        // Given
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <uslm>
              <main>
                <title identifier="/us/usc/t5">
                  <num>5</num>
                  <section identifier="/us/usc/t5/s101">
                    <num>§ 101</num>
                    <heading>Test section</heading>
                    <content><p>Content</p></content>
                  </section>
                </title>
              </main>
            </uslm>
            """;

        // When
        List<ParsedStatuteSection> sections = parseXml(xml);

        // Then
        assertEquals(1, sections.size());
        assertNull(sections.get(0).getSourceCredit());
    }

    @Test
    void parse_missingChapter_handlesGracefully() throws XMLStreamException {
        // Given - Section directly under title without chapter
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <uslm>
              <main>
                <title identifier="/us/usc/t5">
                  <num>5</num>
                  <heading>Title heading</heading>
                  <section identifier="/us/usc/t5/s1">
                    <num>§ 1</num>
                    <heading>Test</heading>
                    <content><p>Content</p></content>
                  </section>
                </title>
              </main>
            </uslm>
            """;

        // When
        List<ParsedStatuteSection> sections = parseXml(xml);

        // Then
        assertEquals(1, sections.size());
        assertNull(sections.get(0).getChapterNumber());
        assertNull(sections.get(0).getChapterName());
    }

    // =====================================================================
    // Title Number Extraction Tests
    // =====================================================================

    @Test
    void parse_extractsTitleNumber_fromIdentifier() throws XMLStreamException {
        // Given
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <uslm>
              <main>
                <section identifier="/us/usc/t42/s1234">
                  <num>§ 1234</num>
                  <heading>Test</heading>
                  <content><p>Content</p></content>
                </section>
              </main>
            </uslm>
            """;

        // When
        List<ParsedStatuteSection> sections = parseXml(xml);

        // Then
        assertEquals(1, sections.size());
        assertEquals(42, sections.get(0).getTitleNumber());
    }

    // =====================================================================
    // Section Number Cleaning Tests
    // =====================================================================

    @Test
    void parse_cleansSectionNumber_removesSymbol() throws XMLStreamException {
        // Given
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <uslm>
              <main>
                <section identifier="/us/usc/t5/s101a-1">
                  <num>§ 101a-1</num>
                  <heading>Test</heading>
                  <content><p>Content</p></content>
                </section>
              </main>
            </uslm>
            """;

        // When
        List<ParsedStatuteSection> sections = parseXml(xml);

        // Then
        assertEquals(1, sections.size());
        assertEquals("101a-1", sections.get(0).getSectionNumber());
    }

    // =====================================================================
    // Chapter Number Extraction Tests
    // =====================================================================

    @Test
    void parse_extractsChapterNumber_fromText() throws XMLStreamException {
        // Given
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <uslm>
              <main>
                <title identifier="/us/usc/t5">
                  <num>5</num>
                  <chapter identifier="/us/usc/t5/ch5A">
                    <num>CHAPTER 5A</num>
                    <heading>Special Chapter</heading>
                    <section identifier="/us/usc/t5/s501">
                      <num>§ 501</num>
                      <heading>Test</heading>
                      <content><p>Content</p></content>
                    </section>
                  </chapter>
                </title>
              </main>
            </uslm>
            """;

        // When
        List<ParsedStatuteSection> sections = parseXml(xml);

        // Then
        assertEquals(1, sections.size());
        assertEquals("5A", sections.get(0).getChapterNumber());
    }

    // =====================================================================
    // Content Extraction Tests
    // =====================================================================

    @Test
    void parse_extractsTextContent_fromNestedXml() throws XMLStreamException {
        // Given
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <uslm>
              <main>
                <section identifier="/us/usc/t5/s101">
                  <num>§ 101</num>
                  <heading>Test</heading>
                  <content>
                    <p>First paragraph.</p>
                    <subsection>
                      <num>(a)</num>
                      <p>Subsection text.</p>
                    </subsection>
                    <subsection>
                      <num>(b)</num>
                      <p>Another subsection.</p>
                    </subsection>
                  </content>
                </section>
              </main>
            </uslm>
            """;

        // When
        List<ParsedStatuteSection> sections = parseXml(xml);

        // Then
        assertEquals(1, sections.size());
        String contentText = sections.get(0).getContentText();
        assertTrue(contentText.contains("First paragraph"));
        assertTrue(contentText.contains("Subsection text"));
        assertTrue(contentText.contains("Another subsection"));
    }

    // =====================================================================
    // Streaming Tests
    // =====================================================================

    @Test
    void parseStream_callsConsumerForEachSection() throws XMLStreamException {
        // Given
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <uslm>
              <main>
                <section identifier="/us/usc/t5/s1">
                  <num>§ 1</num>
                  <content><p>One</p></content>
                </section>
                <section identifier="/us/usc/t5/s2">
                  <num>§ 2</num>
                  <content><p>Two</p></content>
                </section>
                <section identifier="/us/usc/t5/s3">
                  <num>§ 3</num>
                  <content><p>Three</p></content>
                </section>
              </main>
            </uslm>
            """;

        List<ParsedStatuteSection> collected = new ArrayList<>();
        InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        // When
        parser.parseStream(stream, collected::add);

        // Then
        assertEquals(3, collected.size());
    }

    // =====================================================================
    // Edge Cases
    // =====================================================================

    @Test
    void parse_emptyContent_handlesGracefully() throws XMLStreamException {
        // Given
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <uslm>
              <main>
                <section identifier="/us/usc/t5/s101">
                  <num>§ 101</num>
                  <heading>Empty section</heading>
                  <content></content>
                </section>
              </main>
            </uslm>
            """;

        // When
        List<ParsedStatuteSection> sections = parseXml(xml);

        // Then
        assertEquals(1, sections.size());
        // Empty content is acceptable
    }

    @Test
    void parse_noSections_returnsEmptyList() throws XMLStreamException {
        // Given
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <uslm>
              <main>
                <title identifier="/us/usc/t5">
                  <num>5</num>
                  <heading>Empty title</heading>
                </title>
              </main>
            </uslm>
            """;

        // When
        List<ParsedStatuteSection> sections = parseXml(xml);

        // Then
        assertEquals(0, sections.size());
    }

    // =====================================================================
    // Helper Methods
    // =====================================================================

    private List<ParsedStatuteSection> parseXml(String xml) throws XMLStreamException {
        InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        return parser.parse(stream);
    }
}
