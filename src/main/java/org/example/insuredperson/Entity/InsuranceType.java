package org.example.insuredperson.Entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum InsuranceType {
    AUTO_INSURANCE("Auto"),
    HEALTH_INSURANCE("Health"),
    LIFE_INSURANCE("Life"),
    HOME_INSURANCE("Home"),
    TRAVEL_INSURANCE("Travel");

    private final String label;

    InsuranceType(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static InsuranceType fromValue(String value) {
        for (InsuranceType type : values()) {
            if (type.label.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid insurance type: " + value);
    }
}
