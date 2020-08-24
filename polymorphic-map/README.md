# Plugin Library

A generic library designed to help create extensible software applications through dynamic plugins.

## Table of Contents
1. [Overview](#overview)
1. [Polymorphic Map](#polymorphic-map)
    - [Storing Data](#storing-data)
    - [Assignable Type Retrieval](#assignable-type-retrieval)
    - [Exact Type Retrieval](#exact-type-retrieval)
    - [Determining Value Types](#determining-value-types)
    - [Extracting Values of a Specific Type](#extracting-values-of-a-specific-type)
1. [Domain Map](#domain-map)
    - [Defining a Domain Map](#defining-a-domain-map)
    - [Using a Domain Map](#using-a-domain-map)
    - [Benefits](#domain-map-benefits)
1. [Closed Domain Map](#closed-domain-map)
    - [Defining a Closed Domain Map](#defining-a-closed-domain-map)
    - [Using a Domain Map](#using-a-closed-domain-map)
    - [Benefitscl](#closed-domain-map-benefits)
1. [Limitations](#limitations)
    - [Thread Safety](#thread-safety)

<br/>

## Overview <a name="overview"></a>

The Software Extensibility Library provides a set of tools designed to help
developers craft extensible software applications in Java.

- Flexible data structures
- Polymorphic data storage with type safety

<br/>

## Polymorphic Map <a name="polymorphic-map"></a>

`PolymorphicMap` is a map-like data structure containing any number of key/value pairs, where the 
keys are of the `String` type, and the values can be any arbitrary data type.

### Storing Data <a name="storing-data></a>

What makes `PolymorphicMap` unique is that, unlike normal Java maps, the values in the key/value pairs 
contained in the map need not be of the same type.

```java
/*************************************************************
 * Demonstrates inserting different types of data into a     *
 * PolymorphicMap                                            *
 *************************************************************/

// Instantiate a PolymorphicMap
PolymorphicMap polyMap = new PolymorphicMap();

// Insert an entry with a boolean value into the map
polyMap.put("BlueSky", true);

// Insert an entry with an integer value into the map
polyMap.put("Answer", 42);

// Insert an entry with a value that is an instance of a
// user-defined class into the map
polyMap.put("Guitar", new Guitar());
```

### Assignable Type Retrieval <a name="assignable-type-retrieval"></a>

Values can be retrieved from the map via the `<T> T get(String key, Class<T> type)` method, which 
takes the class of the value corresponding to the provided key as an argument to ensure type safety.

```java
/******************************************
 * Demonstrates correct usage of get(...) *
 ******************************************/

// Insert an entry with an integer value into the map
polyMap.put("Answer", 42);

// Retrieve a value from the PolymorphicMap
int answer = polyMap.get("Answer", Integer.class);
```

If the provided class does not match that of the retrieved value, an `IllegalArgumentException`
will be thrown.

```java
/********************************************
 * Demonstrates incorrect usage of get(...) *
 ********************************************/

// Insert a Car into the PolymorphicMap
polyMap.put("Car", new Car());

// Throws IllegalArgumentException, as the value corresponding with the key "Car"
// is not an instance of MusicalInstrument or a subclass of MusicalInstrument
MusicalInstrument musicalInstrument = polyMap.get("Car", MusicalInstrument.class);
```

`PolymorphicMap` supports polymorphic behavior when retrieving values via `get(String, Class<T>)`.  
For instance, when retrieving a subclass from the map, it can be retrieved as an instance of its
parent class by providing the class of the parent class as an argument.

```java
/***********************************************************
 * Demonstrates the polymorphic behavior of PolymorphicMap *
 ***********************************************************/

// Instantiate a new PolymorphicMap
PolymorphicMap polyMap = new PolymorphicMap();

// Insert a Car into the PolymorphicMap
polyMap.put("Car", new Car());

// Insert a Tesla, which extends Car, into the PolymorphicMap.
polyMap.put("Tesla", new Tesla());

// This statement will execute successfully with the get(...) method, as the
// value associated with the "Tesla" key is assignable to variable of type Car.
polyMap.get("Tesla", Tesla.class);

// This statement will execute successfully with the get(...) method, as the
// value associated with the "Tesla" key is assignable to a variable of type Car.
polyMap.get("Tesla", Car.class);

// This statement will execute successfully with the get(...) method, as the
// value associated with the "Car" key is assignable to a variable of type Car.
polyMap.get("Car", Car.class);

// Throws IllegalArgumentException, as the value associated with the "Car" key is 
// NOT assignable to a variable of type Tesla.
polyMap.get("Car", Tesla.class);
```  

### Exact Type Retrieval <a name="exact-type-retrieval"></a>

Additionally, to disallow the polymorphic behavior of `get(String, Class<T>)`, the 
`<T> T getExact(String key, Class<T> type)` method can be used.  `getExact(String, Class<T>)`
requires that the EXACT class of the retrieved value be provided as an argument.  If the
provided class argument does not EXACTLY match that of the retrieved value (i.e. if the provided
class is a superclass of the retrieved value), an `IllegalArgumentException` will be thrown.
Concisely, `get(String, Class<T>)` requires only that the value associated with the given key
be assignable to the provided class argument.  `getExact(String, Class<T>)` requires that the
class of the value associated with the given key be equal to the provided class argument.

```java
/*************************************************************
 * Demonstrates correct and incorrect usage of getExact(...) *
 *************************************************************/

// Instantiate a new PolymorphicMap
PolymorphicMap polyMap = new PolymorphicMap();

// Insert a Car into the PolymorphicMap.
polyMap.put("Car", new Car());

// Insert a Tesla, which extends Car, into the PolymorphicMap.
polyMap.put("Tesla", new Tesla());

// Executes successfully, as the class of the value associated
// with the "Tesla" key is equal to the class argument passed
// to the getExact(...) method.
polyMap.getExact("Tesla", Tesla.class);

// Causes IllegalArgumentException to be thrown.  Unlike get(...), the getExact(...)
// method requires the provided class argument to be equal to the class of the
// retrieved value.  In this case, the retrieved value is of class Tesla.class, which
// is not equal to Car.class.
polyMap.getExact("Tesla", Car.class);

// Causes IllegalArgumentException to be thrown.  Unlike get(...), the getExact(...)
// method requires the provided class argument to be equal to the class of the
// retrieved value.  In this case, the retrieved value is of class Car.class, which
// is not equal to Tesla.class.
polyMap.getExact("Car", Tesla.class);

// Executes successfully, as the class of the value associated
// with the "Car" key is equal to the class argument passed
// to the getExact(...) method.
polyMap.getExact("Car", Car.class);
```

### Determining Value Types <a name="determining-value-types"></a>

Polymorphic maps can provide information about the type of a value stored at a given key.

```java
/*************************************************************
 * Demonstrates various ways of determining the type of a    *
 * value stored in a PolymorphicMap                          *
 *************************************************************/

// Instantiate a PolymorphicMap
PolymorphicMap polyMap = new PolymorphicMap();

// Insert a value into the map
polyMap.put("Answer", 42);

// Check if the "Answer" key contains an Integer value.
if (polyMap.getType("Answer") == Integer.class) {
  // Do something...
} else if (polyMap.getType("Answer") == Boolean.class) {
  // Do something else...
}

// Check if a given key exists in the polymorphic map.
if (polyMap.containsKey("Answer")) {
  // This test will pass.
}

// Check if a given key of a given type exists in the polymorphic map.
if (polyMap.containsKeyOfType("Answer", Integer.class)) {
  // This test will pass as well.
}

// This test returns false, because "Answer" contains an Integer value.
if (polyMap.containsKeyOfType("Answer", Boolean.class)) {
  // This test will fail!
}
```

### Extracting Values of a Specific Type <a name="extracting-values-of-a-specific-type"></a>

Polymorphic maps have the ability to export values of a specific type to a traditional map.

```java
/*************************************************************
 * Demonstrates how to export values of a given type from a  *
 * polymorphic map to a normal map                           *
 *************************************************************/

// Instantiate a PolymorphicMap
PolymorphicMap polyMap = new PolymorphicMap();

// Add a few values.
polyMap.put("FirstName", "John");
polyMap.put("LastName", "Smith");
polyMap.put("Age", 35);
polyMap.put("IsMarried", false);
polyMap.put("FavoriteFood", "Ice cream");

// Extract all of the string values to a Map<String, String>.
Map<String, String> stringValues = polyMap.extractValuesOfType(String.class);

// The new map contains the following three values.
stringValues.get("FirstName");    // "John"
stringValues.get("LastName");     // "Smith"
stringValues.get("FavoriteFood"); // "Ice cream"
```

<br/>

## Domain Map <a name="domain-map"></a>

Domain Maps are polymorphic maps that add type-specific getters and setters. Domain maps provide
additional convenience to developers by preventing them from having to explicitly specify the type 
of the object upon retrieval.

### Defining a Domain Map <a name="defining-a-domain-map"></a>

To define a Domain Map, simply create a class that extends `PolymorphicMap` class and
add getter and setter methods for each type that you intend to use.

```java
/*******************************************
 * Demonstrates how to define a domain map *
 *******************************************/

class MyDomainMap extends PolymorphicMap {
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
   * Stores the Boolean value at the given key.
   * Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final Boolean putBoolean(String key, Boolean value) {
    return this.put(key, value);
  }
}
```

The domain map defined above can now be used as follows.

```java
/****************************************
 * Demonstrates how to use a domain map *
 ****************************************/

// Instantiate the domain map.
MyDomainMap myDomainMap = new MyDomainMap();

// Write a boolean to the domain map.
myDomainMap.putBoolean("BlueSky", true);

// Retrieve the boolean from the domain map.
bool blueSky = myDomainMap.getBoolean("BlueSky");

// Notice that you can still add and retrieve random objects.
myDomainMap.put("Name", "John Smith");
String name = myDomainMap.get("Name", String.class);
```

### Using a Domain Map <a name="using-a-domain-map"></a>

`JavaDomainMap` is an extension of `PolymorphicMap` that provides variations of the 
`get(String, Class<T>)` method, each of which is implemented to retrieve one specific type
of value.  These variations of `get(String, Class<T>)` eliminate the need to pass the Class
of the retrieved value as an argument.  `JavaDomainMap` provides such methods for many of 
Java's built-in data types.  One such method,`Integer getInteger(String key)`, is demonstrated
in the following example.

```java
/*********************************************
 * Demonstrates basic usage of JavaDomainMap *
 *********************************************/

// Instantiate a JavaDomainMap
JavaDomainMap domainMap = new JavaDomainMap();

// Insert an integer into the map
domainMap.put("Answer", 42);

// Retrieve the integer from the map via the
// get(...) method, which requires the class of the 
// retrieved value as an argument
int answer = domainMap.get("Answer", Integer.class);

// Retrieve the integer from the map via the getInteger(...)
// method defined in domainMap
int otherAnswer = domainMap.getInteger("Answer");
```

`JavaDomainMap` can be extended to support variations of the `get(String, Class<T>)` method that
are designed to retrieve instances user-defined classes from the map.

```java
/**********************************************************
 * Demonstrates basic usage of a user-defined subclass of *
 * JavaDomainMap                                          *
 **********************************************************/

// CarMap is a user-defined class that extends JavaDomainMap
CarMap carMap = new CarMap();

// Insert a Ford and Tesla into the map
carMap.put("Tesla", new Tesla());
carMap.put("Ford", new Ford());

// Retrieve the Ford and Tesla from the map via the
// get(...) method, which requires the class of the 
// retrieved value as an argument
Tesla tesla = carMap.get("Tesla", Tesla.class);
Ford ford = carMap.get("Ford", Ford.class);

// Retrieve the Ford and Tesla from the map via the
// getTesla(...) and getFord(...) methods defined in CarMap
Tesla otherTesla = carMap.getTesla("Tesla");
Ford otherFord = carMap.getFord("Ford");
```

### Benefits <a name="domain-map-benefits"></a>

The primary benefits of using domain maps are:
    1. Increased type safety, due to the fact that the getters and setters only accept or return the defined type.
    1. Less error prone, since developers are not required to specify the expected type upon retrieval.
    1. Implies a set of objects that the map is intended to be used for.

<br/>

## Closed Domain Map <a name="closed-domain-map"></a>

Closed Domain Maps are similar to regular Domain Maps with one exception. A closed domain map 
may only store or return values exposed by type-specific getters and setters. The advantage of 
using a closed domain map is to ensure that the map only ever contain objects of a defined set.  

### Defining a Closed Domain Map <a name="defining-a-closed-domain-map"></a>

To define a Closed Domain Map, simply create a class that extends `ClosedPolymorphicMap` class, 
and add getter and setter methods for each type that you intend to use.

```java
/*******************************************
 * Demonstrates how to define a domain map *
 *******************************************/

class MyClosedDomainMap extends ClosedPolymorphicMap {
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
   * Stores the Boolean value at the given key.
   * Returns the old value, or null if none exists.
   *
   * @param key Key.
   * @return Old value, or null if none exists.
   */
  public final Boolean putBoolean(String key, Boolean value) {
    return this.put(key, value);
  }
}
```

The closed domain map defined above can now be used as follows.

```java
/****************************************
 * Demonstrates how to use a domain map *
 ****************************************/

// Instantiate the domain map.
MyClosedDomainMap map = new MyClosedDomainMap();

// Write a boolean to the domain map.
map.putBoolean("BlueSky", true);

// Retrieve the boolean from the domain map.
bool blueSky = map.getBoolean("BlueSky");

// NOTE: Random objects can NOT be added to a Closed Domain Map, 
// since the `put()` and `get()` methods are protected.

```

### Using a Domain Map <a name="sing-a-closed-domain-map"></a>

`JavaClosedDomainMap` is similar to `JavaDomainMap`, except that the `get(String, Class<T>)` method
has been disabled.  

```java
/***************************************************
 * Demonstrates basic usage of JavaClosedDomainMap *
 ***************************************************/

// Instantiate a JavaClosedDomainMap
JavaClosedDomainMap closedDomainMap = new JavaClosedDomainMap();

// Insert an integer into the map
domainMap.put("Answer", 42);

// The following statement attempting to retrieve the integer
// will not compile, as the get(...) method is not accessible
// from JavaClosedDomainMap
int answer = domainMap.get("Answer", Integer.class);

// Retrieve the integer from the map via the getInteger(...)
// method defined in JavaClosedDomainMap
int otherAnswer = domainMap.getInteger("Answer");
```

Additionally, `JavaClosedDomainMap` can be extended similarly to `JavaDomainMap`.

```java
/**********************************************************
 * Demonstrates basic usage of a user-defined subclass of *
 * JavaClosedDomainMap                                    *
 **********************************************************/

// ClosedCarMap is a user-defined class that extends
// JavaClosedDomainMap
ClosedCarMap closedCarMap = new ClosedCarMap();

// Insert a Ford and Tesla into the map
closedCarMap.put("Tesla", new Tesla());
closedCarMap.put("Ford", new Ford());

// The following two statements attempting to retrieve the
// Ford and Tesla will not compile, as the get(...)
// method is not accessible from JavaClosedDomainMap
// or extensions of JavaClosedDomainMap
Tesla tesla = closedCarMap.get("Tesla", Tesla.class);
Ford ford = closedCarMap.get("Ford", Ford.class);

// Retrieve the Ford and Tesla from the map via the
// getTesla(...) and getFord(...) methods defined in 
// ClosedCarMap
Tesla otherTesla = closedCarMap.getTesla("Tesla");
Ford otherFord = closedCarMap.getFord("Ford");
```

### Benefits <a name="closed-domain-map-benefits"></a>

The primary benefits of using domain maps are:
    1. Imposes a rigid set of objects that the map can store and return.
    1. Increased type safety, due to the fact that the getters and setters only accept or return pre-defined types.
    1. Less error prone, since developers are not required to specify the expected type upon retrieval.

<br/>

## Limitations <a name="limitations"></a>

This section discusses the known limitations of Polymorphic Maps.

### Thread Safety <a name="thread-saftey"></a>

The Polymorphic Map was designed thread safety in mind, but has not been rigorously tested. Until 
a comprehensive set of tests are written, it is safest to assume that Polymorphic Map is not thread 
safe.
