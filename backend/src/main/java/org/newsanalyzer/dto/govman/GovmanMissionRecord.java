package org.newsanalyzer.dto.govman;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

/**
 * JAXB model for Record element within MissionStatement in GOVMAN XML.
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class GovmanMissionRecord {

    @XmlElement(name = "Paragraph")
    private String paragraph;

    @XmlElement(name = "FooterDetails")
    private GovmanFooterDetails footerDetails;
}
