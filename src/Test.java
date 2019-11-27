import com.sun.source.tree.Tree;
import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.io.File;
import java.util.*;

public class Test {

    public JPanel CreateComponent() {
        JPanel panel=new JPanel();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("D");
        root.add(new DefaultMutableTreeNode("E"));
        JTree tree=new JTree(root);
        panel.add(tree);
        panel.setVisible(true);
        return panel;
    }

    public static void main(String[] args) {
        JFrame frame=new JFrame("教师学历信息");
        frame.setSize(330,300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new Test().CreateComponent());
        frame.pack();
        frame.setVisible(true);
    }
}
