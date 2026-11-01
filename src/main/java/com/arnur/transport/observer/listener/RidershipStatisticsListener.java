package com.arnur.transport.observer.listener;

import com.arnur.transport.observer.Observer;
import com.arnur.transport.observer.TransportEvent;
import java.util.concurrent.atomic.LongAdder;

public final class RidershipStatisticsListener implements Observer<TransportEvent> {
    private final LongAdder boardings = new LongAdder();
    private final LongAdder events = new LongAdder();

    @Override
    public void onEvent(TransportEvent event) {
        boardings.add(event.intAttribute("boardings"));
        events.increment();
    }

    public long totalBoardings() { return boardings.sum(); }
    public long eventCount() { return events.sum(); }
}
