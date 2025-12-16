package org.newsanalyzer.model;

/**
 * Type of congressional committee.
 * Maps to Congress.gov API committee type values.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
public enum CommitteeType {
    STANDING,
    SELECT,
    SPECIAL,
    JOINT,
    SUBCOMMITTEE,
    OTHER
}
