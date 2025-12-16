package org.newsanalyzer.dto.govman;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * JAXB model for the root element of Government Manual XML files.
 *
 * Parses the GovernmentManual XML format from GovInfo.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@XmlRootElement(name = "GovernmentManual")
@XmlAccessorType(XmlAccessType.FIELD)
public class GovmanDocument {

    @XmlElement(name = "Entity")
    private List<GovmanEntity> entities = new ArrayList<>();

    /**
     * Get all entities from the document.
     */
    public List<GovmanEntity> getEntities() {
        return entities != null ? entities : new ArrayList<>();
    }
}
