package techmoc.extensibility.pluginlibrary.test_objects;


import techmoc.extensibility.pluginlibrary.test_objects.some_plugin_interfaces.Cat;
import techmoc.extensibility.polymorphicmap.PolymorphicMap;

public class Tabby implements Cat {

  @Override
  public void initializePluginAttributes(PolymorphicMap pluginAttributes) {
    pluginAttributes.put("MeowVolume", 12);
    pluginAttributes.put("TotalStripes", 25);
  }

  @Override
  public String meow() {
    return "Meooow";
  }
}
