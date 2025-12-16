package org.newsanalyzer.dto.govman;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

import java.util.List;

/**
 * JAXB model for Addresses container in GOVMAN XML.
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class GovmanAddresses {

    @XmlElement(name = "Address")
    private List<GovmanAddress> addressList;
}
