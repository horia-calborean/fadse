package core.network;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

public class PendingSimulationQueueTest {
    @Test
    public void healthySingleWorkerDrainsBatchLongerThanTheStallTimeout() {
        Map<Integer, String> pending = new HashMap<>(Map.of(1, "a", 2, "b", 3, "c", 4, "d"));
        AtomicLong clock = new AtomicLong();
        AtomicLong busyUntil = new AtomicLong();
        List<Integer> admitted = new ArrayList<>();

        PendingSimulationQueue.DrainResult result = PendingSimulationQueue.drain(pending, (id, value) -> {
            if (clock.get() < busyUntil.get()) {
                return false;
            }
            admitted.add(id);
            busyUntil.set(clock.get() + 4);
            return true;
        }, () -> clock.addAndGet(5), () -> true, clock::get, 10);

        assertEquals(PendingSimulationQueue.DrainResult.COMPLETE, result);
        assertTrue("Total batch time exceeds one stall interval", clock.get() > 10);
        assertTrue(pending.isEmpty());
        assertEquals(4, admitted.size());
        assertEquals(Set.of(1, 2, 3, 4), Set.copyOf(admitted));
    }

    @Test
    public void stalledWorkersTimeOutWithoutLosingPendingJobs() {
        Map<Integer, String> original = Map.of(1, "a", 2, "b");
        Map<Integer, String> pending = new HashMap<>(original);
        AtomicLong clock = new AtomicLong();
        PendingSimulationQueue.DrainResult result = PendingSimulationQueue.drain(pending, (id, value) -> false,
                () -> clock.addAndGet(5), () -> true, clock::get, 10);
        assertEquals(PendingSimulationQueue.DrainResult.STALLED, result);
        assertEquals(10, clock.get());
        assertEquals(original, pending);
    }

    @Test
    public void failedDispatchKeepsTheUnsentJob() {
        Map<Integer, String> pending = new HashMap<>(Map.of(1, "a"));
        assertThrows(IllegalStateException.class, () -> PendingSimulationQueue.drain(pending,
                (id, value) -> { throw new IllegalStateException("send failed"); },
                () -> {}, () -> true, () -> 0, 10));
        assertEquals(Map.of(1, "a"), pending);
    }

    @Test
    public void stopsImmediatelyWhenTheLastWorkerDisappearsWhileWaiting() {
        Map<Integer, String> pending = new HashMap<>(Map.of(1, "a"));
        boolean[] available = {true};
        AtomicLong clock = new AtomicLong();

        PendingSimulationQueue.DrainResult result = PendingSimulationQueue.drain(
                pending,
                (id, value) -> false,
                () -> available[0] = false,
                () -> available[0],
                clock::get,
                10_000
        );

        assertEquals(PendingSimulationQueue.DrainResult.NO_CAPACITY, result);
        assertEquals(Map.of(1, "a"), pending);
        assertEquals(0, clock.get());
    }
}
