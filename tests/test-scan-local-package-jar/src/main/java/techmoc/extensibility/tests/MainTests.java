package techmoc.extensibility.tests;

import java.io.IOException;
import java.util.Set;
import techmoc.extensibility.interfaces.Fly;
import techmoc.extensibility.pluginlibrary.PluginRegistry;

public class MainTests {

  public static PluginRegistry testScanLocalJar() throws IOException {
    PluginRegistry pr = new PluginRegistry();
    pr.registerPluginInterfaces(Fly.class);
    pr.scan(Fly.class);

    return pr;
  }

  public static PluginRegistry testScanWithFilters(
      Set<String> targetPackages, boolean scanSubpackages) throws IOException {
    PluginRegistry pr = new PluginRegistry();
    pr.registerPluginInterfaces(Fly.class);
    pr.scan(targetPackages, scanSubpackages);

    return pr;
  }
}
