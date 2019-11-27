import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.*;
import java.util.zip.CheckedOutputStream;

public class Test1 {

    public static void main (String[]args){
        DefaultMutableTreeNode t = new DefaultMutableTreeNode();
        t.add(new DefaultMutableTreeNode("A"));
        t.add(new DefaultMutableTreeNode("B"));
        t.add(new DefaultMutableTreeNode("C"));
        System.out.println(t.getDepth());
    }
}