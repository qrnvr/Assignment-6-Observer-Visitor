package com.arnur.transport.service;

import com.arnur.transport.visitor.TransportElement;
import com.arnur.transport.visitor.element.*;
import java.math.BigDecimal;
import java.util.List;

public final class SampleTransportTree {
    private SampleTransportTree() {}

    public static TransportNetwork create() {
        Stop s1 = new Stop("S1", "Central Stop", List.<TransportElement>of(
                new Bus("B1", "Bus 10", true, new BigDecimal("1200.00")),
                new StationFacility("SF1", "Central Platform", true, new BigDecimal("150.00")),
                new TicketMachine("TM1", "Central Ticket Machine", false, new BigDecimal("250.00"))));
        Stop s2 = new Stop("S2", "River Stop", List.<TransportElement>of(
                new Tram("T1", "Tram 3", false, new BigDecimal("900.00"))));
        Route r1 = new Route("R1", "Blue Route", List.of(s1, s2));

        Stop s3 = new Stop("S3", "University Stop", List.<TransportElement>of(
                new Bus("B2", "Bus 21", true, new BigDecimal("700.00")),
                new Tram("T2", "Tram 8", true, new BigDecimal("500.00"))));
        Route r2 = new Route("R2", "Green Route", List.of(s3));
        return new TransportNetwork("N1", "Astana Public Transport", List.of(r1, r2));
    }
}
