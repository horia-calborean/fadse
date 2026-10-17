package core.network;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;

/** Drains pending work while timing out only when dispatch stops making progress. */
final class PendingSimulationQueue {
    enum DrainResult {
        COMPLETE,
        STALLED,
        NO_CAPACITY
    }

    private PendingSimulationQueue() {
    }

    static <K, V> DrainResult drain(Map<K, V> pending, BiPredicate<K, V> dispatch,
            Runnable awaitCapacity, BooleanSupplier canProgress,
            LongSupplier clock, long stallTimeout) {
        long lastProgress = clock.getAsLong();
        while (!pending.isEmpty()) {
            if (!canProgress.getAsBoolean()) {
                return DrainResult.NO_CAPACITY;
            }
            if (clock.getAsLong() - lastProgress >= stallTimeout) {
                return DrainResult.STALLED;
            }
            for (Map.Entry<K, V> entry : new HashMap<>(pending).entrySet()) {
                if (clock.getAsLong() - lastProgress >= stallTimeout) {
                    return DrainResult.STALLED;
                }
                if (!pending.remove(entry.getKey(), entry.getValue())) {
                    continue;
                }
                boolean admitted = false;
                try {
                    admitted = dispatch.test(entry.getKey(), entry.getValue());
                    if (admitted) {
                        lastProgress = clock.getAsLong();
                    }
                } finally {
                    if (!admitted) {
                        pending.putIfAbsent(entry.getKey(), entry.getValue());
                    }
                }
            }
            if (!pending.isEmpty()) {
                awaitCapacity.run();
                if (!canProgress.getAsBoolean()) {
                    return DrainResult.NO_CAPACITY;
                }
            }
        }
        return DrainResult.COMPLETE;
    }
}
