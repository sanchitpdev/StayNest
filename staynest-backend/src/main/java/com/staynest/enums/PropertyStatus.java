package com.staynest.enums;

public enum PropertyStatus {
    DRAFT,        // Created but not visible to guests
    ACTIVE,       // Visible and bookable
    INACTIVE,     // Temporarily hidden by host
    SUSPENDED,    // Disabled by admin
    DELETED       // Soft deleted
}