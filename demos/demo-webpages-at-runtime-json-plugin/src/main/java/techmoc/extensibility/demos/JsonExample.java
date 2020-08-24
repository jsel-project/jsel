package techmoc.extensibility.demos;

public class JsonExample implements Webpage {

  @Override
  public String getContentType() {
    return "application/json";
  }

  @Override
  public String getPageContent() {

    return "{ \n" +
        "  \"version\": \"1.0.0\"\n" +
        "  \"info\": { \n" +
        "    \"name\": \"Joe\"\n" +
        "    \"age\": 100\n" +
        "    }\n" +
        "}";
  }
}
