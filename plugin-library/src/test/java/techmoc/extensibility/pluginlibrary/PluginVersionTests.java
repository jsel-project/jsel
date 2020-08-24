package techmoc.extensibility.pluginlibrary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;


class PluginVersionTests {

  @Test
  void testIllegalArgumentExceptions() {
    assertThrows(IllegalArgumentException.class, () -> new PluginVersion(-1));
    assertThrows(IllegalArgumentException.class, () -> new PluginVersion(-1, 1));
    assertThrows(IllegalArgumentException.class, () -> new PluginVersion(1, -1));
    assertThrows(IllegalArgumentException.class, () -> new PluginVersion(-1, 1, "SHAPSHOT"));
    assertThrows(IllegalArgumentException.class, () -> new PluginVersion(1, -1, "SHAPSHOT"));
    assertThrows(IllegalArgumentException.class, () -> new PluginVersion(-1, 1, 1));
    assertThrows(IllegalArgumentException.class, () -> new PluginVersion(1, -1, 1));
    assertThrows(IllegalArgumentException.class, () -> new PluginVersion(1, 1, -1));
    assertThrows(IllegalArgumentException.class, () -> new PluginVersion(-1, 1, 1, "SNAPSHOT"));
    assertThrows(IllegalArgumentException.class, () -> new PluginVersion(1, -1, 1, "SNAPSHOT"));
    assertThrows(IllegalArgumentException.class, () -> new PluginVersion(1, 1, -1, "SNAPSHOT"));

    // Plugin equality tests.
    PluginVersion pv = new PluginVersion(1, 1, 100, "SNAPSHOT");
    assertEquals("1.1.100-SNAPSHOT", pv.toString());
    pv = new PluginVersion(1, 1, 100);
    assertEquals("1.1.100", pv.toString());
    pv = new PluginVersion(1, 1, 1, "SNAPSHOT");
    assertEquals("SNAPSHOT", pv.getRevisionTag());
    assertEquals("1.1.1-SNAPSHOT", pv.toString());

    // Sorting / comparison tests.
    PluginVersion[] pvArray = new PluginVersion[]{
        new PluginVersion(1, 0, 0),
        new PluginVersion(2, 0, 0),
        new PluginVersion(3, 0, 0)};
    Arrays.sort(pvArray);
    assertEquals(1, pvArray[0].getMajorVersion());
    assertEquals(2, pvArray[1].getMajorVersion());
    assertEquals(3, pvArray[2].getMajorVersion());
    Arrays.sort(pvArray, Collections.reverseOrder());
    assertEquals(3, pvArray[0].getMajorVersion());
    assertEquals(2, pvArray[1].getMajorVersion());
    assertEquals(1, pvArray[2].getMajorVersion());

    pvArray = new PluginVersion[]{
        new PluginVersion(1, 1),
        new PluginVersion(1, 2),
        new PluginVersion(1, 3)};
    Arrays.sort(pvArray);
    assertEquals(1, pvArray[0].getMinorVersion());
    assertEquals(2, pvArray[1].getMinorVersion());
    assertEquals(3, pvArray[2].getMinorVersion());
    Arrays.sort(pvArray, Collections.reverseOrder());
    assertEquals(3, pvArray[0].getMinorVersion());
    assertEquals(2, pvArray[1].getMinorVersion());
    assertEquals(1, pvArray[2].getMinorVersion());

    pvArray = new PluginVersion[]{
        new PluginVersion(1, 1, 1),
        new PluginVersion(1, 1, 2),
        new PluginVersion(1, 1, 3)};
    Arrays.sort(pvArray);
    assertEquals(1, pvArray[0].getBuildNumber());
    assertEquals(2, pvArray[1].getBuildNumber());
    assertEquals(3, pvArray[2].getBuildNumber());
    Arrays.sort(pvArray, Collections.reverseOrder());
    assertEquals(3, pvArray[0].getBuildNumber());
    assertEquals(2, pvArray[1].getBuildNumber());
    assertEquals(1, pvArray[2].getBuildNumber());
  }

  @Test
  void testDefaultValues() {
    // Test no-arg constructor value.
    PluginVersion pv = new PluginVersion();
    assertEquals(0, pv.getMajorVersion());
    assertEquals(0, pv.getMinorVersion());
    assertEquals(0, pv.getBuildNumber());
    assertNull(pv.getRevisionTag());
    assertEquals("0.0.0", pv.toVersionNumber());
    assertEquals("0.0.0", pv.toString());

    // Test KeyMaker-arg constructor value.
    pv = new PluginVersion(1);
    assertEquals(1, pv.getMajorVersion());
    assertEquals(0, pv.getMinorVersion());
    assertEquals(0, pv.getBuildNumber());
    assertNull(pv.getRevisionTag());
    assertEquals("1.0.0", pv.toVersionNumber());
    assertEquals("1.0.0", pv.toString());

    // Test two-arg constructor value.
    pv = new PluginVersion(1, 2);
    assertEquals(1, pv.getMajorVersion());
    assertEquals(2, pv.getMinorVersion());
    assertEquals(0, pv.getBuildNumber());
    assertNull(pv.getRevisionTag());
    assertEquals("1.2.0", pv.toVersionNumber());
    assertEquals("1.2.0", pv.toString());

    // Test three-arg constructor value.
    pv = new PluginVersion(1, 2, 345);
    assertEquals(1, pv.getMajorVersion());
    assertEquals(2, pv.getMinorVersion());
    assertEquals(345, pv.getBuildNumber());
    assertNull(pv.getRevisionTag());
    assertEquals("1.2.345", pv.toVersionNumber());
    assertEquals("1.2.345", pv.toString());

    // Test three-arg constructor value.
    pv = new PluginVersion(1, 2, 345, "SNAPSHOT");
    assertEquals(1, pv.getMajorVersion());
    assertEquals(2, pv.getMinorVersion());
    assertEquals(345, pv.getBuildNumber());
    assertEquals("SNAPSHOT", pv.getRevisionTag());
    assertEquals("1.2.345", pv.toVersionNumber());
    assertEquals("1.2.345-SNAPSHOT", pv.toString());
  }
}
