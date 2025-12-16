package org.newsanalyzer.dto.govman;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

/**
 * JAXB model for Address element in GOVMAN XML.
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class GovmanAddress {

    @XmlAttribute(name = "EntityAddressId")
    private String entityAddressId;

    @XmlAttribute(name = "SortOrder")
    private Integer sortOrder;

    @XmlElement(name = "FooterDetails")
    private GovmanFooterDetails footerDetails;

    @XmlElement(name = "Address")
    private String address;

    @XmlElement(name = "Phone")
    private String phone;

    @XmlElement(name = "Fax")
    private String fax;
}
