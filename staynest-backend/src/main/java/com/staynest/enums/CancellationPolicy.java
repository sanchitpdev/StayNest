package com.staynest.enums;

public enum CancellationPolicy {
    FLEXIBLE,     // Full refund up to 24hrs before check-in
    MODERATE,     // Full refund up to 5 days before check-in
    STRICT,       // 50% refund up to 7 days before check-in
    NON_REFUNDABLE // No refund
}