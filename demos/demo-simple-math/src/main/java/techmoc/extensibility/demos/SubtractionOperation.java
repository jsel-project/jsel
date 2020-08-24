package techmoc.extensibility.demos;

public class SubtractionOperation implements MathOperationInterface {

  @Override
  public double calculate(double x, double y) {
    return x - y;
  }
}
