package com.arnur.transport.visitor.result;

import java.util.ArrayList;
import java.util.List;

public record AccessibilityAudit(int checked, int accessible, List<String> issues) {
    public AccessibilityAudit {
        issues = List.copyOf(issues);
    }

    public static AccessibilityAudit empty() {
        return new AccessibilityAudit(0, 0, List.of());
    }

    public AccessibilityAudit plus(AccessibilityAudit other) {
        List<String> merged = new ArrayList<>(issues);
        merged.addAll(other.issues);
        return new AccessibilityAudit(checked + other.checked, accessible + other.accessible, merged);
    }
}
