package techmoc.extensibility.polymorphicmap;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class PolymorphicMapTests {

  private PolymorphicMap polyMap;
  private HashMap<String, String> phoneNumbers;
  private HashMap<String, String> badPhoneNumbers;

  private interface Car {

    int getTotalDoors();
  }

  private class HondaAccord implements Car {

    @Override
    public int getTotalDoors() {
      return 4;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.getClass().getCanonicalName());
    }
  }

  private class FordMustang implements Car {

    @Override
    public int getTotalDoors() {
      return 2;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.getClass().getCanonicalName());
    }
  }

  private class TeslaTruck implements Car {

    @Override
    public int getTotalDoors() {
      return 3;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.getClass().getCanonicalName());
    }
  }

  @BeforeEach
  public void setUp() {

    // Create a list of phone number map entries.
    phoneNumbers = new HashMap<>();
    phoneNumbers.put("Main", "555-111-1111");
    phoneNumbers.put("Mobile", "555-222-2222");
    phoneNumbers.put("Home", "555-333-3333");

    // Create a list of bad phone numbers.
    badPhoneNumbers = new HashMap<>();
    badPhoneNumbers.put("Main", "555-666-6666");
    badPhoneNumbers.put("Mobile", "555-777-7777");
    badPhoneNumbers.put("Home", "555-888-8888");

    Car car = new HondaAccord();

    // Initialize the poly map.
    polyMap = new PolymorphicMap();
    polyMap.put("FirstName", "John");
    polyMap.put("LastName", "Smith");
    polyMap.put("Age", 29);
    polyMap.put("Weight", 180.5);
    polyMap.put("Exercises Regularly?", false);
    polyMap.put("BirthDay", Instant.parse("1990-01-01T08:00:00Z"));
    polyMap.put("Interests", new String[]{"Comics", "Video games", "Movies"});
    polyMap.put("PhoneNumbers", phoneNumbers);
    polyMap.put("Gender", 'M');
    polyMap.put("Car_FordMustang", new FordMustang());
    polyMap.put("Car_HondaAccord", new HondaAccord());
    polyMap.put("Car", car);
  }

  @Test
  void testGetMethod() {

    // Basic retrieval.
    Integer age = polyMap.get("Age", Integer.class);
    assertEquals(29, age);
    Character c = polyMap.get("Gender", Character.class);
    assertEquals('M', c);

    // Boxing.
    char ch = polyMap.get("Gender", Character.class);
    assertEquals('M', ch);

    // Retrieve array.
    String[] interests = polyMap.get("Interests", String[].class);
    assertArrayEquals(new String[]{"Comics", "Video games", "Movies"}, interests);

    // Retrieve generic type.
    // NOTE: Parameterized types (such as HashMap<T>) produce unchecked cast warnings at compile time.
    @SuppressWarnings("unchecked")
    HashMap<String, String> phones = polyMap.get("PhoneNumbers", HashMap.class);
    assertEquals(phoneNumbers, phones);

    // Retrieve non-existent key.
    age = polyMap.get("FakeKey", Integer.class);
    assertNull(age);

    // Retrieve child as parent.
    Car car = polyMap.get("Car_FordMustang", FordMustang.class);
    assertEquals(new FordMustang(), car);

    // Retrieve child as child.
    FordMustang fordMustang = polyMap.get("Car_FordMustang", FordMustang.class);
    assertEquals(new FordMustang(), fordMustang);

    // Retrieve parent as parent (behaves differently from getExact() method).
    car = polyMap.get("Car", Car.class);
    assertEquals(new HondaAccord(), car);

    // Retrieve parent as child.
    HondaAccord ha = polyMap.get("Car", HondaAccord.class);
    assertEquals(new HondaAccord(), ha);

    // Retrieve parent as incorrect child.
    assertThrows(IllegalArgumentException.class, () -> polyMap.get("Car", FordMustang.class));
  }

  @Test
  void testGetExactMethod() {

    // Basic retrieval.
    Integer age = polyMap.getExact("Age", Integer.class);
    assertEquals(29, age);
    Character c = polyMap.getExact("Gender", Character.class);
    assertEquals('M', c);

    // Boxing.
    char ch = polyMap.getExact("Gender", Character.class);
    assertEquals('M', ch);

    // Retrieve array.
    String[] interests = polyMap.getExact("Interests", String[].class);
    assertArrayEquals(new String[]{"Comics", "Video games", "Movies"}, interests);

    // Retrieve generic type.
    // NOTE: Parameterized types (such as HashMap<T>) produce unchecked cast warnings at compile time.
    @SuppressWarnings("unchecked")
    HashMap<String, String> phones = polyMap.getExact("PhoneNumbers", HashMap.class);
    assertEquals(phoneNumbers, phones);

    // Retrieve non-existent key.
    age = polyMap.getExact("FakeKey", Integer.class);
    assertNull(age);

    // Retrieve child as parent.
    Car car = polyMap.getExact("Car_FordMustang", FordMustang.class);
    assertEquals(new FordMustang(), car);

    // Retrieve child as child.
    FordMustang fordMustang = polyMap.getExact("Car_FordMustang", FordMustang.class);
    assertEquals(new FordMustang(), fordMustang);

    // Retrieve parent as parent (behaves differently from get() method).
    assertThrows(IllegalArgumentException.class, () -> polyMap.getExact("Car", Car.class));

    // Retrieve parent as child.
    HondaAccord ha = polyMap.getExact("Car", HondaAccord.class);
    assertEquals(new HondaAccord(), ha);

    // Retrieve parent as incorrect child.
    assertThrows(IllegalArgumentException.class, () -> polyMap.getExact("Car", FordMustang.class));
  }

  @Test
  void testGetOrDefaultMethod() {

    // Basic retrieval.
    Integer age = polyMap.getOrDefault("Age", 5);
    assertEquals(29, age);
    Character c = polyMap.getOrDefault("Gender", 'Z');
    assertEquals('M', c);
    age = polyMap.getOrDefault("FakeAge", 5);
    assertEquals(5, age);
    c = polyMap.getOrDefault("FakeGender", 'Z');
    assertEquals('Z', c);

    // Boxing.
    char ch = polyMap.getOrDefault("Gender", Character.valueOf('Z'));
    assertEquals('M', ch);

    // Retrieve array.
    String[] interests = polyMap.getOrDefault("Interests", new String[]{"Blah", "Blah"});
    assertArrayEquals(new String[]{"Comics", "Video games", "Movies"}, interests);
    interests = polyMap.getOrDefault("FakeInterests", new String[]{"Blah", "Blah"});
    assertArrayEquals(new String[]{"Blah", "Blah"}, interests);

    // Retrieve generic type.
    HashMap<String, String> phonesDefault = new HashMap<>();
    HashMap<String, String> phones = polyMap.getOrDefault("PhoneNumbers", phonesDefault);
    assertEquals(phoneNumbers, phones);
    phones = polyMap.getOrDefault("FakePhoneNumbers", phonesDefault);
    assertEquals(phonesDefault, phones);

    // Retrieve non-existent key.
    age = polyMap.getOrDefault("FakeKey", 5);
    assertEquals(5, age);

    // Retrieve non-existent key with NULL default value.
    age = polyMap.getOrDefault("FakeKey", null);
    assertNull(age);

    // Retrieve child as parent.
    Car car = polyMap.getOrDefault("Car_FordMustang", new FordMustang());
    assertEquals(new FordMustang(), car);
    assertThrows(IllegalArgumentException.class,
        () -> polyMap.getOrDefault("Car_FordMustang", new TeslaTruck()));

    // Retrieve child as child.
    FordMustang fordMustang = polyMap.getOrDefault("Car_FordMustang", new FordMustang());
    assertEquals(new FordMustang(), fordMustang);

    // Retrieve parent as incorrect child.
    assertThrows(IllegalArgumentException.class,
        () -> polyMap.getOrDefault("Car", new TeslaTruck()));
  }

  @Test
  void testGetExactOrDefaultMethod() {

    // Basic retrieval.
    Integer age = polyMap.getExactOrDefault("Age", 5);
    assertEquals(29, age);
    Character c = polyMap.getExactOrDefault("Gender", 'Z');
    assertEquals('M', c);
    age = polyMap.getExactOrDefault("FakeAge", 5);
    assertEquals(5, age);
    c = polyMap.getExactOrDefault("FakeGender", 'Z');
    assertEquals('Z', c);

    // Boxing.
    char ch = polyMap.getExactOrDefault("Gender", Character.valueOf('Z'));
    assertEquals('M', ch);

    // Retrieve array.
    String[] interests = polyMap.getExactOrDefault("Interests", new String[]{"Blah", "Blah"});
    assertArrayEquals(new String[]{"Comics", "Video games", "Movies"}, interests);
    interests = polyMap.getExactOrDefault("FakeInterests", new String[]{"Blah", "Blah"});
    assertArrayEquals(new String[]{"Blah", "Blah"}, interests);

    // Retrieve generic type.
    HashMap<String, String> phonesDefault = new HashMap<>();
    HashMap<String, String> phones = polyMap.getExactOrDefault("PhoneNumbers", phonesDefault);
    assertEquals(phoneNumbers, phones);
    phones = polyMap.getExactOrDefault("FakePhoneNumbers", phonesDefault);
    assertEquals(phonesDefault, phones);

    // Retrieve non-existent key.
    age = polyMap.getExactOrDefault("FakeKey", 5);
    assertEquals(5, age);

    // Retrieve non-existent key with NULL default value.
    age = polyMap.getOrDefault("FakeKey", null);
    assertNull(age);

    // Retrieve child as parent.
    Car car = polyMap.getExactOrDefault("Car_FordMustang", new FordMustang());
    assertEquals(new FordMustang(), car);
    assertThrows(IllegalArgumentException.class,
        () -> polyMap.getExactOrDefault("Car_FordMustang", new TeslaTruck()));

    // Retrieve child as child.
    FordMustang fordMustang = polyMap.getExactOrDefault("Car_FordMustang", new FordMustang());
    assertEquals(new FordMustang(), fordMustang);

    // Retrieve parent as incorrect child.
    assertThrows(IllegalArgumentException.class,
        () -> polyMap.getExactOrDefault("Car", new TeslaTruck()));
  }

  @Test
  void testGetTypeMethod() {
    // Check for expected return types.
    assertEquals(String.class, polyMap.getType("FirstName"));
    assertEquals(Integer.class, polyMap.getType("Age"));
    assertEquals(Double.class, polyMap.getType("Weight"));
    assertEquals(Boolean.class, polyMap.getType("Exercises Regularly?"));
    assertEquals(Character.class, polyMap.getType("Gender"));
    assertEquals(String[].class, polyMap.getType("Interests"));
    assertEquals(HashMap.class, polyMap.getType("PhoneNumbers"));
    assertEquals(FordMustang.class, polyMap.getType("Car_FordMustang"));
    assertEquals(HondaAccord.class, polyMap.getType("Car_HondaAccord"));
    assertEquals(HondaAccord.class, polyMap.getType("Car"));

    // Check non-existent key.
    assertNull(polyMap.getType("FakeKey"));

    // Check exception cases.
    assertThrows(NullPointerException.class, () -> polyMap.getType(null));
    assertThrows(IllegalArgumentException.class, () -> polyMap.getType(""));
  }

  @Test
  void testExtractValuesOfTypeMethod() {
    // Extract a map of String values.
    assertEquals(12, polyMap.size());
    Map<String, String> strMap = polyMap.extractValuesOfType(String.class);
    assertEquals(2, strMap.size());
    assertEquals(10, polyMap.size());
    assertTrue(strMap.containsKey("FirstName"));
    assertTrue(strMap.containsKey("LastName"));
    assertFalse(polyMap.containsKey("FirstName"));
    assertFalse(polyMap.containsKey("LastName"));
    strMap = null;

    // Extract Cars (behavior differes from extractExactValuesOfType() method).
    Map<String, Car> carMap = polyMap.extractValuesOfType(Car.class);
    assertEquals(3, carMap.size());
    assertEquals(7, polyMap.size());
    assertTrue(carMap.containsKey("Car_FordMustang"));
    assertTrue(carMap.containsKey("Car_HondaAccord"));
    assertTrue(carMap.containsKey("Car"));
    assertFalse(polyMap.containsKey("Car_FordMustang"));
    assertFalse(polyMap.containsKey("Car_HondaAccord"));
    assertFalse(polyMap.containsKey("Car"));
    assertEquals(new FordMustang(), carMap.get("Car_FordMustang"));
    assertEquals(new HondaAccord(), carMap.get("Car_HondaAccord"));
    assertEquals(new HondaAccord(), carMap.get("Car"));
  }

  @Test
  void testExtractExactValuesOfTypeMethod() {
    // Extract a map of String values.
    assertEquals(12, polyMap.size());
    Map<String, String> strMap = polyMap.extractExactValuesOfType(String.class);
    assertEquals(2, strMap.size());
    assertEquals(10, polyMap.size());
    assertTrue(strMap.containsKey("FirstName"));
    assertTrue(strMap.containsKey("LastName"));
    assertFalse(polyMap.containsKey("FirstName"));
    assertFalse(polyMap.containsKey("LastName"));
    strMap = null;

    // Extract Cars (behavior differes from extractValuesOfType() method).
    Map<String, Car> carMap = polyMap.extractExactValuesOfType(Car.class);
    assertEquals(0, carMap.size());
    assertEquals(10, polyMap.size());
    carMap = null;

    // Extract HondaAccords.
    Map<String, HondaAccord> haMap = polyMap.extractExactValuesOfType(HondaAccord.class);
    assertEquals(2, haMap.size());
    assertEquals(8, polyMap.size());
    assertTrue(haMap.containsKey("Car_HondaAccord"));
    assertTrue(haMap.containsKey("Car"));
    assertFalse(polyMap.containsKey("Car_HondaAccord"));
    assertFalse(polyMap.containsKey("Car"));
    assertEquals(new HondaAccord(), haMap.get("Car_HondaAccord"));
    assertEquals(new HondaAccord(), haMap.get("Car"));
  }

  @Test
  void testPutMethod_TwoParameters() {
    PolymorphicMap map = new PolymorphicMap();
    assertEquals(0, map.size());

    // Store a String value.
    assertNull(map.put("key", "value"));
    assertEquals(1, map.size());
    assertEquals("value", map.get("key", String.class));

    // Store same value again, and check that old value is returned.
    assertEquals("value", map.put("key", "value"));
    assertEquals(1, map.size());
    assertEquals("value", map.get("key", String.class));

    // Store new value at existing key, and check that old value is returned.
    assertEquals("value", map.put("key", "some new value"));
    assertEquals(1, map.size());
    assertEquals("some new value", map.get("key", String.class));

    // Store invalid keys.
    assertThrows(NullPointerException.class, () -> map.put(null, 55));
    assertEquals(1, map.size());
    assertThrows(IllegalArgumentException.class, () -> map.put("", true));
    assertEquals(1, map.size());

    // Store invalid values.
    assertThrows(NullPointerException.class, () -> map.put("key", null));
    assertEquals(1, map.size());
    assertEquals("some new value", map.get("key", String.class));

    // Change the type of the value at an existing key (strangely this works!!!).
    assertEquals("some new value", map.put("key", 55));
    // But this will not compile:
    // String value = map.put("key", 55);
  }

  @Test
  void testPutMethod_ThreeParameters() {
    PolymorphicMap map = new PolymorphicMap();
    assertEquals(0, map.size());

    // Store a String value.
    assertNull(map.put("key", "value", String.class));
    assertEquals(1, map.size());
    assertEquals("value", map.get("key", String.class));

    // Store same value again, and check that old value is returned.
    assertEquals("value", map.put("key", "value", String.class));
    assertEquals(1, map.size());
    assertEquals("value", map.get("key", String.class));

    // Store new value at existing key, and check that old value is returned.
    assertEquals("value", map.put("key", "some new value", String.class));
    assertEquals(1, map.size());
    assertEquals("some new value", map.get("key", String.class));

    // Store invalid keys.
    assertThrows(NullPointerException.class, () -> map.put(null, 55, String.class));
    assertEquals(1, map.size());
    assertThrows(IllegalArgumentException.class, () -> map.put("", true, String.class));
    assertEquals(1, map.size());

    // Store invalid values.
    assertThrows(NullPointerException.class, () -> map.put("key", null, String.class));
    assertEquals(1, map.size());
    assertEquals("some new value", map.get("key", String.class));
    assertThrows(NullPointerException.class, () -> map.put("key", "latest value", null));
    assertEquals(1, map.size());
    assertEquals("some new value", map.get("key", String.class));

    // Change the type of the value at an existing key.
    String oldValue = map.put("key", 55, String.class);
    assertEquals("some new value", oldValue);
    assertEquals(55, map.get("key", Integer.class));
    assertEquals(1, map.size());
  }

  @Test
  void testPutAllMethod_MapSignature() {
    Map<String, Integer> intMap = new HashMap<>();
    intMap.put("A", 1);
    intMap.put("B", 2);
    intMap.put("C", 3);
    intMap.put("D", 4);
    intMap.put("E", 5);

    // Add map of Integers to fresh poly map.
    PolymorphicMap map = new PolymorphicMap();
    assertEquals(0, map.size());
    map.putAll(intMap);
    assertEquals(5, map.size());
    assertTrue(map.containsKey("A"));
    assertTrue(map.containsKey("B"));
    assertTrue(map.containsKey("C"));
    assertTrue(map.containsKey("D"));
    assertTrue(map.containsKey("E"));
    assertEquals(1, map.get("A", Integer.class));
    assertEquals(2, map.get("B", Integer.class));
    assertEquals(3, map.get("C", Integer.class));
    assertEquals(4, map.get("D", Integer.class));
    assertEquals(5, map.get("E", Integer.class));

    // Add map of Integers to a populated poly map.
    map = new PolymorphicMap();
    assertEquals(0, map.size());
    map.put("string", "string");
    map.put("char", 'A');
    map.put("int", 55);
    assertEquals(3, map.size());
    map.putAll(intMap);
    assertEquals(8, map.size());
    assertTrue(map.containsKey("A"));
    assertTrue(map.containsKey("B"));
    assertTrue(map.containsKey("C"));
    assertTrue(map.containsKey("D"));
    assertTrue(map.containsKey("E"));
    assertTrue(map.containsKey("char"));
    assertTrue(map.containsKey("int"));
    assertTrue(map.containsKey("string"));
    assertEquals(1, map.get("A", Integer.class));
    assertEquals(2, map.get("B", Integer.class));
    assertEquals(3, map.get("C", Integer.class));
    assertEquals(4, map.get("D", Integer.class));
    assertEquals(5, map.get("E", Integer.class));
    assertEquals('A', map.get("char", Character.class));
    assertEquals(55, map.get("int", Integer.class));
    assertEquals("string", map.get("string", String.class));

    // Add map of Integers to a populated poly map containing keys of the same name.
    map = new PolymorphicMap();
    assertEquals(0, map.size());
    map.put("A", "string");
    map.put("C", false);
    map.put("D", 400.123);
    map.put("int", 55);
    assertEquals(4, map.size());
    map.putAll(intMap);
    assertEquals(6, map.size());
    assertTrue(map.containsKey("A"));
    assertTrue(map.containsKey("B"));
    assertTrue(map.containsKey("C"));
    assertTrue(map.containsKey("D"));
    assertTrue(map.containsKey("E"));
    assertTrue(map.containsKey("int"));
    assertEquals(1, map.get("A", Integer.class));
    assertEquals(2, map.get("B", Integer.class));
    assertEquals(3, map.get("C", Integer.class));
    assertEquals(4, map.get("D", Integer.class));
    assertEquals(5, map.get("E", Integer.class));
    assertEquals(55, map.get("int", Integer.class));
  }

  @Test
  void testPutAllMethod_PolymorphicMapSignature() {
    PolymorphicMap polyMapAdditions = new PolymorphicMap();
    polyMapAdditions.put("A", 1);
    polyMapAdditions.put("B", "blah");
    polyMapAdditions.put("C", 'c');
    polyMapAdditions.put("D", 400.123);
    polyMapAdditions.put("E", false);

    // Add map of Integers to fresh poly map.
    PolymorphicMap map = new PolymorphicMap();
    assertEquals(0, map.size());
    map.putAll(polyMapAdditions);
    assertEquals(5, map.size());
    assertTrue(map.containsKey("A"));
    assertTrue(map.containsKey("B"));
    assertTrue(map.containsKey("C"));
    assertTrue(map.containsKey("D"));
    assertTrue(map.containsKey("E"));
    assertEquals(1, map.get("A", Integer.class));
    assertEquals("blah", map.get("B", String.class));
    assertEquals('c', map.get("C", Character.class));
    assertEquals(400.123, map.get("D", Double.class));
    assertEquals(false, map.get("E", Boolean.class));

    // Add map of Integers to a populated poly map.
    map = new PolymorphicMap();
    assertEquals(0, map.size());
    map.put("string", "string");
    map.put("char", 'A');
    map.put("int", 55);
    assertEquals(3, map.size());
    map.putAll(polyMapAdditions);
    assertEquals(8, map.size());
    assertTrue(map.containsKey("A"));
    assertTrue(map.containsKey("B"));
    assertTrue(map.containsKey("C"));
    assertTrue(map.containsKey("D"));
    assertTrue(map.containsKey("E"));
    assertTrue(map.containsKey("char"));
    assertTrue(map.containsKey("int"));
    assertTrue(map.containsKey("string"));
    assertEquals(1, map.get("A", Integer.class));
    assertEquals("blah", map.get("B", String.class));
    assertEquals('c', map.get("C", Character.class));
    assertEquals(400.123, map.get("D", Double.class));
    assertEquals(false, map.get("E", Boolean.class));
    assertEquals('A', map.get("char", Character.class));
    assertEquals(55, map.get("int", Integer.class));
    assertEquals("string", map.get("string", String.class));

    // Add map of Integers to a populated poly map containing keys of the same name.
    map = new PolymorphicMap();
    assertEquals(0, map.size());
    map.put("A", "string");
    map.put("C", false);
    map.put("D", 400.123);
    map.put("int", 55);
    assertEquals(4, map.size());
    map.putAll(polyMapAdditions);
    assertEquals(6, map.size());
    assertTrue(map.containsKey("A"));
    assertTrue(map.containsKey("B"));
    assertTrue(map.containsKey("C"));
    assertTrue(map.containsKey("D"));
    assertTrue(map.containsKey("E"));
    assertTrue(map.containsKey("int"));
    assertEquals(1, map.get("A", Integer.class));
    assertEquals("blah", map.get("B", String.class));
    assertEquals('c', map.get("C", Character.class));
    assertEquals(400.123, map.get("D", Double.class));
    assertEquals(false, map.get("E", Boolean.class));
    assertEquals(55, map.get("int", Integer.class));
  }

  @Test
  void testParameterizedRemoveMethod() {

    PolymorphicMap map = new PolymorphicMap();
    map.put("Key1", "Value");
    map.put("Key2", 29);
    map.put("Key3", new FordMustang());
    map.put("Key4", new HondaAccord());
    map.put("Key5", new HondaAccord());
    assertEquals(5, map.size());

    // Returns null, since key does not exist.
    Car car = map.remove("Key7", Car.class);
    assertEquals(5, map.size());
    assertNull(car);

    // Removes FordMustang value.
    car = map.remove("Key3", Car.class);
    assertEquals(4, map.size());
    assertEquals(new FordMustang(), car);

    // Removes HondaAccord value.
    car = map.remove("Key4", HondaAccord.class);
    assertEquals(3, map.size());
    assertEquals(new HondaAccord(), car);

    // Removes HondaAccord value.
    HondaAccord ha = map.remove("Key5", HondaAccord.class);
    assertEquals(2, map.size());
    assertEquals(new HondaAccord(), ha);

    // Throw exception, and does NOT change the map!
    assertThrows(IllegalArgumentException.class, () -> map.remove("Key1", Integer.class));
    assertEquals(2, map.size());

    // Removes String value.
    String strValue = map.remove("Key1", String.class);
    assertEquals(1, map.size());
    assertEquals("Value", strValue);
  }

  @Test
  void testParameterizedRemoveExactMethod() {

    //----- Test the return value of the remove(String key, Class<?> type) method. -----//
    PolymorphicMap map = new PolymorphicMap();
    map.put("Key1", "Value");
    map.put("Key2", 29);
    map.put("Key3", new FordMustang());
    map.put("Key4", new HondaAccord());
    map.put("Key5", new HondaAccord());
    assertEquals(5, map.size());

    // Returns null, since key does not exist.
    Car car = map.removeExact("Key7", Car.class);
    assertEquals(5, map.size());
    assertNull(car);

    // Fails to remove FordMustang value, since it is not exactly of type Car.
    assertThrows(IllegalArgumentException.class, () -> map.removeExact("Key3", Car.class));
    assertEquals(5, map.size());

    // Fails to remove FordMustang value, since it is not exactly of type HondaAccord.
    assertThrows(IllegalArgumentException.class, () -> map.removeExact("Key3", HondaAccord.class));
    assertEquals(5, map.size());

    // Remove FordMustang value.
    car = map.removeExact("Key3", FordMustang.class);
    assertEquals(4, map.size());
    assertEquals(new FordMustang(), car);

    // Removes HondaAccord value.
    car = map.removeExact("Key4", HondaAccord.class);
    assertEquals(3, map.size());
    assertEquals(new HondaAccord(), car);

    // Removes HondaAccord value.
    HondaAccord ha = map.removeExact("Key5", HondaAccord.class);
    assertEquals(2, map.size());
    assertEquals(new HondaAccord(), ha);

    // Throw exception, and does NOT change the map!
    assertThrows(IllegalArgumentException.class, () -> map.removeExact("Key1", Integer.class));
    assertEquals(2, map.size());

    // Removes String value.
    String strValue = map.removeExact("Key1", String.class);
    assertEquals(1, map.size());
    assertEquals("Value", strValue);
  }
}
