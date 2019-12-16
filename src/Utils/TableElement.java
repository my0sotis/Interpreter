package Utils;

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
    private int arrayNum;
    private List<Value> array;

    public TableElement(String name, String type, int level) {
        this.name = name;
        this.type = type;
        this.level = level;
        this.intValue = "";
        this.realValue = "";
        this.stringValue = "";
        this.arrayNum = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
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
        return arrayNum;
    }

    public void setArrayNum(int arrayNum) {
        this.arrayNum = arrayNum;
    }

    public void setArray(List<Value> array) {
        this.array = array;
        arrayNum = array.size();
    }

    public List<Value> getArray() {
        return array;
    }

    public Value getArrayElementAt(int index) {
        return array.get(index);
    }

    public int getArraySize() {
        return arrayNum;
    }

    @Override
    public String toString() {
        return "TableElement{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", level=" + level +
                ", arrayNum=" + arrayNum +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableElement that = (TableElement) o;
        return level == that.level &&
                arrayNum == that.arrayNum &&
                name.equals(that.name) &&
                type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, level, arrayNum);
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
