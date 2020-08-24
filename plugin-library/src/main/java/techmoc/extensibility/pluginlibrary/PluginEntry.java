package techmoc.extensibility.pluginlibrary;

import java.util.Objects;
import techmoc.extensibility.polymorphicmap.PolymorphicMap;


/**
 * Container for storing Plugin class and attributes information.
 */
class PluginEntry implements Comparable<PluginEntry> {

  private final Class<? extends Pluggable> pluggableClass;
  private final String pluginName;
  private final PluginVersion pluginVersion;
  private final PolymorphicMap pluginAttributes;

  /**
   * Constructor.
   *
   * @param pluggableClass Plugin class.
   */
  public PluginEntry(Class<? extends Pluggable> pluggableClass) {
    Objects.requireNonNull(pluggableClass);

    Pluggable pluggable = instantiatePluginAsPluggable(pluggableClass);
    PolymorphicMap pluginAttributes = new PolymorphicMap();
    pluggable.initializePluginAttributes(pluginAttributes);

    Objects.requireNonNull(pluggable.getPluginName());
    Objects.requireNonNull(pluggable.getPluginVersion());
    Objects.requireNonNull(pluginAttributes);

    this.pluggableClass = pluggableClass;
    this.pluginName = pluggable.getPluginName();
    this.pluginVersion = pluggable.getPluginVersion();
    this.pluginAttributes = pluginAttributes;
  }

  /**
   * Returns the plugin class.
   *
   * @return Plugin class.
   */
  final Class<? extends Pluggable> getPluginClass() {
    return pluggableClass;
  }

  /**
   * Returns the plugin name.
   *
   * @return Plugin name.
   */
  final String getPluginName() {
    return pluginName;
  }

  /**
   * Returns the version information for this plugin.
   *
   * @return Plugin version.
   */
  final PluginVersion getPluginVersion() {
    return pluginVersion;
  }

  /**
   * Returns an instantiated Plugin object, cast as the interface that it implements.
   *
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @return Instance of an instantiated Plugin, as a Pluggable.
   */
  final <T extends Pluggable> T getPluginAsInterface(Class<T> registeredPluginInterface) {
    try {
      @SuppressWarnings("unchecked")
      T pluginInstance = (T) pluggableClass.getConstructor().newInstance();
      return pluginInstance;
    } catch (Exception e) {
      throw new IllegalArgumentException(String.format(
          "Plugin [%s] either could not be instantiated, or could not be casted to [%s].",
          pluggableClass.getCanonicalName(),
          registeredPluginInterface.getCanonicalName()), e);
    }
  }

  /**
   * Returns an instantiated Plugin object, cast as a Pluggable.
   *
   * @return Instance of an instantiated Plugin, as a Pluggable.
   */
  final Pluggable getPluginAsPluggable() {
    return instantiatePluginAsPluggable(pluggableClass);
  }

  /**
   * Returns the Plugin Attributes map.
   *
   * @return Plugin attributes map.
   */
  final PolymorphicMap getPluginAttributes() {
    return pluginAttributes;
  }

  @Override
  public int compareTo(PluginEntry o) {
    String thisKey = pluginName + pluginVersion.toVersionNumber();
    String thatKey = o.pluginName + o.pluginVersion.toVersionNumber();
    return thisKey.compareTo(thatKey);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PluginEntry that = (PluginEntry) o;
    return pluggableClass.equals(that.pluggableClass) &&
        pluginName.equals(that.pluginName) &&
        pluginVersion.equals(that.pluginVersion);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pluggableClass, pluginName, pluginVersion);
  }

  /**
   * Returns an instantiated Plugin object, cast as a Pluggable.
   *
   * @param pluginClazz The Plugin class object.
   * @return Instance of an instantiated Plugin, as a Pluggable.
   */
  private Pluggable instantiatePluginAsPluggable(Class<? extends Pluggable> pluginClazz) {
    try {
      return pluginClazz.getConstructor().newInstance();
    } catch (Exception e) {
      throw new IllegalArgumentException(String.format(
          "Plugin [%s] could not be instantiated.", pluginClazz.getName()), e);
    }
  }
}
