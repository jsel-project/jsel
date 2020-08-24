package techmoc.extensibility.pluginlibrary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static techmoc.extensibility.pluginlibrary.AssertUtil.assertEqualsWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import techmoc.extensibility.pluginlibrary.test_objects.Bass;
import techmoc.extensibility.pluginlibrary.test_objects.Calico;
import techmoc.extensibility.pluginlibrary.test_objects.Calico2;
import techmoc.extensibility.pluginlibrary.test_objects.Calico3;
import techmoc.extensibility.pluginlibrary.test_objects.CatFish;
import techmoc.extensibility.pluginlibrary.test_objects.Dog;
import techmoc.extensibility.pluginlibrary.test_objects.Fish;
import techmoc.extensibility.pluginlibrary.test_objects.Husky;
import techmoc.extensibility.pluginlibrary.test_objects.Poodle;
import techmoc.extensibility.pluginlibrary.test_objects.Poodle2;
import techmoc.extensibility.pluginlibrary.test_objects.Poodle201;
import techmoc.extensibility.pluginlibrary.test_objects.Poodle210;
import techmoc.extensibility.pluginlibrary.test_objects.Salmon;
import techmoc.extensibility.pluginlibrary.test_objects.Tabby;
import techmoc.extensibility.pluginlibrary.test_objects.Tabby2;
import techmoc.extensibility.pluginlibrary.test_objects.some_plugin_interfaces.Cat;
import techmoc.extensibility.pluginlibrary.test_objects.some_plugin_interfaces.Frog;
import techmoc.extensibility.test_interfaces.Bird;


/**
 * Plugin Registry unit tests.
 */
class PluginRegistryTests {

  private class MyClass implements Pluggable {

    String hello() {
      return "Hello";
    }
  }

  @Test
  void testRegisterInterfaceMethods() {
    PluginRegistry pr = new PluginRegistry();
    pr.registerPluginInterfaces(Dog.class);

    assertEquals(1, pr.getRegisteredInterfaces().size());
    assertEquals(Dog.class, pr.getRegisteredInterfaces().stream().findFirst().get());

    pr.registerPluginInterfaces(Cat.class, Frog.class);
    assertEquals(3, pr.getRegisteredInterfaces().size());

    assertEquals(List.of(Cat.class, Dog.class, Frog.class), pr.getRegisteredInterfaces());

    // Check that a non-interface class cannot be registered.
    assertThrows(IllegalArgumentException.class, () -> pr.registerPluginInterfaces(MyClass.class));
  }

  @Test
  void testUnregisterInterfaceMethods() {
    PluginRegistry pr = new PluginRegistry();
    assertEquals(0, pr.getRegisteredInterfaceCount());

    // Register classes.
    pr.registerPluginInterfaces(Dog.class, Cat.class, Frog.class);
    assertEquals(3, pr.getRegisteredInterfaceCount());

    // Unregister two classes.
    pr.unregisterPluginInterfaces(Frog.class, Cat.class);
    assertEquals(1, pr.getRegisteredInterfaceCount());

    // Unregister class.
    pr.unregisterPluginInterfaces(Dog.class);
    assertEquals(0, pr.getRegisteredInterfaceCount());

    assertThrows(IllegalArgumentException.class, () -> pr.unregisterPluginInterfaces(Dog.class));
  }

  @Test
  void testRegisterPluginMethods() {
    PluginRegistry pr = new PluginRegistry();

    // Throws exception when the Plugin Interface is not registered.
    assertThrows(IllegalStateException.class, () -> pr.registerPlugin(Husky.class));
    assertThrows(IllegalStateException.class, () -> pr.registerPlugin(Husky.class, Dog.class));

    // Register the Plugin Interface.
    pr.registerPluginInterfaces(Dog.class);

    // Register the plugin.
    pr.registerPlugin(Husky.class);

    // Check that the Plugin was registered.
    assertTrue(pr.isRegisteredPlugin("Husky", Dog.class));
    assertEquals("WOOF", pr.getAll(Dog.class).stream().findFirst().get().bark());
  }

  @Test
  void testUnregisterPluginMethods() {
    PluginRegistry pr = new PluginRegistry();

    // Register the Plugin Interface.
    pr.registerPluginInterfaces(Dog.class);

    // Register and unregister the plugin.
    pr.registerPlugin(Husky.class);
    assertEquals(1, pr.count(Dog.class));
    pr.unregisterPlugin(Husky.class);
    assertEquals(0, pr.count());

    // Register and unregister the plugin.
    pr.registerPlugin(Husky.class);
    assertEquals(1, pr.count(Dog.class));
    pr.unregisterPlugin(Husky.class, Dog.class);
    assertEquals(0, pr.count());

    // Register and unregister the plugin.
    pr.registerPlugin(Husky.class);
    assertEquals(1, pr.count(Dog.class));
    pr.unregisterPlugin("Husky");
    assertEquals(0, pr.count());

    // Register and unregister the plugin.
    pr.registerPlugin(Husky.class);
    assertEquals(1, pr.count(Dog.class));
    pr.unregisterPlugin("Husky", Dog.class);
    assertEquals(0, pr.count());

    pr.unregisterPluginInterfaces(Dog.class);
    pr.registerPluginInterfaces(Cat.class, Fish.class);

    // Register and unregister the plugin.
    pr.registerPlugin(CatFish.class);
    assertEquals(1, pr.count(Cat.class));
    assertEquals(1, pr.count(Fish.class));
    assertEquals(2, pr.count());
    assertThrows(IllegalArgumentException.class, () -> pr.unregisterPlugin(
        "Fish", 0, 0, 0));
    assertThrows(IllegalArgumentException.class, () -> pr.unregisterPlugin(
        "Fish", 0, 0, 0, Fish.class));

    pr.unregisterPlugin("CatFish", 1, 2, 345);
    assertEquals(0, pr.count());

    pr.registerPlugin(CatFish.class);
    assertEquals(1, pr.count(Cat.class));
    assertEquals(1, pr.count(Fish.class));
    assertEquals(2, pr.count());
    pr.unregisterPlugin(
        "CatFish", 1, 2, 345, Fish.class);
    assertEquals(1, pr.count(Cat.class));
    assertEquals(0, pr.count(Fish.class));
    assertEquals(1, pr.count());
  }

  @Test
  void testIsRegisteredPluginInterface() {
    PluginRegistry pr = new PluginRegistry();
    assertFalse(pr.isRegisteredPluginInterface(Dog.class));

    pr.registerPluginInterfaces(Dog.class);
    assertTrue(pr.isRegisteredPluginInterface(Dog.class));

    pr.registerPluginInterfaces(Cat.class, Frog.class, Fish.class);
    assertTrue(pr.isRegisteredPluginInterface(Dog.class));

    pr.unregisterPluginInterfaces(Dog.class);
    assertFalse(pr.isRegisteredPluginInterface(Dog.class));
  }

  @Test
  void testClearMethods() {
    PluginRegistry pr = new PluginRegistry();

    pr.registerPluginInterfaces(Dog.class);
    assertEquals(1, pr.getRegisteredInterfaceCount());
    assertEquals(0, pr.count());

    pr.clear();
    assertEquals(0, pr.getRegisteredInterfaceCount());
    assertEquals(0, pr.count());

    pr.registerPluginInterfaces(Dog.class);
    pr.registerPlugin(Husky.class);
    pr.registerPlugin(Poodle.class);
    pr.registerPlugin(Poodle201.class);
    pr.registerPlugin(Poodle210.class);
    assertEquals(1, pr.getRegisteredInterfaceCount());
    assertEquals(4, pr.count());

    pr.clear(Dog.class);
    assertEquals(1, pr.getRegisteredInterfaceCount());
    assertEquals(0, pr.count());
  }

  @Test
  void testScan() throws IOException {
    PluginRegistry pr = new PluginRegistry();
    pr.registerPluginInterfaces(Dog.class);
    assertEquals(0, pr.count(Dog.class));

    pr.scan(Dog.class);
    assertEquals(5, pr.count(Dog.class));

    assertTrue(pr.isRegisteredPlugin("Poodle", Dog.class));
    assertTrue(pr.isRegisteredPlugin("Husky", Dog.class));

    // Run the plugins.
    List<Dog> dogs = pr.getAll(Dog.class);
    List<String> dogBarks = List.of("WOOF", "Yap", "Yap yap", "Yap yap!!!", "Yap yap yap!!!");
    for (Dog dog : dogs) {
      assertTrue(dogBarks.contains(dog.bark()));
    }

    // Test for double registration.
    pr.unregisterPluginInterfaces(Dog.class);
    pr.registerPluginInterfaces(Cat.class, Fish.class);
    assertFalse(pr.isRegisteredPlugin("CatFish", Cat.class));
    assertFalse(pr.isRegisteredPlugin("CatFish", Fish.class));
    pr.scan();
    assertTrue(pr.isRegisteredPlugin("CatFish", Cat.class));
    assertTrue(pr.isRegisteredPlugin("CatFish", Fish.class));
  }

  @Test
  void testScanJar() throws IOException {
    PluginRegistry pr = new PluginRegistry();

    // Get the path to the 'libs/plugin-library-*.jar' package directory.
    ClassLoader classLoader = getClass().getClassLoader();
    File jarFile = new File(classLoader.getResource("test-plugins.jar").getFile());

    // Check initial plugin registry state.
    pr.registerPluginInterfaces(Bird.class);
    assertEquals(0, pr.count(Bird.class));

    // Scan for test_plugins.
    pr.scanJar(jarFile.getAbsolutePath(), Bird.class);

    // Check for loaded test_plugins.
    assertEquals(2, pr.count(Bird.class));
    assertTrue(pr.isRegisteredPlugin("Crow", Bird.class));
    assertTrue(pr.isRegisteredPlugin("Parakeet", Bird.class));

    // Run the plugins.
    List<Bird> birds = pr.getAll(Bird.class);
    List<String> birdChirps = List.of("Tweet!", "Cawww cawww!");
    for (Bird bird : birds) {
      assertTrue(birdChirps.contains(bird.chirp()));
    }
  }

  @Test
  void testScanJar2() throws IOException {
    PluginRegistry pr = new PluginRegistry();

    // Get the path to the 'libs/plugin-library-*.jar' package directory.
    ClassLoader classLoader = getClass().getClassLoader();
    File jarFile = new File(classLoader.getResource("test-plugins.jar").getFile());

    // Check initial plugin registry state.
    pr.registerPluginInterfaces(Bird.class);
    assertEquals(0, pr.count(Bird.class));

    // Scan for test_plugins.
    pr.scanJar(jarFile.getAbsolutePath());

    // Check for loaded test_plugins.
    assertEquals(2, pr.count(Bird.class));
    assertTrue(pr.isRegisteredPlugin("Crow", Bird.class));
    assertTrue(pr.isRegisteredPlugin("Parakeet", Bird.class));

    // Run the plugins.
    List<Bird> birds = pr.getAll(Bird.class);
    List<String> birdChirps = List.of("Tweet!", "Cawww cawww!");
    for (Bird bird : birds) {
      assertTrue(birdChirps.contains(bird.chirp()));
    }
  }

  @Test
  @Disabled // TODO: Requires following classes to be generated:
            //   - tests/test-package-directory-root/techmoc/testing/Bluejay.class
            //   - tests/test-package-directory-root/techmoc/testing/Woodpecker.class
  void testScanPackageDirectory() {
    PluginRegistry pr = new PluginRegistry();
    pr.registerPluginInterfaces(Bird.class);
    assertEquals(0, pr.count());

    // Get the path to the 'tests/test-package-directory-root' package root directory.
    ClassLoader classLoader = getClass().getClassLoader();
    File dirPath = new File(classLoader.getResource("").getFile());
    dirPath = new File(dirPath.getAbsolutePath()
        .substring(0, dirPath.getAbsolutePath().lastIndexOf("plugin-library/build/classes/")) +
        "/tests/test-package-directory-root");

    pr.scanPackageDirectory(dirPath.getPath(), Bird.class);
    assertEquals(2, pr.count(Bird.class));

    assertTrue(pr.isRegisteredPlugin("Bluejay", Bird.class));
    assertTrue(pr.isRegisteredPlugin("Woodpecker", Bird.class));

    // Run the plugins.
    List<Bird> birds = pr.getAll(Bird.class);
    List<String> birdChirps = List.of("Chirp!", "Tap tap tap!");
    for (Bird bird : birds) {
      assertTrue(birdChirps.contains(bird.chirp()));
    }
  }

  @Test
  @Disabled // TODO: Requires following classes to be generated:
            //   - tests/test-package-directory-root/techmoc/testing/Bluejay.class
            //   - tests/test-package-directory-root/techmoc/testing/Woodpecker.class
  void testScanPackageDirectory2() {
    PluginRegistry pr = new PluginRegistry();
    pr.registerPluginInterfaces(Bird.class);
    assertEquals(0, pr.count());

    // Get the path to the 'tests/test-package-directory-root' package root directory.
    ClassLoader classLoader = getClass().getClassLoader();
    File dirPath = new File(classLoader.getResource("").getFile());
    dirPath = new File(dirPath.getAbsolutePath()
        .substring(0, dirPath.getAbsolutePath().lastIndexOf("plugin-library/build/classes/")) +
        "/tests/test-package-directory-root");

    pr.scanPackageDirectory(dirPath.getPath());
    assertEquals(2, pr.count(Bird.class));

    assertTrue(pr.isRegisteredPlugin("Bluejay", Bird.class));
    assertTrue(pr.isRegisteredPlugin("Woodpecker", Bird.class));

    // Run the plugins.
    List<Bird> birds = pr.getAll(Bird.class);
    List<String> birdChirps = List.of("Chirp!", "Tap tap tap!");
    for (Bird bird : birds) {
      assertTrue(birdChirps.contains(bird.chirp()));
    }
  }

  @Test
  void testPluginLoadingMethods() {
    PluginVersion PLUGIN_VERSION_ZERO = new PluginVersion(0, 0, 0);
    PluginRegistry pr = new PluginRegistry();

    // Throws exception when the Plugin Interface is not registered.
    assertThrows(IllegalStateException.class, () -> pr.registerPlugin(Husky.class));
    assertThrows(IllegalStateException.class, () -> pr.registerPlugin(Husky.class, Dog.class));

    // Register the Plugin Interface.
    pr.registerPluginInterfaces(Dog.class);

    // Check that Husky is not available.
    assertNull(pr.get("Husky", 0, 0, 0, Dog.class));

    // Register the plugin.
    pr.registerPlugin(Husky.class);
    pr.registerPlugin(Poodle.class);
    pr.registerPlugin(Poodle2.class);
    pr.registerPlugin(Poodle201.class);
    pr.registerPlugin(Poodle210.class);

    pr.printRegistryState();

    // Check that Husky is available.
    Dog dog = pr.get("Husky", 0, 0, 0, Dog.class);
    Husky husky = (Husky) dog;
    assertEquals("Husky", husky.getPluginName());
    assertEquals(PLUGIN_VERSION_ZERO, husky.getPluginVersion());
    assertEquals("WOOF", husky.bark());

    List<Dog> dogs = pr.getAll("Husky", Dog.class);
    assertEquals(1, dogs.size());
    husky = (Husky) dogs.iterator().next();
    assertEquals("Husky", husky.getPluginName());
    assertEquals(PLUGIN_VERSION_ZERO, husky.getPluginVersion());
    assertEquals("WOOF", husky.bark());

    dogs = pr.getAll("Poodle", Dog.class);
    assertEquals(4, dogs.size());

    Dog dog2 = pr.get("Poodle", 2, 0, 1, Dog.class);
    assertEquals("Yap yap!!!", dog2.bark());
    Poodle201 poodle201 = (Poodle201) dog2;
    assertEquals("Poodle", poodle201.getPluginName());
    assertEquals("2.0.1", poodle201.getPluginVersion().toVersionNumber());

    Dog poodleLatest = pr.getLatestVersion("Poodle", Dog.class);
    assertEquals("Yap yap yap!!!", poodleLatest.bark());
    Poodle210 poodle210 = (Poodle210) poodleLatest;
    assertEquals("Poodle", poodle210.getPluginName());
    assertEquals("2.1.0", poodle210.getPluginVersion().toVersionNumber());
  }

  @Test
  void testGetRegisteredInterfaceCount() {
    PluginRegistry pr = new PluginRegistry();
    assertEquals(0, pr.getRegisteredInterfaceCount());

    // Register two classes.
    pr.registerPluginInterfaces(Dog.class, Cat.class);
    assertEquals(2, pr.getRegisteredInterfaceCount());
  }

  @Test
  void testCountMethods() {
    PluginRegistry pr = new PluginRegistry();
    pr.registerPluginInterfaces(Dog.class, Cat.class, Fish.class);
    assertEquals(0, pr.count());
    assertEquals(0, pr.count(Dog.class));
    assertEquals(0, pr.count(Cat.class));

    // Register a plugin.
    pr.registerPlugin(Husky.class);
    assertEquals(1, pr.count());
    assertEquals(1, pr.count(Dog.class));
    assertEquals(0, pr.count(Cat.class));
    assertEquals(0, pr.count(Fish.class));

    // Register a plugin.
    pr.registerPlugin(CatFish.class);
    assertEquals(3, pr.count());
    assertEquals(1, pr.count(Dog.class));
    assertEquals(1, pr.count(Cat.class));
    assertEquals(1, pr.count(Fish.class));

    // Register a plugin.
    pr.registerPlugin(Poodle.class);
    pr.registerPlugin(Poodle2.class);
    pr.registerPlugin(Poodle201.class);
    pr.registerPlugin(Poodle210.class);
    assertEquals(7, pr.count());
    assertEquals(5, pr.count(Dog.class));
    assertEquals(1, pr.count("Husky", Dog.class));
    assertEquals(4, pr.count("Poodle", Dog.class));
    assertEquals(1, pr.count(Cat.class));
    assertEquals(1, pr.count(Fish.class));
  }

  @Test
  void testIsRegisteredMethods() {
    PluginRegistry pr = new PluginRegistry();
    pr.registerPluginInterfaces(Cat.class, Dog.class);

    // Check that the Husky class has not been registered.
    assertFalse(pr.isRegisteredPlugin("Husky", Dog.class));

    // Add a Plugin that implements the Dog interface.
    pr.registerPlugin(Husky.class, Dog.class);

    // Check that the Husky class has been registered.
    assertTrue(pr.isRegisteredPlugin("Husky"));
    assertTrue(pr.isRegisteredPlugin("Husky", Dog.class));
    assertFalse(pr.isRegisteredPlugin("Husky", Cat.class));

    // Check that the Poodle class with the specified version number is registered.
    pr.registerPlugin(Poodle.class, Dog.class);
    assertFalse(pr.isRegisteredPlugin("Poodle", 2, 0, 0));
    assertFalse(pr.isRegisteredPlugin("Poodle", 1, 0, 0));
    assertFalse(pr.isRegisteredPlugin("Poodle", 1, 2, 0));
    assertTrue(pr.isRegisteredPlugin("Poodle", 1, 2, 345));

    assertFalse(pr.isRegisteredPlugin("Poodle", 2, 0, 0, Dog.class));
    assertFalse(pr.isRegisteredPlugin("Poodle", 1, 0, 0, Dog.class));
    assertFalse(pr.isRegisteredPlugin("Poodle", 1, 2, 0, Dog.class));
    assertTrue(pr.isRegisteredPlugin("Poodle", 1, 2, 345, Dog.class));
    assertFalse(pr.isRegisteredPlugin("Poodle", 1, 2, 345, Cat.class));
  }

  @Test
  void testScanDirForClassFiles(@TempDir Path tempDir) throws IOException {
    // Create temporary directory tree.
    byte[] data = new byte[]{};
    OpenOption CREATE_NEW = StandardOpenOption.CREATE_NEW;
    Files.createDirectory(tempDir.resolve("test/"));
    Files.createDirectory(tempDir.resolve("test/plugin/"));
    Files.createDirectory(tempDir.resolve("test/plugin2/"));
    Files.write(tempDir.resolve("Ignore.txt"), data, CREATE_NEW);
    Files.write(tempDir.resolve("One.class"), data, CREATE_NEW);
    Files.write(tempDir.resolve("test/Ignore.csv"), data, CREATE_NEW);
    Files.write(tempDir.resolve("test/Two.class"), data, CREATE_NEW);
    Files.write(tempDir.resolve("test/plugin/logger.log"), data, CREATE_NEW);
    Files.write(tempDir.resolve("test/plugin/Three.class"), data, CREATE_NEW);
    Files.write(tempDir.resolve("test/plugin/Four.class"), data, CREATE_NEW);
    Files.write(tempDir.resolve("test/plugin2/Five.class"), data, CREATE_NEW);
    Files.write(tempDir.resolve("test/plugin2/index.html"), data, CREATE_NEW);

    PluginRegistry pr = new PluginRegistry();

    // Scan for .class files.
    List<ScanLog> scanLogs = new ArrayList<>();
    Set<File> classFiles = pr.retrieveClassFilesFromDirectoryTree(
        tempDir.resolve("").toFile(), scanLogs);
    Set<String> expectedClassFileNames =
        Set.of("One.class", "Two.class", "Three.class", "Four.class", "Five.class");

    // Test the results.
    assertEquals(5, classFiles.size());
    classFiles.forEach(f -> assertTrue(f.isFile()));
    classFiles.forEach(f -> assertTrue(expectedClassFileNames.contains(f.getName())));
  }

  @Test
  void testDirectoryMonitorMethods(@TempDir Path tempDir)
      throws InterruptedException, IOException {
    PluginRegistry pr = new PluginRegistry();
    pr.registerPluginInterfaces(Dog.class);
    assertEquals(0, pr.count(Dog.class));

    // Get the path to the 'build/classes/java/test/techmoc' package directory.
    ClassLoader classLoader = getClass().getClassLoader();
    File srcDir = new File(classLoader.getResource("techmoc").getFile());

    // Set the destination path.
    File destPath = tempDir.resolve("PackageRootDir/techmoc").toFile();

    // Start the directory monitor.
    assertFalse(pr.isDirectoryMonitorRunning());
    pr.startDirectoryMonitor(tempDir.toString());
    assertTrue(pr.isDirectoryMonitorRunning());

    // Copy the test package from the build directory to a temp directory.
    FileUtils.copyDirectory(srcDir, destPath);

    // Check for new packages.
    assertEqualsWait(() -> pr.count(Dog.class), 5, 20000);

    // Check that the monitoring service can be stopped and restarted.
    pr.stopDirectoryMonitor();
    assertFalse(pr.isDirectoryMonitorRunning());

    pr.startDirectoryMonitor(tempDir.toString());
    assertTrue(pr.isDirectoryMonitorRunning());

    pr.stopDirectoryMonitor();
    assertFalse(pr.isDirectoryMonitorRunning());
  }

  @Test
  void testToRegistryStateMethods() {
    PluginRegistry pr = new PluginRegistry();

    // Test the empty registry toJson() result.
    assertEquals(
        "------------------------------\n"
            + "| PLUGIN INTERFACE | PLUGINS |\n"
            + "------------------------------\n"
            + "|                            |\n"
            + "------------------------------\n", pr.toRegistryState());

    // Register the Plugin Interface.
    pr.registerPluginInterfaces(Dog.class);
    assertEquals(
        "------------------------------\n"
            + "| PLUGIN INTERFACE | PLUGINS |\n"
            + "------------------------------\n"
            + "| Dog              |         |\n"
            + "------------------------------\n",
        pr.toRegistryState());
    assertEquals(
        "------------------------------------------------------------------\n"
            + "| PLUGIN INTERFACE                                     | PLUGINS |\n"
            + "------------------------------------------------------------------\n"
            + "| techmoc.extensibility.pluginlibrary.test_objects.Dog |         |\n"
            + "------------------------------------------------------------------\n",
        pr.toRegistryState(true));

    // Register a plugin.
    pr.registerPlugin(Husky.class);
    assertEquals(
        "------------------------------\n"
            + "| PLUGIN INTERFACE | PLUGINS |\n"
            + "------------------------------\n"
            + "| Dog              | Husky   |\n"
            + "------------------------------\n",
        pr.toRegistryState());
    assertEquals(
        "------------------------------------------------------------------\n"
            + "| PLUGIN INTERFACE                                     | PLUGINS |\n"
            + "------------------------------------------------------------------\n"
            + "| techmoc.extensibility.pluginlibrary.test_objects.Dog | Husky   |\n"
            + "------------------------------------------------------------------\n",
        pr.toRegistryState(true));

    // Register a second plugin.
    pr.registerPlugin(Poodle.class);
    assertEquals(
        "----------------------------------------------------\n"
            + "| PLUGIN INTERFACE | PLUGINS                       |\n"
            + "----------------------------------------------------\n"
            + "| Dog              | Husky                         |\n"
            + "|                  | Poodle (version 1.2.345-TEST) |\n"
            + "----------------------------------------------------\n",
        pr.toRegistryState());
    assertEquals(
        "----------------------------------------------------------------------------------------\n"
            + "| PLUGIN INTERFACE                                     | PLUGINS                       |\n"
            + "----------------------------------------------------------------------------------------\n"
            + "| techmoc.extensibility.pluginlibrary.test_objects.Dog | Husky                         |\n"
            + "|                                                      | Poodle (version 1.2.345-TEST) |\n"
            + "----------------------------------------------------------------------------------------\n",
        pr.toRegistryState(true));

    // Register a second version of a plugin.
    pr.registerPlugin(Poodle2.class);
    assertEquals(
        "----------------------------------------------------\n"
            + "| PLUGIN INTERFACE | PLUGINS                       |\n"
            + "----------------------------------------------------\n"
            + "| Dog              | Husky                         |\n"
            + "|                  | Poodle (version 1.2.345-TEST) |\n"
            + "|                  | Poodle (version 2.0.0)        |\n"
            + "----------------------------------------------------\n",
        pr.toRegistryState());
    assertEquals(
        "----------------------------------------------------------------------------------------\n"
            + "| PLUGIN INTERFACE                                     | PLUGINS                       |\n"
            + "----------------------------------------------------------------------------------------\n"
            + "| techmoc.extensibility.pluginlibrary.test_objects.Dog | Husky                         |\n"
            + "|                                                      | Poodle (version 1.2.345-TEST) |\n"
            + "|                                                      | Poodle (version 2.0.0)        |\n"
            + "----------------------------------------------------------------------------------------\n",
        pr.toRegistryState(true));

    // Register multiple Plugin Interfaces.
    pr.registerPluginInterfaces(Cat.class, Fish.class, Frog.class);
    assertEquals(
        "----------------------------------------------------\n"
            + "| PLUGIN INTERFACE | PLUGINS                       |\n"
            + "----------------------------------------------------\n"
            + "| Cat              |                               |\n"
            + "| Dog              | Husky                         |\n"
            + "|                  | Poodle (version 1.2.345-TEST) |\n"
            + "|                  | Poodle (version 2.0.0)        |\n"
            + "| Fish             |                               |\n"
            + "| Frog             |                               |\n"
            + "----------------------------------------------------\n",
        pr.toRegistryState());
    assertEquals(
        "----------------------------------------------------------------------------------------------------------------\n"
            + "| PLUGIN INTERFACE                                                             | PLUGINS                       |\n"
            + "----------------------------------------------------------------------------------------------------------------\n"
            + "| techmoc.extensibility.pluginlibrary.test_objects.some_plugin_interfaces.Cat  |                               |\n"
            + "| techmoc.extensibility.pluginlibrary.test_objects.Dog                         | Husky                         |\n"
            + "|                                                                              | Poodle (version 1.2.345-TEST) |\n"
            + "|                                                                              | Poodle (version 2.0.0)        |\n"
            + "| techmoc.extensibility.pluginlibrary.test_objects.Fish                        |                               |\n"
            + "| techmoc.extensibility.pluginlibrary.test_objects.some_plugin_interfaces.Frog |                               |\n"
            + "----------------------------------------------------------------------------------------------------------------\n",
        pr.toRegistryState(true));

    // Register multiple Plugin Interfaces.
    pr.registerPluginInterfaces(Cat.class, Fish.class, Frog.class);
    assertEquals(
        "----------------------------------------------------\n"
            + "| PLUGIN INTERFACE | PLUGINS                       |\n"
            + "----------------------------------------------------\n"
            + "| Cat              |                               |\n"
            + "| Dog              | Husky                         |\n"
            + "|                  | Poodle (version 1.2.345-TEST) |\n"
            + "|                  | Poodle (version 2.0.0)        |\n"
            + "| Fish             |                               |\n"
            + "| Frog             |                               |\n"
            + "----------------------------------------------------\n",
        pr.toRegistryState());
    assertEquals(
        "----------------------------------------------------------------------------------------------------------------\n"
            + "| PLUGIN INTERFACE                                                             | PLUGINS                       |\n"
            + "----------------------------------------------------------------------------------------------------------------\n"
            + "| techmoc.extensibility.pluginlibrary.test_objects.some_plugin_interfaces.Cat  |                               |\n"
            + "| techmoc.extensibility.pluginlibrary.test_objects.Dog                         | Husky                         |\n"
            + "|                                                                              | Poodle (version 1.2.345-TEST) |\n"
            + "|                                                                              | Poodle (version 2.0.0)        |\n"
            + "| techmoc.extensibility.pluginlibrary.test_objects.Fish                        |                               |\n"
            + "| techmoc.extensibility.pluginlibrary.test_objects.some_plugin_interfaces.Frog |                               |\n"
            + "----------------------------------------------------------------------------------------------------------------\n",
        pr.toRegistryState(true));

    // Register multiple Plugins under multiple Plugin Interfaces.
    pr.registerPlugin(Calico.class);
    pr.registerPlugin(Calico.class);
    pr.registerPlugin(CatFish.class);
    pr.registerPlugin(Bass.class);
    pr.registerPlugin(Salmon.class);

    assertEquals(
        "----------------------------------------------------\n"
            + "| PLUGIN INTERFACE | PLUGINS                       |\n"
            + "----------------------------------------------------\n"
            + "| Cat              | Calico                        |\n"
            + "|                  | CatFish (version 1.2.345)     |\n"
            + "| Dog              | Husky                         |\n"
            + "|                  | Poodle (version 1.2.345-TEST) |\n"
            + "|                  | Poodle (version 2.0.0)        |\n"
            + "| Fish             | Bass                          |\n"
            + "|                  | CatFish (version 1.2.345)     |\n"
            + "|                  | Salmon                        |\n"
            + "| Frog             |                               |\n"
            + "----------------------------------------------------\n",
        pr.toRegistryState());

    assertEquals(
        "----------------------------------------------------------------------------------------------------------------\n"
            + "| PLUGIN INTERFACE                                                             | PLUGINS                       |\n"
            + "----------------------------------------------------------------------------------------------------------------\n"
            + "| techmoc.extensibility.pluginlibrary.test_objects.some_plugin_interfaces.Cat  | Calico                        |\n"
            + "|                                                                              | CatFish (version 1.2.345)     |\n"
            + "| techmoc.extensibility.pluginlibrary.test_objects.Dog                         | Husky                         |\n"
            + "|                                                                              | Poodle (version 1.2.345-TEST) |\n"
            + "|                                                                              | Poodle (version 2.0.0)        |\n"
            + "| techmoc.extensibility.pluginlibrary.test_objects.Fish                        | Bass                          |\n"
            + "|                                                                              | CatFish (version 1.2.345)     |\n"
            + "|                                                                              | Salmon                        |\n"
            + "| techmoc.extensibility.pluginlibrary.test_objects.some_plugin_interfaces.Frog |                               |\n"
            + "----------------------------------------------------------------------------------------------------------------\n",
        pr.toRegistryState(true)
    );
  }

  @Test
  void testToStringAndToJsonMethods() {
    PluginRegistry pr = new PluginRegistry();

    // Test the empty registry toJson() result.
    assertEquals("[]", pr.toString());

    // Register the Plugin Interface.
    pr.registerPluginInterfaces(Dog.class);
    assertEquals(
        "[{ \"techmoc.extensibility.pluginlibrary.test_objects.Dog\": []}]",
        pr.toString());
    assertEquals(
        "[{ \"Dog\": []}]",
        pr.toJson(false, false));
    assertEquals(
        "[\n  { \"techmoc.extensibility.pluginlibrary.test_objects.Dog\": []}\n]",
        pr.toJson(true, true));
    assertEquals(
        "[\n  { \"Dog\": []}\n]",
        pr.toJson(false, true));

    // Register a plugin.
    pr.registerPlugin(Husky.class);
    assertEquals(
        "[{ \"techmoc.extensibility.pluginlibrary.test_objects.Dog\": [ \"Husky\" ]}]",
        pr.toString());

    // Register a second plugin.
    pr.registerPlugin(Poodle.class);
    assertEquals(
        "[{ \"techmoc.extensibility.pluginlibrary.test_objects.Dog\": [ \"Husky\", \"Poodle (version 1.2.345-TEST)\" ]}]",
        pr.toString());

    // Register a second version of a plugin.
    pr.registerPlugin(Poodle2.class);
    assertEquals(
        "[{ \"techmoc.extensibility.pluginlibrary.test_objects.Dog\": [ \"Husky\", \"Poodle (version 1.2.345-TEST)\", \"Poodle (version 2.0.0)\" ]}]",
        pr.toString());

    // Register multiple Plugin Interfaces.
    pr.registerPluginInterfaces(Cat.class, Fish.class, Frog.class);
    assertEquals(
        "[{ \"techmoc.extensibility.pluginlibrary.test_objects.some_plugin_interfaces.Cat\": []},{ \"techmoc.extensibility.pluginlibrary.test_objects.Dog\": [ \"Husky\", \"Poodle (version 1.2.345-TEST)\", \"Poodle (version 2.0.0)\" ]},{ \"techmoc.extensibility.pluginlibrary.test_objects.Fish\": []},{ \"techmoc.extensibility.pluginlibrary.test_objects.some_plugin_interfaces.Frog\": []}]",
        pr.toString());

    // Register multiple Plugin Interfaces.
    pr.registerPluginInterfaces(Cat.class, Fish.class, Frog.class);
    assertEquals(
        "[{ \"techmoc.extensibility.pluginlibrary.test_objects.some_plugin_interfaces.Cat\": []},{ \"techmoc.extensibility.pluginlibrary.test_objects.Dog\": [ \"Husky\", \"Poodle (version 1.2.345-TEST)\", \"Poodle (version 2.0.0)\" ]},{ \"techmoc.extensibility.pluginlibrary.test_objects.Fish\": []},{ \"techmoc.extensibility.pluginlibrary.test_objects.some_plugin_interfaces.Frog\": []}]",
        pr.toString());
    System.out.println(pr.toJson(true, true));
    System.out.println(pr.toString());

    // Register multiple Plugins under multiple Plugin Interfaces.
    pr.registerPlugin(Calico.class);
    pr.registerPlugin(Calico.class);
    pr.registerPlugin(CatFish.class);
    pr.registerPlugin(Bass.class);
    pr.registerPlugin(Salmon.class);
    assertEquals(
        "[{ \"techmoc.extensibility.pluginlibrary.test_objects.some_plugin_interfaces.Cat\": [ \"Calico\", \"CatFish (version 1.2.345)\" ]},{ \"techmoc.extensibility.pluginlibrary.test_objects.Dog\": [ \"Husky\", \"Poodle (version 1.2.345-TEST)\", \"Poodle (version 2.0.0)\" ]},{ \"techmoc.extensibility.pluginlibrary.test_objects.Fish\": [ \"Bass\", \"CatFish (version 1.2.345)\", \"Salmon\" ]},{ \"techmoc.extensibility.pluginlibrary.test_objects.some_plugin_interfaces.Frog\": []}]",
        pr.toString());
    assertEquals(
        "[\n"
            + "  { \"techmoc.extensibility.pluginlibrary.test_objects.some_plugin_interfaces.Cat\": [ \"Calico\", \"CatFish (version 1.2.345)\" ]},\n"
            + "  { \"techmoc.extensibility.pluginlibrary.test_objects.Dog\": [ \"Husky\", \"Poodle (version 1.2.345-TEST)\", \"Poodle (version 2.0.0)\" ]},\n"
            + "  { \"techmoc.extensibility.pluginlibrary.test_objects.Fish\": [ \"Bass\", \"CatFish (version 1.2.345)\", \"Salmon\" ]},\n"
            + "  { \"techmoc.extensibility.pluginlibrary.test_objects.some_plugin_interfaces.Frog\": []}\n"
            + "]",
        pr.toJson(true, true));
    assertEquals(
        "[\n"
            + "  { \"Cat\": [ \"Calico\", \"CatFish (version 1.2.345)\" ]},\n"
            + "  { \"Dog\": [ \"Husky\", \"Poodle (version 1.2.345-TEST)\", \"Poodle (version 2.0.0)\" ]},\n"
            + "  { \"Fish\": [ \"Bass\", \"CatFish (version 1.2.345)\", \"Salmon\" ]},\n"
            + "  { \"Frog\": []}\n"
            + "]",
        pr.toJson(false, true));
    assertEquals(
        "[{ \"Cat\": [ \"Calico\", \"CatFish (version 1.2.345)\" ]},{ \"Dog\": [ \"Husky\", \"Poodle (version 1.2.345-TEST)\", \"Poodle (version 2.0.0)\" ]},{ \"Fish\": [ \"Bass\", \"CatFish (version 1.2.345)\", \"Salmon\" ]},{ \"Frog\": []}]",
        pr.toJson(false, false));
  }

  @Test
  void testAttributeMethods() {
    PluginRegistry pr = new PluginRegistry();

    // Register plugins.
    pr.registerPluginInterfaces(Dog.class);
    pr.registerPlugin(Husky.class);
    pr.registerPlugin(Poodle.class);
    pr.registerPlugin(Poodle2.class);

    // Check that all attributes are empty.
    assertEquals(0, pr.getPluginAttributeCount("Husky", Dog.class));
    assertEquals(0, pr.getPluginAttributeCount("Poodle", 1, 2, 345, Dog.class));
    assertEquals(0, pr.getPluginAttributeCount("Poodle", 2, 0, 0, Dog.class));

    // Add attributes.
    assertNull(pr.putPluginAttribute("Husky", Dog.class, "IsFurry", true));
    assertNull(pr.putPluginAttribute("Husky", Dog.class, "TotalLegs", 4));
    assertNull(
        pr.putPluginAttribute("Poodle", new PluginVersion(1, 2, 345), Dog.class, "IsLarge", false));
    assertNull(pr.putPluginAttribute("Poodle", 1, 2, 345, Dog.class, "TotalTails", 1));
    assertNull(
        pr.putPluginAttribute("Poodle", new PluginVersion(2, 0, 0), Dog.class, "IsLarge", true));
    assertNull(pr.putPluginAttribute("Poodle", 2, 0, 0, Dog.class, "TotalTails", 0));
    assertNull(pr.putPluginAttribute("Poodle", 2, 0, 0, Dog.class, "WetNose", true));

    assertEquals(2, pr.getPluginAttributeCount("Husky", Dog.class));
    assertEquals(2, pr.getPluginAttributeCount("Poodle", 1, 2, 345, Dog.class));
    assertEquals(3, pr.getPluginAttributeCount("Poodle", 2, 0, 0, Dog.class));

    assertTrue(pr.pluginAttributeExists("Husky", Dog.class, "IsFurry"));
    assertTrue(pr.pluginAttributeExists("Husky", Dog.class, "TotalLegs"));
    assertFalse(pr.pluginAttributeExists("Husky", Dog.class, "IsRed"));
    assertTrue(pr.pluginAttributeExists("Poodle", 1, 2, 345, Dog.class, "IsLarge"));
    assertTrue(pr.pluginAttributeExists("Poodle", 1, 2, 345, Dog.class, "TotalTails"));
    assertFalse(pr.pluginAttributeExists("Poodle", 1, 2, 345, Dog.class, "WetNose"));
    assertTrue(pr.pluginAttributeExists("Poodle", 2, 0, 0, Dog.class, "IsLarge"));
    assertTrue(pr.pluginAttributeExists("Poodle", 2, 0, 0, Dog.class, "TotalTails"));
    assertTrue(pr.pluginAttributeExists("Poodle", 2, 0, 0, Dog.class, "WetNose"));
    assertFalse(pr.pluginAttributeExists("Poodle", 2, 0, 0, Dog.class, "IsRed"));

    assertEquals(
        Set.of("IsFurry", "TotalLegs"), pr.getPluginAttributeNames("Husky", Dog.class));
    assertEquals(
        Set.of("IsLarge", "TotalTails"),
        pr.getPluginAttributeNames("Poodle", 1, 2, 345, Dog.class));
    assertEquals(
        Set.of("IsLarge", "TotalTails", "WetNose"),
        pr.getPluginAttributeNames("Poodle", 2, 0, 0, Dog.class));

    assertEquals(Boolean.class,
        pr.getPluginAttributeType("Husky", Dog.class, "IsFurry"));
    assertEquals(Integer.class,
        pr.getPluginAttributeType("Husky", Dog.class, "TotalLegs"));
    assertEquals(Boolean.class,
        pr.getPluginAttributeType("Poodle", 1, 2, 345, Dog.class, "IsLarge"));
    assertEquals(
        Integer.class,
        pr.getPluginAttributeType("Poodle", 1, 2, 345, Dog.class, "TotalTails"));
    assertEquals(Boolean.class,
        pr.getPluginAttributeType("Poodle", 2, 0, 0, Dog.class, "IsLarge"));
    assertEquals(Integer.class,
        pr.getPluginAttributeType("Poodle", 2, 0, 0, Dog.class, "TotalTails"));
    assertEquals(Boolean.class,
        pr.getPluginAttributeType("Poodle", 2, 0, 0, Dog.class, "WetNose"));

    assertEquals(true,
        pr.getPluginAttribute("Husky", Dog.class, "IsFurry", Boolean.class));
    assertEquals(4,
        pr.getPluginAttribute("Husky", Dog.class, "TotalLegs", Integer.class));
    assertEquals(false,
        pr.getPluginAttribute("Poodle", 1, 2, 345, Dog.class, "IsLarge", Boolean.class));
    assertEquals(1,
        pr.getPluginAttribute("Poodle", 1, 2, 345, Dog.class, "TotalTails", Integer.class));
    assertEquals(true,
        pr.getPluginAttribute("Poodle", 2, 0, 0, Dog.class, "IsLarge", Boolean.class));
    assertEquals(0,
        pr.getPluginAttribute("Poodle", 2, 0, 0, Dog.class, "TotalTails", Integer.class));
    assertEquals(true,
        pr.getPluginAttribute("Poodle", 2, 0, 0, Dog.class, "WetNose", Boolean.class));

    assertEquals(true,
        pr.getExactPluginAttribute("Husky", Dog.class, "IsFurry", Boolean.class));
    assertEquals(4,
        pr.getExactPluginAttribute("Husky", Dog.class, "TotalLegs", Integer.class));
    assertEquals(false,
        pr.getExactPluginAttribute("Poodle", 1, 2, 345, Dog.class, "IsLarge", Boolean.class));
    assertEquals(1,
        pr.getExactPluginAttribute("Poodle", 1, 2, 345, Dog.class, "TotalTails", Integer.class));
    assertEquals(true,
        pr.getExactPluginAttribute("Poodle", 2, 0, 0, Dog.class, "IsLarge", Boolean.class));
    assertEquals(0,
        pr.getExactPluginAttribute("Poodle", 2, 0, 0, Dog.class, "TotalTails", Integer.class));
    assertEquals(true,
        pr.getExactPluginAttribute("Poodle", 2, 0, 0, Dog.class, "WetNose", Boolean.class));

    // Check attribute overwrite.
    assertEquals(true,
        pr.putPluginAttribute("Husky", Dog.class, "IsFurry", false));
    assertEquals(false,
        pr.getPluginAttribute("Husky", Dog.class, "IsFurry", Boolean.class));

    assertEquals(false,
        pr.putPluginAttribute("Husky", Dog.class, "IsFurry", "Yes", Boolean.class));
    assertEquals("Yes",
        pr.getPluginAttribute("Husky", Dog.class, "IsFurry", String.class));

    assertEquals(false,
        pr.putPluginAttribute(
            "Poodle",
            new PluginVersion(1, 2, 345),
            Dog.class,
            "IsLarge", true));
    assertEquals(true,
        pr.putPluginAttribute(
            "Poodle",
            1, 2, 345,
            Dog.class,
            "IsLarge", false, Boolean.class));
  }

  @Test
  void testGetLatestVersionMethods() {
    PluginRegistry pr = new PluginRegistry();

    // Register plugins.
    pr.registerPluginInterfaces(Cat.class, Dog.class);

    // Empty set returned.
    assertEquals(List.of(), pr.getLatestVersions(Cat.class));

    // Register only dog plugins (just to add noise).
    pr.registerPlugin(Husky.class);
    pr.registerPlugin(Poodle.class);
    pr.registerPlugin(Poodle2.class);
    pr.registerPlugin(Poodle201.class);
    pr.registerPlugin(Poodle210.class);

    // Empty set returned.
    assertEquals(List.of(), pr.getLatestVersions(Cat.class));

    // Add the Calico plugin, and check that it is returned.
    pr.registerPlugin(Calico.class);
    List<Cat> cats = pr.getLatestVersions(Cat.class);
    assertEquals(1, cats.size());
    assertEquals(Calico.class, cats.stream().findFirst().get().getClass());
    Cat c = pr.getLatestVersion("Calico", Cat.class);
    assertEquals(Calico.class, c.getClass());

    // Calico and Tabby returned.
    pr.registerPlugin(Tabby.class);
    cats = pr.getLatestVersions(Cat.class);
    assertEquals(2, cats.size());
    assertTrue(cats.stream().map(Cat::getClass).anyMatch(x -> x.equals(Calico.class)));
    assertTrue(cats.stream().map(Cat::getClass).anyMatch(x -> x.equals(Tabby.class)));
    for (Cat cat : cats) {
      assertEquals(new PluginVersion(0, 0, 0), cat.getPluginVersion());
    }

    c = pr.getLatestVersion("Calico", Cat.class);
    assertEquals("Calico", c.getPluginName());
    assertEquals(new PluginVersion(0, 0, 0), c.getPluginVersion());

    c = pr.getLatestVersion("Tabby", Cat.class);
    assertEquals("Tabby", c.getPluginName());
    assertEquals(new PluginVersion(0, 0, 0), c.getPluginVersion());

    // Add more versions of Calico and Tabby, and check for the latest versions of Calico and Tabby returned.
    pr.registerPlugin(Calico2.class);
    pr.registerPlugin(Tabby2.class);
    cats = pr.getLatestVersions(Cat.class);
    assertEquals(2, cats.size());
    for (Cat cat : cats) {
      assertEquals(new PluginVersion(2, 0, 0), cat.getPluginVersion());
    }

    c = pr.getLatestVersion("Calico", Cat.class);
    assertEquals("Calico", c.getPluginName());
    assertEquals(new PluginVersion(2, 0, 0), c.getPluginVersion());

    c = pr.getLatestVersion("Tabby", Cat.class);
    assertEquals("Tabby", c.getPluginName());
    assertEquals(new PluginVersion(2, 0, 0), c.getPluginVersion());

    // Add another version of Calico, and check for latest versions of Calico and Tabby returned.
    pr.registerPlugin(Calico3.class);
    cats = pr.getLatestVersions(Cat.class);
    assertEquals(2, cats.size());
    for (Cat cat : cats) {
      if (cat.getPluginName().equals("Calico")) {
        assertEquals(new PluginVersion(3, 0, 0), cat.getPluginVersion());
      } else {
        assertEquals(new PluginVersion(2, 0, 0), cat.getPluginVersion());
      }
    }

    c = pr.getLatestVersion("Calico", Cat.class);
    assertEquals("Calico", c.getPluginName());
    assertEquals(new PluginVersion(3, 0, 0), c.getPluginVersion());

    c = pr.getLatestVersion("Tabby", Cat.class);
    assertEquals("Tabby", c.getPluginName());
    assertEquals(new PluginVersion(2, 0, 0), c.getPluginVersion());
  }

  @Test
  void testGetByAttributeMethods() {
    PluginRegistry pr = new PluginRegistry();

    // Register plugins.
    pr.registerPluginInterfaces(Cat.class, Dog.class);
    pr.registerPlugin(Calico.class);
    pr.registerPlugin(Calico2.class);
    pr.registerPlugin(Calico3.class);
    pr.registerPlugin(Tabby.class);
    pr.registerPlugin(Tabby2.class);

    // Test the getLatestVersionByAttribute(Predicate<> attributesTest, registeredPluginInterface) method.
    Cat calico = pr.getLatestVersionByAttribute(
        (polyMap) -> polyMap.containsKeyOfType("HasPuffyTail", Boolean.class) &&
            !polyMap.get("HasPuffyTail", Boolean.class),
        "Calico", Cat.class);
    assertEquals(new PluginVersion(2, 0, 0), calico.getPluginVersion());

    calico = pr.getLatestVersionByAttribute(
        (polyMap) -> polyMap.containsKey("NonExistentAttribute"), "Calico", Cat.class);
    assertNull(calico);

    // Test the getLatestVersionByAttribute(BiPredicate<> biPredicate, registeredPluginInterface) method.
    calico = pr.getLatestVersionByAttribute(
        (polyMap, pluginVersion) ->
            !polyMap.get("HasPuffyTail", Boolean.class) &&
                pluginVersion.compareTo(new PluginVersion(1, 0, 0)) < 0,
        "Calico", Cat.class);
    assertEquals(PluginVersion.DEFAULT, calico.getPluginVersion());

    calico = pr.getLatestVersionByAttribute(
        (polyMap, pluginVersion) -> pluginVersion.compareTo(new PluginVersion(5, 0, 0)) > 0,
        "Calico", Cat.class);
    assertNull(calico);

    // Test the getLatestVersionsByAttribute(Predicate<> attributesTest, registeredPluginInterface) method.
    List<Cat> cats = pr.getLatestVersionsByAttribute(
        (polyMap) -> polyMap.containsKeyOfType("HasPuffyTail", Boolean.class) &&
            !polyMap.get("HasPuffyTail", Boolean.class),
        Cat.class);
    assertEquals(1, cats.size());
    cats.forEach((cat) ->
        assertTrue(
            cat.getPluginVersion().equals(PluginVersion.DEFAULT) ||
                cat.getPluginVersion().equals(new PluginVersion(2, 0, 0))));

    cats = pr.getLatestVersionsByAttribute(
        (polyMap) -> polyMap.containsKeyOfType("NonExistentAttribute", Boolean.class), Cat.class);
    assertTrue(cats.isEmpty());

    // Test the getLatestVersionsByAttribute(BiPredicate<> biPredicate, registeredPluginInterface) method.
    cats = pr.getLatestVersionsByAttribute(
        (polyMap, pluginVersion) -> polyMap.containsKeyOfType("HasPuffyTail", Boolean.class) &&
            !polyMap.get("HasPuffyTail", Boolean.class) &&
            pluginVersion.compareTo(new PluginVersion(1, 0, 0)) < 0,
        Cat.class);
    assertEquals(1, cats.size());
    cats.forEach((cat) ->
        assertTrue(
            cat.getPluginVersion().equals(PluginVersion.DEFAULT) ||
                cat.getPluginVersion().equals(new PluginVersion(2, 0, 0))));

    cats = pr.getLatestVersionsByAttribute(
        (polyMap, pluginVersion) -> pluginVersion.compareTo(new PluginVersion(5, 0, 0)) > 0,
        Cat.class);
    assertTrue(cats.isEmpty());

    // Test the getByAttribute(Predicate<> attributesTest, registeredPluginInterface) method.
    cats = pr.getByAttribute(
        (polyMap) -> polyMap.containsKeyOfExactType("MeowVolume", Integer.class) &&
            polyMap.get("MeowVolume", Integer.class) <= 5,
        Cat.class);
    assertEquals(3, cats.size());
    cats.stream().map(c -> c.getClass()).forEach(
        c -> assertTrue(Set.of(Calico.class, Calico2.class, Tabby2.class).contains(c)));

    cats = pr.getByAttribute((polyMap) -> polyMap.containsKey("NonExistentKey"), Cat.class);
    assertTrue(cats.isEmpty());

    // Test the getByAttribute(BiPredicate<> biPredicate, registeredPluginInterface) method.
    cats = pr.getByAttribute(
        (polyMap, pluginVersion) ->
            polyMap.containsKeyOfExactType("MeowVolume", Integer.class) &&
                pluginVersion.equals(PluginVersion.DEFAULT),
        Cat.class);
    assertEquals(2, cats.size());
    cats.stream().map(c -> c.getClass()).forEach(
        c -> assertTrue(Set.of(Calico.class, Tabby.class).contains(c)));

    cats = pr.getByAttribute(
        (polyMap, pluginVersion) -> pluginVersion.equals(new PluginVersion(5, 0, 0)),
        Cat.class);
    assertTrue(cats.isEmpty());

    // Test the getByAttribute(Predicate<> attributesTest, pluginName, registeredPluginInterface) method.
    cats = pr.getByAttribute(
        (polyMap) -> polyMap.containsKeyOfExactType("MeowVolume", Integer.class) &&
            polyMap.get("MeowVolume", Integer.class) <= 5,
        "Calico", Cat.class);
    assertEquals(2, cats.size());
    cats.stream().map(c -> c.getClass()).forEach(
        c -> assertTrue(Set.of(Calico.class, Calico2.class).contains(c)));

    cats = pr.getByAttribute(
        (polyMap) -> polyMap.containsKey("NonExistentKey"), "Calico", Cat.class);
    assertTrue(cats.isEmpty());

    // Test the getByAttribute(BiPredicate<> biPredicate, registeredPluginInterface) method.
    cats = pr.getByAttribute(
        (polyMap, pluginVersion) ->
            polyMap.containsKeyOfExactType("MeowVolume", Integer.class) &&
                pluginVersion.equals(PluginVersion.DEFAULT),
        "Calico",
        Cat.class);
    assertEquals(1, cats.size());
    cats.stream().map(c -> c.getClass()).forEach(c -> assertEquals(Calico.class, c));

    cats = pr.getByAttribute(
        (polyMap, pluginVersion) -> pluginVersion.equals(new PluginVersion(5, 0, 0)),
        "Calico",
        Cat.class);
    assertTrue(cats.isEmpty());
  }

  @Test
  void testScanJarMethod_NestedPackages() throws IOException {
    PluginRegistry pr = new PluginRegistry();

    // Get the path to the 'libs/plugin-library-*.jar' package directory.
    ClassLoader classLoader = getClass().getClassLoader();
    File jarFile = new File(classLoader.getResource("test-plugins-nested-packages.jar").getFile());

    // Check initial plugin registry state.
    pr.registerPluginInterfaces(Bird.class);
    assertEquals(0, pr.count(Bird.class));

    // Scan for test_plugins.
    pr.scanJar(jarFile.getAbsolutePath(), Bird.class);

    // Verify that the correct number of plugins were loaded.
    assertEquals(5, pr.count(Bird.class));
    assertTrue(pr.isRegisteredPlugin("GreyBird", Bird.class));
    assertTrue(pr.isRegisteredPlugin("BlueBird", Bird.class));
    assertTrue(pr.isRegisteredPlugin("GreenBird", Bird.class));
    assertTrue(pr.isRegisteredPlugin("RedBird", Bird.class));
    assertTrue(pr.isRegisteredPlugin("PinkBird", Bird.class));
  }

  @Test
  void testEncryptDecryptApi(@TempDir Path tempDir) throws IOException {
    PluginRegistry pr = new PluginRegistry();

    // Determine the path to the tmp directory.
    File tmpDir = tempDir.resolve("techmoc").toFile();

    // Get the path to the 'libs/plugin-library-*.jar' package directory.
    ClassLoader classLoader = getClass().getClassLoader();
    File jarFile = new File(classLoader.getResource("test-plugins.jar").getFile());

    // Copy the test-plugins.jar file into the temp directory.
    FileUtils.copyFileToDirectory(jarFile, tmpDir);
    String testPluginsJarPath = tmpDir.getAbsolutePath() + "/test-plugins.jar";
    assertTrue(Files.exists(Path.of(testPluginsJarPath)));

    // Generate a key pair, to represent the encryption keys used by the receiver.
    PluginKeyPair keyPair = new PluginKeyPair();

    // Create the encrypted package.
    pr.createEncryptedPackage(testPluginsJarPath, keyPair.getPublicKey());
    String encryptedPluginsPath = testPluginsJarPath
        .replace("test-plugins.jar", "test-plugins.plugins");
    assertTrue(Files.exists(Path.of(encryptedPluginsPath)));

    // Scan the encrypted package.
    pr.registerPluginInterfaces(Bird.class);
    assertEquals(0, pr.count(Bird.class));
    pr.scanEncryptedPackage(encryptedPluginsPath, keyPair.getPrivateKey());
    assertEquals(2, pr.count(Bird.class));
  }

  @Test
  void testEncryptDecryptApi_SpecifyOutputDirectory(@TempDir Path tempDir, @TempDir Path tempDir2)
      throws IOException {
    PluginRegistry pr = new PluginRegistry();

    // Determine the path to the tmp directory.
    File tmpDir = tempDir.resolve("techmoc").toFile();
    String outputDirPath = tempDir2.resolve("techmoc").toFile().getPath();

    // Get the path to the 'libs/plugin-library-*.jar' package directory.
    ClassLoader classLoader = getClass().getClassLoader();
    File jarFile = new File(classLoader.getResource("test-plugins.jar").getFile());

    // Copy the test-plugins.jar file into the temp directory.
    FileUtils.copyFileToDirectory(jarFile, tmpDir);
    String testPluginsJarPath = tmpDir.getAbsolutePath() + "/test-plugins.jar";
    assertTrue(Files.exists(Path.of(testPluginsJarPath)));

    // Generate a key pair, to represent the encryption keys used by the receiver.
    PluginKeyPair keyPair = new PluginKeyPair();

    // Create the encrypted package.
    pr.createEncryptedPackage(testPluginsJarPath, keyPair.getPublicKey(), outputDirPath);
    String encryptedPluginsPath = outputDirPath + "/test-plugins.plugins";
    assertTrue(Files.exists(Path.of(encryptedPluginsPath)));

    // Scan the encrypted package.
    pr.registerPluginInterfaces(Bird.class);
    assertEquals(0, pr.count(Bird.class));
    pr.scanEncryptedPackage(encryptedPluginsPath, keyPair.getPrivateKey(), Bird.class);
    assertEquals(2, pr.count(Bird.class));
  }

  @Test
  void testScanPluginMultipleTimes() {
    PluginRegistry pr = new PluginRegistry();
    pr.registerPluginInterfaces(Dog.class);

    assertFalse(pr.isRegisteredPlugin("Husky"));
    assertFalse(pr.isRegisteredPlugin("Poodle"));
    assertEquals(0, pr.count());

    pr.registerPlugin(Husky.class, Dog.class);
    assertTrue(pr.isRegisteredPlugin("Husky"));
    assertEquals(1, pr.count());

    pr.registerPlugin(Husky.class, Dog.class);
    assertTrue(pr.isRegisteredPlugin("Husky"));
    assertEquals(1, pr.count());

    pr.registerPlugin(Poodle.class, Dog.class);
    assertTrue(pr.isRegisteredPlugin("Husky"));
    assertTrue(pr.isRegisteredPlugin("Poodle"));
    assertEquals(2, pr.count());

    pr.registerPlugin(Husky.class, Dog.class);
    assertTrue(pr.isRegisteredPlugin("Husky"));
    assertTrue(pr.isRegisteredPlugin("Poodle"));
    assertEquals(2, pr.count());

    pr.registerPlugin(Poodle.class, Dog.class);
    assertTrue(pr.isRegisteredPlugin("Husky"));
    assertTrue(pr.isRegisteredPlugin("Poodle"));
    assertEquals(2, pr.count());
  }

  @Test
  void testScanResults_PackageDirectory() throws IOException {
    // Test successful plugin registration.
    PluginRegistry pr = new PluginRegistry();
    pr.registerPluginInterfaces(Dog.class);
    ScanLog scanLog = pr.registerPlugin(Husky.class, Dog.class);
    assertTrue(scanLog.isFile());
    assertTrue(scanLog.registeredSuccessfully());
    assertEquals(
        "techmoc.extensibility.pluginlibrary.test_objects.Husky",
        scanLog.getFullyQualifiedName());
    assertTrue(scanLog.getPath().endsWith(
        "/techmoc/extensibility/pluginlibrary/test_objects/Husky.class"));
    assertEquals("", scanLog.getReason());

    // Test scan results.
    pr = new PluginRegistry();
    pr.registerPluginInterfaces(Dog.class);
    ScanResults scanResults = pr
        .scan(Set.of("techmoc.extensibility.pluginlibrary.test_objects"), true);
    scanResults.getDirectoryLogs().forEach(System.out::println);
    assertEquals(6, scanResults.getTotalDirectoriesScanned());
    assertEquals(30, scanResults.getTotalFilesScanned());
    assertEquals(25, scanResults.getTotalFilesIgnored()); // Non-plugins and non-Dog plugins.
    assertEquals(5, scanResults.getTotalPluginsRegistered()); // Dog plugins only!

    pr.printRegistryState();

    scanResults = pr.scan(Set.of("techmoc.extensibility.pluginlibrary.test_objects"), true);
    assertEquals(6, scanResults.getTotalDirectoriesScanned());
    assertEquals(30, scanResults.getTotalFilesScanned());
    assertEquals(30, scanResults.getTotalFilesIgnored());
    assertEquals(0, scanResults.getTotalPluginsRegistered());

    pr.printRegistryState();
  }

  @Test
  void testScanResults_Jar() throws IOException {
    // Get the path to the 'libs/plugin-library-*.jar' package directory.
    ClassLoader classLoader = getClass().getClassLoader();
    File jarFile = new File(classLoader.getResource("test-plugins.jar").getFile());

    // Test scan results.
    PluginRegistry pr = new PluginRegistry();
    pr.registerPluginInterfaces(Bird.class);
    ScanResults scanResults = pr.scanJar(jarFile.getAbsolutePath());
    assertEquals(3,
        scanResults.getTotalDirectoriesScanned()); // Package root directory isn't counted.
    assertEquals(2,
        scanResults.getTotalFilesScanned()); // Files from main and test (but no resource files).
    assertEquals(0, scanResults.getTotalFilesIgnored()); // Non-plugins and non-Dog plugins.
    assertEquals(2, scanResults.getTotalPluginsRegistered()); // Dog plugins only!

    pr.printRegistryState();

    scanResults = pr.scanJar(jarFile.getAbsolutePath());
    assertEquals(3, scanResults.getTotalDirectoriesScanned());
    assertEquals(2, scanResults.getTotalFilesScanned());
    assertEquals(2, scanResults.getTotalFilesIgnored());
    assertEquals(0, scanResults.getTotalPluginsRegistered());

    pr.printRegistryState();
  }

  @Test
  void testOrderingOfKeys() {
    PluginRegistry pr = new PluginRegistry();
    pr.registerPluginInterfaces(Frog.class, Cat.class, Fish.class, Dog.class);
    List<Class<? extends Pluggable>> keys = pr.getRegisteredInterfaces();
    assertEquals(Cat.class, keys.get(0));
    assertEquals(Dog.class, keys.get(1));
    assertEquals(Fish.class, keys.get(2));
    assertEquals(Frog.class, keys.get(3));
  }
}
