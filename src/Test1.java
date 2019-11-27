import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.*;
import java.util.zip.CheckedOutputStream;

public class Test1 {
    public static class MyEnumeration implements Enumeration {
        int count = 0;
        int length;
        List<TreeNode> list = new ArrayList<>();

        public void add(TreeNode node) {
            list.add(node);
            length++;
        }

        @Override
        public boolean hasMoreElements() {
            return count < length;
        }

        @Override
        public Object nextElement() {
            return list.get(count++);
        }
    }

    public static void Reverse(Enumeration nodes) {
        MyEnumeration e = new MyEnumeration();
        Enumeration enumeration = Collections.emptyEnumeration();
        List<TreeNode> list = new ArrayList<>();
        while (nodes.hasMoreElements()) {
            list.add((TreeNode) nodes.nextElement());
        }
        for (int i = list.size() - 1; i >= 0; i--) {
            e.add(list.get(i));
        }
        nodes = e;
    }

    public static void reverse(DefaultMutableTreeNode node) {
        if (!node.children().hasMoreElements()) return;
        Reverse(node.children());
//        List<TreeNode> children = node.children();
        while (node.children().hasMoreElements()) {
            reverse((DefaultMutableTreeNode) node.children().nextElement());
        }
    }

    public static void main (String[]args){
        DefaultMutableTreeNode t = new DefaultMutableTreeNode();
        t.add(new DefaultMutableTreeNode("A"));
        t.add(new DefaultMutableTreeNode("B"));
        t.add(new DefaultMutableTreeNode("C"));
        System.out.println(t.getDepth());
    }
}