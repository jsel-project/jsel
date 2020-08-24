package techmoc.extensibility.demos;

public class Cat implements Webpage {

  @Override
  public String getContentType() {
    return "text/plain";
  }

  @Override
  public String getPageContent() {
    return "   \\    /\\\n"
        + "    )  ( ')\n"
        + "   (  /  )\n"
        + "    \\(__)|";
  }
}
