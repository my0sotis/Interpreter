package Utils;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
    private List<TreeNode> Children = new ArrayList<>();
    private String name;

    public TreeNode(String name) {
        this.name = name;
    }

    public void addChildren(TreeNode node) {
        Children.add(node);
    }

    public String getName() {
        return name;
    }

    public List<TreeNode> getChildren() {
        return Children;
    }

    public TreeNode getChildAt(int index) {
        return Children.get(index);
    }

    public int getChildrenNum() {
        return Children.size();
    }
}
