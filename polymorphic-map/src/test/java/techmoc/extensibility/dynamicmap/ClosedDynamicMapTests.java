package techmoc.extensibility.dynamicmap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.NetPermission;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import techmoc.extensibility.domainmaps.JavaClosedDomainMap;


public class ClosedDynamicMapTests {

  @Test
  void getterAndSetterTests() throws MalformedURLException {
    JavaClosedDomainMap boundedDynamicMap = new JavaClosedDomainMap();

    Set<String> testSet = new HashSet<>();
    testSet.add("One");
    testSet.add("Two");
    testSet.add("Three");

    Map<String, String> testMap = new HashMap<>();
    testMap.put("1", "One");
    testMap.put("2", "Two");
    testMap.put("3", "Three");

    // AtomicBoolean.
    testGetterSetter(new AtomicBoolean(true),
        boundedDynamicMap::getAtomicBoolean, boundedDynamicMap::putAtomicBoolean);

    // AtomicInteger.
    testGetterSetter(new AtomicInteger(1),
        boundedDynamicMap::getAtomicInteger, boundedDynamicMap::putAtomicInteger);

    // AtomicLong.
    testGetterSetter(new AtomicLong(5555),
        boundedDynamicMap::getAtomicLong, boundedDynamicMap::putAtomicLong);

    // BigDecimal.
    testGetterSetter(new BigDecimal(10.555),
        boundedDynamicMap::getBigDecimal, boundedDynamicMap::putBigDecimal);

    // BigInteger.
    testGetterSetter(new BigInteger(new byte[]{5, 5, 5, 5}),
        boundedDynamicMap::getBigInteger, boundedDynamicMap::putBigInteger);

    // Boolean.
    testGetterSetter(false,
        boundedDynamicMap::getBoolean, boundedDynamicMap::putBoolean);

    // Byte.
    testGetterSetter(Byte.parseByte("1"),
        boundedDynamicMap::getByte, boundedDynamicMap::putByte);

    // ByteBuffer.
    testGetterSetter(ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5}),
        boundedDynamicMap::getByteBuffer, boundedDynamicMap::putByteBuffer);

    // Character.
    testGetterSetter('a',
        boundedDynamicMap::getCharacter, boundedDynamicMap::putCharacter);

    // CharBuffer.
    testGetterSetter(CharBuffer.wrap(new char[]{'a', 'b', 'c', 'd', 'e'}),
        boundedDynamicMap::getCharBuffer, boundedDynamicMap::putCharBuffer);

    // ConcurrentHashMap.
    testGetterSetter(new ConcurrentHashMap<>(testMap),
        boundedDynamicMap::getConcurrentHashMap, boundedDynamicMap::putConcurrentHashMap);

    // ConcurrentLinkedDeque.
    testGetterSetter(new ConcurrentLinkedDeque<>(testSet),
        boundedDynamicMap::getConcurrentLinkedDeque, boundedDynamicMap::putConcurrentLinkedDeque);

    // ConcurrentLinkedQueue.
    testGetterSetter(new ConcurrentLinkedQueue<>(testSet),
        boundedDynamicMap::getConcurrentLinkedQueue, boundedDynamicMap::putConcurrentLinkedQueue);

    // Double.
    testGetterSetter(5.555,
        boundedDynamicMap::getDouble, boundedDynamicMap::putDouble);

    // DoubleBuffer.
    testGetterSetter(DoubleBuffer.wrap(new double[]{1.1, 2.2, 3.3, 4.4, 5.5}),
        boundedDynamicMap::getDoubleBuffer, boundedDynamicMap::putDoubleBuffer);

    // Duration.
    testGetterSetter(Duration.of(5, ChronoUnit.SECONDS),
        boundedDynamicMap::getDuration, boundedDynamicMap::putDuration);

    // File.
    testGetterSetter(new File("/whatever"),
        boundedDynamicMap::getFile, boundedDynamicMap::putFile);

    // Float.
    testGetterSetter(1.2345f,
        boundedDynamicMap::getFloat, boundedDynamicMap::putFloat);

    // FloatBuffer.
    testGetterSetter(FloatBuffer.wrap(new float[]{1.1f, 2.2f, 3.3f, 4.4f, 5.5f}),
        boundedDynamicMap::getFloatBuffer, boundedDynamicMap::putFloatBuffer);

    // HashMap.
    testGetterSetter(new HashMap<>(testMap),
        boundedDynamicMap::getHashMap, boundedDynamicMap::putHashMap);

    // HashSet.
    testGetterSetter(new HashSet<>(testSet),
        boundedDynamicMap::getHashSet, boundedDynamicMap::putHashSet);

    // InetAddress.
    testGetterSetter(InetAddress.getLoopbackAddress(),
        boundedDynamicMap::getInetAddress, boundedDynamicMap::putInetAddress);

    // InetSocketAddress.
    testGetterSetter(InetSocketAddress.createUnresolved("127.0.0.1", 5000),
        boundedDynamicMap::getInetSocketAddress, boundedDynamicMap::putInetSocketAddress);

    // Instant.
    testGetterSetter(Instant.EPOCH,
        boundedDynamicMap::getInstant, boundedDynamicMap::putInstant);

    // IntBuffer.
    testGetterSetter(IntBuffer.wrap(new int[]{1, 2, 3, 4, 5, 6, 7}),
        boundedDynamicMap::getIntBuffer, boundedDynamicMap::putIntBuffer);

    // Integer.
    testGetterSetter(55555,
        boundedDynamicMap::getInteger, boundedDynamicMap::putInteger);

    // LinkedHashMap.
    testGetterSetter(new LinkedHashMap<>(testMap),
        boundedDynamicMap::getLinkedHashMap, boundedDynamicMap::putLinkedHashMap);

    // LinkedHashSet.
    testGetterSetter(new LinkedHashSet<>(testSet),
        boundedDynamicMap::getLinkedHashSet, boundedDynamicMap::putLinkedHashSet);

    // LocalDate.
    testGetterSetter(LocalDate.EPOCH,
        boundedDynamicMap::getLocalDate, boundedDynamicMap::putLocalDate);

    // LocalDateTime.
    testGetterSetter(LocalDateTime.MAX,
        boundedDynamicMap::getLocalDateTime, boundedDynamicMap::putLocalDateTime);

    // LocalTime.
    testGetterSetter(LocalTime.MIDNIGHT,
        boundedDynamicMap::getLocalTime, boundedDynamicMap::putLocalTime);

    // Long.
    testGetterSetter((long) 123456789,
        boundedDynamicMap::getLong, boundedDynamicMap::putLong);

    // LongBuffer.
    testGetterSetter(LongBuffer.wrap(new long[]{100000, 200000, 300000, 400000, 500000}),
        boundedDynamicMap::getLongBuffer, boundedDynamicMap::putLongBuffer);

    // MonthDay.
    testGetterSetter(MonthDay.now(),
        boundedDynamicMap::getMonthDay, boundedDynamicMap::putMonthDay);

    // NetPermission.
    testGetterSetter(new NetPermission("blah", "blah-blah"),
        boundedDynamicMap::getNetPermission, boundedDynamicMap::putNetPermission);

    // OffsetDateTime.
    testGetterSetter(OffsetDateTime.now(),
        boundedDynamicMap::getOffsetDateTime, boundedDynamicMap::putOffsetDateTime);

    // OffsetTime.
    testGetterSetter(OffsetTime.now(),
        boundedDynamicMap::getOffsetTime, boundedDynamicMap::putOffsetTime);

    // Period.
    testGetterSetter(Period.ofYears(10),
        boundedDynamicMap::getPeriod, boundedDynamicMap::putPeriod);

    // Short.
    testGetterSetter((short) 12345,
        boundedDynamicMap::getShort, boundedDynamicMap::putShort);

    // ShortBuffer.
    testGetterSetter(ShortBuffer.wrap(new short[]{1, 2, 3, 4, 5, 6, 7}),
        boundedDynamicMap::getShortBuffer, boundedDynamicMap::putShortBuffer);

    // String.
    testGetterSetter("blah blah blah",
        boundedDynamicMap::getString, boundedDynamicMap::putString);

    // TreeMap.
    testGetterSetter(new TreeMap<>(testMap),
        boundedDynamicMap::getTreeMap, boundedDynamicMap::putTreeMap);

    // TreeSet.
    testGetterSetter(new TreeSet<>(testSet),
        boundedDynamicMap::getTreeSet, boundedDynamicMap::putTreeSet);

    // URI.
    testGetterSetter(URI.create("http://127.0.0.1:80"),
        boundedDynamicMap::getURI, boundedDynamicMap::putURI);

    // URL.
    testGetterSetter(new URL("http", "127.0.0.1", "/index.html"),
        boundedDynamicMap::getURL, boundedDynamicMap::putURL);

    // UUID.
    testGetterSetter(UUID.randomUUID(),
        boundedDynamicMap::getUUID, boundedDynamicMap::putUUID);

    // Year.
    testGetterSetter(Year.now(),
        boundedDynamicMap::getYear, boundedDynamicMap::putYear);

    // YearMonth.
    testGetterSetter(YearMonth.now(),
        boundedDynamicMap::getYearMonth, boundedDynamicMap::putYearMonth);

    // ZonedDateTime.
    testGetterSetter(ZonedDateTime.now(),
        boundedDynamicMap::getZonedDateTime, boundedDynamicMap::putZonedDateTime);

    // ZoneId.
    testGetterSetter(ZoneId.systemDefault(),
        boundedDynamicMap::getZoneId, boundedDynamicMap::putZoneId);

    // ZoneOffset.
    testGetterSetter(ZoneOffset.ofHours(5),
        boundedDynamicMap::getZoneOffset, boundedDynamicMap::putZoneOffset);

//    // .
//    testGetterSetter(new (),
//        boundedDynamicMap::get, boundedDynamicMap::put);
  }

  private <T> void testGetterSetter(
      T value,
      Function<String, T> getMethod,
      BiConsumer<String, T> putMethod) {
    String key = value.getClass().getSimpleName();
    putMethod.accept(key, value);
    assertEquals(value, getMethod.apply(key));
  }
}
