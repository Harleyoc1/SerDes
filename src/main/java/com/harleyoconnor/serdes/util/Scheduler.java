package com.harleyoconnor.serdes.util;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Harley O'Connor
 * @since 0.0.5
 */
// TODO: Move to JavaUtilities, add Javadoc, and make more overloads.
public final class Scheduler {

    private Scheduler() {}

    public static void schedule(final Runnable runnable, final Duration period) {
        schedule(runnable, 0, period.toMillis());
    }

    public static void schedule(final Runnable runnable, final Duration delay, final Duration period) {
        schedule(runnable, delay.toMillis(), period.toMillis());
    }

    public static void schedule(final Runnable runnable, final long delay, final long period) {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runnable.run();
            }
        }, delay, period);
    }

}
