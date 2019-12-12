import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import Compiler.LexicalAnalyzer;
import Compiler.Parser;

public class Main {
    public static void main(String[] args) {
        LexicalAnalyzer la = new LexicalAnalyzer();
        try {
            FileOutputStream fos = new FileOutputStream("./output/out");
            la.setOut(fos);
            la.setFile(new File("./Tests/Test1"));
            la.Analyze();
            System.out.println("词法分析完毕！");
            Parser.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
