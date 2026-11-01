package com.arnur.transport;

import com.arnur.transport.service.SampleTransportTree;
import com.arnur.transport.visitor.*;
import com.arnur.transport.visitor.element.*;
import com.arnur.transport.visitor.operation.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VisitorPatternTest {
    @Test void totalTicketRevenueMatchesHandCalculation() {
        TransportNetwork network = SampleTransportTree.create();
        assertEquals(new BigDecimal("3700.00"), network.accept(new TicketRevenueVisitor()));
    }

    @Test void routeRevenueMatchesHandCalculation() {
        Route route = SampleTransportTree.create().routes().get(0);
        assertEquals(new BigDecimal("2500.00"), route.accept(new TicketRevenueVisitor()));
    }

    @Test void accessibilityAuditMatchesHandCalculation() {
        var audit = SampleTransportTree.create().accept(new AccessibilityAuditVisitor());
        assertEquals(6, audit.checked());
        assertEquals(4, audit.accessible());
        assertEquals(2, audit.issues().size());
    }

    @Test void accessibleFilterReturnsExpectedElements() {
        List<TransportElement> result = SampleTransportTree.create().accept(new AccessibleElementFilterVisitor());
        assertEquals(4, result.size());
        assertEquals(List.of("B1", "SF1", "B2", "T2"), result.stream().map(TransportElement::id).toList());
    }

    @Test void jsonExportPreservesTreeNesting() {
        String json = SampleTransportTree.create().accept(new JsonExportVisitor());
        assertTrue(json.startsWith("{\"type\":\"network\""));
        assertTrue(json.contains("\"routes\":["));
        assertTrue(json.contains("\"stops\":["));
        assertTrue(json.contains("\"type\":\"ticketMachine\""));
        assertTrue(json.endsWith("}"));
    }

    @Test void busUsesDoubleDispatch() {
        Bus bus = new Bus("B", "Bus", true, BigDecimal.ZERO);
        assertEquals("bus:B", bus.accept(new RecordingVisitor()));
    }

    @Test void ticketMachineUsesDoubleDispatch() {
        TicketMachine machine = new TicketMachine("TM", "Machine", true, BigDecimal.ZERO);
        assertEquals("machine:TM", machine.accept(new RecordingVisitor()));
    }

    private static final class RecordingVisitor implements TransportVisitor<String> {
        public String visitNetwork(TransportNetwork x) { return "network:" + x.id(); }
        public String visitRoute(Route x) { return "route:" + x.id(); }
        public String visitStop(Stop x) { return "stop:" + x.id(); }
        public String visitBus(Bus x) { return "bus:" + x.id(); }
        public String visitTram(Tram x) { return "tram:" + x.id(); }
        public String visitStationFacility(StationFacility x) { return "station:" + x.id(); }
        public String visitTicketMachine(TicketMachine x) { return "machine:" + x.id(); }
    }
}
