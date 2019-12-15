import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Test {
    public static void main(String[] args) {
        String verify="Hello";

        List<String> list= Stream.iterate(0, n -> ++n).limit(verify.length())
                .map(n -> "" + verify.charAt(n))
                .collect(Collectors.toList());
        for(String str:list){
            System.out.println(str);

        }
    }
}
