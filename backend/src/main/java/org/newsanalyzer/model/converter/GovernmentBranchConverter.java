package org.newsanalyzer.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.newsanalyzer.model.GovernmentOrganization.GovernmentBranch;

/**
 * JPA converter for GovernmentBranch enum.
 * Converts between database values (lowercase) and Java enum constants.
 */
@Converter(autoApply = true)
public class GovernmentBranchConverter implements AttributeConverter<GovernmentBranch, String> {

    @Override
    public String convertToDatabaseColumn(GovernmentBranch attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    @Override
    public GovernmentBranch convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        return GovernmentBranch.fromValue(dbData);
    }
}
