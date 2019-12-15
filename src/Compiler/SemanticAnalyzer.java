package Compiler;

import Utils.Table;
import Utils.TableElement;
import Utils.TreeNode;
import Utils.Value;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SemanticAnalyzer {
    private Table table = new Table();
    private TreeNode root;

    private int errorNum = 0;
    private List<String> errorInfo = new ArrayList<>();

    private final File dataLocation = new File("./output/data.table");
    private final File stringLocation = new File("./output/string.table");
    private final File variableLocation = new File("./output/variable.table");
    private File inputLocation = new File("./output/in");

    private int level = 0;
    private List<String> dataTable = new ArrayList<>();
    private int dataIndex = 0;
    private List<String> stringTable = new ArrayList<>();
    private int stringIndex = 0;
    private List<String> variableTable = new ArrayList<>();
    private int variableIndex = 0;
    private int inputIndex = 0;
    private List<String> inputTable = new ArrayList<>();

    private int whileNum = 0;
    private int forNum = 0;

    public SemanticAnalyzer(TreeNode node) {
        root = node;
        LoadDataTable();
        LoadStringTable();
        LoadVariableTable();
        LoadInputTable();
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }

    private void LoadDataTable() {
        try {
            dataTable.clear();
            BufferedReader br = new BufferedReader(new FileReader(dataLocation));
            String tmp;
            while ((tmp = br.readLine()) != null && (!tmp.equals(""))) {
                dataTable.add(tmp);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("文件读取失败");
        }
    }

    private void LoadStringTable() {
        try {
            stringTable.clear();
            BufferedReader br = new BufferedReader(new FileReader(stringLocation));
            String tmp;
            while ((tmp = br.readLine()) != null && (!tmp.equals(""))) {
                stringTable.add(tmp);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("文件读取失败");
        }
    }

    private void LoadVariableTable() {
        try {
            variableTable.clear();
            BufferedReader br = new BufferedReader(new FileReader(variableLocation));
            String tmp;
            while ((tmp = br.readLine()) != null && (!tmp.equals(""))) {
                variableTable.add(tmp);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("文件读取失败");
        }
    }

    private void LoadInputTable() {
        try {
            inputTable.clear();
            BufferedReader br = new BufferedReader(new FileReader(inputLocation));
            String tmp;
            while ((tmp = br.readLine()) != null && (!tmp.equals(""))) {
                inputTable.add(tmp);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("文件读取失败");
        }
    }

    private String getNextData() {
        if (dataIndex >= dataTable.size()) {
            error("Cannot get number");
            return null;
        }
        return dataTable.get(dataIndex++);
    }

    private String getNextString() {
        if (stringIndex >= stringTable.size()) {
            error("Cannot get number");
            return null;
        }
        return stringTable.get(stringIndex++);
    }

    private String getNextVariable() {
        if (variableIndex >= variableTable.size()) {
            error("Cannot get number");
            return null;
        }
        return variableTable.get(variableIndex++);
    }

    private String getNextInput() {
        if (inputIndex >= inputTable.size()) {
            error("Cannot get number");
            return null;
        }
        return inputTable.get(inputIndex++);
    }

    private void error(String error) {
        errorNum++;
        String message = "Wrong: " + error;
        errorInfo.add(message);
    }

    public void Analyse() {
        S(root);
        for (var x :
                table.getSymbolTable()) {
            System.out.println(x.getName());
            System.out.println(x.getType());
            if (x.getType().equals("array")) {
                for (var y :
                        x.getArray()) {
                    System.out.println(y.getValue());
                }
            }
        }
        System.out.println();
//        System.out.println(x.getValue());
    }

    private void S(TreeNode node) {
        for (int i = 0; i < node.getChildrenNum(); i++) {
            TreeNode current = node.getChildAt(i);
            String content = current.getName();
            if (content.equals("C")) {

            } else if (content.equals("SS")) {
                SS(current);
            } else if (content.equals("L")) {

            } else if (content.equals("S")) {
                S(current);
            }
        }
    }

    private void SS(TreeNode node) {
        for (int i = 0; i < node.getChildrenNum(); i++) {
            TreeNode current = node.getChildAt(i);
            String content = current.getName();
            switch (content) {
                case "Jump":

                    break;
                case "Print":
                    Print(current);
                    break;
                case "State":
                    State(current);
                    break;
                case "Assign":
                    Assign(current);
                    break;
                case "ScanSentence":
                    Scan(current.getChildAt(0));
                    break;
            }
        }
    }

    private void Assign(TreeNode node) {
        TreeNode leftNode = node.getChildAt(0);
        String leftValue = getNextVariable();
        if (leftValue == null) {
            error("获取不到对应的变量值");
            return;
        }
        TableElement te = table.getElementAllLever(leftValue, level);
        if (te != null) {
            TreeNode assign1 = node.getChildAt(1);
            if (assign1.getChildAt(0).getName().equals("=")) {
                switch (assign1.getChildAt(1).getChildAt(0).getName()) {
                    case "Character":
                        if (!te.getType().equals("char")) {
                            error("此处需要字符型！");
                            return;
                        }
                        // 判断字符是否为空
                        if (assign1.getChildAt(1).getChildAt(0).getChildAt(0).getName().equals("'")) {
                            te.setStringValue("");
                            return;
                        }
                        te.setStringValue(getNextString());
                        break;
                    case "Expression":
                        Value result = Calculate(assign1.getChildAt(1).getChildAt(0));
                        if (result == null || result.getType() == null) {
                            error("表达式有误！");
                            return;
                        }
                        if (!te.getType().equals("int") || !te.getType().equals("real")) {
                            error("此处需要数值而非其他！");
                            return;
                        }
                        String targetType = te.getType();
                        String resultType = result.getType();
                        if (resultType.equals("real")) {
                            if (targetType.equals("real")) {
                                te.setRealValue(result.getValue());
                            } else {
                                error("此处需要int，而不是real类型");
                            }
                        } else {
                            if (targetType.equals("real")) {
                                te.setRealValue(result.getValue());
                            } else {
                                te.setIntValue(result.getValue());
                            }
                        }
                        break;
                    case "String":
                        if (!te.getType().equals("array")) {
                            error("此处需要字符数组！");
                            return;
                        }
                        // 判断字符是否为空
                        if (assign1.getChildAt(1).getChildAt(0).getChildAt(0).getName().equals("\"")) {
                            te.setArray(Collections.singletonList(new Value("char", "")));
                            return;
                        }
                        String next = getNextString();
                        if (next == null) {
                            error("没有String数值了");
                            return;
                        }
                        te.setArray(getValueList(getListFromString(next)));
                        break;
                }
            } else {
                TreeNode tmp = assign1.getChildAt(1);
                if (!te.getType().equals("array")) {
                    error("获取的值并非Array");
                    return;
                }
            }
        } else {
            String info = "变量" + leftValue +"未在使用前声明。";
            error(info);
        }
    }

    private List<String> getListFromString(String str) {
        return Stream.iterate(0, n -> ++n).limit(str.length())
                .map(n -> "" + str.charAt(n))
                .collect(Collectors.toList());
    }

    private List<Value> getValueList(List<String> list) {
        List<Value> res = new ArrayList<>();
        for (var l :
                list) {
            res.add(new Value("char", l));
        }
        return res;
    }

    private String getString(TreeNode node) {
        if (node.getChildAt(1).getChildAt(0).getName().equals("\"")) {
            return "";
        } else {
            String res = getNextString();
            if (res == null) {
                error("未找到对应String");
                return null;
            }
            return res;
        }
    }

    private void State(TreeNode node) {
        String type = node.getChildAt(0).getName();
        String id = getNextVariable();
        TableElement element;
        if (id == null) {
            error("无法读取下一个变量");
            return;
        }
        TreeNode child = node.getChildAt(2);
        switch (child.getChildAt(0).getName()) {
            case "State2":
                element = new TableElement(id, type, level);
                table.add(element);
                TreeNode tnode = child.getChildAt(0);
                while (tnode.getChildrenNum() != 1) {
                    String tid = getNextVariable();
                    if (tid == null) {
                        error("找不到对应变量值");
                        return;
                    }
                    switch (tnode.getChildAt(2).getChildAt(0).getName()) {
                        case "ε":
                            element = new TableElement(tid, type, level);
                            table.add(element);
                            break;
                        case "=":
                            Value x =  Value4(tnode.getChildAt(2).getChildAt(1));
                            if (x == null) {
                                error("数值有误");
                                return;
                            }
                            element = new TableElement(tid, type, level);
                            switch (type) {
                                case "int":
                                    switch (x.getType()) {
                                        case "int":
                                            element.setIntValue(x.getValue());
                                            break;
                                        case "real":
                                            error("无法将real转换为int");
                                            return;
                                        case "char":
                                            element.setIntValue(String.valueOf(Integer.valueOf(x.getValue())));
                                            break;
                                    }
                                    break;
                                case "real":
                                    switch (x.getType()) {
                                        case "int":
                                            element.setRealValue(x.getValue());
                                            break;
                                        case "real":
                                            element.setRealValue(x.getValue());
                                            return;
                                        case "char":
                                            element.setRealValue(String.valueOf(Integer.valueOf(x.getValue())));
                                            break;
                                    }
                                    break;
                                case "char":
                                    element.setStringValue(x.getValue());
                                    break;
                            }
                            table.add(element);
                    }
                }
                break;
            case "[":
                TreeNode array = child.getChildAt(1);
                if (array.getChildAt(0).getName().equals("Expression")) {
                    Value res = Calculate(array.getChildAt(0));
                    if (res == null) {
                        error("数组索引有误");
                        return;
                    }
                    if (!res.getType().equals("int")) {
                        error("数组长度不可为real");
                        return;
                    }
                    element = new TableElement(id, "array", level);
                    List<Value> values = new ArrayList<>();
                    for (int i = 0; i < Integer.parseInt(res.getValue()); i++) {
                        values.add(new Value(type));
                    }
                    element.setArray(values);
                } else {
                    element = new TableElement(id, "array", level);
                }
                TreeNode array1 = array.getChildAt(array.getChildrenNum()-1);
                switch (array1.getChildAt(0).getName()) {
                    case "ε":
                        table.add(element);
                        break;
                    case "=":
                        TreeNode array2 = array1.getChildAt(1);
                        if (array2.getChildAt(0).getName().equals("{")) {
//                            int x = 1;
                            List<Value> values = new ArrayList<>();
                            values.add(Value4(array2.getChildAt(1)));
                            TreeNode temp = array2.getChildAt(2);
                            while (temp.getChildrenNum() != 1) {
                                values.add(Value4(temp.getChildAt(1)));
                                temp = temp.getChildAt(2);
                            }
                            element.setArray(values);
                            table.add(element);
                            break;
                        } else {
//                            element = new TableElement(id, "array", level);
                            String x = getString(array2.getChildAt(0));
                            if (x == null) {
                                error("未找到对应字符串");
                                return;
                            }
                            element.setArray(getValueList(getListFromString(x)));
                        }
                }
                tnode = child.getChildAt(2);
                while (tnode.getChildrenNum() != 1) {
                    String tid = getNextVariable();
                    if (tid == null) {
                        error("找不到对应变量值");
                        return;
                    }
                    switch (tnode.getChildAt(2).getChildAt(0).getName()) {
                        case "ε":
                            element = new TableElement(tid, type, level);
                            table.add(element);
                            break;
                        case "=":
                            Value x =  Value4(tnode.getChildAt(2).getChildAt(1));
                            if (x == null) {
                                error("数值有误");
                                return;
                            }
                            element = new TableElement(tid, type, level);
                            switch (type) {
                                case "int":
                                    switch (x.getType()) {
                                        case "int":
                                            element.setIntValue(x.getValue());
                                            break;
                                        case "real":
                                            error("无法将real转换为int");
                                            return;
                                        case "char":
                                            element.setIntValue(String.valueOf(Integer.valueOf(x.getValue())));
                                            break;
                                    }
                                    break;
                                case "real":
                                    switch (x.getType()) {
                                        case "int":
                                            element.setRealValue(x.getValue());
                                            break;
                                        case "real":
                                            element.setRealValue(x.getValue());
                                            return;
                                        case "char":
                                            element.setRealValue(String.valueOf(Integer.valueOf(x.getValue())));
                                            break;
                                    }
                                    break;
                                case "char":
                                    element.setStringValue(x.getValue());
                                    break;
                            }
                            table.add(element);
                    }
                }
                break;
            case "=":
                String name = child.getChildAt(1).getChildAt(0).getName();
                String next;
                TableElement te;
                switch (name) {
                    case "Character":
                        if (!type.equals("char")) {
                            error("此处需要char类型");
                        }
                        // 判断字符是否为空
                        if (child.getChildAt(1).getChildAt(0).getChildAt(0).getName().equals("'")) {
                            te = new TableElement(id, type, level);
                            te.setStringValue("");
                            table.add(te);
                            break;
                        }
                        next = getNextString();
                        if (next == null) {
                            error("无法获取下一个string");
                            return;
                        }
                        te = new TableElement(id, type, level);
                        te.setStringValue(next);
                        table.add(te);
                    case "Expression":
                        Value result = Calculate(child.getChildAt(1).getChildAt(0));
                        if (result == null || result.getType() == null) {
                            error("表达式有误！");
                            return;
                        }
                        switch (type) {
                            case "int":
                                switch (result.getType()) {
                                    case "int":
                                        te = new TableElement(id, type, level);
                                        te.setIntValue(result.getValue());
                                        table.add(te);
                                        break;
                                    case "real":
                                        error("无法将real强制转换为int");
                                        break;
                                    case "char":
                                        te = new TableElement(id, type, level);
                                        te.setIntValue(String.valueOf(Integer.valueOf(result.getValue())));
                                        table.add(te);
                                        break;
                                }
                                break;
                            case "real":
                                switch (result.getType()) {
                                    case "int":
                                    case "real":
                                        te = new TableElement(id, type, level);
                                        te.setRealValue(result.getValue());
                                        table.add(te);
                                        break;
                                    case "char":
                                        te = new TableElement(id, type, level);
                                        te.setRealValue(String.valueOf(Integer.valueOf(result.getValue())));
                                        table.add(te);
                                        break;
                                }
                                break;
                            case "char":
                                te = new TableElement(id, type, level);
                                te.setStringValue(result.getValue());
                                table.add(te);
                                break;
                        }
                }
                tnode = child.getChildAt(2);
                while (tnode.getChildrenNum() != 1) {
                    String tid = getNextVariable();
                    if (tid == null) {
                        error("找不到对应变量值");
                        return;
                    }
                    switch (tnode.getChildAt(2).getChildAt(0).getName()) {
                        case "ε":
                            element = new TableElement(tid, type, level);
                            table.add(element);
                            break;
                        case "=":
                            Value x =  Value4(tnode.getChildAt(2).getChildAt(1));
                            if (x == null) {
                                error("数值有误");
                                return;
                            }
                            element = new TableElement(tid, type, level);
                            switch (type) {
                                case "int":
                                    switch (x.getType()) {
                                        case "int":
                                            element.setIntValue(x.getValue());
                                            break;
                                        case "real":
                                            error("无法将real转换为int");
                                            return;
                                        case "char":
                                            element.setIntValue(String.valueOf(Integer.valueOf(x.getValue())));
                                            break;
                                    }
                                    break;
                                case "real":
                                    switch (x.getType()) {
                                        case "int":
                                            element.setRealValue(x.getValue());
                                            break;
                                        case "real":
                                            element.setRealValue(x.getValue());
                                            return;
                                        case "char":
                                            element.setRealValue(String.valueOf(Integer.valueOf(x.getValue())));
                                            break;
                                    }
                                    break;
                                case "char":
                                    element.setStringValue(x.getValue());
                                    break;
                            }
                            table.add(element);
                    }
                }

        }
    }

    private Value Value4(TreeNode node) {
        String name = node.getChildAt(0).getName();
        String next;
        switch (name) {
            case "Character":
//                if (!type.equals("char")) {
//                    error("此处需要char类型");
//                }
                // 判断字符是否为空
                if (node.getChildAt(0).getChildAt(1).getChildAt(0).getName().equals("'")) {
//                    te = new TableElement(id, type, level);
//                    te.setStringValue("");
//                    table.add(te);
//                    break;
                    return new Value("char", "");
                }
                next = getNextString();
                if (next == null) {
                    error("无法获取下一个string");
                    return null;
                }
//                te = new TableElement(id, type, level);
//                te.setStringValue(next);
//                table.add(te);
                return new Value("char", next);
            case "Expression":
                Value result = Calculate(node.getChildAt(0));
                if (result == null || result.getType() == null) {
                    error("表达式有误！");
                    return null;
                }
                return result;
//                switch (type) {
//                    case "int":
//                        switch (result.getType()) {
//                            case "int":
//                                te = new TableElement(id, type, level);
//                                te.setIntValue(result.getValue());
//                                table.add(te);
//                                return;
//                            case "real":
//                                error("无法将real强制转换为int");
//                                return;
//                            case "char":
//                                te = new TableElement(id, type, level);
//                                te.setIntValue(String.valueOf(Integer.valueOf(result.getValue())));
//                                table.add(te);
//                                return;
//                        }
//                        break;
//                    case "real":
//                        switch (result.getType()) {
//                            case "int":
//                            case "real":
//                                te = new TableElement(id, type, level);
//                                te.setRealValue(result.getValue());
//                                table.add(te);
//                                return;
//                            case "char":
//                                te = new TableElement(id, type, level);
//                                te.setRealValue(String.valueOf(Integer.valueOf(result.getValue())));
//                                table.add(te);
//                                return;
//                        }
//                        break;
//                    case "char":
//                        te = new TableElement(id, type, level);
//                        te.setStringValue(result.getValue());
//                        table.add(te);
//                        return new ;
        }
        return null;
    }

    private boolean CheckInt(String str) {
        String REGEX_INT = "^[+|-]?((\\d+\\.?)|(0x(\\d|[A-F]|[a-f])+\\.?))$";
        return Pattern.matches(REGEX_INT, str);
    }

    private boolean CheckDigital(String str) {
         String REGEX_DIGITAL =
                "^[+|-]?((\\d+(\\.\\d+)?)|(0x(\\d|[A-F]|[a-f])+(\\.(\\d|[A-F]|[a-f])+)?))$";
         return Pattern.matches(REGEX_DIGITAL, str);
    }

    private Value and(Value x, Value y) {
        if (Double.parseDouble(x.getValue()) != 0 && Double.parseDouble(y.getValue()) != 0) {
            return new Value("int", String.valueOf(1));
        }
        return new Value("int", String.valueOf(0));
    }

    private Value or(Value x, Value y) {
        if (Double.parseDouble(x.getValue()) == 0 && Double.parseDouble(y.getValue()) == 0) {
            return new Value("int", String.valueOf(0));
        }
        return new Value("int", String.valueOf(1));
    }

    private Value equal(Value x, Value y) {
        if (Double.parseDouble(x.getValue()) == Double.parseDouble(y.getValue())) {
            return new Value("int", String.valueOf(1));
        }
        return new Value("int", String.valueOf(0));
    }

    private Value neq(Value x, Value y) {
        if (Double.parseDouble(x.getValue()) == Double.parseDouble(y.getValue())) {
            return new Value("int", String.valueOf(0));
        }
        return new Value("int", String.valueOf(1));
    }

    private Value g(Value x, Value y) {
        if (Double.parseDouble(x.getValue()) > Double.parseDouble(y.getValue())) {
            return new Value("int", String.valueOf(1));
        }
        return new Value("int", String.valueOf(0));
    }

    private Value ge(Value x, Value y) {
        if (Double.parseDouble(x.getValue()) >= Double.parseDouble(y.getValue())) {
            return new Value("int", String.valueOf(1));
        }
        return new Value("int", String.valueOf(0));
    }

    private Value l(Value x, Value y) {
        if (Double.parseDouble(x.getValue()) < Double.parseDouble(y.getValue())) {
            return new Value("int", String.valueOf(1));
        }
        return new Value("int", String.valueOf(0));
    }

    private Value le(Value x, Value y) {
        if (Double.parseDouble(x.getValue()) <= Double.parseDouble(y.getValue())) {
            return new Value("int", String.valueOf(1));
        }
        return new Value("int", String.valueOf(0));
    }

    // Unfinished
    private Value Calculate(TreeNode node) {
        if (node.getName().equals("E11")) {
            if (node.getChildAt(0).getName().equals("(")) {
                return Calculate(node.getChildAt(1));
            } else {
                return Value(node.getChildAt(0));
            }
        }
        if (node.getChildAt(1).getChildAt(0).getName().equals("ε"))
            return Calculate(node.getChildAt(0));
        Value left = Calculate(node.getChildAt(0));
        Value right = Calculate(node.getChildAt(1).getChildAt(1));
        if (left == null || right == null) {
            return null;
        }
        String resType = "int";
        if (left.getType().equals("real") || right.getType().equals("real")) {
            resType = "real";
        }
        switch (node.getName()) {
            case "Expression":
                return or(left, right);
            case "E2":
                return and(left, right);
            case "E3":
                if (resType.equals("real")) {
                    error("Real values cannot be bitwise operated!");
                    return null;
                }
                return new Value("int",
                        String.valueOf(Integer.parseInt(left.getValue()) | Integer.parseInt(right.getValue())));
            case "E4":
                if (resType.equals("real")) {
                    error("Real values cannot be bitwise operated!");
                    return null;
                }
                return new Value("int",
                        String.valueOf(Integer.parseInt(left.getValue()) ^ Integer.parseInt(right.getValue())));
            case "E5":
                if (resType.equals("real")) {
                    error("Real values cannot be bitwise operated!");
                    return null;
                }
                return new Value("int",
                        String.valueOf(Integer.parseInt(left.getValue()) & Integer.parseInt(right.getValue())));
            case "E6":
                switch (node.getChildAt(1).getChildAt(0).getName()) {
                    case "==":
                        return equal(left, right);
                    case "!=":
                    case "<>":
                        return neq(left, right);
                }
                break;
            case "E7":
                switch (node.getChildAt(1).getChildAt(0).getName()) {
                    case ">":
                        return g(left, right);
                    case ">=":
                        return ge(left, right);
                    case "<":
                        return l(left, right);
                    case "<=":
                        return le(left, right);
                }
                break;
            case "E8":
                switch (node.getChildAt(1).getChildAt(0).getName()) {
                    case "<<":
                        if (resType.equals("real")) {
                            error("Real values cannot be bitwise operated!");
                            return null;
                        }
                        return new Value("int",
                                String.valueOf(Integer.parseInt(left.getValue()) << Integer.parseInt(right.getValue())));
                    case ">>":
                        if (resType.equals("real")) {
                            error("Real values cannot be bitwise operated!");
                            return null;
                        }
                        return new Value("int",
                                String.valueOf(Integer.parseInt(left.getValue()) >> Integer.parseInt(right.getValue())));
                }
            case "E9":
                switch (node.getChildAt(1).getChildAt(0).getName()) {
                    case "+":
                        if (resType.equals("real")) {
                            return new Value("real",
                                    String.valueOf(Double.parseDouble(left.getValue())
                                            + Double.parseDouble(right.getValue())));
                        } else {
                            return new Value("int",
                                    String.valueOf(Integer.parseInt(left.getValue())
                                            + Integer.parseInt(right.getValue())));
                        }
                    case "-":
                        if (resType.equals("real")) {
                            return new Value("real",
                                    String.valueOf(Double.parseDouble(left.getValue())
                                            - Double.parseDouble(right.getValue())));
                        } else {
                            return new Value("int",
                                    String.valueOf(Integer.parseInt(left.getValue())
                                            - Integer.parseInt(right.getValue())));
                        }
                }
            case "E10":
                switch (node.getChildAt(1).getChildAt(0).getName()) {
                    case "*":
                        if (resType.equals("real")) {
                            return new Value("real",
                                    String.valueOf(Double.parseDouble(left.getValue())
                                            * Double.parseDouble(right.getValue())));
                        } else {
                            return new Value("int",
                                    String.valueOf(Integer.parseInt(left.getValue())
                                            * Integer.parseInt(right.getValue())));
                        }
                    case "/":
                        if (resType.equals("real")) {
                            return new Value("real",
                                    String.valueOf(Double.parseDouble(left.getValue())
                                            / Double.parseDouble(right.getValue())));
                        } else {
                            return new Value("int",
                                    String.valueOf(Integer.parseInt(left.getValue())
                                            / Integer.parseInt(right.getValue())));
                        }
                    case "%":
                        if (resType.equals("real")) {
                            return new Value("real",
                                    String.valueOf(Double.parseDouble(left.getValue())
                                            - Double.parseDouble(right.getValue())));
                        } else {
                            return new Value("int",
                                    String.valueOf(Integer.parseInt(left.getValue())
                                            / Integer.parseInt(right.getValue())));
                        }
                }
        }
        return null;
    }

    private Value Value(TreeNode node) {
        TreeNode child = node.getChildAt(0);
        String name = child.getName();
        switch (name) {
            case "number":
                String next = getNextData();
                if (CheckInt(next))
                    return new Value("int", next);
                return new Value("real", next);
            case "true":
                return new Value("int", String.valueOf(1));
            case "false":
                return new Value("int", String.valueOf(0));
            case "identifier":
                String n = node.getChildAt(1).getChildAt(0).getName();
                switch (n) {
                    case "ε":
                        String id = getNextVariable();
                        if (table.getElementAllLever(id, level) == null) {
                            String error = "变量" + id + "在使用前未声明";
                            error(error);
                            return null;
                        } else {
                            TableElement te = table.getElementAllLever(id, level);
                            String t = te.getType();
                            String va = te.getValue();
                            if (va.equals("")) {
                                String error = "变量" + name + "并非整数，此处所需整数。";
                                error(error);
                                return null;
                            } else {
                                switch (t) {
                                    case "int":
                                        return new Value("int", va);
                                    case "real":
                                        return new Value("real", va);
                                }
                            }
                        }
                    case "[":
                        // Need Array Dispose
                        Value indexName = Calculate(child.getChildAt(1).getChildAt(1));
                        String aid = getNextVariable();
                        TableElement e = table.getElementAllLever(aid, level);
                        if (e == null) {
                            String error = "变量" + aid + "在使用前未声明";
                            error(error);
                            return null;
                        } else {
                            if (indexName == null) {
                                error("找不到对应的数组索引");
                                return null;
                            }
                            if (!indexName.getType().equals("int")) {
                                error("数组索引不可为real类型");
                            }
                            int index = Integer.parseInt(indexName.getValue());
                            if (index < 0 || index >= e.getArrayNum()) {
                                error("数组越界");
                            }
                            Value target = e.getArrayElementAt(index);
                            if (CheckInt(target.getValue())) {
                                return new Value("real", target.getValue());
                            }
                            return new Value("real", target.getValue());
                        }
                }
                break;
            case "Scan":
                // Need Function
                return Scan(child);
        }
        return null;
    }

    private void Print(TreeNode node) {
        Value target = Calculate(node.getChildAt(2));
        if (target == null) {
            error("运算式有误！");
            return;
        }
        String res = target.getValue();
        System.out.println(res);
    }

    private Value Scan(TreeNode node) {
        TreeNode child = node.getChildAt(2);
        String input = getNextInput();
        if (input == null) {
            error("没有任何输出");
            return null;
        }
        String id = getNextVariable();
        TableElement te = table.getElementAllLever(id, level);
        if (te == null) {
            String error = "变量" + id + "在使用前未声明";
            error(error);
            return null;
        }
        switch (child.getChildAt(1).getChildAt(0).getName()) {
            case "ε":
                if (CheckInt(input)) {
                    switch (te.getType()) {
                        case "int":
                            te.setIntValue(input);
                            return new Value("int", input);
                        case "real":
                            te.setRealValue(input);
                            return new Value("real", input);
                        case "char":
                            te.setStringValue(input);
                            return new Value("char", input);
                        case "array":
                            te.setArray(getValueList(getListFromString(input)));
                            // Need Operation
                            return new Value(te.getArrayElementAt(0).getType(),
                                    te.getArrayElementAt(0).getValue());
                    }
                } else if (CheckDigital(input)) {
                    switch (te.getType()) {
                        case "int":
                            error("无法将real强制转换为int");
                            return null;
                        case "real":
                            te.setRealValue(input);
                            return new Value("real", input);
                        case "char":
                            te.setStringValue(input);
                            return new Value("char", input);
                        case "array":
                            te.setArray(getValueList(getListFromString(input)));
                            return new Value(te.getArrayElementAt(0).getType(),
                                    te.getArrayElementAt(0).getValue());
                    }
                } else {
                    switch (te.getType()) {
                        case "int":
                            error("无法将string强制转换为int");
                            return null;
                        case "real":
                            error("无法将string强制转换为real");
                            return null;
                        case "char":
                            te.setStringValue(input);
                            return new Value("char", input);
                        case "array":
                            te.setArray(getValueList(getListFromString(input)));
                            return new Value(te.getArrayElementAt(0).getType(),
                                    te.getArrayElementAt(0).getValue());
                    }
                }
                return null;
            case "[":
                Value indexName = Calculate(child.getChildAt(1).getChildAt(1));
                if (indexName == null) {
                    error("找不到对应的数组索引");
                    return null;
                }
                if (!indexName.getType().equals("int")) {
                    error("数组索引不可为real类型");
                    return null;
                }
                int index = Integer.parseInt(indexName.getValue());
                if (index < 0 || index >= te.getArrayNum()) {
                    error("数组越界");
                    return null;
                }
                Value target = te.getArrayElementAt(index);
                return new Value("real", target.getValue());
        }
        return null;
    }

    private void C(TreeNode node) {

    }
}
