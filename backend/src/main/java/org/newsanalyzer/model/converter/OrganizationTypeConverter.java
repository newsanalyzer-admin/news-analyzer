package org.newsanalyzer.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.newsanalyzer.model.GovernmentOrganization.OrganizationType;

/**
 * JPA converter for OrganizationType enum.
 * Converts between database values (lowercase) and Java enum constants.
 */
@Converter(autoApply = true)
public class OrganizationTypeConverter implements AttributeConverter<OrganizationType, String> {

    @Override
    public String convertToDatabaseColumn(OrganizationType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    @Override
    public OrganizationType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        return OrganizationType.fromValue(dbData);
    }
}
