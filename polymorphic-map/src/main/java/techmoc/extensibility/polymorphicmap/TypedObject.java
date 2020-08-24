package techmoc.extensibility.polymorphicmap;

import java.util.Arrays;
import java.util.Objects;


/**
 * Simple container for typed values.
 */
public final class TypedObject {

  private final Class<?> type;
  private final Object value;

  public TypedObject(Object value) {
    Objects.requireNonNull(value);

    this.type = value.getClass();
    this.value = value;
  }

  public Class<?> getType() {
    return type;
  }

  public Object getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TypedObject that = (TypedObject) o;
    if (value.getClass().isArray()) {
      return type.equals(that.getType()) &&
          Arrays.deepEquals((Object[]) value, (Object[]) that.getValue());
    } else {
      return type.equals(that.type) && value.equals(that.value);
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, value);
  }
}

