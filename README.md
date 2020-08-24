# Plugin Library

A generic library designed to help create extensible software applications through dynamic plugins.

## Table of Contents
1. [Overview](#overview)
1. [Plugin Registry Example](#plugin-registry-example)
1. [Polymorphic Map Example](#polymorphic-map-example)
1. [Documentation](#documentation)
    - [Plugin Library](plugin-library/README.md)
    - [Polymorphic Map](polymorphic-map/README.md)

## Overview <a name="overview"></a>

The Software Extensibility Library provides a set of tools designed to help
developers craft extensible software applications in Java.

- Extendability via Plugins
- Added flexibility
- Runtime extension
- Flexible data structures
- Polymorphic data storage with type safety

## Plugin Registry Example <a name="plugin-registry-example"></a>

Let's start by defining a **Plugin Interface**. Notice that all Plugin Interfaces must extend the
Pluggable interface provided by the library.

```java
interface Dog extends Pluggable {
  String bark();
}
```

Now let's implement two **Plugins**. Every Plugin must be a concrete class that implements a
Plugin Interface.

```java
class Husky implements Dog {
  public String bark() { return "WOOF"; }
}

class Poodle implements Dog {
  public String bark() { return "yap!"; }
}
```

Finally, we can use the **Plugin Registry** to find and load all of our Plugins.

```java
// Instantiate the plugin registry.
PluginRegistry pr = new PluginRegistry();

// Register a Plugin Interface.
pr.registerPluginInterface(Dog.class);

// Scan the currently running JAR for Plugins.
pr.scan();

// Retrieve all of the Dog Plugin Implementations found.
Set<Dog> dogs = pr.getPlugins(Dog.class);

// Run the bark() method of each Plugin scanned in.
dog.stream().forEach(dog -> System.out.println(dog.bark()));
```

Further information about Plugins and the Plugin Registry can be found in the 
[Plugin Library README](plugin-library/README.md).

## Polymorphic Map Example <a name="polymorphic-map-example"></a>

`PolymorphicMap` is a map-like data structure containing any number of key/value pairs, where the 
keys are of the `String` type, and the values can be any arbitrary data type. What make 
`PolymorphicMap` unique is that, unlike normal Java maps, the values in the key/value pairs 
contained in the map need not be of the same type.

```java
// Insantiate a PolymorphicMap
PolymorphicMap polyMap = new PolymorphicMap();

// Insert an entry with a boolean value into the map
polyMap.put("BlueSky", true);

// Insert an entry with an integer value into the map
polyMap.put("Answer", 42);

// Insert an entry with a value that is an instance of a
// user-defined class into the map
polyMap.put("Car", new Car());
```

Values can be retrieved from the map via the `<T> T get(String key, Class<T> type)` method, which 
takes the Class of the value corresponding to the provided key as an argument to ensure type safety.

```java
// Retrieve a value from the PolymorphicMap
int answer = polymap.get("Answer", Integer.class);
```

Further information about [Polymorphic Maps](polymorphic-map/README.md) can be found in the 
[Polymorphic Maps README](polymorphic-map/README.md).

## Documentation <a name="documentation"></a>

Project documentation has been added to the base folder of each Gradle sub-project.
Projects focused on testing the library are organized under the **test/** directory.
Demo projects are organized under the **demos/** directory.

1. [Plugin Library documentation home page.](plugin-library/README.md)
2. [Polymorphic Map documentation home page.](polymorphic-map/README.md)

<br/>
