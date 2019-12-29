package Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TableElement {
    private String name;
    private String type;
    // 作用域
    private int level;
    private String intValue;
    private String realValue;
    private String stringValue;
    private List<Value> array;

    public TableElement(String name, String type, int level) {
        this.name = name;
        this.type = type;
        this.level = level;
        this.intValue = "";
        this.realValue = "";
        this.stringValue = "";
        array = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    public String getIntValue() {
        return intValue;
    }

    public void setIntValue(String intValue) {
        this.intValue = intValue;
    }

    public void setArrayAt(int index, Value value) {
        array.set(index, value);
    }

    public String getRealValue() {
        return realValue;
    }

    public void setRealValue(String realValue) {
        this.realValue = realValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public int getArrayNum() {
        return array.size();
    }

    public void setArray(List<Value> array) {
        this.array = array;
    }

    public List<Value> getArray() {
        return array;
    }

    public Value getArrayElementAt(int index) {
        if (index >= array.size()) {
            return null;
        }
        return array.get(index);
    }

    public int getArraySize() {
        return array.size();
    }

    @Override
    public String toString() {
        return "TableElement{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", level=" + level +
                ", arrayNum=" + array.size() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableElement that = (TableElement) o;
        return level == that.level &&
                array.size() == that.array.size() &&
                name.equals(that.name) &&
                type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, level, array.size());
    }

    public String getValue() {
        switch (type) {
            case "int":
                return getIntValue();
            case "real":
                return getRealValue();
            case "char":
                return getStringValue();
        }
        return null;
    }
}
