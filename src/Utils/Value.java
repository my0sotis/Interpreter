package Utils;


import java.util.regex.Pattern;

public class Value {
    private String Type;
    private int intValue = 0;
    private double doubleValue = 0.0;
    private String charValue = "";

    public Value(String type) {
        Type = type;
    }

    public Value(String type, String value) {
        Type = type;
        setValue(type, value);
    }

    public void setType(String type) {
        Type = type;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    public String getType() {
        return Type;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public String getCharValue() {
        return charValue;
    }

    public void setCharValue(String charValue) {
        this.charValue = charValue;
    }

    public void setDoubleValue(double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public String getValue() {
        switch (Type) {
            case "int":
                return String.valueOf(intValue);
            case "real":
                return String.valueOf(doubleValue);
            case "char":
                return charValue;
        }
        return null;
    }

    public boolean CheckInteger(String t) {
        String intRegex = "^[+|-]?((\\d+\\.?)|(0x(\\d|[A-F]|[a-f])+\\.?))$";
        return Pattern.matches(intRegex, t);
    }

    public void setValue(String Type, String Value) {
        switch (Type) {
            case "int":
                intValue = Integer.parseInt(Value);
                break;
            case "real":
                doubleValue = Double.parseDouble(Value);
                break;
            case "char":
                charValue = Value;
                break;
        }
    }
}
