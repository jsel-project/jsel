package techmoc.extensibility.demos;

import techmoc.extensibility.pluginlibrary.Pluggable;

public interface Webpage extends Pluggable {
  String getContentType();
  String getPageContent();
}
