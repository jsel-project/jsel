package techmoc.extensibility.polymorphicmap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public abstract class ClosedPolymorphicMap extends AbstractPolymorphicMap {

  //---------- Retrieval Methods ----------//


  /**
   * Returns the value stored at the given key, cast to the given type.
   *
   * If the value at the given key is not assignable to the class specified in the 'type' parameter,
   * then an IllegalArgumentException will be thrown.
   *
   * @param key Key.
   * @param type Class that the value is to be casted to.
   * @param <T> Type that the value is to be casted to.
   * @return Value stored at the given key, cast to the given type, if the key exists. Returns null
   * if the key does not exist.
   * @throws IllegalArgumentException Thrown when the value stored at the given key is not
   * assignable to the given type.
   */
  protected final <T> T get(String key, Class<T> type) {
    Objects.requireNonNull(type);
    validateKey(key);

    TypedObject typedObject = map.get(key); // Returns NULL if key does not exist.
    if (typedObject == null) {
      // Returning NULL indicates that the key does not exist (since PolymorphicMap does not all NULL to be stored as a value).
      return null;
    } else {

      // Check that the value is assignable to the specified type.
      validateValueIsAssignableToType(typedObject, type);

      // Cast the object to the specified type.
      @SuppressWarnings("unchecked")
      T value = (T) typedObject.getValue();

      return value;
    }
  }

  /**
   * Returns the value stored at the given key, cast to the given type; if-and-only-if the value is
   * of exactly the same type.
   *
   * If the value at the given key is not of exactly the type as the class specified in the 'type'
   * parameter, then an IllegalArgumentException will be thrown.
   *
   * @param key Key.
   * @param type Type that the value is expected to be.
   * @param <T> Exact type that the value is expected to be.
   * @return Value stored at the given key, cast to the given type, if the key exists. Returns null
   * if the key does not exist.
   * @throws IllegalArgumentException Thrown when the value stored at the given key is not of the
   * exact same type as the class specified in the 'type' parameter.
   */
  protected final <T> T getExact(String key, Class<T> type) {
    Objects.requireNonNull(type);
    validateKey(key);

    TypedObject typedObject = map.get(key); // Returns NULL if key does not exist.
    if (typedObject == null) {
      // Returning NULL indicates that the key does not exist (since PolymorphicMap does not all NULL to be stored as a value).
      return null;
    } else {

      // Check that the value is assignable to the specified type.
      validateValueIsOfType(typedObject, type);

      // Cast the object to the specified type.
      @SuppressWarnings("unchecked")
      T value = (T) typedObject.getValue();

      return value;
    }
  }

  /**
   * Returns the value stored at the given key, cast to the type of the defaultValue parameter;
   * if-and-only-if the value is assignable to the type of the defaultValue parameter.
   *
   * If the value at the given key is not assignable to the type of the defaultValue parameter, then
   * an IllegalArgumentException will be thrown.
   *
   * @param key Key.
   * @param defaultValue Default value to return if the key does not exist. If the key does exist,
   * then the value returned will be cast to the same type as this parameter.
   * @param <V> Type that the value is to be casted to.
   * @return If the key exists, then the value stored at the given key is returned after being
   * casted to the type of the defaultValue parameter. If the key does not exist, the specified
   * defaultValue is returned.
   * @throws IllegalArgumentException Thrown when the value stored at the given key is not
   * assignable to the type of the defaultValue parameter.
   */
  protected final <V> V getOrDefault(String key, V defaultValue) {
    validateKey(key);

    TypedObject typedObject = map.get(key); // Returns NULL if key does not exist.
    if (typedObject == null) {
      // Indicates that the key does not exist (since PolymorphicMap does not all NULL to be stored as a value).
      return defaultValue;
    } else {

      // Check that the value is assignable to the specified type.
      validateValueIsAssignableToType(typedObject, defaultValue.getClass());

      // Cast the object to the specified type.
      @SuppressWarnings("unchecked")
      V value = (V) typedObject.getValue();

      return value;
    }
  }

  /**
   * Returns the value stored at the given key, cast to the type of the defaultValue parameter;
   * if-and-only-if the value is of exactly the same type.
   *
   * If the value at the given key is not of exactly the same type as the defaultValue parameter,
   * then an IllegalArgumentException will be thrown.
   *
   * @param key Key.
   * @param defaultValue Default value to return if the key does not exist. If the key does exist,
   * then the value returned will be cast to the same type as this parameter.
   * @param <V> Exact type that the value is expected to be.
   * @return If the key exists, then the value stored at the given key is returned after being
   * casted to the type of the defaultValue parameter. If the key does not exist, the specified
   * defaultValue is returned.
   * @throws IllegalArgumentException Thrown when the value stored at the given key does not exactly
   * match the type of the defaultValue parameter.
   */
  protected final <V> V getExactOrDefault(String key, V defaultValue) {
    validateKey(key);

    TypedObject typedObject = map.get(key); // Returns NULL if key does not exist.
    if (typedObject == null) {
      // Indicates that the key does not exist (since PolymorphicMap does not all NULL to be stored as a value).
      return defaultValue;
    } else {

      // Check that the value is assignable to the specified type.
      validateValueIsOfType(typedObject, defaultValue.getClass());

      // Cast the object to the specified type.
      @SuppressWarnings("unchecked")
      V value = (V) typedObject.getValue();

      return value;
    }
  }

  /**
   * Returns a map containing all entries assignable to the specified type. These entries are
   * removed from the current map.
   *
   * @param type Class that the value is to be casted to.
   * @param <V> Type that the value is to be casted to.
   * @return Map of string to given type.
   */
  protected final <V> Map<String, V> extractValuesOfType(Class<V> type) {
    Map<String, V> exportMap = new HashMap<>();
    for (String key : map.keySet()) {
      TypedObject typedObject = map.get(key);
      if (type.isAssignableFrom(typedObject.getType())) {

        // Move the value to the new map.
        @SuppressWarnings("unchecked")
        V value = (V) typedObject.getValue();
        exportMap.put(key, value);

        // Remove the key from the current map.
        map.remove(key);
      }
    }
    return exportMap;
  }

  /**
   * Returns a map containing all entries of the exact specified type. These entries are removed
   * from the current map.
   *
   * @param type Exact type of the values targeted for extraction.
   * @param <V> Exact type of the values targeted for extraction.
   * @return Map of string to given type.
   */
  protected final <V> Map<String, V> extractExactValuesOfType(Class<V> type) {
    Map<String, V> exportMap = new HashMap<>();
    for (String key : map.keySet()) {
      TypedObject typedObject = map.get(key);
      if (typedObject.getType().equals(type)) {

        // Move the value to the new map.
        @SuppressWarnings("unchecked")
        V value = (V) typedObject.getValue();
        exportMap.put(key, value);

        // Remove the key from the current map.
        map.remove(key);
      }
    }
    return exportMap;
  }

  /**
   * Returns the class type of the object stored at the given key, or null if the key does not
   * exist.
   *
   * @param key Key.
   * @return Returns the class type of the object stored at the given key, or null if the key does
   * not exist.
   */
  protected final Class<?> getType(String key) {
    validateKey(key);

    TypedObject typedObject = map.get(key);
    return (typedObject == null) ? null : typedObject.getType();
  }

  //---------- Storage Methods ----------//


  /**
   * Stores the key and value to the map. If the key already exists, it will be replaced; and the
   * prior value will be returned after being cast to the type of the value parameter.
   * <p>
   * NOTE: This method may only allows existing values to be replaced by new values of the same
   * type. To replace an existing value with a value of a different type, call an alternative
   * signature: put(String key, V value, Class&lt;T&gt; type)
   *
   * @param key Key.
   * @param value Value.
   * @param <V> Type of the value.
   * @return Old value if one exists, otherwise null.
   */
  protected final <V> V put(String key, V value) {
    Objects.requireNonNull(value);
    validateKey(key);

    TypedObject typedObject = map.put(key, new TypedObject(value));
    if (typedObject == null) {
      return null;
    } else {
      @SuppressWarnings("unchecked")
      V oldValue = (V) typedObject.getValue();
      return oldValue;
    }
  }

  /**
   * Stores the key and value to the map. If the key already exists, it will be replaced; and the
   * prior value will be returned after being cast to the type specified by the type parameter.
   *
   * @param key Key.
   * @param value Value.
   * @param type Type that an existing prior value will be cast to before being returned.
   * @param <V> Type of the value.
   * @param <T> Type that an existing prior value will be cast to before being returned.
   * @return Old value cast to the specified type, if one exists; otherwise null.
   */
  protected final <V, T> T put(String key, V value, Class<T> type) {
    Objects.requireNonNull(value);
    Objects.requireNonNull(type);
    validateKey(key);

    TypedObject typedObject = map.put(key, new TypedObject(value));
    if (typedObject == null) {
      return null;
    } else {
      @SuppressWarnings("unchecked")
      T oldValue = (T) typedObject.getValue();
      return oldValue;
    }
  }

  /**
   * Stores all entries from the given map into the current map. If the key already exists, it will
   * be replaced.
   *
   * @param m Input map.
   */
  protected final <V> void putAll(Map<String, V> m) {
    for (String key : m.keySet()) {
      validateKey(key);
      V value = m.get(key);
      this.put(key, value);
    }
  }

  /**
   * Stores all entries from the given map into the current map. If the key already exists, it will
   * be replaced.
   *
   * @param polyMap Map.
   */
  protected final void putAll(PolymorphicMap polyMap) {
    for (String key : polyMap.keySet()) {
      this.put(key, polyMap.get(key, polyMap.getType(key)));
    }
  }

  //---------- Removal Methods ----------//


  /**
   * Removes the given key, and returns the removed value (or null if the key does not exist).
   * <p>
   * If the value at the specified key is not assignable to the specified type, then the operation
   * fails and an IllegalArgumentException is thrown (i.e. the key removal operation does not take
   * effect).
   *
   * @param key Key.
   * @param type Type that the value is to be casted to.
   * @throws IllegalArgumentException Thrown if the value at the specified key is not assignable to
   * the specified type.
   */
  protected final <T> T remove(String key, Class<T> type) {
    validateKey(key);

    TypedObject typedObject = map.get(key); // Returns NULL if key does not exist.
    if (typedObject == null) {
      // Returning NULL indicates that the key does not exist (since PolymorphicMap does not all NULL to be stored as a value).
      return null;
    } else {

      // Check that the value is assignable to the specified type.
      validateValueIsAssignableToType(typedObject, type);

      // Cast the object to the specified type.
      @SuppressWarnings("unchecked")
      T removedValue = (T) typedObject.getValue();

      // Remove the value.
      map.remove(key);

      return removedValue;
    }
  }

  /**
   * Removes the given key, and returns the removed value (or null if the key does not exist).
   * <p>
   * If the value at the specified key does not exactly match the specified type, then the operation
   * fails and an IllegalArgumentException is thrown (i.e. the key removal operation does not take
   * effect).
   *
   * @param key Key.
   * @param type Exact type that the value is expected to be.
   * @throws IllegalArgumentException Thrown if the value at the specified key does not exactly
   * match the specified type.
   */
  protected final <T> T removeExact(String key, Class<T> type) {
    validateKey(key);

    TypedObject typedObject = map.get(key); // Returns NULL if key does not exist.
    if (typedObject == null) {
      // Returning NULL indicates that the key does not exist (since PolymorphicMap does not all NULL to be stored as a value).
      return null;
    } else {

      // Check that the value is assignable to the specified type.
      validateValueIsOfType(typedObject, type);

      // Cast the object to the specified type.
      @SuppressWarnings("unchecked")
      T removedValue = (T) typedObject.getValue();

      // Remove the value.
      map.remove(key);

      return removedValue;
    }
  }

  //---------- Private Methods ----------//


  /**
   * Throws exception if value cannot be assigned to the specified type.
   *
   * @param typedObject Value.
   * @param type Assignable type.
   * @param <T> Assignable type.
   */
  private <T> void validateValueIsAssignableToType(TypedObject typedObject, Class<T> type) {
    if (!type.isAssignableFrom(typedObject.getType())) {
      throw new IllegalArgumentException(String.format(
          "Type type specified [%s] is not assignable from the value's actual type [%s].",
          type.getCanonicalName(), typedObject.getType().getCanonicalName()));
    }
  }

  /**
   * Throws exception if value is not exactly of the specified type.
   *
   * @param typedObject Value.
   * @param type Expected type.
   * @param <T> Expected type.
   */
  private <T> void validateValueIsOfType(TypedObject typedObject, Class<T> type) {
    if (!typedObject.getType().equals(type)) {
      throw new IllegalArgumentException(String.format(
          "Type type specified [%s] does not match the value's actual type [%s].",
          type.getCanonicalName(), typedObject.getType().getCanonicalName()));
    }
  }
}
