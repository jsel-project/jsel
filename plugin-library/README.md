# Plugin Library

A generic library designed to help create extensible software applications through dynamic plugins.

<br/>

## Table of Contents
1. [Overview](#overview)
    - [Plugin Registry Example](#plugin-registry-example)
1. [Defining Plugins](#defining-plugins)
    - [Defining Plugin Interfaces](#defining-plugin-interfaces)
    - [Defining Plugin Classes](#defining-plugin-classes)
    - [Defining Multiple Versions of a Plugin](#defining-multiple-versions-of-a-plugin)
    - [Defining Plugin Attributes](#defining-plugin-attributes)
1. [Registering and Scanning for Plugins](#plugin-registration-and-scanning)
    - [Registering Plugin Interfaces](#registering-plugin-interfaces)
    - [Scanning for Locally Defined Plugins](#scanning-for-locally-defined-plugins)
    - [Scanning for Plugins in an External JAR](#scanning-for-plugins-in-an-external-jar)
    - [Scanning for Plugins in an Package Directory](#scanning-for-plugins-in-an-external-package-directory)
    - [Manually Registering Plugins](#manually-registering-plugins)
    - [Removing Registered Plugin Interfaces](#removing-registered-plugin-interfaces)
    - [Removing Registered Plugins](#removing-registered-plugins)
    - [Scan Results](#scan-results)
1. [Retrieving Plugins](#retrieving-plugins)
    - [Querying for Sets of Plugins](#querying-for-all-plugins)
    - [Querying for Single Plugin](#querying-for-a-single-plugin)
    - [Querying for the latest version of a Plugin](#querying-for-the-latest-version-of-a-plugin)
    - [Querying by Plugin Attributes](#querying-by-plugin-attributes)
1. [Registry Statistics and State](#supporting-methods)
    - [Counts](#registry-count-statistics)
    - [Checking for Plugin existence within a registry](#checking-plugin-existence)
    - [Registry state](#registry-state)
1. [Directory Monitor](#directory-monitor)
    - [Starting the Directory Monitor](#starting-the-directory-monitor)
    - [Stopping the Directory Monitor](#stopping-the-directory-monitor)
1. [Encrypted Plugin Packages](#encrypted-plugin-packages)
    - [Creating Encrypted Plugin Packages](#creating-encrypted-plugin-packages)
    - [Loading Encrypted Plugin Packages](#loading-encrypted-plugin-packages)
1. [Edge Cases](edge-cases)
    - [Plugin Interface Definition Workaround](#plugin-interface-definition-workaround)
    - [Plugins that implement multiple Plugin Interfaces](#plugins-that-implement-multiple-plugin-interfaces)
    - [Plugin Constructors](#plugin-constructors)
    - [Thread Safety](#thread-safety)
1. [Use Cases](#use-cases)
    - [Using Plugin Versions](#using-plugin-versions)
    - [Plugin Registration at Runtime](#plugin-registration-at-runtime)
1. [Limitations](#limitations)
    - [Loading Plugins at Runtime](#loading-plugins-at-runtime)
    - [Plugin Constructors](#plugin-constructor-limitations)

<br/>

## Overview <a name="overview"></a>

The Software Extensibility Library provides a set of tools designed to help
developers craft extensible software applications in Java.

- Extendability via Plugins
- Runtime extension
- Flexible plugin query
- Customizable plugin attributes

### Plugin Registry Example <a name="plugin-registry-example"></a>

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
pr.registerPluginInterfaces(Dog.class);

// Scan the currently running JAR for Plugins.
pr.scan();

// Retrieve all of the Dog Plugin Implementations found.
List<Dog> dogs = pr.getPlugins(Dog.class);

// Run the bark() method of each Plugin scanned in.
dog.stream().forEach(dog -> System.out.println(dog.bark()));

// Print the registry state.
pr.printRegistryState();
// ------------------------------
// | PLUGIN INTERFACE | PLUGINS |
// ------------------------------
// | Dog              | Husky   |
// |                  | Poodle  |
// ------------------------------
```

<br/>

## Defining Plugins <a name="defining-plugins"></a>

This section describes how to define plugins, and introduces the terms
**Plugin Interface**, **Plugin**, and **Pluggable interface**.

### Defining Plugin Interfaces <a name="defining-plugin-interfaces"></a>

In order to create custom plugins, you first need to define a **Plugin Interface**. Plugin
Interfaces must:
- be declared as a Java interface (not as a class), and
- extend the [Pluggable](src/main/java/techmoc/extensibility/pluginlibrary/Pluggable.java) interface.

The [Pluggable](src/main/java/techmoc/extensibility/pluginlibrary/Pluggable.java)
interface exposes optional methods that allow the user to set the plugin name and version number.
By default, the plugin name is set to the name of the class, and the version number is set
to: **0.0.0**

For more details about the Pluggable interface, see
[Pluggable Interface](src/main/java/techmoc/extensibility/pluginlibrary/Pluggable.java).

### Defining Plugin Classes <a name="defining-plugin-classes"></a>

Before a concrete Plugin class can be implemented, a **Plugin Interface** must
first be defined. Plugin Interfaces define the set of methods that a concrete
**Plugin** class is required to implement. All valid Plugin classes must:

- be defined as a concrete Java class (not an abstract class, or an interface),
- implement a Plugin Interface, and
- expose a public no-args constructor.

The following example demonstrates how to define three Plugins that implement
the Dog Plugin Interface.

```java
// Plugin Interface.
interface Dog extends Pluggable {
  String bark();
}

// Plugin #1
class Husky implements Dog {
  public String bark() { return "woof"; }
}

// Plugin #2
class Poodle implements Dog {
  public String bark() { return "yap"; }
}

// Plugin #3
class Pug implements Dog {
  public String bark() { return "ruff"; }
}
```

### Defining Multiple Versions of a Single Plugin <a name="defining-multiple-versions-of-a-plugin"></a>

All Plugins have the ability to set a Plugin name and version by overriding the
**getPluginName()** and **getPluginVersion()** methods provided by the Pluggable
interface. By default, a Plugin's name is defined as the name of the class
itself, and it's version is set to: **0.0.0**

Plugins with the same name and version number cannot be registered within the
Plugin Registry (the first one registered will be registered, and all subsequent
Plugins of the same name and version will be ignored).

The following example demonstrates how to define three different versions of
the "Husky" Plugin.

```java
// Plugin Interface
interface Dog extends Pluggable {
  String bark();
}

// Plugin: Husky (version 0.0.0)
class Husky implements Dog {
  public String bark() { return "woof"; }
}

// Plugin: Husky (version 2.0.0)
class Husky2 implements Dog {
  public String getPluginName() { return "Husky"; }

  public PluginVersion getPluginVersion() {
    return new PluginVersion(2, 0, 0);
  }

  public String bark() { return "woof woof!"; }
}

// Plugin: Husky (version 3.1.2)
class Husky3 implements Dog {
  public String getPluginName() { return "Husky"; }

  public PluginVersion getPluginVersion() {
    return new PluginVersion(3, 1, 2);
  }

  public String bark() { return "WOOF WOOF WOOF!!!"; }
}
```

In cases where a Java interface cannot be modified to extend the Pluggable interface,
[follow this workaround](#plugin-interface-definition-workaround).

### Defining Plugin Attributes <a name="defining-plugin-attributes"></a>

There are two points at which custom attributes can be applied to a Plugin:

- During plugin definition, and
- After a plugin has been registered into a Plugin Registry.

To add custom attributes to a Plugin definition, simply implement the 
`void initializePluginAttributes(PolymorphicMap pluginAttributes)` method.

```java
public class Tabby implements Cat {

  @Override
  public void initializePluginAttributes(PolymorphicMap pluginAttributes) {
    pluginAttributes.put("LongHaired", true);
    pluginAttributes.put("TotalStripes", 7);
    pluginAttributes.put("FoodPreference", "Dry food");
  }

  @Override
  public String meow() {
    return "Meooow";
  }
}
```

To add custom attributes to a plugin after it is registered to a Plugin Registry, 
call one of the `putPluginAttribute(...)` methods.

```java
// Instantiate the plugin registry.
PluginRegistry pr = new PluginRegistry();

// Register a Plugin Interface.
pr.registerPluginInterfaces(Dog.class);

// Register a plugin.
pr.registerPlugin(Husky.class, Dog.class);

// Add a custom attribute to the registered plugin.
pr.putPluginAttribute("Husky", Dog.class, "HumanFriendly", true));
```

Note that Plugin Attributes are mutable, and can be added to, modified, or removed 
from any registered plugin.

Note that Plugin Attributes are stored at the level of the Plugin Registry, not the 
Plugin itself (i.e. attributes cannot be queried from Plugin instances directly).

<br/>

## Registering and Scanning for Plugins <a name="registering-and-scanning-for-plugins"></a>

Once a set of Plugin Interfaces and concrete Plugins have been defined, they
can be registered into the
[Plugin Registry](src/main/java/techmoc/extensibility/pluginlibrary/PluginRegistry.java).
The Plugin Registry makes it easy for developers to quickly discover and load
Plugins defined throughout their application. It also provides useful plugin
related statistics and query methods.

### Registering Plugin Interfaces <a name="registering-plugin-interfaces"></a>

The first step in the plugin registration process is to register a
**Plugin Interface**. Several methods are provided for Plugin Interface
registration.

```java
// Instantiate the plugin registry.
PluginRegistry pr = new PluginRegistry();

// Register a Plugin Interface.
pr.registerPluginInterfaces(Dog.class);

// Register multiple Plugin Interfaces.
pr.registerPluginInterfaces(Bird.class, Frog.class);

// Register a set of Plugin Interfaces.
List<Class<? extends Pluggable>> pluginInterfaces = List.of(Cat.class, Rat.class);
pr.registerPluginInterfaces(pluginInterfaces);
```

### Scanning for Locally Defined Plugins <a name="scanning-for-locally-defined-plugins"></a>

Once a few Plugin Interfaces have been registered into the Plugin Registry,
developers can easily scan-in all classes the implement a
**Registered Plugin Interface**.
By calling a scan method, the Plugin Registry traverses the application's
Class Path and registers classes that implement Registered Plugin Interfaces.

```java
// Instantiate the plugin registry.
PluginRegistry pr = new PluginRegistry();

// Register a Plugin Interface.
pr.registerPluginInterfaces(Dog.class);

// Scan the currently running JAR for Plugins that implement all Registered Java Interfaces.
pr.scan();
```

### Scanning for Plugins in an External JAR <a name="scanning-for-plugins-in-an-external-jar"></a>

Plugins may also be registered from external JAR files at runtime.

```java
// Instantiate the plugin registry.
PluginRegistry pr = new PluginRegistry();

// Register a Plugin Interface.
pr.registerPluginInterfaces(Dog.class);

// Scan the specified JAR file for Plugins that implement all Registered Java Interfaces.
pr.scanJar("path/to/JAR.jar");
```

### Scanning for Plugins in a Package Directory <a name="scanning-for-plugins-in-a-package-directory"></a>

Plugins may also be registered from external Package Directories at runtime.
Notice that the path specified in the **scanPackageDirectory(String path)**
method refers to the root of the package directory.

```java
// Instantiate the plugin registry.
PluginRegistry pr = new PluginRegistry();

// Register a Plugin Interface.
pr.registerPluginInterfaces(Dog.class);

// Scan the specified folder for Plugins that implement all Registered Java Interfaces.
pr.scanPackageDirectory("/path/to/package/folder");
```

### Manually Registering Plugins <a name="manually-registering-plugins"></a>

Only Plugins that are defined locally within an application can be registered
manually (Plugins defined in external JAR files cannot be registered manually
at runtime).

```java
// Instantiate the plugin registry.
PluginRegistry pr = new PluginRegistry();

// Register a Plugin Interface.
pr.registerPluginInterfaces(Dog.class);

// Manually register a specific Plugin, to a specific Plugin Interface.
pr.registerPlugin(Husky.class);

// Manually register a specific Plugin, under a specific Plugin Interface.
pr.registerPlugin(Husky.class, Dog.class);
```

### Removing Registered Plugin Interfaces <a name="removing-registered-plugin-interfaces"></a>

Plugin Interfaces that have been registered into a Plugin Registry may be
removed at any time by calling one of the removal methods provided. When a
Plugin Interface is removed, all of the Plugins that have been registered under
it will also be removed.

```java
// Instantiate the plugin registry.
PluginRegistry pr = new PluginRegistry();

// Register the Cat and Rat Plugin Interfaces.
pr.registerPluginInterfaces(Cat.class, Rat.class);

// Unregister the Rat Plugin Interface.
pr.unregisterInterface(Rat.class);

// At this point, only the "Cat" Plugin Interface is registered.
```

### Removing Registered Plugins <a name="removing-registered-plugins"></a>

Individual Plugins may also be removed from the Plugin Registry at any time by
calling a removal method.

```java
// Instantiate the plugin registry.
PluginRegistry pr = new PluginRegistry();

// Register a Plugin Interface.
pr.registerPluginInterfaces(Dog.class);

// Manually register a specific Plugin.
pr.registerPlugins(Husky.class, Poodle.class, Pug.class, Collie.class);

// Manually register a specific Plugin, to a specific Java Interface.
pr.unregisterPlugin(Husky.class);
```

### Scan Results <a name="scan-results"></a>

All plugin scan methods return a `ScanResults` object containing information about 
what the scan had discovered. Scan results can be analyzed to determine how many 
new plugins were registered by a scan, or investigate why an expected plugin failed 
to register successfully.

```java
// Instantiate the plugin registry.
PluginRegistry pr = new PluginRegistry();

// Register a Plugin Interface.
pr.registerPluginInterfaces(Dog.class);

// Scan for plugins.
ScanResults scanResults = pr.scan();

for (ScanLog log : scanResults.getSuccessfulPluginFileLogs()) {
  System.out.println("New plugin registered: " + log.getFullyQualifiedName);
}
```

<br/>

## Retrieving Plugins <a name="retrieving-plugins"></a>

The [Plugin Registry](src/main/java/techmoc/extensibility/pluginlibrary/PluginRegistry.java)
provides a number of useful ways to retrieve Plugins from the registry.

### Querying for all Plugins <a name="querying-for-all-plugins"></a>

The simplest plugin retrieval query is one where we ask the **Plugin Registry**
to return all Plugins that have been scanned-in or manually registered under a
given **Plugin Interface**. These Plugins are first instantiated and then
returned in a Set.

```java
// Instantiate the plugin registry.
PluginRegistry pr = new PluginRegistry();

// Register a Plugin Interface.
pr.registerPluginInterfaces(Dog.class);

// Scan for Plugins.
pr.scan();

// Retrieve all Plugins that were scanned-in.
List<Dog> plugins = pr.getPlugins(Dog.class);
```

### Querying for a Single Plugin <a name="querying-for-a-single-plugin"></a>

In cases where only a single version of a Plugin is registered within a
Plugin Registry, one can safely retrieve that Plugin by its name.
Note that if multiple Plugins are registered under the same name, then an
exception will be thrown.

```java
// Instantiate the plugin registry.
PluginRegistry pr = new PluginRegistry();

// Register a Plugin Interface.
pr.registerPluginInterfaces(Dog.class);

// Scan for Plugins.
pr.scan();

// Retrieve the "Husky" Plugin, registered under the "Dog" Plugin Interface.
Dog husky = pr.getPlugin("Husky", Dog.class);
```

### Querying for the latest version of a Plugin <a name="querying-for-the-latest-version-of-a-plugin"></a>

In cases where multiple versions of a Plugin are registered within a
Plugin Registry, the latest version of a Plugin can be retrieved.

```java
// Instantiate the plugin registry.
PluginRegistry pr = new PluginRegistry();

// Register a Plugin Interface.
pr.registerPluginInterfaces(Dog.class);

// Scan for Plugins.
pr.scan();

// Retrieve the latest version of the "Husky" Plugin, registered under the "Dog" Plugin Interface.
Dog husky = pr.getPluginLatestVersion("Husky", Dog.class);
```

### Querying by Plugin Attributes <a name="querying-by-plugin-attributes"></a>

Plugins can be queried by custom attributes via a lambda function:

```java
// Instantiate the plugin registry.
PluginRegistry pr = new PluginRegistry();

// Register a Plugin Interface.
pr.registerPluginInterfaces(Cat.class);

// Scan for Plugins.
pr.scan();

// Retrieve all Cat Plugins that are less than 1 year old.
List<Cat> kittenPlugins = pr.getByAttribute(
    (polyMap) ->
        polyMap.containsKeyOfType("Age", Double.class) &&
        polyMap.get("Age", Double.class) < 1.0,
    Cat.class);
```

<br/>

## Registry Statistics and State <a name="registry-statistics-and-state"></a>

The Plugin Registry provides access to a variety of statistics and registry
state information.

### Counts <a name="registry-count-statistics"></a>

Information about the total number of Plugin Interfaces or Plugins can be
retrieved from a Plugin Registry.

```java
// Instantiate the plugin registry.
PluginRegistry pr = new PluginRegistry();

// Register a Plugin Interface.
pr.registerInterfaces(Dog.class, Cat.class, Rat.class);

// Count the number of registered plugin interfaces.
int totalRegisteredPluginInterfaces = pr.getRegisteredInterfaceCount(); // 1

// Scan the currently running JAR for Plugins.
pr.scan();

// Count the number of plugin implementations found.
int totalPluginImplementationsFound = pr.count();
int totalDogImplementationsFound = pr.count(Dog.class);
int totalCatAndRatImplementationsFound = pr.count(Cat.class, Rat.class);
```

### Checking for Plugin existence within a registry <a name="checking-plugin-existence"></a>

The following plugin existence methods are provided by the Plugin Registry.

```java
// Instantiate the plugin registry.
PluginRegistry pr = new PluginRegistry();

// Register a Java Interface.
pr.registerInterfaces(Dog.class, Cat.class);

// Scan the currently running JAR for Plugins.
pr.scan();

// Check if a specific Plugin exists in the Plugin Registry.
if (pr.isRegistered("Husky", Dog.class)) {
  System.out.println("Plugin found.");
} else {
  System.out.println("Plugin not found.");
}

// Check if a specific version of a Plugin exists in the Plugin Registry.
if (pr.isRegistered("Persian", 1, 2, 345, Cat.class)) {
  System.out.println("Plugin found.");
} else {
  System.out.println("Plugin not found.");
}
```

### Registry state <a name="registry-state"></a>

Registry state can be printed to standard out by calling one of the
**printRegistryState()** methods.

```java

// Instantiate the plugin registry.
PluginRegistry pr = new PluginRegistry();

// Register a Java Interface.
pr.registerInterfaces(Dog.class, Cat.class);
pr.registerPlugin(Husky.class);
pr.registerPlugin(Calico.class);
pr.registerPlugin(Tabby.class);

// Print the registry state.
pr.printRegistryState();
// ------------------------------
// | PLUGIN INTERFACE | PLUGINS |
// ------------------------------
// | Cat              | Calico  |
// |                  | Tabby   |
// | Dog              | Husky   |
// ------------------------------
```

Likewise, a JSON representation of the registry state can be retrieved by
calling a **toString()** or **toJson()** method.

```java

// Instantiate the plugin registry.
PluginRegistry pr = new PluginRegistry();

// Register a Java Interface.
pr.registerInterfaces(Dog.class, Cat.class);
pr.registerPlugin(Husky.class);
pr.registerPlugin(Calico.class);
pr.registerPlugin(Tabby.class);

// Print the registry state as a JSON string.
System.out.print(pr.toString());
// [
//   { "Cat" : [ "Calico", "Tabby" ]},
//   { "Dog" : [ "Husky" ]}
// ]
```

<br/>

## Directory Monitor <a name="directory-monitor"></a>

The directory monitor allows developers to designate a directory that can be
used to received Plugin JARs and Package Directories. The monitor automatically
scans JAR files or Package Directory copied into the specified directory. The
new files are scanned for Plugins that implement a Registered Plugin Interface,
and loaded into the Plugin Registry.

### Starting the Directory Monitor <a name="starting-the-directory-monitor"></a>

The following example demonstrates how to start the directory monitor.

```java
PluginRegistry pr = new PluginRegistry();
pr.registerInterfaces(Dog.class);

// Start the directory monitor.
pr.startDirectoryMonitor("/path/to/target/directory");

// At this point, any JAR file or Package Directory placed in the target directory will be scanned for Plugins.

```

### Stopping the Directory Monitor <a name="stopping-the-directory-monitor"></a>

The following method can be called to stop the directory monitor.
Once stopped the directory monitor may be started again.

```java
// The directory monitor can be stopped by calling the method below.
pr.stopDirectoryMonitor();
```

<br/>

## Encrypted Plugin Packages <a name="encrypted-plugin-packages"></a>

Applications that attempt to load Plugins at runtime need to protect themselves from 
malicious plugin injection. The Plugin Library offers one solution to this problem  
by providing an API for packaging, encrypting, loading, and validating 
"Encrypted Plugin Packages".

### Creating Encrypted Plugin Packages <a name="creating-encrypted-plugin-packages"></a>

To create an Encrypted Plugin Package:

```java
    // Declare a Plugin Registry.
    PluginRegistry pr = new PluginRegistry();

    // Determine the path to the tmp directory.
    File tmpDir = tempDir.resolve("techmoc").toFile();

    // Get the path to the 'libs/plugin-library-*.jar' package directory.
    ClassLoader classLoader = getClass().getClassLoader();
    File jarFile = new File(classLoader.getResource("test-plugins.jar").getFile());

    // Generate a key pair, to represent the encryption keys used by the receiver.
    PluginKeyPair keyPair = new PluginKeyPair();

    // Create the encrypted package.
    pr.createEncryptedPackage("/path/to/JarFile.jar", keyPair.getPublicKey(), "/output/dir/");

    // NOTE: Encrypted Plugin Package will be written: /output/dir/JarFile.plugins
```

### Loading Encrypted Plugin Packages <a name="loading-encrypted-plugin-packages"></a>

To load an Encrypted Plugin Package:

```java
    // Declare a Plugin Registry.
    PluginRegistry pr = new PluginRegistry();

    // Scan the encrypted package.
    pr.registerPluginInterfaces(Bird.class, Cat.class, Dog.class);

    // Scan the encrypted package using the Private Key generated during the encryption process.
    pr.scanEncryptedPackage("/path/to/JarFile.plugins", keyPair.getPrivateKey());
```

<br/>

## Edge Cases <a name="edge-cases"></a>

This section discuses known edge cases.

### Plugin Interface Definition Workaround <a name="plugin-interface-definition-workaround"></a>

In some cases, developers are unable to modify a desired Java interface in
order to convert it into a Plugin Interface. Common reasons include:

- Project constraints disallow modification of Java interfaces.
- Java interface is provided via a 3rd party package, and cannot be modified.

In these cases, a Plugin Interface can still be created by implementing the
following pattern.

```java
// Non-modifiable Java interface.
interface Dog {
  String bark();
}

// Empty Plugin Interface that extends both Pluggable and the target Java interface.
interface DogPlugin extends Dog, Pluggable {
}

// Plugin class.
class Husky implements DogPlugin {
  public String bark() {
    return "Woof!";
  }
}
```

### Plugins that implement multiple Plugin Interfaces <a name="plugins-that-implement-multiple-plugin-interfaces"></a>

Plugins can implement multiple Plugin Interfaces. In these cases, it is
important to know that by default when a Plugin scanned-in, it is registered
under all Plug Interfaces that it implements. To change this behavior,
developers can specify the exact Plugin Interface that a call to a scan()
method should target.

```java
public interface Cat extends Pluggable {
  String meow();
}

public interface Fish extends Pluggable {
  String swim();
}

public class CatFish implements Cat, Fish {

  @Override
  public String meow() {
    return "meow";
  }

  @Override
  public String swim() {
    return "whoosh";
  }
}
```

```java
// Instantiate the plugin registry.
PluginRegistry pr = new PluginRegistry();

// Register a Plugin Interface.
pr.registerPluginInterfaces(Cat.class, Fish.class);

// Scan.
pr.scan();

// Print the registry state.
pr.printRegistryState();
// ------------------------------
// | PLUGIN INTERFACE | PLUGINS |
// ------------------------------
// | Cat              | CatFish |
// | Fish             | CatFish |
// ------------------------------
```

### Plugin Constructors <a name="plugin-constructors"></a>

In order for the Plugin Registry to perform basic operations with Plugins, it
needs to instantiate the Plugin classes (as Pluggable). This is required for
Plugin registration, existence checks, queries, etc. For this reason, all
Plugin classes must expose a public no-args constructor.

Because the Plugin Registry instantiates Plugins internally, it is important
to keep the Plugin's public no-args constructor lean (preferably empty).
Plugins that implement slow or resource hungry public no-arg constructors can
negatively affect the performance of the Plugin Registry.

### Thread Safety <a name="thread-safety"></a>

The Plugin Registry class is backed by a ConcurrentHashMap, and is thus
generally considered to be thread safe.

<br/>

## Use Cases <a name="use-cases"></a>

This section describes a number of common use cases that the Plugin Registry
fulfills. It also provides suggested patterns for handling the use case.

### Using Plugin Versions <a name="using-plugin-versions"></a>

To create a new version of a Plugin, the `getPluginName()` and `getPluginVersion()` 
methods need to be overridden.

```java
// Plugin Interface.
interface Dog extends Pluggable {
  String bark();
}

// "Husky" version 0.0.0.
class Husky implements Dog {
  public String bark() { return "woof"; }
}

// "Husky" version 1.0.0.
class Husky100 implements Dog {
  @Override
  public String getPluginName() {
    return "Husky";
  }

  @Override
  public PluginVersion getPluginVersion() {
    return new PluginVersion(1, 0, 0);
  }

  @Override
  public String bark() { return "woof woof"; }
}

// "Husky" version 1.2.3.
class Husky123 implements Dog {
  @Override
  public String getPluginName() {
    return "Husky";
  }

  @Override
  public PluginVersion getPluginVersion() {
    return new PluginVersion(1, 2, 3);
  }

  @Override
  public String bark() { return "woof woof woof"; }
}
```

If these methods were to be scanned in, they would produce the following registry state.

```java
PluginRegistry pr = new PluginRegistry();

// Register a Plugin Interface.
pr.registerPluginInterfaces(Dog.class);

// Scan.
pr.scan();

// Print the registry state.
pr.printRegistryState();
//--------------------------------------------
//| PLUGIN INTERFACE | PLUGINS               |
//--------------------------------------------
//| Dog              | Husky (version 0.0.0) |
//|                  | Husky (version 1.0.0) |
//|                  | Husky (version 1.2.3) |
//--------------------------------------------
```

### Plugin Registration at Runtime <a name="plugin-registration-at-runtime"></a>

The Plugin Registry's **scanJar()** and **scanPackageDirectory()** methods
allow developers to import Plugins from an external source, at runtime.


```java
// Instantiate the plugin registry.
PluginRegistry pr = new PluginRegistry();

// Register a Plugin Interface.
pr.registerPluginInterfaces(Dog.class);

// Scan the currently running JAR for Plugins.
pr.scanJar("/path/to/target.jar");
```

<br/>

## Limitations <a name="limitations"></a>

This section discusses the known limitations of the Plugin Library.

### Loading Plugins at Runtime <a name="loading-plugins-at-runtime"></a>

The Plugin Registry's **scanJar()** and **scanPackageDirectory()** methods
allow developers to import Plugins from an external source, at runtime.
Whenever an application employs this capability, they must take extra
precautions to mitigate risks associated with runtime Java code extension.
These issues include security concerns, dependency management, resource
allocation and load balancing, and fault tolerance to name a few.

The Plugin Repository is designed to allow applications to be extended. It is
designed to delegate class loading to its parent, and resolve class
dependencies starting from the highest level class loader working downward.
For this reason, it is important for Plugins to be designed to use only those
dependencies that are provided by the target application. Plugins should not
carry in their own dependencies. Plugins should not be compiled as FatJars or
UberJars. Instead, the parent application should be compiled with all
dependencies that both it and its Plugins will need.

### Plugin Constructor Limitations <a name="plugin-constructor-limitations"></a>

The Plugin Registry currently requires that concrete Plugin classes
implement a light-weight public no-args constructor. This requirement stems
from the fact that the Plugin Registry must instantiate Plugin classes
internally (as Pluggable) in order to perform basic tasks like registration,
validation, existence checking, and retrieval.
