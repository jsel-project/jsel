package techmoc.extensibility.domainmaps;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import techmoc.extensibility.polymorphicmap.PolymorphicMap;


public class JavaDomainMap extends PolymorphicMap {

  //---------- java.lang.* Object Methods ----------//


  /**
   * Returns the Boolean value stored at the given key.
   *
   * @param key Key.
   * @return Boolean value.
   */
  public final Boolean getBoolean(String key) {
    return this.get(key, Boolean.class);
  }

  /**
   * Stores the Boolean value at the given key. Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final Boolean putBoolean(String key, Boolean value) {
    return this.put(key, value);
  }

  /**
   * Returns the Byte value stored at the given key.
   *
   * @param key Key.
   * @return Byte value.
   */
  public final Byte getByte(String key) {
    return this.get(key, Byte.class);
  }

  /**
   * Stores the Byte value at the given key. Returns the old value, or null none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final Byte putByte(String key, Byte value) {
    return this.put(key, value);
  }

  /**
   * Returns the Character value stored at the given key.
   *
   * @param key Key.
   * @return Character value.
   */
  public final Character getCharacter(String key) {
    return this.get(key, Character.class);
  }

  /**
   * Stores the Character value at the given key. Returns the old value, or null none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final Character putCharacter(String key, Character value) {
    return this.put(key, value);
  }

  /**
   * Returns the Double value stored at the given key.
   *
   * @param key Key.
   * @return Double value.
   */
  public final Double getDouble(String key) {
    return this.get(key, Double.class);
  }

  /**
   * Stores the Double value at the given key. Returns the old value, or null none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final Double putDouble(String key, Double value) {
    return this.put(key, value);
  }

  /**
   * Returns the Float value stored at the given key.
   *
   * @param key Key.
   * @return Float value.
   */
  public final Float getFloat(String key) {
    return this.get(key, Float.class);
  }

  /**
   * Stores the Float value at the given key. Returns the old value, or null none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final Float putFloat(String key, Float value) {
    return this.put(key, value);
  }

  /**
   * Returns the Integer value stored at the given key.
   *
   * @param key Key.
   * @return Integer value.
   */
  public final Integer getInteger(String key) {
    return this.get(key, Integer.class);
  }

  /**
   * Stores the Integer value at the given key. Returns the old value, or null none exists.
   *
   * @param key Key.
   * @return Integer value.
   */
  public final Integer putInteger(String key, Integer value) {
    return this.put(key, value);
  }

  /**
   * Returns the Long value stored at the given key.
   *
   * @param key Key.
   * @return Long value.
   */
  public final Long getLong(String key) {
    return this.get(key, Long.class);
  }

  /**
   * Stores the Long value at the given key. Returns the old value, or null none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final Long putLong(String key, Long value) {
    return this.put(key, value);
  }

  /**
   * Returns the Short value stored at the given key.
   *
   * @param key Key.
   * @return Short value.
   */
  public final Short getShort(String key) {
    return this.get(key, Short.class);
  }

  /**
   * Stores the Short value at the given key. Returns the old value, or null none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final Short putShort(String key, Short value) {
    return this.put(key, value);
  }

  /**
   * Returns the String value stored at the given key.
   *
   * @param key Key.
   * @return String value.
   */
  public final String getString(String key) {
    return this.get(key, String.class);
  }

  /**
   * Stores the String value at the given key. Returns the old value, or null none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final String putString(String key, String value) {
    return this.put(key, value);
  }

  //---------- java.io.* Object Methods ----------//


  /**
   * Returns the File value stored at the given key.
   *
   * @param key Key.
   * @return File value.
   */
  public final File getFile(String key) {
    return this.get(key, File.class);
  }

  /**
   * Stores the File value at the given key. Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final File putFile(String key, File value) {
    return this.put(key, value);
  }

  //---------- java.math.* Object Methods ----------//


  /**
   * Returns the BigDecimal value stored at the given key.
   *
   * @param key Key.
   * @return BigDecimal value.
   */
  public final BigDecimal getBigDecimal(String key) {
    return this.get(key, BigDecimal.class);
  }

  /**
   * Stores the BigDecimal value at the given key. Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final BigDecimal putBigDecimal(String key, BigDecimal value) {
    return this.put(key, value);
  }

  /**
   * Returns the BigInteger value stored at the given key.
   *
   * @param key Key.
   * @return BigInteger value.
   */
  public final BigInteger getBigInteger(String key) {
    return this.get(key, BigInteger.class);
  }

  /**
   * Stores the BigInteger value at the given key. Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final BigInteger putBigInteger(String key, BigInteger value) {
    return this.put(key, value);
  }

  //---------- java.net.* Object Methods ----------//


  /**
   * Returns the InetAddress value stored at the given key.
   *
   * @param key Key.
   * @return InetAddress value.
   */
  public final InetAddress getInetAddress(String key) {
    return this.get(key, InetAddress.class);
  }

  /**
   * Stores the InetAddress value at the given key. Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final InetAddress putInetAddress(String key, InetAddress value) {
    return this.put(key, value);
  }

  /**
   * Returns the InetSocketAddress value stored at the given key.
   *
   * @param key Key.
   * @return InetSocketAddress value.
   */
  public final InetSocketAddress getInetSocketAddress(String key) {
    return this.get(key, InetSocketAddress.class);
  }

  /**
   * Stores the InetSocketAddress value at the given key. Returns the old value, or null if none
   * exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final InetSocketAddress putInetSocketAddress(String key, InetSocketAddress value) {
    return this.put(key, value);
  }

  /**
   * Returns the NetPermission value stored at the given key.
   *
   * @param key Key.
   * @return NetPermission value.
   */
  public final NetPermission getNetPermission(String key) {
    return this.get(key, NetPermission.class);
  }

  /**
   * Stores the NetPermission value at the given key. Returns the old value, or null if none
   * exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final NetPermission putNetPermission(String key, NetPermission value) {
    return this.put(key, value);
  }

  /**
   * Returns the URI value stored at the given key.
   *
   * @param key Key.
   * @return URI value.
   */
  public final URI getURI(String key) {
    return this.get(key, URI.class);
  }

  /**
   * Stores the URI value at the given key. Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final URI putURI(String key, URI value) {
    return this.put(key, value);
  }

  /**
   * Returns the URL value stored at the given key.
   *
   * @param key Key.
   * @return URL value.
   */
  public final URL getURL(String key) {
    return this.get(key, URL.class);
  }

  /**
   * Stores the URL value at the given key. Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final URL putURL(String key, URL value) {
    return this.put(key, value);
  }

  //---------- java.nio.* Object Methods ----------//


  /**
   * Returns the ByteBuffer value stored at the given key.
   *
   * @param key Key.
   * @return ByteBuffer value.
   */
  public final ByteBuffer getByteBuffer(String key) {
    return this.get(key, ByteBuffer.class);
  }

  /**
   * Stores the ByteBuffer value at the given key. Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final ByteBuffer putByteBuffer(String key, ByteBuffer value) {
    return this.put(key, value);
  }

  /**
   * Returns the CharBuffer value stored at the given key.
   *
   * @param key Key.
   * @return CharBuffer value.
   */
  public final CharBuffer getCharBuffer(String key) {
    return this.get(key, CharBuffer.class);
  }

  /**
   * Stores the CharBuffer value at the given key. Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final CharBuffer putCharBuffer(String key, CharBuffer value) {
    return this.put(key, value);
  }

  /**
   * Returns the DoubleBuffer value stored at the given key.
   *
   * @param key Key.
   * @return DoubleBuffer value.
   */
  public final DoubleBuffer getDoubleBuffer(String key) {
    return this.get(key, DoubleBuffer.class);
  }

  /**
   * Stores the DoubleBuffer value at the given key. Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final DoubleBuffer putDoubleBuffer(String key, DoubleBuffer value) {
    return this.put(key, value);
  }

  /**
   * Returns the FloatBuffer value stored at the given key.
   *
   * @param key Key.
   * @return FloatBuffer value.
   */
  public final FloatBuffer getFloatBuffer(String key) {
    return this.get(key, FloatBuffer.class);
  }

  /**
   * Stores the FloatBuffer value at the given key. Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final FloatBuffer putFloatBuffer(String key, FloatBuffer value) {
    return this.put(key, value);
  }

  /**
   * Returns the IntBuffer value stored at the given key.
   *
   * @param key Key.
   * @return IntBuffer value.
   */
  public final IntBuffer getIntBuffer(String key) {
    return this.get(key, IntBuffer.class);
  }

  /**
   * Stores the IntBuffer value at the given key. Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final IntBuffer putIntBuffer(String key, IntBuffer value) {
    return this.put(key, value);
  }

  /**
   * Returns the LongBuffer value stored at the given key.
   *
   * @param key Key.
   * @return LongBuffer value.
   */
  public final LongBuffer getLongBuffer(String key) {
    return this.get(key, LongBuffer.class);
  }

  /**
   * Stores the LongBuffer value at the given key. Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final LongBuffer putLongBuffer(String key, LongBuffer value) {
    return this.put(key, value);
  }

  /**
   * Returns the ShortBuffer value stored at the given key.
   *
   * @param key Key.
   * @return ShortBuffer value.
   */
  public final ShortBuffer getShortBuffer(String key) {
    return this.get(key, ShortBuffer.class);
  }

  /**
   * Stores the ShortBuffer value at the given key. Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final ShortBuffer putShortBuffer(String key, ShortBuffer value) {
    return this.put(key, value);
  }

  //---------- java.time.* Object Methods ----------//


  /**
   * Returns the Duration value stored at the given key.
   *
   * @param key Key.
   * @return Duration value.
   */
  public final Duration getDuration(String key) {
    return this.get(key, Duration.class);
  }

  /**
   * Stores the Duration value at the given key. Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final Duration putDuration(String key, Duration value) {
    return this.put(key, value);
  }

  /**
   * Returns the Instant value stored at the given key.
   *
   * @param key Key.
   * @return Instant value.
   */
  public final Instant getInstant(String key) {
    return this.get(key, Instant.class);
  }

  /**
   * Stores the Instant value at the given key. Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final Instant putInstant(String key, Instant value) {
    return this.put(key, value);
  }

  /**
   * Returns the LocalDate value stored at the given key.
   *
   * @param key Key.
   * @return LocalDate value.
   */
  public final LocalDate getLocalDate(String key) {
    return this.get(key, LocalDate.class);
  }

  /**
   * Stores the LocalDate value at the given key. Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final LocalDate putLocalDate(String key, LocalDate value) {
    return this.put(key, value);
  }

  /**
   * Returns the LocalDateTime value stored at the given key.
   *
   * @param key Key.
   * @return LocalDateTime value.
   */
  public final LocalDateTime getLocalDateTime(String key) {
    return this.get(key, LocalDateTime.class);
  }

  /**
   * Stores the LocalDateTime value at the given key. Returns the old value, or null if none
   * exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final LocalDateTime putLocalDateTime(String key, LocalDateTime value) {
    return this.put(key, value);
  }

  /**
   * Returns the LocalTime value stored at the given key.
   *
   * @param key Key.
   * @return LocalTime value.
   */
  public final LocalTime getLocalTime(String key) {
    return this.get(key, LocalTime.class);
  }

  /**
   * Stores the LocalTime value at the given key. Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final LocalTime putLocalTime(String key, LocalTime value) {
    return this.put(key, value);
  }

  /**
   * Returns the MonthDay value stored at the given key.
   *
   * @param key Key.
   * @return MonthDay value.
   */
  public final MonthDay getMonthDay(String key) {
    return this.get(key, MonthDay.class);
  }

  /**
   * Stores the MonthDay value at the given key. Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final MonthDay putMonthDay(String key, MonthDay value) {
    return this.put(key, value);
  }

  /**
   * Returns the OffsetDateTime value stored at the given key.
   *
   * @param key Key.
   * @return OffsetDateTime value.
   */
  public final OffsetDateTime getOffsetDateTime(String key) {
    return this.get(key, OffsetDateTime.class);
  }

  /**
   * Stores the OffsetDateTime value at the given key. Returns the old value, or null if none
   * exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final OffsetDateTime putOffsetDateTime(String key, OffsetDateTime value) {
    return this.put(key, value);
  }

  /**
   * Returns the OffsetTime value stored at the given key.
   *
   * @param key Key.
   * @return OffsetTime value.
   */
  public final OffsetTime getOffsetTime(String key) {
    return this.get(key, OffsetTime.class);
  }

  /**
   * Stores the OffsetTime value at the given key. Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final OffsetTime putOffsetTime(String key, OffsetTime value) {
    return this.put(key, value);
  }

  /**
   * Returns the Period value stored at the given key.
   *
   * @param key Key.
   * @return Period value.
   */
  public final Period getPeriod(String key) {
    return this.get(key, Period.class);
  }

  /**
   * Stores the Period value at the given key. Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final Period putPeriod(String key, Period value) {
    return this.put(key, value);
  }

  /**
   * Returns the Year value stored at the given key.
   *
   * @param key Key.
   * @return Year value.
   */
  public final Year getYear(String key) {
    return this.get(key, Year.class);
  }

  /**
   * Stores the Year value at the given key. Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final Year putYear(String key, Year value) {
    return this.put(key, value);
  }

  /**
   * Returns the YearMonth value stored at the given key.
   *
   * @param key Key.
   * @return YearMonth value.
   */
  public final YearMonth getYearMonth(String key) {
    return this.get(key, YearMonth.class);
  }

  /**
   * Stores the YearMonth value at the given key. Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final YearMonth putYearMonth(String key, YearMonth value) {
    return this.put(key, value);
  }

  /**
   * Returns the ZonedDateTime value stored at the given key.
   *
   * @param key Key.
   * @return ZonedDateTime value.
   */
  public final ZonedDateTime getZonedDateTime(String key) {
    return this.get(key, ZonedDateTime.class);
  }

  /**
   * Stores the ZonedDateTime value at the given key. Returns the old value, or null if none
   * exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final ZonedDateTime putZonedDateTime(String key, ZonedDateTime value) {
    return this.put(key, value);
  }

  /**
   * Returns the ZoneId value stored at the given key.
   *
   * @param key Key.
   * @return ZoneId value.
   */
  public final ZoneId getZoneId(String key) {
    return this.get(key, ZoneId.class);
  }

  /**
   * Stores the ZoneId value at the given key. Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final ZoneId putZoneId(String key, ZoneId value) {
    return this.put(key, value);
  }

  /**
   * Returns the ZoneOffset value stored at the given key.
   *
   * @param key Key.
   * @return ZoneOffset value.
   */
  public final ZoneOffset getZoneOffset(String key) {
    return this.get(key, ZoneOffset.class);
  }

  /**
   * Stores the ZoneOffset value at the given key. Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final ZoneOffset putZoneOffset(String key, ZoneOffset value) {
    return this.put(key, value);
  }

  //---------- java.util.* Object Methods ----------//


  /**
   * Returns the HashMap value stored at the given key.
   *
   * @param key Key.
   * @return HashMap value.
   */
  public final HashMap getHashMap(String key) {
    return this.get(key, HashMap.class);
  }

  /**
   * Stores the HashMap value at the given key. Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final HashMap putHashMap(String key, HashMap value) {
    return this.put(key, value);
  }

  /**
   * Returns the HashSet value stored at the given key.
   *
   * @param key Key.
   * @return HashSet value.
   */
  public final HashSet getHashSet(String key) {
    return this.get(key, HashSet.class);
  }

  /**
   * Stores the HashSet value at the given key. Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final HashSet putHashSet(String key, HashSet value) {
    return this.put(key, value);
  }

  /**
   * Returns the LinkedHashMap value stored at the given key.
   *
   * @param key Key.
   * @return LinkedHashMap value.
   */
  public final LinkedHashMap getLinkedHashMap(String key) {
    return this.get(key, LinkedHashMap.class);
  }

  /**
   * Stores the LinkedHashMap value at the given key. Returns the old value, or null if none
   * exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final LinkedHashMap putLinkedHashMap(String key, LinkedHashMap value) {
    return this.put(key, value);
  }

  /**
   * Returns the LinkedHashSet value stored at the given key.
   *
   * @param key Key.
   * @return LinkedHashSet value.
   */
  public final LinkedHashSet getLinkedHashSet(String key) {
    return this.get(key, LinkedHashSet.class);
  }

  /**
   * Stores the LinkedHashSet value at the given key. Returns the old value, or null if none
   * exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final LinkedHashSet putLinkedHashSet(String key, LinkedHashSet value) {
    return this.put(key, value);
  }

  /**
   * Returns the TreeMap value stored at the given key.
   *
   * @param key Key.
   * @return TreeMap value.
   */
  public final TreeMap getTreeMap(String key) {
    return this.get(key, TreeMap.class);
  }

  /**
   * Stores the TreeMap value at the given key. Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final TreeMap putTreeMap(String key, TreeMap value) {
    return this.put(key, value);
  }

  /**
   * Returns the TreeSet value stored at the given key.
   *
   * @param key Key.
   * @return TreeSet value.
   */
  public final TreeSet getTreeSet(String key) {
    return this.get(key, TreeSet.class);
  }

  /**
   * Stores the TreeSet value at the given key. Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final TreeSet putTreeSet(String key, TreeSet value) {
    return this.put(key, value);
  }

  /**
   * Returns the UUID value stored at the given key.
   *
   * @param key Key.
   * @return UUID value.
   */
  public final UUID getUUID(String key) {
    return this.get(key, UUID.class);
  }

  /**
   * Stores the UUID value at the given key. Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final UUID putUUID(String key, UUID value) {
    return this.put(key, value);
  }

  //---------- java.util.concurrent.* Object Methods ----------//


  /**
   * Returns the ConcurrentHashMap value stored at the given key.
   *
   * @param key Key.
   * @return ConcurrentHashMap value.
   */
  public final ConcurrentHashMap getConcurrentHashMap(String key) {
    return this.get(key, ConcurrentHashMap.class);
  }

  /**
   * Stores the ConcurrentHashMap value at the given key. Returns the old value, or null if none
   * exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final ConcurrentHashMap putConcurrentHashMap(String key, ConcurrentHashMap value) {
    return this.put(key, value);
  }

  /**
   * Returns the ConcurrentLinkedDeque value stored at the given key.
   *
   * @param key Key.
   * @return ConcurrentLinkedDeque value.
   */
  public final ConcurrentLinkedDeque getConcurrentLinkedDeque(String key) {
    return this.get(key, ConcurrentLinkedDeque.class);
  }

  /**
   * Stores the ConcurrentLinkedDeque value at the given key. Returns the old value, or null if none
   * exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final ConcurrentLinkedDeque putConcurrentLinkedDeque(
      String key,
      ConcurrentLinkedDeque value) {
    return this.put(key, value);
  }

  /**
   * Returns the ConcurrentLinkedQueue value stored at the given key.
   *
   * @param key Key.
   * @return ConcurrentLinkedQueue value.
   */
  public final ConcurrentLinkedQueue getConcurrentLinkedQueue(String key) {
    return this.get(key, ConcurrentLinkedQueue.class);
  }

  /**
   * Stores the ConcurrentLinkedQueue value at the given key. Returns the old value, or null if none
   * exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final ConcurrentLinkedQueue putConcurrentLinkedQueue(
      String key,
      ConcurrentLinkedQueue value) {
    return this.put(key, value);
  }

  //---------- java.util.concurrent.atomic.* Object Methods ----------//


  /**
   * Returns the AtomicBoolean value stored at the given key.
   *
   * @param key Key.
   * @return AtomicBoolean value.
   */
  public final AtomicBoolean getAtomicBoolean(String key) {
    return this.get(key, AtomicBoolean.class);
  }

  /**
   * Stores the AtomicBoolean value at the given key. Returns the old value, or null if none
   * exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final AtomicBoolean putAtomicBoolean(String key, AtomicBoolean value) {
    return this.put(key, value);
  }

  /**
   * Returns the AtomicInteger value stored at the given key.
   *
   * @param key Key.
   * @return AtomicInteger value.
   */
  public final AtomicInteger getAtomicInteger(String key) {
    return this.get(key, AtomicInteger.class);
  }

  /**
   * Stores the AtomicInteger value at the given key. Returns the old value, or null if none
   * exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final AtomicInteger putAtomicInteger(String key, AtomicInteger value) {
    return this.put(key, value);
  }

  /**
   * Returns the AtomicLong value stored at the given key.
   *
   * @param key Key.
   * @return AtomicLong value.
   */
  public final AtomicLong getAtomicLong(String key) {
    return this.get(key, AtomicLong.class);
  }

  /**
   * Stores the AtomicLong value at the given key. Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final AtomicLong putAtomicLong(String key, AtomicLong value) {
    return this.put(key, value);
  }

//  /**
//   * Returns the  value stored at the given key.
//   *
//   * @param key Key.
//   * @return  value.
//   */
//  public final  get(String key) {
//    return this.get(key, .class);
//  }
//
//  /**
//   * Stores the  value at the given key. Returns the old value, or null if none exists.
//   *
//   * @param key Key.
//   * @return Old value, or null if none exists.
//   */
//  public final  put(String key,  value) {
//    return this.put(key, value, .class);
//  }
}
