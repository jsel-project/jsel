package techmoc.extensibility.demos;

import techmoc.extensibility.pluginlibrary.Pluggable;


public interface MathOperationInterface extends Pluggable {
  double calculate(double x, double y);
}
