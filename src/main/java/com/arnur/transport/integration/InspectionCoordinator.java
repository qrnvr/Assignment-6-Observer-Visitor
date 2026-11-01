package com.arnur.transport.integration;

import com.arnur.transport.observer.*;
import com.arnur.transport.service.TransportTreeIndex;
import com.arnur.transport.visitor.operation.AccessibilityAuditVisitor;
import com.arnur.transport.visitor.result.AccessibilityAudit;
import java.time.Instant;
import java.util.Map;

public final class InspectionCoordinator implements Observer<TransportEvent> {
    private final TransportTreeIndex treeIndex;
    private final Subject<TransportEvent> reportSubject;

    public InspectionCoordinator(TransportTreeIndex treeIndex, Subject<TransportEvent> reportSubject) {
        this.treeIndex = treeIndex;
        this.reportSubject = reportSubject;
    }

    @Override
    public void onEvent(TransportEvent event) {
        treeIndex.route(event.routeId()).ifPresent(route -> {
            AccessibilityAudit audit = route.accept(new AccessibilityAuditVisitor());
            reportSubject.publish(new TransportEvent(
                    EventType.INSPECTION_REPORT,
                    "inspection-coordinator",
                    route.id(),
                    Instant.now(),
                    Map.of(
                            "checked", Integer.toString(audit.checked()),
                            "accessible", Integer.toString(audit.accessible()),
                            "issues", Integer.toString(audit.issues().size()))));
        });
    }
}
