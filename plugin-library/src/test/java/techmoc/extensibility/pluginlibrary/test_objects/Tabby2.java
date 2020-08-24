package techmoc.extensibility.pluginlibrary.test_objects;


import techmoc.extensibility.pluginlibrary.PluginVersion;
import techmoc.extensibility.pluginlibrary.test_objects.some_plugin_interfaces.Cat;
import techmoc.extensibility.polymorphicmap.PolymorphicMap;

public class Tabby2 implements Cat {

  @Override
  public String getPluginName() {
    return Tabby.class.getSimpleName();
  }

  @Override
  public PluginVersion getPluginVersion() {
    return new PluginVersion(2, 0, 0);
  }

  @Override
  public void initializePluginAttributes(PolymorphicMap pluginAttributes) {
    pluginAttributes.put("MeowVolume", 2);
    pluginAttributes.put("TotalStripes", 10);
  }

  @Override
  public String meow() {
    return "Meooow Meooow";
  }
}
