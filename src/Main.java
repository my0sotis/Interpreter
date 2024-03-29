import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import Compiler.LexicalAnalyzer;
import Compiler.Parser;
import Compiler.SemanticAnalyzer;

public class Main {
    public static void main(String[] args) {
        LexicalAnalyzer la = new LexicalAnalyzer();
        Parser p = new Parser();
        try {
            FileOutputStream fos = new FileOutputStream("./output/out");
            la.setOut(fos);
            la.setFile(new File("./Tests/test"));
            la.Analyze();
            System.out.println("词法分析完毕！");
            FileOutputStream fo = new FileOutputStream("./output/pout");
            p.setOut(fo);
            p.start();
            SemanticAnalyzer sa = new SemanticAnalyzer(p.getRoot());
//            sa.setFileInput(false);
            sa.Analyse();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
