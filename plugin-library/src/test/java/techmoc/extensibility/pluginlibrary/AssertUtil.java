package techmoc.extensibility.pluginlibrary;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;


public final class AssertUtil {

  private static String MAX_TIMEOUT_EXPIRED_ERROR_MESSAGE =
      "Max wait time expired before the expected value appeared.";
  private static long SLEEP_TIME_MS = 5;

  private static long minWaitTimeMs() {
    return SLEEP_TIME_MS * 2;
  }

  public static void assertTrueWait(BooleanSupplier test, long maxWaitTimeMs)
      throws InterruptedException {
    assertTrueWait(MAX_TIMEOUT_EXPIRED_ERROR_MESSAGE, test, maxWaitTimeMs);
  }

  public static void assertTrueWait(String errorMsg, BooleanSupplier test, long maxWaitTimeMs)
      throws InterruptedException {
    // Validate input.
    Objects.requireNonNull(errorMsg);
    Objects.requireNonNull(test);
    if (maxWaitTimeMs <= 0) {
      throw new IllegalArgumentException("Max Wait Time MS must be greater than zero.");
    }

    // Adjust the max wait time, if it was set below the minimum allowed.
    maxWaitTimeMs = adjustMaxWaitTimeMsIfSetTooLow(maxWaitTimeMs);

    // Set the end time.
    long endTime = calcEndTime(maxWaitTimeMs);

    // Wait for the expected value, or for the wait time to expire (whichever comes first).
    do {
      // Test if the expected value has appeared.
      if (test.getAsBoolean()) {
        return;
      }

      // Sleep for a short period of time.
      Thread.sleep(SLEEP_TIME_MS);
    } while (System.nanoTime() < endTime);

    throw new AssertionError(errorMsg);
  }

  public static void assertFalseWait(BooleanSupplier test, long maxWaitTimeMs)
      throws InterruptedException {
    assertFalseWait(MAX_TIMEOUT_EXPIRED_ERROR_MESSAGE, test, maxWaitTimeMs);
  }

  public static void assertFalseWait(String errorMsg, BooleanSupplier test, long maxWaitTimeMs)
      throws InterruptedException {
    // Validate input.
    Objects.requireNonNull(errorMsg);
    Objects.requireNonNull(test);
    if (maxWaitTimeMs <= 0) {
      throw new IllegalArgumentException("Max Wait Time MS must be greater than zero.");
    }

    // Adjust the max wait time, if it was set below the minimum allowed.
    maxWaitTimeMs = adjustMaxWaitTimeMsIfSetTooLow(maxWaitTimeMs);

    // Set the end time.
    long endTime = calcEndTime(maxWaitTimeMs);

    // Wait for the expected value, or for the wait time to expire (whichever comes first).
    do {
      // Test if the expected value has appeared.
      if (!test.getAsBoolean()) {
        return;
      }

      // Sleep for a short period of time.
      Thread.sleep(SLEEP_TIME_MS);
    } while (System.nanoTime() < endTime);

    throw new AssertionError(errorMsg);
  }

  public static void assertEqualsWait(IntSupplier test, int expectedValue, long maxWaitTimeMs)
      throws InterruptedException {
    assertEqualsWait(MAX_TIMEOUT_EXPIRED_ERROR_MESSAGE, test, expectedValue, maxWaitTimeMs);
  }

  public static void assertEqualsWait(
      String errorMsg, IntSupplier test, int expectedValue, long maxWaitTimeMs)
      throws InterruptedException {
    // Validate input.
    Objects.requireNonNull(errorMsg);
    Objects.requireNonNull(test);
    if (maxWaitTimeMs <= 0) {
      throw new IllegalArgumentException("Max Wait Time MS must be greater than zero.");
    }

    // Adjust the max wait time, if it was set below the minimum allowed.
    maxWaitTimeMs = adjustMaxWaitTimeMsIfSetTooLow(maxWaitTimeMs);

    // Set the end time.
    long endTime = calcEndTime(maxWaitTimeMs);

    // Wait for the expected value, or for the wait time to expire (whichever comes first).
    do {
      // Test if the expected value has appeared.
      if (test.getAsInt() == expectedValue) {
        return;
      }

      // Sleep for a short period of time.
      Thread.sleep(SLEEP_TIME_MS);
    } while (System.nanoTime() < endTime);

    throw new AssertionError(errorMsg);
  }

  public static void assertEqualsWait(LongSupplier test, long expectedValue, long maxWaitTimeMs)
      throws InterruptedException {
    assertEqualsWait(MAX_TIMEOUT_EXPIRED_ERROR_MESSAGE, test, expectedValue, maxWaitTimeMs);
  }

  public static void assertEqualsWait(
      String errorMsg, LongSupplier test, long expectedValue, long maxWaitTimeMs)
      throws InterruptedException {
    // Validate input.
    Objects.requireNonNull(errorMsg);
    Objects.requireNonNull(test);
    if (maxWaitTimeMs <= 0) {
      throw new IllegalArgumentException("Max Wait Time MS must be greater than zero.");
    }

    // Adjust the max wait time, if it was set below the minimum allowed.
    maxWaitTimeMs = adjustMaxWaitTimeMsIfSetTooLow(maxWaitTimeMs);

    // Set the end time.
    long endTime = calcEndTime(maxWaitTimeMs);

    // Wait for the expected value, or for the wait time to expire (whichever comes first).
    do {
      // Test if the expected value has appeared.
      if (test.getAsLong() == expectedValue) {
        return;
      }

      // Sleep for a short period of time.
      Thread.sleep(SLEEP_TIME_MS);
    } while (System.nanoTime() < endTime);

    throw new AssertionError(errorMsg);
  }

  private static long adjustMaxWaitTimeMsIfSetTooLow(long maxWaitTimeMs) {
    return (maxWaitTimeMs < minWaitTimeMs()) ? minWaitTimeMs() : maxWaitTimeMs;
  }

  private static long calcEndTime(long maxWaitTimeMs) {
    return System.nanoTime() + (maxWaitTimeMs * 1000000);
  }
}
