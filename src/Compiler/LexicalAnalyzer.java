package Compiler;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class LexicalAnalyzer {
    private final static String[] KEYWORD = {
            "int", "real", "char", "while", "for", "break", "bool",
            "continue", "if", "else", "print", "scan", "return", "do",
            "define", "goto", "switch", "include", "typedef", "true", "false"
    };
    private final static String[] MONOCULAR_OP = {
            "+", "-", "*", "/", "%", "<", ">", "=",
            "&", "!", "|"
    };
    private final static String[] BINOCULAR_OP = {
            "+=", "-=", "*=", "/=", "%=",
            "<<", ">>", "&&", "||",
            "<=", ">=", "==", "!=", "<>",
            "++", "--"
    };
    private final static String[] DELIMITER = {
            ";", ",", "\"", "'", "(", "[", "{", ")", "]", "}"
    };

    private final static int NUM_KEYWORD = KEYWORD.length;
    private final static int NUM_MONOCULAR_OP = MONOCULAR_OP.length;
    private final static int NUM_BINOCULAR_OP = BINOCULAR_OP.length;
    private final static int NUM_DELIMITER = DELIMITER.length;
    private final static int NUM_TOTAL = NUM_KEYWORD + NUM_MONOCULAR_OP + NUM_BINOCULAR_OP + NUM_DELIMITER;
    private final static String REGEX_DIGITAL =
            "^[+|-]?((\\d+(\\.\\d+)?)|(0x(\\d|[A-F]|[a-f])+(\\.(\\d|[A-F]|[a-f])+)?))$";
    private static OutputStream out = System.out;
    private static File file = new File("./test");
    private static File variable_table = new File("./output/variable.table");
    private static File data_table = new File("./output/data.table");
    private static File string_table = new File("./output/string.table");

    public LexicalAnalyzer() {
        try {
            if(!variable_table.exists()) {
                variable_table.createNewFile();
            }
            FileWriter variableWriter = new FileWriter(variable_table);
            variableWriter.flush();
            variableWriter.close();
            if(!data_table.exists()) {
                data_table.createNewFile();
            }
            FileWriter dataWriter = new FileWriter(data_table);
            dataWriter.flush();
            dataWriter.close();
            if (!string_table.exists()) {
                string_table.createNewFile();
            }
            FileWriter stringWriter = new FileWriter(string_table);
            stringWriter.flush();
            stringWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置输出的方式
     * @param out 输出的方式
     */
    public void setOut(OutputStream out) {
        LexicalAnalyzer.out = out;
    }

    /**
     * 设置输出的方式
     * @param _file 需设置的文件
     */
    public void setFile(File _file) {
        LexicalAnalyzer.file = _file;
    }

    /**
     * 载入需处理的程序
     *
     * @param file 要载入的文件
     * @return 返回一个包含所有行的list
     */
    private static List<String> Load_file(File file) {
        List<String> list = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String tmp;
            while ((tmp = br.readLine()) != null) {
                list.add(tmp);
            }
        } catch (IOException e) {
            System.out.println("未找到对应文件。");
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 得到对应的标号
     *
     * @param _string 要找到的字符
     * @param _opcode 操作码
     * @return 返回对应的标号
     */
    private static int get_num(String _string, int _opcode) {
        switch (_opcode) {
            case 0:
                for (int i = 0; i < NUM_MONOCULAR_OP; i++) {
                    if (MONOCULAR_OP[i].equals(_string)) {
                        return (NUM_KEYWORD + i + 1);
                    }
                }
            case 1:
                for (int i = 0; i < NUM_BINOCULAR_OP; i++) {
                    if (BINOCULAR_OP[i].equals(_string)) {
                        return (NUM_KEYWORD + NUM_MONOCULAR_OP + i + 1);
                    }
                }
            case 2:
                for (int i = 0; i < NUM_DELIMITER; i++) {
                    if (DELIMITER[i].equals(_string)) {
                        return (NUM_TOTAL - NUM_DELIMITER + i + 1);
                    }
                }
            default:
                return -1;
        }
    }

    /**
     * 判断是否为关键字，如果是，返回标号，反之返回-1
     *
     * @param _string 需判断的字符串
     * @return 标号或-1
     */
    private static int is_Keyword(String _string) {
        for (int i = 0; i < NUM_KEYWORD; i++) {
            if (KEYWORD[i].equals(_string)) {
                return (i + 1);
            }
        }
        return -1;
    }

    /**
     * 判断是否为界限符
     *
     * @param _char 需判断的字符
     * @return 如果是，返回标号，反之返回-1
     */
    private static int is_Delimiter(char _char) {
        String tmp = String.valueOf(_char);
        for (int i = 0; i < NUM_DELIMITER; i++) {
            if (DELIMITER[i].equals(tmp)) {
                return (NUM_TOTAL - NUM_DELIMITER + i + 1);
            }
        }
        return -1;
    }

    /**
     * 得到需输出的字符串
     *
     * @param _no     标号
     * @param _string 原字符串
     * @param _row    行号
     * @param _column 列号
     * @return 输出的字符串
     */
    private static byte[] get_String(int _no, String _string, int _row, int _column) {
        StringBuilder sb = new StringBuilder();
        if (_no <= NUM_KEYWORD) {
            sb.append("<Keyword, ");
        } else if (_no <= NUM_KEYWORD + NUM_MONOCULAR_OP) {
            sb.append("<Monocular Operation, ");
        } else if (_no <= NUM_KEYWORD + NUM_MONOCULAR_OP + NUM_BINOCULAR_OP) {
            sb.append("<Binocular Operation, ");
        } else if (_no <= NUM_TOTAL) {
            sb.append("<Delimiter, ");
        } else if (_no == NUM_TOTAL + 1) {
            sb.append("<Number, ");
            input_data(_string);
        } else if (_no == NUM_TOTAL + 2) {
            sb.append("<Identifier, ");
            input_variable(_string);
        } else if (_no == NUM_TOTAL + 3) {
            sb.append("<String, ");
            input_string(_string);
        }
        sb.append(_string).append(", ")
                .append(_row + 1).append(":").append(_column + 1).append(">\n");
        return sb.toString().getBytes();
    }

    /**
     * 返回错误的输出
     *
     * @param _opcode 操作码
     * @param _row    行号
     * @param _column 列号
     * @return 报错信息
     */
    private static byte[] get_Error_String(int _opcode, int _row, int _column) {
        StringBuilder sb = new StringBuilder();
        sb.append("Error in (").append(_row + 1).append(":").append(_column + 1).append("): ");
        switch (_opcode) {
            case 0:
                sb.append("Cannot end with _.");
                break;
            case 1:
                sb.append("Cannot start with _.");
                break;
            case 2:
                sb.append("Wrong operation!");
                break;
            case 3:
                sb.append("Binocular operator misses item.");
                break;
            case 4:
                sb.append("Monocular operator misses item.");
                break;
            case 5:
                sb.append("Wrong numerical form.");
                break;
            case 6:
                sb.append("Wrong neq symbol.");
                break;
            case 7:
                sb.append("\" cannot be the end of a sentence.");
                break;
            case 8:
                sb.append("The escape character does not follow the specified character.");
                break;
            case 9:
                sb.append("There can only be one element in a single quote.");
                break;
            default:
                sb.append("Wrong expression!");
                break;
        }
        sb.append("\n");
        return sb.toString().getBytes();
    }

    /**
     * 输出变量到变量表中
     * @param _variable 变量名
     */
    private static void input_variable(String _variable) {
        try {
            FileOutputStream file_out = new FileOutputStream(variable_table, true);
            file_out.write((_variable + "\n").getBytes());
            file_out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 输出数据到数据表中
     * @param _data 数据
     */
    private static void input_data(String _data) {
        try {
            FileOutputStream file_out = new FileOutputStream(data_table, true);
            file_out.write((_data + "\n").getBytes());
            file_out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 输出字符串到字符串表中
     * @param _string 字符串
     */
    private static void input_string(String _string) {
        try {
            FileOutputStream fos = new FileOutputStream(string_table, true);
            fos.write((_string + "\n").getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 分析的主程序
     * @throws IOException 使用OutputStream可能会引发IOException，先抛出，到外部主程序中
     */
    public void Analyze() throws IOException {
        List<String> list = Load_file(file);                    // 得到所需list
        int last_token = 0;                                     // 上一个token的类型
        int may_error_row = -1;                                 // 可能发生错误的行号
        int may_error_column = -1;                              // 可能发生错误的列号
        String may_error_string = null;                         // 可能发生错误的字符
        String quota_mark = null;                               // 引号，表明后面为字符串
        List<Character> EscapeSymbol = new ArrayList<>();
        EscapeSymbol.add('\\');
        EscapeSymbol.add('\"');
        EscapeSymbol.add('\'');
        List<Character> EscapeChar = new ArrayList<>();
        EscapeChar.add('n');
        EscapeChar.add('r');
        EscapeChar.add('b');
        EscapeChar.add('t');
        EscapeChar.add('a');
        for (int i = 0; i < list.size(); i++) {                 // 遍历每一行，i为行号
            String string_test = list.get(i);                   // 本次要分析的字符串
            for (int j = 0; j < string_test.length(); j++) {    // 遍历该字符串
                char tmp = string_test.charAt(j);
                if (quota_mark != null && quota_mark.equals("\"")) {                       // 如果引号不为空，则说明接下来字符为String
                    StringBuilder sb = new StringBuilder();
                    if (j + 1 > string_test.length()) {
                        out.write(get_Error_String(7, i, j));
                        continue;
                    }
                    int k = j + 1;
                    sb.append(string_test.charAt(j));
                    boolean isFinished = false;
                    while (k < string_test.length()) {
                        if (string_test.charAt(k) == '\\') {
                            if (k + 1 <= string_test.length()
                                    && EscapeSymbol.contains(string_test.charAt(k+1))) {
                                sb.append(string_test.charAt(k+1));
                                k+=2;
                                continue;
                            } else if (k + 1 <= string_test.length()
                                        && EscapeChar.contains(string_test.charAt(k+1))) {
                                sb.append(string_test.charAt(k));
                                sb.append(string_test.charAt(k+1));
                                k+=2;
                                continue;
                            } else {
                                out.write(get_Error_String(8, i, j));
                                k+=2;
                                continue;
                            }
                        }
                        if (string_test.charAt(k) == '\"') {
                            out.write(get_String(NUM_TOTAL + 3, sb.toString(), i, j));
                            out.write(get_String(get_num(String.valueOf(string_test.charAt(k)), 2),
                                    String.valueOf(string_test.charAt(k)), i, k));
                            quota_mark = null;
                            last_token = get_num(String.valueOf(string_test.charAt(k)), 2);
                            j = k;
                            isFinished = true;
                            break;
                        }
                        sb.append(string_test.charAt(k));
                        k++;
                    }
                    if (isFinished) continue;
                } else if (quota_mark != null && quota_mark.equals("'")) {
                    if (string_test.charAt(j) == '\\') {
                        if (j + 2 <= string_test.length()
                            && EscapeSymbol.contains(string_test.charAt(j+1)) && string_test.charAt(j+2)=='\'') {
                            out.write(get_String(NUM_TOTAL + 3, String.valueOf(string_test.charAt(j+1)), i, j));
                            String quote = String.valueOf(string_test.charAt(j + 2));
                            out.write(get_String(get_num(quote, 2),
                                    quote, i, j+2));
                            quota_mark = null;
                            last_token = get_num(quote, 2);
                            j = j + 2;
                            continue;
                        } else if (j + 2 <= string_test.length()
                                && EscapeChar.contains(string_test.charAt(j+1)) && string_test.charAt(j+2)=='\''){
                            out.write(get_String(NUM_TOTAL+3, string_test.substring(j, j+2), i, j));
                            String quote = String.valueOf(string_test.charAt(j + 2));
                            out.write(get_String(get_num(quote, 2),
                                    quote, i, j+2));
                            quota_mark = null;
                            last_token = get_num(quote, 2);
                            j = j + 2;
                            continue;
                        } else {
                            out.write(get_Error_String(8, i, j));
                            j++;
                            continue;
                        }
                    } else {
                        if (j + 1 < string_test.length() && string_test.charAt(j+1) != '\'') {
                            out.write(get_Error_String(9, i, j));
                            j++;
                            continue;
                        } else {
                            out.write(get_String(NUM_TOTAL+3,String.valueOf(string_test.charAt(j)), i, j));
                            out.write(get_String(get_num(String.valueOf(string_test.charAt(j+1)), 2),
                                    String.valueOf(string_test.charAt(j+1)), i, j+1));
                            quota_mark = null;
                            j++;
                            continue;
                        }
                    }
                }
                if (Character.isLetter(tmp)) {                  // 判断为字母
                    StringBuilder sb = new StringBuilder();
                    sb.append(string_test.charAt(j));
                    if (j + 1 >= string_test.length()) {        // 若为最后一个字符，则直接输出为标识符
                        if (may_error_row != -1) {              // 若之前有可能发生错误，通过上下文得未发生错误，输出正确信息
                            out.write(get_String(last_token, may_error_string, may_error_row, may_error_column));
                            may_error_row = may_error_column = -1;  // 重置为-1和null
                            may_error_string = null;
                        }
                        out.write(get_String(NUM_TOTAL + 2, sb.toString(), i, j));      // 输出字符串
                        last_token = NUM_TOTAL + 2;                                         // 设置last_token
                        continue;                                                           // 跳出
                    }
                    int k = j + 1;
                    // 如若后面有符合要求的字符，加入需判断的字符串中
                    while (k < string_test.length() && (Character.isLetter(string_test.charAt(k))
                            || Character.isDigit(string_test.charAt(k)) || string_test.charAt(k) == '_')) {
                        sb.append(string_test.charAt(k));
                        k++;
                    }
                    int x = is_Keyword(sb.toString());                                      // 判断是否为关键字
                    if (x != -1) {
                        if (may_error_row != -1) {                          // 若是，则上部有错误，输出错误信息
                            out.write(get_Error_String(3, may_error_row, may_error_column));
                            may_error_row = may_error_column = -1;          // 重置记录信息
                            may_error_string = null;
                        }
                        out.write(get_String(x, sb.toString(), i, j));      // 输出正确输出
                        last_token = x;
                        j = k - 1;
                    } else {                                                // 如若为否则视为标识符
                        if (sb.toString().substring(sb.length() - 1, sb.length()).equals("_")) {      // 最后不可为_
                            out.write(get_Error_String(0, i, j));
                            j = k - 1;
                            continue;
                        }
                        if (may_error_row != -1) {                          // 上部可能发生的错误不会发生，正确输出即可
                            out.write(get_String(last_token, may_error_string, may_error_row, may_error_column));
                            may_error_row = may_error_column = -1;          // 重置错误信息
                            may_error_string = null;
                        }
                        out.write(get_String(NUM_TOTAL + 2, sb.toString(), i, j));              // 正确输出
                        last_token = NUM_TOTAL + 2;
                        j = k - 1;
                    }
                } else if (Character.isDigit(tmp)) {                                                 // 判断为数字
                    int k = j + 1;
                    StringBuilder sb = new StringBuilder();
                    sb.append(tmp);
                    // 将合乎要求的字符加入
                    while (k < string_test.length() && ( Character.isDigit(string_test.charAt(k))
                            || string_test.charAt(k) == '.' || string_test.charAt(k) == 'x'
                            || (string_test.charAt(k) <= 'F' && string_test.charAt(k) >= 'A'))) {
                        sb.append(string_test.charAt(k));
                        k++;
                    }
                    if (Pattern.matches(REGEX_DIGITAL, sb.toString())) {                // 通过正则表达式判断
                        last_token = NUM_TOTAL + 1;
                        if (may_error_row != -1) {
                            out.write(get_String(last_token, may_error_string, may_error_row, may_error_column));
                            may_error_row = may_error_column = -1;
                            may_error_string = null;
                        }
                        out.write(get_String(last_token, sb.toString(), i, j));
                    } else {                                                                // 若不符合正则，输出错误信息
                        out.write(get_Error_String(5, i, j));
                    }
                    j = k - 1;
                } else if (tmp == '+' || tmp == '-') {                                      // 若为+或者-
                    boolean mono = (last_token >= get_num(String.valueOf('<'), 0))  // 前面可为位运算和比较运算
                            && (last_token <= get_num(String.valueOf('|'), 0));
                    boolean bino = (last_token >= get_num("&&", 1))
                            && (last_token <= get_num("<>", 1));
                    if (last_token > NUM_KEYWORD && last_token <= NUM_TOTAL - NUM_DELIMITER
                            && !(mono || bino)) {
                        out.write(get_Error_String(2, i, j));               // 若上一位为运算符且非位运算和比较运算报错
                        continue;
                    }
                    if (j + 1 >= string_test.length()) {                            // +或者-位于最后一维，有可能出错
                        may_error_row = i;                                          // 输入错误信息
                        may_error_column = j;
                        may_error_string = String.valueOf(tmp);
                        last_token = get_num(String.valueOf(tmp), 0);
                        continue;
                    }
                    char next = string_test.charAt(j + 1);
                    if (next == tmp) {                                  // 有可能为++或者--
                        String sub = string_test.substring(j, j + 2);
                        int x = get_num(sub, 1);
                        if (may_error_row != -1) {                                  // 由上文可知，此处应有错误
                            out.write(get_Error_String(3, may_error_row, may_error_column));
                            may_error_row = may_error_column = -1;
                            may_error_string = null;
                        }
                        out.write(get_String(x, sub, i, j));
                        last_token = x;
                        j = j + 1;
                        continue;
                    } else if (next == '=') {                           // 可能为+=或者-=
                        if (j + 2 >= string_test.length()) {            // 当+=或-=位于最后
                            may_error_row = i;
                            may_error_column = j;
                            String sub = string_test.substring(j, j + 2);
                            last_token = get_num(sub, 1);
                            may_error_string = sub;
                            continue;
                        } else {                                            // 输出
                            String sub = string_test.substring(j, j + 2);
                            int x = get_num(sub, 1);
                            if (may_error_row != -1) {                      // 由于为运算符，前面出错，输出错误信息
                                out.write(get_Error_String(3, may_error_row, may_error_column));
                                may_error_row = may_error_column = -1;      // 重置可能错误的量
                                may_error_string = null;
                            }
                            out.write(get_String(x, sub, i, j));            // 正常输出
                            last_token = x;
                            j = j + 1;
                            continue;
                        }
                    }
                    // 若上一个token为数字或表示符，则表明此处为加减符号
                    if (last_token == NUM_TOTAL + 1 || last_token == NUM_TOTAL + 2
                                || last_token == get_num(")", 2) || last_token == get_num("]", 2)) {
                        String t = String.valueOf(tmp);
                        if (may_error_row != -1) {
                            out.write(get_Error_String(3, may_error_row, may_error_column));
                            may_error_row = may_error_column = -1;
                            may_error_string = null;
                        }
                        out.write(get_String(get_num(t, 0), t, i, j));
                        last_token = get_num(t, 0);
                        continue;
                    }
                    // 若都不是，则判断为数字的正负号
                    int k = j + 1;
                    StringBuilder sb = new StringBuilder();
                    sb.append(tmp);
                    if (string_test.charAt(j + 1) == ' ') k++;
                    char x = string_test.charAt(k);
                    while (k < string_test.length() && (Character.isDigit(string_test.charAt(k))
                            || string_test.charAt(k) == '.')) {
                        sb.append(string_test.charAt(k));
                        k++;
                    }
                    // 判断是否为正确的数字
                    if (Pattern.matches(REGEX_DIGITAL, sb.toString())) {
                        if (may_error_row != -1) {
                            out.write(get_String(last_token, may_error_string, may_error_row, may_error_column));
                            may_error_row = may_error_column = -1;
                            may_error_string = null;
                        }
                        last_token = NUM_TOTAL + 1;
                        out.write(get_String(last_token, sb.toString(), i, j));
                    } else {
                        out.write(get_Error_String(5, i, j));
                    }
                    j = k - 1;
                    // 若为*，/，%，=
                } else if (tmp == '*' || tmp == '/' || tmp == '%' || tmp == '=' || tmp == '!') {
                    // 若处于最后一个字符，则有可能出错
                    if (j + 1 >= string_test.length()) {
                        may_error_row = i;
                        may_error_column = j;
                        may_error_string = String.valueOf(tmp);
                        last_token = get_num(String.valueOf(tmp), 0);
                        continue;
                    }
                    char next = string_test.charAt(j + 1);
                    // 如果遇到//，视为注释并跳出本行分析
                    if (next == '/' && tmp == '/') break;
                    // 判断上一个为运算符，则报错
                    if (last_token > NUM_KEYWORD && last_token <= NUM_TOTAL - NUM_DELIMITER) {
                        out.write(get_Error_String(2, i, j));
                        continue;
                    }
                    // 可能为*=等
                    if (next == '=') {
                        if (j + 2 >= string_test.length()) {
                            may_error_row = i;
                            may_error_column = j;
                            String sub = string_test.substring(j, j + 2);
                            may_error_string = sub;
                            last_token = get_num(sub, 0);
                            continue;
                        } else {
                            String sub = string_test.substring(j, j + 2);
                            int x = get_num(sub, 1);
                            if (may_error_row != -1) {
                                out.write(get_Error_String(3, may_error_row, may_error_column));
                                may_error_row = may_error_column = -1;
                                may_error_string = null;
                            }
                            out.write(get_String(x, sub, i, j));
                            last_token = x;
                            j = j + 1;
                            continue;
                        }
                    }
                    // 若都不是，则为单个运算符
                    last_token = get_num(String.valueOf(tmp), 0);
                    if (may_error_row != -1) {
                        out.write(get_Error_String(3, may_error_row, may_error_column));
                        may_error_row = may_error_column = -1;
                        may_error_string = null;
                    }
                    out.write(get_String(last_token, String.valueOf(tmp), i, j));
                    // 若为<或>
                } else if (tmp == '<' || tmp == '>') {
                    // 若上一次字符为运算符，报错
                    if (last_token > NUM_KEYWORD && last_token <= NUM_TOTAL - NUM_DELIMITER) {
                        out.write(get_Error_String(2, i, j));
                        continue;
                    }
                    // 若为最后一个字符，则有可能报错
                    if (j + 1 >= string_test.length()) {
                        may_error_row = i;
                        may_error_column = j;
                        may_error_string = String.valueOf(tmp);
                        last_token = get_num(String.valueOf(tmp), 0);
                        continue;
                    }
                    char next = string_test.charAt(j + 1);
                    // 有可能为位运算，"<<"或者">>"
                    if (next == tmp) {
                        String sub = string_test.substring(j, j + 2);
                        int x = get_num(sub, 1);
                        if (may_error_row != -1) {
                            out.write(get_Error_String(3, may_error_row, may_error_column));
                            may_error_row = may_error_column = -1;
                            may_error_string = null;
                        }
                        out.write(get_String(x, sub, i, j));
                        last_token = x;
                        j = j + 1;
                        continue;
                    } else if (tmp == '<' && next == '>') {             // 可能为<>
                        String sub = string_test.substring(j, j + 2);
                        int x = get_num(sub, 1);
                        out.write(get_String(x, sub, i, j));
                        last_token = x;
                        j = j + 1;
                        continue;
                    } else if (next == '=') {                           // 可能为<=或者>=
                        if (j + 2 >= string_test.length()) {            // 处在最后则报错
                            out.write(get_Error_String(4, i, j));
                            continue;
                        } else {
                            String sub = string_test.substring(j, j + 2);
                            int x = get_num(sub, 1);
                            if (may_error_row != -1) {
                                out.write(get_Error_String(3, may_error_row, may_error_column));
                                may_error_row = may_error_column = -1;
                                may_error_string = null;
                            }
                            out.write(get_String(x, sub, i, j));
                            last_token = x;
                            j = j + 1;
                            continue;
                        }
                    }
                    // 若都不是，则为单个运算符
                    last_token = get_num(String.valueOf(tmp), 0);
                    if (may_error_row != -1) {
                        out.write(get_Error_String(3, may_error_row, may_error_column));
                        may_error_row = may_error_column = -1;
                        may_error_string = null;
                    }
                    out.write(get_String(last_token, String.valueOf(tmp), i, j));
                } else if (tmp == '&' || tmp == '|') {
                    // 若为&或者|
                    if (last_token > NUM_KEYWORD && last_token <= NUM_TOTAL - NUM_DELIMITER) {
                        out.write(get_Error_String(2, i, j));
                        continue;
                    }
                    // 若为最后一个字符则可能出错
                    if (j + 1 >= string_test.length()) {
                        may_error_row = i;
                        may_error_column = j;
                        may_error_string = String.valueOf(tmp);
                        last_token = get_num(String.valueOf(tmp), 0);
                        continue;
                    }
                    char next = string_test.charAt(j + 1);
                    // 可能为&&或者||
                    if (next == tmp) {
                        String sub = string_test.substring(j, j + 2);
                        int x = get_num(sub, 1);
                        if (may_error_row != -1) {
                            out.write(get_Error_String(3, may_error_row, may_error_column));
                            may_error_row = may_error_column = -1;
                            may_error_string = null;
                        }
                        out.write(get_String(x, sub, i, j));
                        last_token = x;
                        j = j + 1;
                        continue;
                    }
                    // 都不是则为单个位运算
                    last_token = get_num(String.valueOf(tmp), 0);
                    if (may_error_row != -1) {
                        out.write(get_Error_String(3, may_error_row, may_error_column));
                        may_error_row = may_error_column = -1;
                        may_error_string = null;
                    }
                    out.write(get_String(last_token, String.valueOf(tmp), i, j));
                } else if (is_Delimiter(tmp) != -1) {
                    // 为界限符
                    int x = get_num(String.valueOf(tmp), 2);
                    if (may_error_row != -1) {
                        out.write(get_Error_String(3, may_error_row, may_error_column));
                        may_error_row = may_error_column = -1;
                        may_error_string = null;
                    }
                    if (tmp == '\'' || tmp == '\"') {
                        quota_mark = String.valueOf(tmp);
                    }
                    last_token = x;
                    out.write(get_String(x, String.valueOf(tmp), i, j));
                }
            }
        }
    }
}
