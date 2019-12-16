import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Test {
    public static void main(String[] args) {
        int m = 100;
        int n = 5;

        int[] v = {92,22,87,46,90}; // value cost
        int[] c = {77,22,29,50,99};

        int[] a = new int[m + 1];
        int i = 0;
        while (i <= m) {
            a[i] = 0;
            i++;
        }

        // core
        i = 0;
        while (i < n) {
            int j = m;
            while (j >= c[i]) {
                int k = j - c[i];
                if (a[j] < a[k] + v[i])
                    a[j] = a[k] + v[i];
                j = j - 1;
            }
            i = i + 1;
        }


        // print
        System.out.println(a[m]);
    }
}
