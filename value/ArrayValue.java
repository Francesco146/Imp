package value;

import java.util.ArrayList;
import java.util.List;

public class ArrayValue extends ExpValue<List<?>> {
    private final List<ExpValue<?>> values;

    public ArrayValue(List<?> value) {
        super(value);
        this.values = new ArrayList<>();
        value.forEach(o -> this.values.add((ExpValue<?>) o));
    }

    public void set(int index, ExpValue<?> value) {
        if (index < this.values.size()) this.values.set(index, value);
        else this.values.add(index, value);
    }

    public List<ExpValue<?>> getValues() {
        return this.values;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ArrayValue that = (ArrayValue) obj;
        return this.values.equals(that.values);
    }

    @Override
    public int hashCode() {
        return this.values.hashCode();
    }
}
