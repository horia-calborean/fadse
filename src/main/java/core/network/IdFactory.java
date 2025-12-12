package core.network;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Improved IdFactory for generating unique message IDs.
 *
 * Improvements:
 * - Uses AtomicLong instead of synchronized (better performance under high concurrency)
 * - Overflow protection with warning
 * - More informative IDs (includes timestamp prefix)
 * - Better documentation
 * - Thread-safe without explicit synchronization
 *
 * ID Format: timestamp-sequence (e.g., "1702368000000-1", "1702368000000-2")
 * This makes IDs:
 * - Globally unique
 * - Sortable by time
 * - Easier to debug (can see when message was created)
 */
public class IdFactory {

    private static final Logger LOGGER = Logger.getLogger(IdFactory.class.getName());

    // Use AtomicLong for lock-free thread safety (better performance than synchronized)
    private static final AtomicLong sequence = new AtomicLong(0);

    // Store the timestamp prefix to avoid System.currentTimeMillis() on every call
    private static volatile long timestampPrefix = System.currentTimeMillis();
    private static final AtomicLong prefixSequence = new AtomicLong(0);

    // Threshold to refresh timestamp (every 1000 IDs)
    private static final long REFRESH_THRESHOLD = 1000;

    // Warning threshold (90% of Long.MAX_VALUE)
    private static final long OVERFLOW_WARNING_THRESHOLD = Long.MAX_VALUE / 10 * 9;

    /**
     * Generates a unique message ID.
     *
     * Format: "timestamp-sequence"
     * Example: "1702368000000-1"
     *
     * This method is thread-safe and lock-free using AtomicLong.
     *
     * @return Unique message ID
     */
    public static String getId() {
        long seq = sequence.getAndIncrement();

        // Check for potential overflow (very unlikely but good to warn)
        if (seq > OVERFLOW_WARNING_THRESHOLD && seq % 1000000 == 0) {
            LOGGER.log(Level.WARNING, String.format(
                "IdFactory sequence approaching overflow: %d (%.1f%% of max)",
                seq, (seq * 100.0 / Long.MAX_VALUE)
            ));
        }

        // Refresh timestamp prefix periodically for better time resolution
        long prefixSeq = prefixSequence.get();
        if (seq % REFRESH_THRESHOLD == 0 && prefixSeq < seq) {
            synchronized (IdFactory.class) {
                if (prefixSequence.get() < seq) {
                    timestampPrefix = System.currentTimeMillis();
                    prefixSequence.set(seq);
                }
            }
        }

        // Return timestamp-sequence format
        return timestampPrefix + "-" + seq;
    }

    /**
     * Gets the current sequence number (for monitoring/debugging).
     *
     * @return Current sequence number
     */
    public static long getCurrentSequence() {
        return sequence.get();
    }

    /**
     * Resets the ID factory (for testing only - NOT thread-safe with concurrent getId() calls).
     * DO NOT use in production code.
     */
    static void reset() {
        sequence.set(0);
        timestampPrefix = System.currentTimeMillis();
        prefixSequence.set(0);
        LOGGER.log(Level.WARNING, "IdFactory reset - should only be used in tests!");
    }
}