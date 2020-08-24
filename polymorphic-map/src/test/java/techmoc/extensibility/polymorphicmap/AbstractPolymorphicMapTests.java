package techmoc.extensibility.polymorphicmap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class AbstractPolymorphicMapTests {

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
  }

  @Test
  void testKeySetMethods() {
    // Check that the map returns the full key set.
    Set<String> keys = polyMap.keySet();
    assertEquals(11, keys.size());
    assertTrue(keys.contains("FirstName"));
    assertTrue(keys.contains("LastName"));
    assertTrue(keys.contains("Age"));
    assertTrue(keys.contains("Weight"));
    assertTrue(keys.contains("Exercises Regularly?"));
    assertTrue(keys.contains("BirthDay"));
    assertTrue(keys.contains("Interests"));
    assertTrue(keys.contains("PhoneNumbers"));
    assertTrue(keys.contains("Gender"));
    assertTrue(keys.contains("Car_FordMustang"));
    assertTrue(keys.contains("Car_HondaAccord"));

    // Check retrieval of keys with values of type String.
    keys = polyMap.keySetOfType(String.class);
    assertEquals(2, keys.size());
    assertTrue(keys.contains("FirstName"));
    assertTrue(keys.contains("LastName"));

    // Check retrieval of keys with values of type Integer.
    keys = polyMap.keySetOfType(Integer.class);
    assertEquals(1, keys.size());
    assertTrue(keys.contains("Age"));

    // Check retrieval of keys with values assignable to type Car.
    keys = polyMap.keySetOfType(Car.class);
    assertEquals(2, keys.size());
    assertTrue(keys.contains("Car_FordMustang"));
    assertTrue(keys.contains("Car_HondaAccord"));

    // Check retrieval of keys with values of exact type Car.
    keys = polyMap.keySetOfExactType(Car.class);
    assertEquals(0, keys.size());

    // Check retrieval of keys with values of exact type FordMustang.
    keys = polyMap.keySetOfExactType(FordMustang.class);
    assertEquals(1, keys.size());
    assertTrue(keys.contains("Car_FordMustang"));

    // Check retrieval of keys with values of exact type HondaAccord.
    keys = polyMap.keySetOfExactType(HondaAccord.class);
    assertEquals(1, keys.size());
    assertTrue(keys.contains("Car_HondaAccord"));
  }

  @Test
  void testValuesMethods() {
    // Check that the map returns the full collection values.
    Collection<TypedObject> values = polyMap.values();
    assertEquals(11, values.size());
    assertTrue(values.contains(new TypedObject("John")));
    assertTrue(values.contains(new TypedObject("Smith")));
    assertTrue(values.contains(new TypedObject(29)));
    assertTrue(values.contains(new TypedObject(180.5)));
    assertTrue(values.contains(new TypedObject(false)));
    assertTrue(values.contains(new TypedObject(Instant.parse("1990-01-01T08:00:00Z"))));
    assertTrue(values.contains(new TypedObject(new String[]{"Comics", "Video games", "Movies"})));
    assertTrue(values.contains(new TypedObject(phoneNumbers)));
    assertTrue(values.contains(new TypedObject('M')));
    assertTrue(values.contains(new TypedObject(new FordMustang())));
    assertTrue(values.contains(new TypedObject(new HondaAccord())));

    // Check retrieval of values of type String.
    Collection<String> valuesOfTypeString = polyMap.valuesOfType(String.class);
    assertEquals(2, valuesOfTypeString.size());
    assertTrue(valuesOfTypeString.contains("John"));
    assertTrue(valuesOfTypeString.contains("Smith"));

    // Check retrieval of keys with values of type Integer.
    Collection<Integer> valuesOfTypeInteger = polyMap.valuesOfType(Integer.class);
    assertEquals(1, valuesOfTypeInteger.size());
    assertTrue(valuesOfTypeInteger.contains(29));

    // Check retrieval of values with values assignable to type Car.
    Collection<Car> valuesOfTypeCar = polyMap.valuesOfType(Car.class);
    assertEquals(2, valuesOfTypeCar.size());
    assertTrue(valuesOfTypeCar.contains(new FordMustang()));
    assertTrue(valuesOfTypeCar.contains(new HondaAccord()));

    // Check retrieval of values with values of exact type Car.
    valuesOfTypeCar = polyMap.valuesOfExactType(Car.class);
    assertEquals(0, valuesOfTypeCar.size());

    // Check retrieval of values with values of exact type FordMustang.
    Collection<FordMustang> valuesOfTypeFordMustang = polyMap.valuesOfExactType(FordMustang.class);
    assertEquals(1, valuesOfTypeFordMustang.size());
    assertTrue(valuesOfTypeFordMustang.contains(new FordMustang()));

    // Check retrieval of values with values of exact type HondaAccord.
    Collection<HondaAccord> valuesOfTypeHondaAccord = polyMap.valuesOfExactType(HondaAccord.class);
    assertEquals(1, valuesOfTypeHondaAccord.size());
    assertTrue(valuesOfTypeHondaAccord.contains(new HondaAccord()));
  }

  @Test
  void testRemovalMethod() {
    // Remove some keys, and check the return values.
    polyMap.remove("FirstName");
    polyMap.remove("Interests");
    polyMap.remove("Car_FordMustang");
    Set<String> keys = polyMap.keySet();
    assertEquals(8, keys.size());
    assertFalse(keys.contains("FirstName"));
    assertTrue(keys.contains("LastName"));
    assertTrue(keys.contains("Age"));
    assertTrue(keys.contains("Weight"));
    assertTrue(keys.contains("Exercises Regularly?"));
    assertTrue(keys.contains("BirthDay"));
    assertFalse(keys.contains("Interests"));
    assertTrue(keys.contains("PhoneNumbers"));
    assertTrue(keys.contains("Gender"));
    assertFalse(keys.contains("Car_FordMustang"));
    assertTrue(keys.contains("Car_HondaAccord"));
    assertFalse(polyMap.isEmpty());

    // Check that map can be cleared.
    polyMap.clear();
    keys = polyMap.keySet();
    assertEquals(0, keys.size());
    assertTrue(polyMap.isEmpty());
  }

  @Test
  void testStatisticsAndStateMethods() {
    // Check containsKey() method.
    assertTrue(polyMap.containsKey("FirstName"));
    assertFalse(polyMap.containsKey("Blah Blah Blah"));

    // Check containsKeyOfType() method.
    assertTrue(polyMap.containsKeyOfType("Age", Integer.class));
    assertFalse(polyMap.containsKeyOfType("Age", String.class));
    assertTrue(polyMap.containsKeyOfType("Car_FordMustang", Car.class));
    assertTrue(polyMap.containsKeyOfType("Car_FordMustang", FordMustang.class));
    assertFalse(polyMap.containsKeyOfType("Car_FordMustang", HondaAccord.class));

    // Check containsKeyOfExactType() method.
    assertTrue(polyMap.containsKeyOfExactType("Age", Integer.class));
    assertFalse(polyMap.containsKeyOfExactType("Age", String.class));
    assertFalse(polyMap.containsKeyOfExactType("Car_FordMustang", Car.class));
    assertTrue(polyMap.containsKeyOfExactType("Car_FordMustang", FordMustang.class));
    assertFalse(polyMap.containsKeyOfExactType("Car_FordMustang", HondaAccord.class));

    // Check the containsValue() method.
    assertTrue(polyMap.containsValue("John"));
    assertTrue(polyMap.containsValue("Smith"));
    assertTrue(polyMap.containsValue(29));
    assertTrue(polyMap.containsValue(180.5));
    assertTrue(polyMap.containsValue(false));
    assertTrue(polyMap.containsValue(Instant.parse("1990-01-01T08:00:00Z")));
    assertTrue(polyMap.containsValue(new String[]{"Comics", "Video games", "Movies"}));
    assertTrue(polyMap.containsValue(phoneNumbers));
    assertTrue(polyMap.containsValue('M'));
    assertTrue(polyMap.containsValue(new FordMustang()));
    assertTrue(polyMap.containsValue(new HondaAccord()));

    assertFalse(polyMap.containsValue("Jim"));
    assertFalse(polyMap.containsValue(true));
    assertFalse(polyMap.containsValue(-1));
    assertFalse(polyMap.containsValue(199.9));
    assertFalse(polyMap.containsValue(Instant.parse("2009-09-09T08:00:00Z")));
    assertFalse(polyMap.containsValue(new String[]{"Books", "Chess", "Math"}));
    assertFalse(polyMap.containsValue(badPhoneNumbers));
    assertFalse(polyMap.containsValue('F'));
    assertFalse(polyMap.containsValue(new TeslaTruck()));

    // Check the isEmpty() and size() methods.
    PolymorphicMap map = new PolymorphicMap();
    assertTrue(map.isEmpty());
    assertEquals(0, map.size());
    map.put("Key", "Value");
    assertFalse(map.isEmpty());
    assertEquals(1, map.size());
    map.put("Key", "New Value");
    assertFalse(map.isEmpty());
    assertEquals(1, map.size());
    map.put("Key2", "Value 2");
    map.put("Key3", 3);
    map.put("Key4", 4.1);
    map.put("Key5", new FordMustang());
    assertFalse(map.isEmpty());
    assertEquals(5, map.size());
    map.remove("Key2");

    // Build the map back up again, and then break it down again.
    assertFalse(map.isEmpty());
    assertEquals(4, map.size());
    map.clear();
    assertTrue(map.isEmpty());
    assertEquals(0, map.size());
    map.put("Key6", "Value 2");
    map.put("Key7", 3);
    map.put("Key8", 4.1);
    map.put("Key9", new FordMustang());
    assertFalse(map.isEmpty());
    assertEquals(4, map.size());
    map.remove("Key6");
    map.remove("Key7");
    map.remove("Key8");
    map.remove("Key9");
    map.remove("Key9");
    map.remove("Key8");
    map.remove("Key7");
    assertTrue(map.isEmpty());
    assertEquals(0, map.size());
  }

  @Test
  void testEqualsHashCodeMethods() {
    PolymorphicMap map1 = new PolymorphicMap();
    PolymorphicMap map2 = new PolymorphicMap();
    assertEquals(map1, map2);
    assertEquals(map1.hashCode(), map2.hashCode());

    map1.put("key", "value");
    assertNotEquals(map1, map2);
    assertNotEquals(map1.hashCode(), map2.hashCode());
    map2.put("key", "value");
    assertEquals(map1, map2);
    assertEquals(map1.hashCode(), map2.hashCode());
    map2.put("key", "value2");
    assertNotEquals(map1, map2);
    assertNotEquals(map1.hashCode(), map2.hashCode());

    map1.clear();
    map2.clear();
    assertEquals(map1, map2);
    assertEquals(map1.hashCode(), map2.hashCode());
  }
}
