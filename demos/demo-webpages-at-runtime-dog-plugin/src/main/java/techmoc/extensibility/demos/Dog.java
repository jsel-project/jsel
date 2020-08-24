package techmoc.extensibility.demos;

public class Dog implements Webpage {

  @Override
  public String getContentType() {
    return "text/plain";
  }

  @Override
  public String getPageContent() {
    return "   (___()'`;\n"
        + "   /,    /`\n"
        + "   \\\\\"--\\\\";
  }
}
