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
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SemanticAnalyzer {
    private Table table = new Table();
    private TreeNode root;

    private int errorNum = 0;
    private List<String> errorInfo = new ArrayList<>();

//    private File inputLocation = new File("./Tests/bag-test/beibao11.in");
    private File inputLocation = new File("./Tests/stack/stack1.in");

    private int level = 0;
    private int inputIndex = 0;
    private List<String> inputTable = new ArrayList<>();

    private int whileNum = 0;
    private int forNum = 0;

    public SemanticAnalyzer(TreeNode node) {
        root = node;
        LoadInputTable();
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }

    private void LoadInputTable() {
        try {
            inputTable.clear();
            BufferedReader br = new BufferedReader(new FileReader(inputLocation));
            String tmp;
            while ((tmp = br.readLine()) != null && (!tmp.equals(""))) {
                String[] sentence = tmp.split(" ");
                inputTable.addAll(Arrays.asList(sentence));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("文件读取失败");
        }
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
        System.out.println("There are " + errorNum + " errors in the program!");
        for (var x :
                errorInfo) {
            System.out.println(x);
        }
    }

    private void S(TreeNode node) {
        if (isBreak || isContinue) return;
        for (int i = 0; i < node.getChildrenNum(); i++) {
            TreeNode current = node.getChildAt(i);
            String content = current.getName();
            switch (content) {
                case "C":
                    C(current);
                    break;
                case "SS":
                    SS(current);
                    break;
                case "L":
                    L(current);
                    break;
                case "S":
                    S(current);
                    break;
            }
        }
    }

    private void SS(TreeNode node) {
        if (isBreak || isContinue) return;
        for (int i = 0; i < node.getChildrenNum(); i++) {
            TreeNode current = node.getChildAt(i);
            String content = current.getName();
            switch (content) {
                case "Jump":
                    Jump(current);
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

    private void Jump(TreeNode node) {
        if (isBreak || isContinue) return;
        if (whileNum > 0 || forNum > 0) {
            TreeNode child = node.getChildAt(0);
            if (child.getName().equals("break")) {
                isBreak = true;
            } else {
                isContinue = true;
            }
        } else {
            error("没有任何循环，无法跳出");
        }
    }

    private void Assign(TreeNode node) {
        if (isContinue || isBreak) return;
        String leftValue = node.getChildAt(0).getName();
        TableElement te = table.getElementAllLever(leftValue, level);
        if (te != null) {
            TreeNode assign1 = node.getChildAt(1);
            if (assign1.getChildAt(0).getName().equals("=")) {
                // = Values
                Value res = Value4(assign1.getChildAt(1));
                if (res == null) {
                    error("表达式错误");
                    return;
                }
                switch (res.getType()) {
                    case "int":
                        switch (te.getType()) {
                            case "int":
                                te.setIntValue(res.getValue());
                                break;
                            case "real":
                                te.setRealValue(res.getValue());
                                break;
                            case "char":
                                te.setStringValue(res.getValue());
                                break;
                            case "array":
                                te.setArray(getValueList(getListFromString(res.getValue())));
                                break;
                        }
                        break;
                    case "real":
                        switch (te.getType()) {
                            case "int":
                                error("无法将real强制转换为int");
                                break;
                            case "real":
                                te.setRealValue(res.getValue());
                                break;
                            case "char":
                                te.setStringValue(res.getValue());
                                break;
                            case "array":
                                te.setArray(getValueList(getListFromString(res.getValue())));
                                break;
                        }
                        break;
                    case "char":
                        switch (te.getType()) {
                            case "int":
                            case "real":
                                error("无法将数值强制转换为char");
                                break;
                            case "char":
                                te.setStringValue(res.getValue());
                                break;
                            case "array":
                                te.setArray(getValueList(getListFromString(res.getValue())));
                                break;
                        }
                        break;
                }
            } else {
                // [ Arrays
                if (!te.getType().equals("array")) {
                    error("获取的值并非Array");
                    return;
                }
                String type = te.getArrayElementAt(0).getType();
                TreeNode array = assign1.getChildAt(1);
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
                    TreeNode array1 = array.getChildAt(2);
                    if ("=".equals(array1.getChildAt(0).getName())) {
                        TreeNode array2 = array1.getChildAt(1);
                        // Operations
                        String str;
                        switch (array2.getChildAt(0).getName()) {
                            case "Character":
                                if (!type.equals("char")) {
                                    error("此处需要char类型");
                                    return;
                                }
                                // 判断字符是否为空
                                if (array2.getChildAt(0).getChildAt(1).getChildAt(0).getName().equals("'")) {
                                    te.setArrayAt(Integer.parseInt(res.getValue()), new Value("char", ""));
                                    break;
                                }
                                str = array2.getChildAt(0).getChildAt(1).getChildAt(0).getName();
                                te.setArrayAt(Integer.parseInt(res.getValue()), new Value("char", str));
                            case "Expression":
                                Value result = Calculate(array2.getChildAt(0));
                                if (result == null || result.getType() == null) {
                                    error("表达式有误！");
                                    return;
                                }
                                switch (type) {
                                    case "int":
                                        switch (result.getType()) {
                                            case "int":
//                                                te.setIntValue(result.getValue());
                                                te.setArrayAt(Integer.parseInt(res.getValue()), new Value("int", result.getValue()));
                                                break;
                                            case "real":
                                                error("无法将real强制转换为int");
                                                break;
                                            case "char":
                                                te.setArrayAt(Integer.parseInt(res.getValue()), new Value("int", result.getValue()));
                                                break;
                                        }
                                        break;
                                    case "real":
                                        switch (result.getType()) {
                                            case "int":
                                            case "real":
                                                te.setArrayAt(Integer.parseInt(res.getValue()), new Value("real", result.getValue()));
                                                break;
                                            case "char":
                                                error("无法得到一个real类型的值");
                                                break;
                                        }
                                        break;
                                    case "char":
                                        te.setStringValue(result.getValue());
                                        break;
                                }
                        }
                    }


//                    List<Value> values = new ArrayList<>();
//                    for (int i = 0; i < Integer.parseInt(res.getValue()); i++) {
//                        values.add(new Value(type));
//                    }
//                    te.setArray(values);

                }
                TreeNode array1 = array.getChildAt(array.getChildrenNum()-1);
                if ("=".equals(array1.getChildAt(0).getName())) {
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
                        te.setArray(values);
                    } else if (array2.getChildAt(0).getName().equals("String")) {
                        String x = getString(array2.getChildAt(0));
                        if (x == null) {
                            error("未找到对应字符串");
                            return;
                        }
                        te.setArray(getValueList(getListFromString(x)));
                    } else {
                        Value res = Calculate(array2.getChildAt(0));
                        if (res == null) {
                            error("数组索引错误");
                            return;
                        }

                    }
                }
                TreeNode tnode = node.getChildAt(2);
                while (tnode.getChildrenNum() != 1) {
                    if ("=".equals(tnode.getChildAt(2).getChildAt(0).getName())) {
                        Value x = Value4(tnode.getChildAt(2).getChildAt(1));
                        if (x == null) {
                            error("数值有误");
                            return;
                        }
                        switch (type) {
                            case "int":
                                switch (x.getType()) {
                                    case "int":
                                        te.setIntValue(x.getValue());
                                        break;
                                    case "real":
                                        error("无法将real转换为int");
                                        return;
                                    case "char":
                                        te.setIntValue(String.valueOf(Integer.valueOf(x.getValue())));
                                        break;
                                }
                                break;
                            case "real":
                                switch (x.getType()) {
                                    case "int":
                                        te.setRealValue(x.getValue());
                                        break;
                                    case "real":
                                        te.setRealValue(x.getValue());
                                        return;
                                    case "char":
                                        te.setRealValue(String.valueOf(Integer.valueOf(x.getValue())));
                                        break;
                                }
                                break;
                            case "char":
                                te.setStringValue(x.getValue());
                                break;
                        }
                    }
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
            return node.getChildAt(1).getChildAt(0).getName();
        }
    }

    private void State(TreeNode node) {
        if (isBreak || isContinue) return;
        String type = node.getChildAt(0).getName();
        String id = node.getChildAt(1).getName();
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
                    String tid = child.getChildAt(0).getChildAt(0).getChildAt(1).getName();
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
                            int x = 0;
//                            List<Value> values = new ArrayList<>();
                            element.setArrayAt(x, Value4(array2.getChildAt(1)));
                            x++;
                            TreeNode temp = array2.getChildAt(2);
                            while (temp.getChildrenNum() != 1) {
                                if (x >= element.getArray().size()) {
                                    error("超出最大范围");
                                    return;
                                }
                                element.setArrayAt(x, Value4(temp.getChildAt(1)));
                                x++;
                                temp = temp.getChildAt(2);
                            }
//                            element.setArray(values);
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
                    String tid = tnode.getChildAt(1).getName();
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
                    tnode = tnode.getChildAt(3);
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
                        if (child.getChildAt(1).getChildAt(0).getChildAt(1).getChildAt(0).getName().equals("'")) {
                            te = new TableElement(id, type, level);
                            te.setStringValue("");
                            table.add(te);
                            break;
                        }
                        next = child.getChildAt(1).getChildAt(0).getChildAt(1).getChildAt(0).getName();
                        te = new TableElement(id, type, level);
                        te.setStringValue(next);
                        table.add(te);
                        break;
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
                    String tid = tnode.getChildAt(1).getName();
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
                    tnode = tnode.getChildAt(3);
                }

        }
    }

    private Value Value4(TreeNode node) {
        String name = node.getChildAt(0).getName();
        String next;
        switch (name) {
            case "Character":
                // 判断字符是否为空
                if (node.getChildAt(0).getChildAt(1).getChildAt(0).getName().equals("'")) {
                    return new Value("char", "");
                }
                next = node.getChildAt(0).getChildAt(1).getChildAt(0).getName();
                if (next == null) {
                    error("无法获取下一个string");
                    return null;
                }
                return new Value("char", next);
            case "Expression":
                Value result = Calculate(node.getChildAt(0));
                if (result == null || result.getType() == null) {
                    error("表达式有误！");
                    return null;
                }
                return result;
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
        if (CheckInt(name)) {
            return new Value("int", name);
        } else if (CheckDigital(name)) {
            return new Value("real", name);
        } else if (name.equals("true")) {
            return new Value("int", String.valueOf(1));
        } else if (name.equals("false")) {
            return new Value("int", String.valueOf(0));
        } else if (name.equals("Scan")) {
             return Scan(child);
        } else {
            String n = node.getChildAt(1).getChildAt(0).getName();
            switch (n) {
                case "ε":
                    String id = node.getChildAt(0).getName();
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
                    Value indexName = Calculate(node.getChildAt(1).getChildAt(1));
                    String aid = node.getChildAt(0).getName();
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
                            return new Value("int", target.getValue());
                        }
                        return new Value("real", target.getValue());
                    }
            }
        }
        return null;
    }

    private void Print(TreeNode node) {
        TreeNode child = node.getChildAt(2);
        String res;
        if (child.getChildAt(0).getName().equals("Expression")) {
            Value target = Calculate(node.getChildAt(2).getChildAt(0));
            if (target == null) {
                error("运算式有误！");
                return;
            }
            res = target.getValue();
        } else {
            res = getString(child.getChildAt(0));
        }
        System.out.println(res);

    }

    private Value Scan(TreeNode node) {
        TreeNode child = node.getChildAt(2);
        String input = getNextInput();
        if (input == null) {
            error("没有任何输出");
            return null;
        }
        String id = child.getChildAt(0).getName();
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
                String tarType = te.getArrayElementAt(0).getType();
                te.setArrayAt(index, new Value(tarType, input));
                Value target = te.getArrayElementAt(index);

                return new Value(tarType, target.getValue());
        }
        return null;
    }

    private void C(TreeNode node) {
        Value condition = Calculate(node.getChildAt(2));
        if (condition == null) {
            error("对应条件错误");
            return;
        }
        TreeNode judge = node.getChildAt(4);
        if (Double.parseDouble(condition.getValue()) != 0) {
            level++;
            S(judge.getChildAt(0).getChildAt(0).getChildAt(1));
            level--;
            table.update(level);
        } else {
            TreeNode Else = judge.getChildAt(0).getChildAt(1);
            TreeNode judge2;
            while (!Else.getChildAt(0).getName().equals("ε")) {
                judge2 = Else.getChildAt(1);
                if (judge2.getChildrenNum() == 1) {
                    level++;
                    S(judge2.getChildAt(0).getChildAt(1));
                    level--;
                    table.update(level);
                    return;
                }
                condition = Calculate(judge2.getChildAt(2));
                if (condition == null) {
                    error("对应条件错误");
                    return;
                }
                if (Double.parseDouble(condition.getValue()) != 0) {
                    level++;
                    S(judge2.getChildAt(4).getChildAt(0).getChildAt(1));
                    level--;
                    table.update(level);
                    return;
                } else {
                    Else = judge2.getChildAt(4).getChildAt(1);
                    if (Else.getChildAt(1).getChildrenNum() == 1) {
                        level++;
                        S(Else.getChildAt(1).getChildAt(0).getChildAt(1));
                        level--;
                        table.update(level);
                        return;
                    }
                }
            }
        }
    }

    private boolean isBreak = false;
    private boolean isContinue = false;

    private void L(TreeNode node) {
        TreeNode child = node.getChildAt(0);
        switch (child.getName()) {
            case "While":
                While(child);
                break;
            case "For":
                For(child);
                break;
        }
    }

    private void While(TreeNode node) {
        whileNum++;
        level++;
        TreeNode condition = node.getChildAt(2);
        if (condition == null) {
            error("判断条件有误");
            whileNum--;
            level--;
            table.update(level);
            return;
        }
        Value con;
        while(true) {
            con = Calculate(condition);
            if (con == null) {
                error("判断条件有误！");
                whileNum--;
                level--;
                table.update(level);
                return;
            }
            if (Double.parseDouble(con.getValue()) == 0) {
                whileNum--;
                level--;
                table.update(level);
                return;
            }
            S(node.getChildAt(4).getChildAt(0).getChildAt(1));
            if (isContinue) {
                isContinue = false;
                continue;
            }
            if (isBreak) {
                isBreak = false;
                whileNum--;
                level--;
                table.update(level);
                return;
            }
        }
    }

    private void For(TreeNode node) {
        forNum++;
        level++;
        TreeNode first = node.getChildAt(2);
        switch (first.getChildAt(0).getName()) {
            case "Assign":
                Assign(first.getChildAt(0));
                break;
            case "State":
                State(first.getChildAt(0));
                break;
        }
        TreeNode second = node.getChildAt(3);
        TreeNode third = node.getChildAt(5);
        Value condition;
        while (true) {
            condition = Calculate(second);
            if (condition == null) {
                error("判断条件有误！");
                whileNum--;
                level--;
                table.update(level);
                return;
            }
            if (Double.parseDouble(condition.getValue()) == 0) {
                whileNum--;
                level--;
                table.update(level);
                return;
            }
            S(node.getChildAt(7).getChildAt(0).getChildAt(1));
            if (isContinue) {
                isContinue = false;
                continue;
            }
            if (isBreak) {
                isBreak = false;
                whileNum--;
                level--;
                table.update(level);
                return;
            }
            Assign(third);
        }
    }
}
