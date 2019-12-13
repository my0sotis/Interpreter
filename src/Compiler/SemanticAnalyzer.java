package Compiler;

import Utils.Table;
import Utils.TreeNode;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SemanticAnalyzer {
    private Table table = new Table();
    private TreeNode root;

    private int errorNum = 0;
    private List<String> errorInfo = new ArrayList<>();

    private final File dataLocation = new File("./output/data.table");
    private final File stringLocation = new File("./output/string.table");
    private final File variableLocation = new File("./output/variable.table");

    private int level = 0;
    private List<String> dataTable = new ArrayList<>();
    private int dataIndex = 0;
    private List<String> stringTable = new ArrayList<>();
    private int stringIndex = 0;
    private List<String> variableTable = new ArrayList<>();
    private int variableIndex = 0;
    private int inputIndex = 0;


    public SemanticAnalyzer(TreeNode node) {
        root = node;
        LoadDataTable();
        LoadStringTable();
        LoadVariableTable();
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
            variableTable.clear();
            BufferedReader br = new BufferedReader(new FileReader(stringLocation));
            String tmp;
            while ((tmp = br.readLine()) != null && (!tmp.equals(""))) {
                variableTable.add(tmp);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("文件读取失败");
        }
    }

    private void LoadVariableTable() {
        try {
            dataTable.clear();
            BufferedReader br = new BufferedReader(new FileReader(variableLocation));
            String tmp;
            while ((tmp = br.readLine()) != null && (!tmp.equals(""))) {
                dataTable.add(tmp);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("文件读取失败");
        }
    }

    private String getNextData() {
        if (dataIndex + 1 >= dataTable.size()) {
            error("Cannot get number");
            return null;
        }
        return dataTable.get(++dataIndex);
    }

    private String getNextString() {
        if (stringIndex + 1 >= stringTable.size()) {
            error("Cannot get number");
            return null;
        }
        return stringTable.get(++stringIndex);
    }

    private String getNextVariable() {
        if (variableIndex + 1 >= variableTable.size()) {
            error("Cannot get number");
            return null;
        }
        return variableTable.get(++variableIndex);
    }

    private void error(String error) {
        errorNum++;
        String message = "Wrong: " + error;
        errorInfo.add(message);
    }

    public void Analyse() {

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
            if (content.equals("Jump")) {

            } else if (content.equals("Print")) {
                Print(current);
            } else if (content.equals("State")) {

            } else if (content.equals("Assign")) {

            }
        }
    }

    private void Assign(TreeNode node) {
        TreeNode leftNode = node.getChildAt(0);
        String leftValue = leftNode.getName();
        if (table.getElementAllLever(leftValue, level) != null) {
            TreeNode assign1 = node.getChildAt(1);
            if (assign1.getChildAt(0).getName().equals("=")) {
                switch (assign1.getChildAt(1).getChildAt(0).getName()) {
                    case "Character":

                        break;
                    case "Expression":

                        break;
                    case "String":

                        break;
                }
            } else {

            }
        } else {
            String info = "变量" + leftValue +"未在使用前声明。";
            error(info);
            return;
        }

    }

    private List<TreeNode> ExpressionLeafNode = new ArrayList<>();

    private void findLeafNode(TreeNode node) {
        if (node.getChildrenNum() == 0) {
            if (!node.getName().equals("ε")) {
                ExpressionLeafNode.add(node);
            }
        } else {
            for (var c: node.getChildren()) {
                findLeafNode(c);
            }
        }
    }

    // Maybe Abandon
    private boolean CheckInt(TreeNode node) {
        ExpressionLeafNode.clear();
        String REGEX_INT = "^[+|-]?((\\d+\\.)|(0x(\\d|[A-F]|[a-f])+\\.))$";
        findLeafNode(node);
        for (var n : ExpressionLeafNode) {
            if (!Pattern.matches(REGEX_INT, n.getName())) {
                return false;
            }
        }
        return true;
    }

    private String Expression(TreeNode node) {
        return String.valueOf(Calculate(node));
    }

    private double or(double x, double y) {
        if (x == 0 && y == 0) {
            return 0;
        }
        return 1;
    }

    private double and(double x, double y) {
        if (x != 0 && y != 0) {
            return 1;
        }
        return 0;
    }

    private double equal(double x, double y) {
        if (x == y) {
            return 1;
        }
        return 0;
    }

    private double neq(double x, double y) {
        if (x != y) {
            return 1;
        }
        return 0;
    }

    private double g(double x, double y) {
        if (x > y) {
            return 1;
        }
        return 0;
    }

    private double ge(double x, double y) {
        if (x >= y) {
            return 1;
        }
        return 0;
    }

    private double l(double x, double y) {
        if (x < y) {
            return 1;
        }
        return 0;
    }

    private double le(double x, double y) {
        if (x <= y) {
            return 1;
        }
        return 0;
    }

    private int or(int x, int y) {
        if (x == 0 && y == 0) {
            return 0;
        }
        return 1;
    }

    private int and(int x, int y) {
        if (x != 0 && y != 0) {
            return 1;
        }
        return 0;
    }

    private int equal(int x, int y) {
        if (x == y) {
            return 1;
        }
        return 0;
    }

    private int neq(int x, int y) {
        if (x != y) {
            return 1;
        }
        return 0;
    }

    private int g(int x, int y) {
        if (x > y) {
            return 1;
        }
        return 0;
    }

    private int ge(int x, int y) {
        if (x >= y) {
            return 1;
        }
        return 0;
    }

    private int l(int x, int y) {
        if (x < y) {
            return 1;
        }
        return 0;
    }

    private int le(int x, int y) {
        if (x <= y) {
            return 1;
        }
        return 0;
    }

    // Unfinished
    private String Calculate(TreeNode node) {
        return null;
    }

    private int intCalculate(TreeNode node) {
        if (node.getChildAt(1).getChildAt(0).getName().equals("ε"))
            return intCalculate(node.getChildAt(0));
        switch (node.getName()) {
            case "Expression":
                return or(intCalculate(node.getChildAt(0)), intCalculate(node.getChildAt(1).getChildAt(1)));
            case "E2":
                return and(intCalculate(node.getChildAt(0)), intCalculate(node.getChildAt(1).getChildAt(1)));
            case "E3":
                return intCalculate(node.getChildAt(0)) | intCalculate(node.getChildAt(1).getChildAt(1));
            case "E4":
                return intCalculate(node.getChildAt(0)) ^ intCalculate(node.getChildAt(1).getChildAt(1));
            case "E5":
                return intCalculate(node.getChildAt(0)) & intCalculate(node.getChildAt(1).getChildAt(1));
            case "E6":
                switch (node.getChildAt(1).getChildAt(0).getName()) {
                    case "==":
                        return equal(intCalculate(node.getChildAt(0)), intCalculate(node.getChildAt(1).getChildAt(1)));
                    case "!=":
                    case "<>":
                        return neq(intCalculate(node.getChildAt(0)), intCalculate(node.getChildAt(1).getChildAt(1)));
                }
                break;
            case "E7":
                switch (node.getChildAt(1).getChildAt(0).getName()) {
                    case ">":
                        return g(intCalculate(node.getChildAt(0)), intCalculate(node.getChildAt(1).getChildAt(1)));
                    case ">=":
                        return ge(intCalculate(node.getChildAt(0)), intCalculate(node.getChildAt(1).getChildAt(1)));
                    case "<":
                        return l(intCalculate(node.getChildAt(0)), intCalculate(node.getChildAt(1).getChildAt(1)));
                    case "<=":
                        return le(intCalculate(node.getChildAt(0)), intCalculate(node.getChildAt(1).getChildAt(1)));
                }
                break;
            case "E8":
                switch (node.getChildAt(1).getChildAt(0).getName()) {
                    case "<<":
                        return intCalculate(node.getChildAt(0))<<intCalculate(node.getChildAt(1).getChildAt(1));
                    case ">>":
                        return intCalculate(node.getChildAt(0))>>intCalculate(node.getChildAt(1).getChildAt(1));
                }
            case "E9":
                switch (node.getChildAt(1).getChildAt(0).getName()) {
                    case "+":
                        return intCalculate(node.getChildAt(0))+intCalculate(node.getChildAt(1).getChildAt(1));
                    case "-":
                        return intCalculate(node.getChildAt(0))-intCalculate(node.getChildAt(1).getChildAt(1));
                }
            case "E10":
                switch (node.getChildAt(1).getChildAt(0).getName()) {
                    case "*":
                        return intCalculate(node.getChildAt(0))*intCalculate(node.getChildAt(1).getChildAt(1));
                    case "/":
                        return intCalculate(node.getChildAt(0))/intCalculate(node.getChildAt(1).getChildAt(1));
                    case "%":
                        return intCalculate(node.getChildAt(0))%intCalculate(node.getChildAt(1).getChildAt(1));
                }
            case "E11":
                if (node.getChildAt(0).getName().equals("(")) {
                    return intCalculate(node.getChildAt(1));
                } else {
                    // Value 处理
//                    return intValue(node.getChildAt(0));
                }
        }
        return 0;
    }

    private double doubleCalculate(TreeNode node) {
        if (node.getChildAt(1).getChildAt(0).getName().equals("ε"))
            return doubleValue(node.getChildAt(0));
        switch (node.getName()) {
            case "Expression":
                return or(doubleCalculate(node.getChildAt(0)), doubleCalculate(node.getChildAt(1).getChildAt(1)));
            case "E2":
                return and(doubleCalculate(node.getChildAt(0)), doubleCalculate(node.getChildAt(1).getChildAt(1)));
            case "E3":
                error("Real values cannot be bitwise operated!");
                return doubleCalculate(node.getChildAt(1).getChildAt(1));
            case "E4":
                error("Real values cannot be bitwise operated!");
                return doubleCalculate(node.getChildAt(0));
//                return doubleCalculate(node.getChildAt(0)) ^ doubleCalculate(node.getChildAt(1).getChildAt(1));
            case "E5":
                error("Real values cannot be bitwise operated!");
                return doubleValue(node.getChildAt(0));
//                return doubleCalculate(node.getChildAt(0)) & doubleCalculate(node.getChildAt(1).getChildAt(1));
            case "E6":
                switch (node.getChildAt(1).getChildAt(0).getName()) {
                    case "==":
                        return equal(doubleCalculate(node.getChildAt(0)), doubleCalculate(node.getChildAt(1).getChildAt(1)));
                    case "!=":
                    case "<>":
                        return neq(doubleCalculate(node.getChildAt(0)), doubleCalculate(node.getChildAt(1).getChildAt(1)));
                }
                break;
            case "E7":
                switch (node.getChildAt(1).getChildAt(0).getName()) {
                    case ">":
                        return g(doubleCalculate(node.getChildAt(0)), doubleCalculate(node.getChildAt(1).getChildAt(1)));
                    case ">=":
                        return ge(doubleCalculate(node.getChildAt(0)), doubleCalculate(node.getChildAt(1).getChildAt(1)));
                    case "<":
                        return l(doubleCalculate(node.getChildAt(0)), doubleCalculate(node.getChildAt(1).getChildAt(1)));
                    case "<=":
                        return le(doubleCalculate(node.getChildAt(0)), doubleCalculate(node.getChildAt(1).getChildAt(1)));
                }
                break;
            case "E8":
                switch (node.getChildAt(1).getChildAt(0).getName()) {
                    case "<<":
                        error("Double values cannot be shifted!");
                        return doubleCalculate(node.getChildAt(0));
//                        return doubleCalculate(node.getChildAt(0))<<doubleCalculate(node.getChildAt(1).getChildAt(1));
                    case ">>":
                        error("Double values cannot be shifted!");
                        return doubleValue(node.getChildAt(0));
//                        return doubleCalculate(node.getChildAt(0))>>doubleCalculate(node.getChildAt(1).getChildAt(1));
                }
            case "E9":
                switch (node.getChildAt(1).getChildAt(0).getName()) {
                    case "+":
                        return doubleCalculate(node.getChildAt(0))+doubleCalculate(node.getChildAt(1).getChildAt(1));
                    case "-":
                        return doubleCalculate(node.getChildAt(0))-doubleCalculate(node.getChildAt(1).getChildAt(1));
                }
            case "E10":
                switch (node.getChildAt(1).getChildAt(0).getName()) {
                    case "*":
                        return doubleCalculate(node.getChildAt(0))*doubleCalculate(node.getChildAt(1).getChildAt(1));
                    case "/":
                        return doubleCalculate(node.getChildAt(0))/doubleCalculate(node.getChildAt(1).getChildAt(1));
                    case "%":
                        return doubleCalculate(node.getChildAt(0))%doubleCalculate(node.getChildAt(1).getChildAt(1));
                }
            case "E11":
                if (node.getChildAt(0).getName().equals("(")) {
                    return doubleCalculate(node.getChildAt(1));
                } else {
                    // Value 处理
                    return doubleValue(node.getChildAt(0));
                }
        }
        return 0;
    }

    // Unfinished
    private String intValue(TreeNode node) {
        TreeNode child = node.getChildAt(0);
        String name = child.getName();
        switch (name) {
            case "number":
                return getNextData();
            case "true":
                return String.valueOf(1);
            case "false":
                return String.valueOf(0);
            case "identifier":
                String n = child.getChildAt(1).getChildAt(0).getName();
                switch (n) {
                    case "ε":
                        String id = getNextVariable();
                        if (table.getElementAllLever(id, level) == null) {
                            String error = "变量" + name + "在使用前未声明";
                            error(error);
                            return null;
                        } else {
                            return table.getElementAllLever(id, level).getIntValue();
                        }
                    case "[":
                        break;
                }
                break;
            case "Scan":

        }
        return null;
    }

    // Unfinished
    private double doubleValue(TreeNode node) {
        return 0;
    }

    private boolean CheckID(TreeNode node, int level) {
        String name = node.getName();
        if (table.getElementAllLever(name, level) == null) {
            String error = "变量" + name + "在使用前未声明";
            error(error);
            return false;
        } else {

        }
        return true;    // Need to be delete.
    }

    private void Print(TreeNode node) {
        String res = Expression(node.getChildAt(2));
        System.out.println(res);
    }

    private void Scan(TreeNode node) {

    }
}
