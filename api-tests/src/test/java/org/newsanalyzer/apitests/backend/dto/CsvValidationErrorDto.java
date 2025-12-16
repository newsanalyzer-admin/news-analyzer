package org.newsanalyzer.apitests.backend.dto;

/**
 * DTO matching backend CsvValidationError response.
 */
public class CsvValidationErrorDto {
    private int line;
    private String field;
    private String value;
    private String message;

    public int getLine() { return line; }
    public void setLine(int line) { this.line = line; }

    public String getField() { return field; }
    public void setField(String field) { this.field = field; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    @Override
    public String toString() {
        return String.format("CsvValidationErrorDto{line=%d, field='%s', value='%s', message='%s'}",
                line, field, value, message);
    }
}
