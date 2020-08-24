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
import techmoc.extensibility.domainmaps.JavaDomainMap;


public class DomainMapTests {

  @Test
  void getterAndSetterTests() throws MalformedURLException {
    JavaDomainMap dynamicMap = new JavaDomainMap();

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
        dynamicMap::getAtomicBoolean, dynamicMap::putAtomicBoolean);

    // AtomicInteger.
    testGetterSetter(new AtomicInteger(1),
        dynamicMap::getAtomicInteger, dynamicMap::putAtomicInteger);

    // AtomicLong.
    testGetterSetter(new AtomicLong(5555),
        dynamicMap::getAtomicLong, dynamicMap::putAtomicLong);

    // BigDecimal.
    testGetterSetter(new BigDecimal(10.555),
        dynamicMap::getBigDecimal, dynamicMap::putBigDecimal);

    // BigInteger.
    testGetterSetter(new BigInteger(new byte[]{5, 5, 5, 5}),
        dynamicMap::getBigInteger, dynamicMap::putBigInteger);

    // Boolean.
    testGetterSetter(false,
        dynamicMap::getBoolean, dynamicMap::putBoolean);

    // Byte.
    testGetterSetter(Byte.parseByte("1"),
        dynamicMap::getByte, dynamicMap::putByte);

    // ByteBuffer.
    testGetterSetter(ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5}),
        dynamicMap::getByteBuffer, dynamicMap::putByteBuffer);

    // Character.
    testGetterSetter('a',
        dynamicMap::getCharacter, dynamicMap::putCharacter);

    // CharBuffer.
    testGetterSetter(CharBuffer.wrap(new char[]{'a', 'b', 'c', 'd', 'e'}),
        dynamicMap::getCharBuffer, dynamicMap::putCharBuffer);

    // ConcurrentHashMap.
    testGetterSetter(new ConcurrentHashMap<>(testMap),
        dynamicMap::getConcurrentHashMap, dynamicMap::putConcurrentHashMap);

    // ConcurrentLinkedDeque.
    testGetterSetter(new ConcurrentLinkedDeque<>(testSet),
        dynamicMap::getConcurrentLinkedDeque, dynamicMap::putConcurrentLinkedDeque);

    // ConcurrentLinkedQueue.
    testGetterSetter(new ConcurrentLinkedQueue<>(testSet),
        dynamicMap::getConcurrentLinkedQueue, dynamicMap::putConcurrentLinkedQueue);

    // Double.
    testGetterSetter(5.555,
        dynamicMap::getDouble, dynamicMap::putDouble);

    // DoubleBuffer.
    testGetterSetter(DoubleBuffer.wrap(new double[]{1.1, 2.2, 3.3, 4.4, 5.5}),
        dynamicMap::getDoubleBuffer, dynamicMap::putDoubleBuffer);

    // Duration.
    testGetterSetter(Duration.of(5, ChronoUnit.SECONDS),
        dynamicMap::getDuration, dynamicMap::putDuration);

    // File.
    testGetterSetter(new File("/whatever"),
        dynamicMap::getFile, dynamicMap::putFile);

    // Float.
    testGetterSetter(1.2345f,
        dynamicMap::getFloat, dynamicMap::putFloat);

    // FloatBuffer.
    testGetterSetter(FloatBuffer.wrap(new float[]{1.1f, 2.2f, 3.3f, 4.4f, 5.5f}),
        dynamicMap::getFloatBuffer, dynamicMap::putFloatBuffer);

    // HashMap.
    testGetterSetter(new HashMap<>(testMap),
        dynamicMap::getHashMap, dynamicMap::putHashMap);

    // HashSet.
    testGetterSetter(new HashSet<>(testSet),
        dynamicMap::getHashSet, dynamicMap::putHashSet);

    // InetAddress.
    testGetterSetter(InetAddress.getLoopbackAddress(),
        dynamicMap::getInetAddress, dynamicMap::putInetAddress);

    // InetSocketAddress.
    testGetterSetter(InetSocketAddress.createUnresolved("127.0.0.1", 5000),
        dynamicMap::getInetSocketAddress, dynamicMap::putInetSocketAddress);

    // Instant.
    testGetterSetter(Instant.EPOCH,
        dynamicMap::getInstant, dynamicMap::putInstant);

    // IntBuffer.
    testGetterSetter(IntBuffer.wrap(new int[]{1, 2, 3, 4, 5, 6, 7}),
        dynamicMap::getIntBuffer, dynamicMap::putIntBuffer);

    // Integer.
    testGetterSetter(55555,
        dynamicMap::getInteger, dynamicMap::putInteger);

    // LinkedHashMap.
    testGetterSetter(new LinkedHashMap<>(testMap),
        dynamicMap::getLinkedHashMap, dynamicMap::putLinkedHashMap);

    // LinkedHashSet.
    testGetterSetter(new LinkedHashSet<>(testSet),
        dynamicMap::getLinkedHashSet, dynamicMap::putLinkedHashSet);

    // LocalDate.
    testGetterSetter(LocalDate.EPOCH,
        dynamicMap::getLocalDate, dynamicMap::putLocalDate);

    // LocalDateTime.
    testGetterSetter(LocalDateTime.MAX,
        dynamicMap::getLocalDateTime, dynamicMap::putLocalDateTime);

    // LocalTime.
    testGetterSetter(LocalTime.MIDNIGHT,
        dynamicMap::getLocalTime, dynamicMap::putLocalTime);

    // Long.
    testGetterSetter((long) 123456789,
        dynamicMap::getLong, dynamicMap::putLong);

    // LongBuffer.
    testGetterSetter(LongBuffer.wrap(new long[]{100000, 200000, 300000, 400000, 500000}),
        dynamicMap::getLongBuffer, dynamicMap::putLongBuffer);

    // MonthDay.
    testGetterSetter(MonthDay.now(),
        dynamicMap::getMonthDay, dynamicMap::putMonthDay);

    // NetPermission.
    testGetterSetter(new NetPermission("blah", "blah-blah"),
        dynamicMap::getNetPermission, dynamicMap::putNetPermission);

    // OffsetDateTime.
    testGetterSetter(OffsetDateTime.now(),
        dynamicMap::getOffsetDateTime, dynamicMap::putOffsetDateTime);

    // OffsetTime.
    testGetterSetter(OffsetTime.now(),
        dynamicMap::getOffsetTime, dynamicMap::putOffsetTime);

    // Period.
    testGetterSetter(Period.ofYears(10),
        dynamicMap::getPeriod, dynamicMap::putPeriod);

    // Short.
    testGetterSetter((short) 12345,
        dynamicMap::getShort, dynamicMap::putShort);

    // ShortBuffer.
    testGetterSetter(ShortBuffer.wrap(new short[]{1, 2, 3, 4, 5, 6, 7}),
        dynamicMap::getShortBuffer, dynamicMap::putShortBuffer);

    // String.
    testGetterSetter("blah blah blah",
        dynamicMap::getString, dynamicMap::putString);

    // TreeMap.
    testGetterSetter(new TreeMap<>(testMap),
        dynamicMap::getTreeMap, dynamicMap::putTreeMap);

    // TreeSet.
    testGetterSetter(new TreeSet<>(testSet),
        dynamicMap::getTreeSet, dynamicMap::putTreeSet);

    // URI.
    testGetterSetter(URI.create("http://127.0.0.1:80"),
        dynamicMap::getURI, dynamicMap::putURI);

    // URL.
    testGetterSetter(new URL("http", "127.0.0.1", "/index.html"),
        dynamicMap::getURL, dynamicMap::putURL);

    // UUID.
    testGetterSetter(UUID.randomUUID(),
        dynamicMap::getUUID, dynamicMap::putUUID);

    // Year.
    testGetterSetter(Year.now(),
        dynamicMap::getYear, dynamicMap::putYear);

    // YearMonth.
    testGetterSetter(YearMonth.now(),
        dynamicMap::getYearMonth, dynamicMap::putYearMonth);

    // ZonedDateTime.
    testGetterSetter(ZonedDateTime.now(),
        dynamicMap::getZonedDateTime, dynamicMap::putZonedDateTime);

    // ZoneId.
    testGetterSetter(ZoneId.systemDefault(),
        dynamicMap::getZoneId, dynamicMap::putZoneId);

    // ZoneOffset.
    testGetterSetter(ZoneOffset.ofHours(5),
        dynamicMap::getZoneOffset, dynamicMap::putZoneOffset);

//    // .
//    testGetterSetter(new (),
//        dynamicMap::get, dynamicMap::put);
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
