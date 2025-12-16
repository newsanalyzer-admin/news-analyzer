package org.newsanalyzer.dto.govman;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

/**
 * JAXB model for FooterDetails element in GOVMAN XML.
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class GovmanFooterDetails {

    @XmlElement(name = "Email")
    private String email;

    @XmlElement(name = "WebAddress")
    private String webAddress;

    @XmlElement(name = "Footer")
    private String footer;
}
