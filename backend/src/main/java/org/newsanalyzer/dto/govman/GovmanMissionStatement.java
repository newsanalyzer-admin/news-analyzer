package org.newsanalyzer.dto.govman;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

import java.util.List;

/**
 * JAXB model for MissionStatement element in GOVMAN XML.
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class GovmanMissionStatement {

    @XmlElement(name = "Heading")
    private String heading;

    @XmlElement(name = "Record")
    private List<GovmanMissionRecord> records;
}
