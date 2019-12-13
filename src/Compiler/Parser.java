package Compiler;

import Utils.GrammarMap;
import Utils.TreeNode;
import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.*;
import java.util.*;
import java.util.List;

public class Parser {
    private File locationFile = new File("./output/out");
    private List<String> sentence = new ArrayList<>();
    private OutputStream out = System.out;

    private GrammarMap gm = new GrammarMap();
    private JSONObject grammar = gm.getGrammar();                           // 获取文法
    private String start = gm.getStartSymbol();                             // 得到开始符号
    private JSONObject firstSet;
    private JSONObject followSet;
    private JSONObject selectSet = new JSONObject();
    private JSONObject analysisTable = new JSONObject();

    public void setLocationFile(File locationFile) {
        this.locationFile = locationFile;
    }

    public void setOut(OutputStream out) {
        this.out = out;
    }

    /**
     * 初始化输入程序
     * @throws IOException 抛出错误
     */
    private void inputProgram() throws IOException {
        try {
            sentence.clear();
            BufferedReader br = new BufferedReader(new FileReader(locationFile));
            String tmp;
            while ((tmp = br.readLine()) != null && (!tmp.equals(""))) {
                sentence.add(tmp);
            }
        } catch (IOException e) {
            e.printStackTrace();
            out.write("文件读取失败".getBytes());
        }
    }

    /**
     * 预处理，把语法分析得到的东西预处理
     * @throws IOException IO
     */
    private void pretreated() throws IOException {
        inputProgram();
        if (sentence.size() == 0) {
            out.write("No Sentence!".getBytes());
            return;
        }
        List<String> sentences = new ArrayList<>();
        for (String section : sentence) {
            String body = section.substring(1, section.length() - 1);
            String[] info = body.split(", ");
            switch (info[0]) {
                case "String":
                    sentences.add("string");
                    break;
                case "Number":
                    sentences.add("number");
                    break;
                case "Identifier":
                    sentences.add("identifier");
                    break;
                default:
                    sentences.add(info[1]);
                    break;
            }
        }
        sentence = sentences;
    }

    @SuppressWarnings("unchecked")
    private List<String> getSingleFirst(Object non_terminal) {
        List<String> exports = (List<String>) grammar.get(non_terminal);        // 产生式
        Set<String> res = new HashSet();                                        // 结果
        for (String s : exports) {                                              // 遍历每一条产生式
            if (s.equals("ε")) {                                                // 如果是空产生式，则first集有空
                res.add("ε");
                continue;
            }
            String[] export = s.split(" ");                               // 将产生式分开
            if (!Character.isUpperCase(export[0].charAt(0))) {                  // 如果第一个为终结符，加入first集中
                res.add(export[0]);
            } else {                                                            // 如果为非终结符，则要往下判断
                int i = 0;
                while (i < export.length) {                                     // 遍历产生式每个元素
                    boolean is_Empty = false;
                    if (!Character.isUpperCase(export[i].charAt(0))) {          // 判断是否为终结符
                        res.add(export[i]);                                     // 若是，则加入first并跳出循环
                        break;
                    }
                    List<String> next = getSingleFirst(export[i]);              // 若为非终结符，则取其first集
                    for (String tmp : next) {                                   // 遍历产生式中其他非终结符的first集
                        if (tmp.equals("ε")) {                                  // 若为空，则表明可以继续向后看
                            is_Empty = true;
                        } else {                                                // 将first集中除空以外加入res中
                            res.add(tmp);
                        }
                    }
                    if (!is_Empty) { break; }                                   // 若后一个非终结符产生式不为空，跳出循环
                    i++;                                                        // 若为空，继续查看之后符
                }
                if (i == export.length) { res.add("ε"); }                       // 若产生式中所有元素都可为空，则将ε加入first集
            }
        }
        return new ArrayList<>(res);
    }

    private void getFirst() {
        HashMap<Object, List<String>> res = new HashMap<>();
        for (Object key : grammar.keySet()) {
            res.put(key, getSingleFirst(key));
        }
        firstSet = new JSONObject(res);
    }

    @SuppressWarnings("unchecked")
    private void getFollow() {
        getFirst();
        HashMap<Object, List<String>> map = new HashMap<>();
        for (Object key : grammar.keySet()) {               // 初始化Follow集
            map.put(key, new ArrayList<>());
            if (key.equals(start)) {                        // 若为开始符号，则在Follow集里加入$
                map.get(key).add("$");
            }
        }
        while (true) {                                                          // 无限循环
            boolean is_Changed = false;
            for (Object key : grammar.keySet()) {                               // 遍历每一个产生式
                List<String> exports = (List<String>) grammar.get(key);         // 获取产生式的右部
                for (String s : exports) {
                    String[] export = s.split(" ");                       // 将右部分开
                    for (int i = 0; i < export.length - 1; i++) {
                        if (Character.isUpperCase(export[i].charAt(0))) {       // 判断前一个为非终结符
                            if (Character.isUpperCase(export[i+1].charAt(0))) { // 后一个也为非终结符
                                for (String t : (List<String>)firstSet.get(export[i+1])) {
                                    if (t.equals("ε")) continue;
                                    if (!map.get(export[i]).contains(t)) {
                                        is_Changed = true;
                                        map.get(export[i]).add(t);
                                    }
                                }
                            } else {                                                // 若为终结符
                                if (!map.get(export[i]).contains(export[i+1])) {    // 若不在原follow集中
                                    is_Changed = true;
                                    map.get(export[i]).add(export[i+1]);
                                }
                            }
                        }
                    }
                    for (int i = export.length - 1; i >= 0; i--) {                  // 倒序扫描产生式
                        if (Character.isUpperCase(export[i].charAt(0))) {           // 若最后一个为非终结符
                            for (String t : map.get(key)) {                         // 将左部的follow集加到这个非终结符
                                if (!map.get(export[i]).contains(t)) {
                                    is_Changed = true;
                                    map.get(export[i]).add(t);
                                }
                            }                                                       // 遇到终结符跳出
                        } else {
                            break;
                        }
                        List<String> lastFirst = (List<String>) firstSet.get(export[i]);    // 查看非终结符的first集
                        if (!lastFirst.contains("ε")) break;                                // 若不可为空跳出循环
                    }
                }
            }
            if (!is_Changed) {          // 如果没有发生改变，跳出循环
                break;
            }
        }
        map.get("Else").remove("else");
        followSet = new JSONObject(map);
    }

    @SuppressWarnings("unchecked")
    private void getSelect() {
        getFollow();
        for (Object key : grammar.keySet()) {                               // 获取产生式
            HashMap<String, List<String>> subSelect = new HashMap<>();      // 建立select集
            List<String> exports = (List<String>) grammar.get(key);
            for (String s : exports) {                                      // 遍历各个产生式
                String [] export = s.split(" ");
                if (Character.isUpperCase(export[0].charAt(0))) {           // 如果产生式第一个为非终结符
                    List<String> temp = (List<String>) firstSet.get(export[0]);        // 获取first集
                    List<String> first = new ArrayList<>(temp);
                    first.remove("ε");
                    int i = 0;
                    for (; i < export.length; i++) {                    // 如果第一个可为空，则把第二个非终结符的first集加进去
                        List<String> tmp= (List<String>) firstSet.get(export[i]);
                        List<String> eachFirst = new ArrayList<>();
                        if (tmp != null) {
                            eachFirst.addAll(tmp);
                        }
                        if (tmp != null && eachFirst.contains("ε")) {
                            eachFirst.remove("ε");
                            for (var f: eachFirst) {
                                if (!first.contains(f)) {
                                    first.add(f);
                                }
                            }
//                            first.addAll(eachFirst);
                        } else break;
                    }
                    if (i == export.length) first.addAll((List<String>) followSet.get(key));  // 如果全可为空，则加入follow
                    subSelect.put(s, first);
                } else if (s.equals("ε")) {                 // 如果为空，则加入follow集
                    subSelect.put(s, (List<String>) followSet.get(key));
                } else {                                    // 如果为终结符，则加入终结符
                    subSelect.put(s, Collections.singletonList(export[0]));
                }
            }
            selectSet.put(key, subSelect);
        }
    }

    /**
     *
     * @return Terminal set
     */
    @SuppressWarnings("unchecked")
    private Set getTerminalSet() {
        Set res = new HashSet();
        for (Object key : firstSet.keySet()) {
            res.addAll((List<String>) firstSet.get(key));
        }
        for (Object key : followSet.keySet()) {
            res.addAll((List<String>) followSet.get(key));
        }
        res.remove("ε");
        return res;
    }

    @SuppressWarnings("unchecked")
    private Set getNonTerminalSet() {
        return new HashSet(firstSet.keySet());
    }

    @SuppressWarnings("unchecked")
    private void getAnalysisTable() {
        getSelect();
        Set terminalSet = getTerminalSet();
        Set nonTerminalSet = getNonTerminalSet();
        for (Object nt : nonTerminalSet) {
            HashMap<String, List<String>> map = new HashMap<>();
            HashMap select = (HashMap) selectSet.get(nt);                   // 取select集
            for (Object t : terminalSet) {
                map.put((String) t, new ArrayList<>());
            }
            for (Object s : select.keySet()) {
                List<String> right = (List<String>) select.get(s);
                for (String r : right) {
                    map.get(r).add(nt + "->" + s);
                }
            }
            List<String> follows = (List<String>) followSet.get(nt);
            for (String follow : follows) {
                if (map.get(follow).size() == 0) {
                    map.get(follow).add("synch");
                }
            }
            analysisTable.put(nt, map);
        }
    }

    private void reverse(TreeNode root) {
        if (root.getChildren().isEmpty()) return;
        Collections.reverse(root.getChildren());
        List<TreeNode> children = root.getChildren();
        for (TreeNode t : children) {
            reverse(t);
        }
    }

    private javax.swing.tree.DefaultMutableTreeNode clone(TreeNode root) {
        DefaultMutableTreeNode t = new DefaultMutableTreeNode(root.getName());
        if (!root.getChildren().isEmpty()) {
            for (TreeNode n : root.getChildren()) {
                t.add(clone(n));
            }
        }
        return t;
    }

    @SuppressWarnings("unchecked")
    private void Analyse() throws IOException {
        getAnalysisTable();
        Stack<String> sen = new Stack<>();
        sen.push("$");
        for (int i = sentence.size() - 1; i >= 0; i--) {
            sen.push(sentence.get(i));
        }
        Stack<String> stack = new Stack<>();
        stack.push("$");
        stack.push(start);
        Set nonTerminalSet = getNonTerminalSet();
        int num_error = 0;
        JPanel panel = new JPanel();
        TreeNode root = new TreeNode(start);
        Stack<TreeNode> tree = new Stack<>();
        tree.push(root);
        while (!stack.empty() || !sen.empty()) {
            out.write(stack.toString().getBytes());
            out.write("   ".getBytes());
            out.write(sen.toString().getBytes());
            out.write("   ".getBytes());
            String peek = stack.peek();
            if (nonTerminalSet.contains(peek)) {
                HashMap<String, List<String>> row = (HashMap<String, List<String>>) analysisTable.get(peek);
                List<String> cell = row.get(sen.peek());
                if (cell == null || cell.size() != 1) {
                    out.write("Wrong: Cannot find the production!\n".getBytes());
                    num_error++;
                    sen.pop();
                    continue;
                }
                // 恐慌模式
                if (cell.get(0).equals("synch")) {
                    out.write("Wrong: Cannot find the production!\n".getBytes());
                    num_error++;
                    stack.pop();
                    tree.pop();
                    continue;
                }
                String production = cell.get(0);
                String[] section = production.split("->");
                String[] right = section[1].split(" ");
                stack.pop();
                TreeNode n = tree.pop();
                for (int i = right.length - 1; i >= 0; i--) {
                    if (Character.isUpperCase(right[i].charAt(0))) {
                        TreeNode t = new TreeNode(right[i]);
                        n.addChildren(t);
                        tree.push(t);
                    } else {
                        n.addChildren(new TreeNode(right[i]));
                    }
                }
                out.write(production.getBytes());
                if (right.length == 1 && right[0].equals("ε")){
                    out.write("\n".getBytes());
                    continue;
                }
                for (int i = right.length - 1; i >= 0; i--) {
                    stack.push(right[i]);
                }
            } else {
                if (peek.equals(sen.peek())) {
                    stack.pop();
                    sen.pop();
                } else {
                    out.write("Wrong: There is a terminator that cannot match.\n".getBytes());
                    num_error++;
                    stack.pop();
                }
            }
            out.write("\n".getBytes());
        }
        if (num_error == 0) {
            System.out.println("Analyse Successful!");
            reverse(root);
            DefaultMutableTreeNode treeNode = clone(root);
            getTree(treeNode);
        } else {
            System.out.println("There are " + num_error + " errors in the program.");
            reverse(root);
            DefaultMutableTreeNode treeNode = clone(root);
            getTree(treeNode);
        }
    }

    private void getTree(DefaultMutableTreeNode node) {
        JTree jtree = new JTree(node);
        JFrame frame=new JFrame("Syntax Tree");
        frame.setSize(1080,900);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JScrollPane(jtree));
        frame.pack();
        frame.setVisible(true);
    }

    public void start() throws IOException {
        pretreated();
        Analyse();
    }

    public static void main(String[] args) throws IOException {
        Parser p = new Parser();
        p.getAnalysisTable();
        FileOutputStream fos = new FileOutputStream(new File("./output/test"));
        fos.write(p.analysisTable.toString().getBytes());
        System.out.println(p.firstSet.get("State2"));
    }
}
