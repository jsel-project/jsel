package techmoc.extensibility.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import techmoc.extensibility.interfaces.Fly;
import techmoc.extensibility.pluginlibrary.PluginRegistry;
import techmoc.extensibility.test_interfaces.Bird;


/**
 * Unit tests.
 */
public class Tests {

  @Test
  void testScanMethod_NestedPackages_Jar() throws IOException {
    PluginRegistry pr = MainTests.testScanLocalJar();

    // Verify that the correct number of plugins were loaded.
    assertEquals(10, pr.count(Fly.class));
    assertTrue(pr.isRegisteredPlugin("BumbleBee", Fly.class));
    assertTrue(pr.isRegisteredPlugin("GreyFly", Fly.class));
    assertTrue(pr.isRegisteredPlugin("HorseFly", Fly.class));
    assertTrue(pr.isRegisteredPlugin("BlueFly", Fly.class));
    assertTrue(pr.isRegisteredPlugin("TealFly", Fly.class));
    assertTrue(pr.isRegisteredPlugin("GreenFly", Fly.class));
    assertTrue(pr.isRegisteredPlugin("RedFly", Fly.class));
    assertTrue(pr.isRegisteredPlugin("PinkFly", Fly.class));
    assertTrue(pr.isRegisteredPlugin("RoseFly", Fly.class));
    assertTrue(pr.isRegisteredPlugin("ThornFly", Fly.class));

    // Run the plugins.
    List<Fly> flies = pr.getAll(Fly.class);
    for (Fly fly : flies) {
      assertEquals(fly.getClass().getSimpleName(), fly.buzz());
    }
  }

  @Test
  void testScanMethod_NestedPackages_PackageDirectory() throws IOException {
    PluginRegistry pr = new PluginRegistry();

    // Check initial plugin registry state.
    pr.registerPluginInterfaces(Bird.class);
    assertEquals(0, pr.count(Bird.class));

    // Scan for test_plugins.
    pr.scan(Bird.class);

    // Verify that the correct number of plugins were loaded.
    assertEquals(10, pr.count(Bird.class));
    assertTrue(pr.isRegisteredPlugin("Dove", Bird.class));
    assertTrue(pr.isRegisteredPlugin("GreyBird", Bird.class));
    assertTrue(pr.isRegisteredPlugin("Raven", Bird.class));
    assertTrue(pr.isRegisteredPlugin("BlueBird", Bird.class));
    assertTrue(pr.isRegisteredPlugin("TealBird", Bird.class));
    assertTrue(pr.isRegisteredPlugin("GreenBird", Bird.class));
    assertTrue(pr.isRegisteredPlugin("RedBird", Bird.class));
    assertTrue(pr.isRegisteredPlugin("PinkBird", Bird.class));
    assertTrue(pr.isRegisteredPlugin("RoseBird", Bird.class));
    assertTrue(pr.isRegisteredPlugin("ThornBird", Bird.class));

    // Run the plugins.
    List<Bird> birds = pr.getAll(Bird.class);
    for (Bird bird : birds) {
      assertEquals(bird.getClass().getSimpleName(), bird.chirp());
    }
  }

  @Test
  void testScanMethod_PackageFiltering_Jar() throws IOException {
    // Throws when passed an empty set of target packages.
    assertThrows(IllegalArgumentException.class,
        () -> MainTests.testScanWithFilters(Set.of(), false));

    //--------------------------------------------------------------------------------
    // Test 1 - Single package filter, scan subpackages.
    //--------------------------------------------------------------------------------
    PluginRegistry pr = MainTests.testScanWithFilters(
        Set.of("techmoc.extensibility.plugins.red"), true);
    pr.printRegistryState();

    // Verify that the correct number of plugins were loaded.
    assertEquals(4, pr.count(Fly.class));
    assertTrue(pr.isRegisteredPlugin("RedFly", Fly.class));
    assertTrue(pr.isRegisteredPlugin("PinkFly", Fly.class));
    assertTrue(pr.isRegisteredPlugin("RoseFly", Fly.class));
    assertTrue(pr.isRegisteredPlugin("ThornFly", Fly.class));

    // Run the plugins.
    List<Fly> flies = pr.getAll(Fly.class);
    for (Fly fly : flies) {
      assertEquals(fly.getClass().getSimpleName(), fly.buzz());
    }

    //--------------------------------------------------------------------------------
    // Test 2 - Multiple package filter, scan subpackages.
    //--------------------------------------------------------------------------------
    pr = MainTests.testScanWithFilters(
        Set.of("techmoc.extensibility.plugins.red", "techmoc.extensibility.plugins.blue"),
        true);
    pr.printRegistryState();

    // Verify that the correct number of plugins were loaded.
    assertEquals(6, pr.count(Fly.class));
    assertTrue(pr.isRegisteredPlugin("BlueFly", Fly.class));
    assertTrue(pr.isRegisteredPlugin("TealFly", Fly.class));
    assertTrue(pr.isRegisteredPlugin("RedFly", Fly.class));
    assertTrue(pr.isRegisteredPlugin("PinkFly", Fly.class));
    assertTrue(pr.isRegisteredPlugin("RoseFly", Fly.class));
    assertTrue(pr.isRegisteredPlugin("ThornFly", Fly.class));

    // Run the plugins.
    flies = pr.getAll(Fly.class);
    for (Fly fly : flies) {
      assertEquals(fly.getClass().getSimpleName(), fly.buzz());
    }

    //--------------------------------------------------------------------------------
    // Test 3 - Single package filter, don't scan subpackages.
    //--------------------------------------------------------------------------------
    pr = MainTests.testScanWithFilters(
        Set.of("techmoc.extensibility.plugins.red"), false);
    pr.printRegistryState();

    // Verify that the correct number of plugins were loaded.
    assertEquals(1, pr.count(Fly.class));
    assertTrue(pr.isRegisteredPlugin("RedFly", Fly.class));

    // Run the plugins.
    flies = pr.getAll(Fly.class);
    for (Fly fly : flies) {
      assertEquals(fly.getClass().getSimpleName(), fly.buzz());
    }

    //--------------------------------------------------------------------------------
    // Test 4 - Multiple package filter, don't scan subpackages.
    //--------------------------------------------------------------------------------
    pr = MainTests.testScanWithFilters(
        Set.of("techmoc.extensibility.plugins.red", "techmoc.extensibility.plugins.blue"),
        false);
    pr.printRegistryState();

    // Verify that the correct number of plugins were loaded.
    assertEquals(2, pr.count(Fly.class));
    assertTrue(pr.isRegisteredPlugin("BlueFly", Fly.class));
    assertTrue(pr.isRegisteredPlugin("RedFly", Fly.class));

    // Run the plugins.
    flies = pr.getAll(Fly.class);
    for (Fly fly : flies) {
      assertEquals(fly.getClass().getSimpleName(), fly.buzz());
    }
  }

  @Test
  void testScanMethod_PackageFiltering_PackageDirectory() throws IOException {
    PluginRegistry pr0 = new PluginRegistry();
    pr0.registerPluginInterfaces(Bird.class);

    // Throws when passed an empty set of target packages.
    assertThrows(IllegalArgumentException.class, () -> pr0.scan(Set.of(), false));

    //--------------------------------------------------------------------------------
    // Test 1 - Single package filter, scan subpackages.
    //--------------------------------------------------------------------------------
    PluginRegistry pr = new PluginRegistry();
    pr.registerPluginInterfaces(Bird.class);
    pr.scan(Set.of("techmoc.extensibility.test_plugins.red"), true);

    // Verify that the correct number of plugins were loaded.
    assertEquals(4, pr.count(Bird.class));
    assertTrue(pr.isRegisteredPlugin("RedBird", Bird.class));
    assertTrue(pr.isRegisteredPlugin("PinkBird", Bird.class));
    assertTrue(pr.isRegisteredPlugin("RoseBird", Bird.class));
    assertTrue(pr.isRegisteredPlugin("ThornBird", Bird.class));

    // Run the plugins.
    List<Bird> birds = pr.getAll(Bird.class);
    for (Bird bird : birds) {
      assertEquals(bird.getClass().getSimpleName(), bird.chirp());
    }

    //--------------------------------------------------------------------------------
    // Test 2 - Multiple package filter, scan subpackages.
    //--------------------------------------------------------------------------------
    pr = new PluginRegistry();
    pr.registerPluginInterfaces(Bird.class);
    pr.scan(
        Set.of("techmoc.extensibility.test_plugins.red", "techmoc.extensibility.test_plugins.blue"),
        true);
    pr.printRegistryState();

    // Verify that the correct number of plugins were loaded.
    assertEquals(6, pr.count(Bird.class));
    assertTrue(pr.isRegisteredPlugin("BlueBird", Bird.class));
    assertTrue(pr.isRegisteredPlugin("TealBird", Bird.class));
    assertTrue(pr.isRegisteredPlugin("RedBird", Bird.class));
    assertTrue(pr.isRegisteredPlugin("PinkBird", Bird.class));
    assertTrue(pr.isRegisteredPlugin("RoseBird", Bird.class));
    assertTrue(pr.isRegisteredPlugin("ThornBird", Bird.class));

    // Run the plugins.
    birds = pr.getAll(Bird.class);
    for (Bird bird : birds) {
      assertEquals(bird.getClass().getSimpleName(), bird.chirp());
    }

    //--------------------------------------------------------------------------------
    // Test 3 - Single package filter, don't scan subpackages.
    //--------------------------------------------------------------------------------
    pr = new PluginRegistry();
    pr.registerPluginInterfaces(Bird.class);
    pr.scan(Set.of("techmoc.extensibility.test_plugins.red"), false);
    pr.printRegistryState();

    // Verify that the correct number of plugins were loaded.
    assertEquals(1, pr.count(Bird.class));
    assertTrue(pr.isRegisteredPlugin("RedBird", Bird.class));

    // Run the plugins.
    birds = pr.getAll(Bird.class);
    for (Bird bird : birds) {
      assertEquals(bird.getClass().getSimpleName(), bird.chirp());
    }

    //--------------------------------------------------------------------------------
    // Test 4 - Multiple package filter, don't scan subpackages.
    //--------------------------------------------------------------------------------
    pr = new PluginRegistry();
    pr.registerPluginInterfaces(Bird.class);
    pr.scan(
        Set.of("techmoc.extensibility.test_plugins.red", "techmoc.extensibility.test_plugins.blue"),
        false);
    pr.printRegistryState();

    // Verify that the correct number of plugins were loaded.
    assertEquals(2, pr.count(Bird.class));
    assertTrue(pr.isRegisteredPlugin("BlueBird", Bird.class));
    assertTrue(pr.isRegisteredPlugin("RedBird", Bird.class));

    // Run the plugins.
    birds = pr.getAll(Bird.class);
    for (Bird bird : birds) {
      assertEquals(bird.getClass().getSimpleName(), bird.chirp());
    }
  }
}
