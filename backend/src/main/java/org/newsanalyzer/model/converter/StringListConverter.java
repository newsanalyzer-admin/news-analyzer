package org.newsanalyzer.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JPA AttributeConverter for PostgreSQL text[] arrays.
 *
 * Handles conversion between Java List<String> and PostgreSQL text[] arrays,
 * bypassing Hibernate's Jackson-based deserialization which doesn't understand
 * PostgreSQL's array format {a,b,c}.
 */
@Converter
public class StringListConverter implements AttributeConverter<List<String>, String[]> {

    @Override
    public String[] convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        return attribute.toArray(new String[0]);
    }

    @Override
    public List<String> convertToEntityAttribute(String[] dbData) {
        if (dbData == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(dbData));
    }
}
