package techmoc.extensibility.polymorphicmap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


abstract class AbstractPolymorphicMap {

  /**
   * Map of typed values (thread safe).
   */
  protected final Map<String, TypedObject> map = new ConcurrentHashMap<>();

  //---------- Key Set and Values Methods ----------//


  /**
   * Returns the set of keys.
   *
   * @return Set of all keys.
   */
  public final Set<String> keySet() {
    return map.keySet();
  }

  /**
   * Returns the subset of keys that point to a value that is assignable to the given type.
   *
   * @param type Type.
   * @return Set of keys.
   */
  public final Set<String> keySetOfType(Class<?> type) {
    Set<String> keys = new HashSet<>();
    for (String key : map.keySet()) {
      if (this.containsKeyOfType(key, type)) {
        keys.add(key);
      }
    }
    return keys;
  }

  /**
   * Returns the subset of keys that point to a value whose type exactly matches the given type.
   *
   * @param type Type.
   * @return Set of keys.
   */
  public final Set<String> keySetOfExactType(Class<?> type) {
    Set<String> keys = new HashSet<>();
    for (String key : map.keySet()) {
      if (this.containsKeyOfExactType(key, type)) {
        keys.add(key);
      }
    }
    return keys;
  }

  /**
   * Returns the value of each map entry as a collection of TypedObject.
   *
   * @return Value of each map entry as a collection of TypedObject.
   */
  public final Collection<TypedObject> values() {
    return map.values();
  }

  /**
   * Returns all values that are assignable to the given type.
   *
   * @return All values that are assignable to the given type.
   */
  public final <T> Collection<T> valuesOfType(Class<T> type) {
    Collection<T> values = new ArrayList<>();
    for (String key : this.keySetOfType(type)) {
      @SuppressWarnings("unchecked")
      T value = (T) map.get(key).getValue();
      values.add(value);
    }
    return values;
  }

  /**
   * Returns all values whose type exactly matches the given type.
   *
   * @return All values whose type exactly matches the given type.
   */
  public final <T> Collection<T> valuesOfExactType(Class<T> type) {
    Collection<T> values = new ArrayList<>();
    for (String key : this.keySetOfExactType(type)) {
      @SuppressWarnings("unchecked")
      T value = (T) map.get(key).getValue();
      values.add(value);
    }
    return values;
  }

  //---------- Removal Methods ----------//


  /**
   * Removes the given key, returning true if the key existed and false if it did not.
   *
   * @param key Key.
   */
  public final boolean remove(String key) {
    validateKey(key);
    TypedObject typedObject = map.remove(key);
    return (typedObject != null);
  }

  /**
   * Removes all entries from the map.
   */
  public final void clear() {
    map.clear();
  }

  //---------- Statistics and State Methods ----------//


  /**
   * Returns true if the given key exists, otherwise returns false.
   *
   * @param key Key.
   * @return True if the given key exists, otherwise false.
   */
  public final boolean containsKey(String key) {
    validateKey(key);
    return map.containsKey(key);
  }

  /**
   * Returns true if the given key exists and contains a value that is assignable to the given type.
   * Otherwise returns false.
   *
   * @param key Key.
   * @param type Type.
   * @return True if the given key exists and its value is assignable to the given type, otherwise
   * false.
   */
  public final boolean containsKeyOfType(String key, Class<?> type) {
    Objects.requireNonNull(type);
    validateKey(key);

    return (map.containsKey(key) && type.isAssignableFrom(map.get(key).getType()));
  }

  /**
   * Returns true if the given key exists and contains a value whose type exactly matches the given
   * type. Otherwise returns false.
   *
   * @param key Key.
   * @param type Type.
   * @return True if the given key exists and contains a value whose type exactly matches the given
   * type, otherwise false.
   */
  public final boolean containsKeyOfExactType(String key, Class<?> type) {
    Objects.requireNonNull(type);
    validateKey(key);

    return (map.containsKey(key) && map.get(key).getType().equals(type));
  }

  /**
   * Returns true if the given value exists, otherwise returns false.
   *
   * @param value Value.
   * @param <T> Type of value.
   * @return True if the given value exists, otherwise false.
   */
  public final <T> boolean containsValue(T value) {
    if (value.getClass().isArray()) {
      return map.values().stream()
          .anyMatch(x -> x.getType().equals(value.getClass()) &&
              Arrays.deepEquals((Object[]) x.getValue(), (Object[]) value));
    } else {
      return map.values().stream()
          .anyMatch(x -> x.getType().equals(value.getClass()) &&
              x.getValue().equals(value));
    }
  }

  /**
   * True if no entries exist in the map, false otherwise.
   *
   * @return True if no entries exist in the map, false otherwise.
   */
  public final boolean isEmpty() {
    return (map.keySet().size() <= 0);
  }

  /**
   * Returns the total number of entries that currently exist in the map.
   *
   * @return Total entries in the map.
   */
  public final int size() {
    return map.keySet().size();
  }

  //---------- Equals / HashCode Methods ----------//


  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AbstractPolymorphicMap that = (AbstractPolymorphicMap) o;
    return map.entrySet().equals(that.map.entrySet());
  }

  @Override
  public final int hashCode() {
    return Objects.hash(map.entrySet());
  }

  //---------- Private Methods ----------//


  /**
   * Validates the key, throwing an IllegalArgumentException if the key is invalid.
   *
   * @param key Key.
   */
  protected final void validateKey(String key) {
    Objects.requireNonNull(key);

    key = key.trim();
    if (key.isBlank()) {
      throw new IllegalArgumentException("Keys may not be empty or blank.");
    }
  }
}
