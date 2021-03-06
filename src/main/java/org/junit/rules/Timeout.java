package org.junit.rules;

import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.concurrent.TimeUnit;

/**
 * The Timeout Rule applies the same timeout to all test methods in a class:
 * <pre>
 * public static class HasGlobalLongTimeout {
 *
 *  &#064;Rule
 *  public Timeout globalTimeout= new Timeout(20);
 *
 *  &#064;Test
 *  public void run1() throws InterruptedException {
 *      Thread.sleep(100);
 *  }
 *
 *  &#064;Test
 *  public void infiniteLoop() {
 *      while (true) {}
 *  }
 * }
 * </pre>
 * <p>
 * Each test is run in a new thread. If the specified timeout elapses before
 * the test completes, its execution is interrupted via {@link Thread#interrupt()}.
 * This happens in interruptable I/O and locks, and methods in {@link Object}
 * and {@link Thread} throwing {@link InterruptedException}.
 * <p>
 * A specified timeout of 0 will be interpreted as not set, however tests will
 * still launch from separate threads. This can be useful for disabling timeouts
 * in environments where they are dynamically set based on some property.
 *
 * @since 4.7
 */
public class Timeout implements TestRule {
    private final long timeout;
    private final TimeUnit timeUnit;
    private final boolean lookForStuckThread;

    /**
     * Create a {@code Timeout} instance with the timeout specified
     * in milliseconds.
     * <p>
     * This constructor is deprecated.
     * <p>
     * Instead use {@link #Timeout(long, java.util.concurrent.TimeUnit)},
     * {@link Timeout#millis(long)}, or {@link Timeout#seconds(long)}.
     *
     * @param millis the maximum time in milliseconds to allow the
     * test to run before it should timeout
     */
    @Deprecated
    public Timeout(int millis) {
        this(millis, TimeUnit.MILLISECONDS);
    }

    /**
     * Create a {@code Timeout} instance with the timeout specified
     * at the timeUnit of granularity of the provided {@code TimeUnit}.
     *
     * @param timeout the maximum time to allow the test to run
     * before it should timeout
     * @param timeUnit the time unit for the {@code timeout}
     * @since 4.12
     */
    public Timeout(long timeout, TimeUnit timeUnit) {
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        lookForStuckThread = false;
    }

    /**
     * Create a {@code Timeout} instance with the same fields as {@code t}
     * except for {@code lookForStuckThread}.
     *
     * @param t the {@code Timeout} instance to copy
     * @param lookForStuckThread whether to look for a stuck thread
     * @since 4.12
     */
    protected Timeout(Timeout t, boolean lookForStuckThread) {
        timeout = t.timeout;
        timeUnit = t.timeUnit;
        this.lookForStuckThread = lookForStuckThread;
    }

    /**
     * @param millis the timeout in milliseconds
     * @since 4.12
     */
    public static Timeout millis(long millis) {
        return new Timeout(millis, TimeUnit.MILLISECONDS);
    }

    /**
     * @param seconds the timeout in seconds
     * @since 4.12
     */
    public static Timeout seconds(long seconds) {
        return new Timeout(seconds, TimeUnit.SECONDS);
    }

    /**
     * Specifies whether to look for a stuck thread.  If a timeout occurs and this
     * feature is enabled, the test will look for a thread that appears to be stuck
     * and dump its backtrace.  This feature is experimental.  Behavior may change
     * after the 4.12 release in response to feedback.
     * @param enable {@code true} to enable the feature
     * @return This object
     * @since 4.12
     */
    public Timeout lookingForStuckThread(boolean enable) {
        return new Timeout(this, enable);
    }

    public Statement apply(Statement base, Description description) {
        try {
            return FailOnTimeout.builder()
                .withTimeout(timeout, timeUnit)
                .withLookingForStuckThread(lookForStuckThread)
                .build(base);
        } catch (final Exception e) {
            return new Statement() {
                @Override public void evaluate() throws Throwable {
                    throw new RuntimeException("Invalid parameters for Timeout", e);
                }
            };
        }
    }
}
