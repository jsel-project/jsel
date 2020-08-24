package techmoc.extensibility.demos;

public class AdditionOperation implements MathOperationInterface {

  @Override
  public double calculate(double x, double y) {
    return x + y;
  }
}
