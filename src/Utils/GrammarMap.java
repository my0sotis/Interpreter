package Utils;

import org.json.simple.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class GrammarMap {
    private JSONObject grammar;

    public GrammarMap() {
        HashMap<String, List<String>> map = new HashMap<>();

//        map.put("E", Collections.singletonList("T E'"));
//        map.put("E'", Arrays.asList("+ T E'", "ε"));
//        map.put("T", Collections.singletonList("F T'"));
//        map.put("T'", Arrays.asList("* F T'", "ε"));
//        map.put("F", Arrays.asList("( E )", "i"));

        // S-开始符号 F-函数 BS-除函数以外的句子 11 "L S",
        map.put("S", Arrays.asList("C S",  "SS S", "L S", "ε"));
        // Parameters-参数列表 FunType-函数类型 BasicType-基础类型 Pointer-指针
        map.put("F", Collections.singletonList("void identifier ( Parameters ) { S Return }"));
        map.put("BasicType", Arrays.asList("int", "char", "real", "bool"));
//        map.put("FunBasicType", Arrays.asList("void", "BasicType"));
//        map.put("FunType", Collections.singletonList("FunBasicType Pointer"));
//        map.put("Pointer", Arrays.asList("* Pointer", "ε"));
        map.put("Parameters", Arrays.asList("BasicType identifier Initialize Parameter", "ε"));
        map.put("Initialize", Arrays.asList("= Expression", "ε"));
        map.put("Parameter", Arrays.asList("ε", ", Parameters"));
        map.put("Return", Arrays.asList("return Expression ;", "ε"));
//        map.put("Type", Collections.singletonList("BasicType Pointer"));

        // 运算式 30
//        map.put("Expression", Collections.singletonList("E1 E'"));
//        map.put("E'", Arrays.asList("= Expression", "/= Expression", "*= Expression", "%= Expression",
//                "+= Expression", "-= Expression", "<<= Expression", ">>= Expression", "&= Expression",
//                "^= Expression", "|= Expression", "ε"));
        map.put("Expression", Collections.singletonList("E2 E1'"));
        map.put("E1'", Arrays.asList("|| E2 E1'", "ε"));
        map.put("E2", Collections.singletonList("E3 E2'"));
        map.put("E2'", Arrays.asList("&& E3 E2'", "ε"));
        map.put("E3", Collections.singletonList("E4 E3'"));
        map.put("E3'", Arrays.asList("| E4 E3'", "ε"));
        map.put("E4", Collections.singletonList("E5 E4'"));
        map.put("E4'", Arrays.asList("^ E5 E4'", "ε"));
        map.put("E5", Collections.singletonList("E6 E5'"));
        map.put("E5'", Arrays.asList("& E6 E5'", "ε"));
        map.put("E6", Collections.singletonList("E7 E6'"));
        map.put("E6'", Arrays.asList("== E7 E6'", "!= E7 E6'", "<> E7 E6'", "ε"));
        map.put("E7", Collections.singletonList("E8 E7'"));
        map.put("E7'", Arrays.asList("> E8 E7'", ">= E8 E7'", "< E8 E7'", "<= E8 E7'", "ε"));
        map.put("E8", Collections.singletonList("E9 E8'"));
        map.put("E8'", Arrays.asList("<< E9 E8'", ">> E9 E8'", "ε"));
        map.put("E9", Collections.singletonList("E10 E9'"));
        map.put("E9'", Arrays.asList("+ E10 E9'", "- E10 E9'", "ε"));
        map.put("E10", Collections.singletonList("E11 E10'"));
        map.put("E10'", Arrays.asList("/ E11 E10'", "* E11 E10'", "% E11 E10'", "ε"));
//        map.put("E11", Collections.singletonList("E12 E11'"));
//        map.put("E11'", Arrays.asList("++ E12", "-- E12", "ε"));
        map.put("E11", Arrays.asList("( Expression )", "Value"));
        map.put("Value", Arrays.asList("identifier E13", "number", "Scan", "true", "false"));
        map.put("Scan", Collections.singletonList("scan ( ScanItem )"));
        // E13 Abandon "( Value1 Value2 )"
        map.put("E13", Arrays.asList("[ Expression ] E13", "ε"));
        // Value1 is E13 related
//        map.put("Value1", Arrays.asList("number", "identifier", "ε"));
//        map.put("Value2", Arrays.asList(", Value1 Value2", "ε"));
        map.put("ScanItem", Collections.singletonList("identifier ScanArray"));
        map.put("ScanArray", Arrays.asList("ε", "[ Expression ]"));

        // L-循环 SS-单句 C-条件 3
        map.put("SS", Arrays.asList("Jump", "Print", "State", "Assign ;", "ScanSentence"));
        map.put("ScanSentence", Collections.singletonList("Scan ;"));
        map.put("Print", Collections.singletonList("print ( PrintItem"));
        map.put("PrintItem", Arrays.asList("Expression ) ;", "String ) ;", "Character ) ;"));
        map.put("Jump", Arrays.asList("break ;", "continue ;"));

        // State Assign 14
        map.put("State", Arrays.asList("int identifier State1", "real identifier State1", "char identifier State1"));
        map.put("State1", Arrays.asList("State2 ;", "[ Array State2 ;", "= Value4 State2 ;"));
        map.put("State2", Arrays.asList(", identifier State3 State2", "ε"));
        map.put("State3", Arrays.asList("[ Array", "= Value4", "ε"));
        map.put("Array", Arrays.asList("Expression ] Array1", "] Array1"));
//        map.put("Array1", Arrays.asList("] Array2"));
        map.put("Array1", Arrays.asList("= Array2", "ε"));
        map.put("Array2", Arrays.asList("{ Value4 Element }", "String", "Character", "Expression"));
        map.put("Element", Arrays.asList(", Value4 Element", "ε"));
        map.put("Value4", Arrays.asList("Character", "Expression", "String"));
        map.put("Value3", Arrays.asList("Character", "Expression", "String"));
        map.put("Character", Collections.singletonList("' Char1"));
        map.put("Char1", Arrays.asList("string '", "'"));
        map.put("String", Collections.singletonList("\" String1"));
        map.put("String1", Arrays.asList("string \"", "\""));
        map.put("Assign", Collections.singletonList("identifier Assign1 State2"));
        map.put("Assign1", Arrays.asList("[ Array", "= Value4"));

        // 条件 6 P
        map.put("C", Collections.singletonList("if ( Expression ) Judge"));
        map.put("Judge", Arrays.asList("if ( Expression ) Judge", "Judge1"));
        map.put("Judge1", Collections.singletonList("Content Else"));
        // , "L", "SS"
        map.put("Content", Collections.singletonList("{ S }"));
        map.put("Else", Arrays.asList("else Judge2", "ε"));
        map.put("Judge2", Arrays.asList("if ( Expression ) Judge1", "Content"));

        // 循环 7
        map.put("L", Arrays.asList("While", "For"));
        map.put("While", Arrays.asList("while ( Expression ) While", "WhileContent"));
        map.put("WhileContent", Collections.singletonList("{ S }"));
        map.put("For1", Arrays.asList("Assign", "State", "ε"));
//        map.put("For2", Arrays.asList(, "ε"));
//        map.put("For3", Arrays.asList("Assign", "ε"));, , "SS", "C"
        map.put("For", Arrays.asList("for ( For1 Expression ; Assign ) For", "ForContent"));
        map.put("ForContent", Collections.singletonList("{ S }"));

        grammar = new JSONObject(map);
    }

    public JSONObject getGrammar() {
        return grammar;
    }

    public String getStartSymbol() {
        return "S";
    }
}
