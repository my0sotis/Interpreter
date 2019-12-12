package Compiler;

import Utils.TreeNode;

import java.util.ArrayList;
import java.util.List;

public class SemanticAnalyzer {
    private TreeNode root;

    private int errorNum = 0;
    private int level = 0;

    public SemanticAnalyzer(TreeNode node) {
        root = node;
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }

    public void Analyse() {

    }

    private void S(TreeNode node) {
        for (int i = 0; i < node.getChildrenNum(); i++) {
            TreeNode current = node.getChildAt(i);
            String content = current.getName();
            if (content.equals("C")) {

            } else if (content.equals("SS")) {
                SS(current);
            } else if (content.equals("L")) {

            } else if (content.equals("S")) {
                S(current);
            }
        }
    }

    private void SS(TreeNode node) {
        for (int i = 0; i < node.getChildrenNum(); i++) {
            TreeNode current = node.getChildAt(i);
            String content = current.getName();
            if (content.equals("Jump")) {

            } else if (content.equals("Print")) {
                Print(current);
            } else if (content.equals("State")) {

            } else if (content.equals("Assign")) {

            }
        }
    }


    private void Print(TreeNode node) {
//        String res = Expression(node.getChildAt(2));
//        System.out.println(res);
    }

//    private String Expression(Utils.TreeNode node) {
//
//    }
//
//    private String E1(Utils.TreeNode node) {
//
//    }

    private void Assign(TreeNode node) {

    }
}
