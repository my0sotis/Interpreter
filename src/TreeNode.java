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

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<TreeNode> getChildren() {
        return Children;
    }
}
