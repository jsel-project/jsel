package techmoc.extensibility.pluginlibrary;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


class PluggableTests {

  private static final String CUSTOM_PLUGIN_NAME = "Cat Plugin";
  private static final PluginVersion CUSTOM_PLUGIN_VERSION =
      new PluginVersion(1, 0, 0, "SNAPSHOT");

  private class DefaultPlugin implements Pluggable {

  }

  // Implement the plugin name.
  private class CustomNamePlugin implements Pluggable {

    public String getPluginName() {
      return CUSTOM_PLUGIN_NAME;
    }
  }

  // Implement the plugin value.
  private class CustomVersionPlugin implements Pluggable {

    public PluginVersion getPluginVersion() {
      return CUSTOM_PLUGIN_VERSION;
    }
  }

  // Implement both name and value.
  private class CustomNameAndValuePlugin implements Pluggable {

    public String getPluginName() {
      return CUSTOM_PLUGIN_NAME;
    }

    public PluginVersion getPluginVersion() {
      return CUSTOM_PLUGIN_VERSION;
    }
  }

  @Test
  public void testNameAndVersion() {
    PluginVersion DEFAULT_PLUGIN_VERSION = new PluginVersion(0, 0, 0);

    // Test the PluginVersion defaults.
    DefaultPlugin defaultPlugin = new DefaultPlugin();
    assertEquals(defaultPlugin.getPluginName(), DefaultPlugin.class.getSimpleName());
    assertEquals(defaultPlugin.getPluginVersion(), DEFAULT_PLUGIN_VERSION);

    // Test when a custom plugin name is applied.
    CustomNamePlugin customNamePlugin = new CustomNamePlugin();
    assertEquals(customNamePlugin.getPluginName(), CUSTOM_PLUGIN_NAME);
    assertEquals(customNamePlugin.getPluginVersion(), DEFAULT_PLUGIN_VERSION);

    // Test when a custom plugin version is applied.
    CustomVersionPlugin customVersionPlugin = new CustomVersionPlugin();
    assertEquals(customVersionPlugin.getPluginName(), CustomVersionPlugin.class.getSimpleName());
    assertEquals(customVersionPlugin.getPluginVersion(), CUSTOM_PLUGIN_VERSION);

    // Test when both a custom plugin name and version is applied.
    CustomNameAndValuePlugin customNameAndValuePlugin = new CustomNameAndValuePlugin();
    assertEquals(customNameAndValuePlugin.getPluginName(), CUSTOM_PLUGIN_NAME);
    assertEquals(customNameAndValuePlugin.getPluginVersion(), CUSTOM_PLUGIN_VERSION);
  }
}
