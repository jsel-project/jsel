package techmoc.extensibility.pluginlibrary.test_objects;

import techmoc.extensibility.pluginlibrary.PluginVersion;


public class Poodle implements Dog {

  @Override
  public String getPluginName() {
    return "Poodle";
  }

  @Override
  public PluginVersion getPluginVersion() {
    return new PluginVersion(1, 2, 345, "TEST");
  }

  public String bark() {
    return "Yap";
  }
}
