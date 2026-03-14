package org.newsanalyzer.model;

/**
 * Congressional chamber enum.
 *
 * Extracted as a standalone type because Chamber is a legislative domain concept
 * used across multiple entities (CongressionalMember, GovernmentPosition, etc.).
 *
 * Part of ARCH-1.9: Extracted from Person.Chamber to decouple from legacy entity.
 *
 * @since 3.0.0
 */
public enum Chamber {
    SENATE,
    HOUSE
}
