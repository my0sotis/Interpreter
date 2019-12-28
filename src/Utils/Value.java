package Utils;

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

    public String getType() {
        return Type;
    }

    public String getValue() {
        switch (Type) {
            case "int":
                return String.valueOf(intValue);
            case "real":
                return String.valueOf(doubleValue);
            case "char":
                return String.valueOf(charValue);
        }
        return "";
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
