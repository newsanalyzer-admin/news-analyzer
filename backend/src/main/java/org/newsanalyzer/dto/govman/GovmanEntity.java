package org.newsanalyzer.dto.govman;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * JAXB model for Entity elements in Government Manual XML.
 *
 * Maps XML attributes and elements from the 2025 GOVMAN format:
 * - EntityId, ParentId, SortOrder (as attributes)
 * - EntityType, Category, AgencyName (as child elements)
 * - Addresses/Address/FooterDetails/WebAddress for web URL
 * - MissionStatement/Record/Paragraph for mission text
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class GovmanEntity {

    @XmlAttribute(name = "EntityId")
    private String entityId;

    @XmlAttribute(name = "ParentId")
    private String parentId;

    @XmlAttribute(name = "SortOrder")
    private Integer sortOrder;

    @XmlElement(name = "EntityType")
    private String entityType;

    @XmlElement(name = "Category")
    private String category;

    @XmlElement(name = "AgencyName")
    private String agencyName;

    @XmlElement(name = "Addresses")
    private GovmanAddresses addresses;

    @XmlElement(name = "MissionStatement")
    private GovmanMissionStatement missionStatement;

    /**
     * Get web address from nested Addresses structure.
     *
     * @return Web URL or null if not present
     */
    public String getWebAddress() {
        if (addresses == null || addresses.getAddressList() == null) {
            return null;
        }
        // Get first address with a web address
        for (GovmanAddress addr : addresses.getAddressList()) {
            if (addr.getFooterDetails() != null && addr.getFooterDetails().getWebAddress() != null) {
                String web = addr.getFooterDetails().getWebAddress();
                if (!web.isBlank()) {
                    return web;
                }
            }
        }
        return null;
    }

    /**
     * Get mission statement as a single concatenated string.
     * Paragraphs are joined with double newlines.
     *
     * @return Concatenated mission statement, or null if no paragraphs
     */
    public String getMissionStatementText() {
        if (missionStatement == null || missionStatement.getRecords() == null) {
            return null;
        }
        List<String> paragraphs = new ArrayList<>();
        for (GovmanMissionRecord record : missionStatement.getRecords()) {
            if (record.getParagraph() != null && !record.getParagraph().isBlank()) {
                paragraphs.add(record.getParagraph().trim());
            }
        }
        return paragraphs.isEmpty() ? null : String.join("\n\n", paragraphs);
    }

    /**
     * Check if this entity has valid required fields.
     *
     * @return true if entityId and agencyName are present
     */
    public boolean hasRequiredFields() {
        return entityId != null && !entityId.isBlank()
                && agencyName != null && !agencyName.isBlank();
    }

    /**
     * Check if this entity has a parent.
     *
     * @return true if parentId is present, not empty, and not "0"
     */
    public boolean hasParent() {
        return parentId != null && !parentId.isBlank() && !"0".equals(parentId);
    }
}
