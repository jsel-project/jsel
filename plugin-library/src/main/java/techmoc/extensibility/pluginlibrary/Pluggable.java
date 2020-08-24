package techmoc.extensibility.pluginlibrary;


import techmoc.extensibility.polymorphicmap.PolymorphicMap;

public interface Pluggable {

  /**
   * Returns the name of the plug-in. By default, returns the simple name of the plugin
   * implementation class. Can be overridden to provide a custom plugin name.
   *
   * @return Name of the plugin.
   */
  default String getPluginName() {
    return this.getClass().getSimpleName();
  }

  /**
   * Returns the version of the plugin. By default, returns version 0.0.0. Can be overridden to
   * provide a custom version value.
   *
   * @return Version of the plugin.
   */
  default PluginVersion getPluginVersion() {
    return new PluginVersion(0, 0, 0);
  }

  /**
   * Sets the initial attributes for the plugin. By default no attributes are added.
   *
   * @param pluginAttributes Plugin attributes map to be initialized.
   */
  default void initializePluginAttributes(PolymorphicMap pluginAttributes) {
    // Examples of how to add attributes:
    //   pluginAttributes.put("key", "value");
    //   pluginAttributes.put("score", 55);
    //   pluginAttributes.put("isDeprecated", false);
  }
}
