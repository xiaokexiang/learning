package top.leejay.learning.algorithm;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * 二叉树遍历
 */
public class BinaryTree {
    private static Node root;
    static Stack<Node> stack = new Stack<>();

    static class Node {
        Node left;
        Node right;
        Integer value;

        public Node(Integer value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "left=" + left +
                    ", right=" + right +
                    ", value=" + value +
                    '}';
        }
    }

    /**
     * 将list转成二叉树结构
     * 5
     * 3      9
     * 2   4   6   10
     */
    static void create(Node root, Integer value) {
        if (root.value > value) {
            if (null == root.left)
                root.left = new Node(value);
            else {
                create(root.left, value);
            }
        }
        if (root.value < value) {
            if (null == root.right) {
                root.right = new Node(value);
            } else {
                create(root.right, value);
            }
        }
    }

    /**
     * 使用stack结构实现前序遍历
     *
     * @param root root
     */
    void stackFirst(Node root) {
        while (root != null || !stack.isEmpty()) {
            while (root != null) {
                System.out.println(root.value);
                stack.push(root);
                root = root.left;
            }

            if (!stack.isEmpty()) {
                root = stack.pop();
                root = root.right;
            }
        }
    }

    void stackSecond(Node root) {
        while (root != null || !stack.isEmpty()) {
            while (root != null) {
                stack.push(root);
                root = root.left;
            }
            System.out.println(stack.pop().value);
            if (!stack.isEmpty()) {
                root = stack.pop();
                System.out.println(root.value);
                root = root.right;
            }
        }
    }

    /**
     * 广度遍历
     *
     * @param root root
     */
    void widthFind(Node root) {
        LinkedList<Node> list = new LinkedList<>();
        list.offer(root);
        while (!list.isEmpty()) {
            Node node = list.poll();
            System.out.println(node.value);
            if (null != node.left) {
                list.offer(node.left);
            }
            if (null != node.right) {
                list.offer(node.right);
            }
        }
    }

    void firstRoot(Node root) {
        if (root == null) {
            return;
        }
        System.out.println(root.value);
        firstRoot(root.left);
        firstRoot(root.right);
    }

    void secondRoot(Node root) {
        if (root == null) {
            return;
        }
        secondRoot(root.left);
        System.out.println(root.value);
        secondRoot(root.right);
    }

    void lastRoot(Node root) {
        if (root == null) {
            return;
        }
        secondRoot(root.left);
        secondRoot(root.right);
        System.out.println(root.value);
    }


    public static void main(String[] args) {
        List<Integer> list = Arrays.asList(3, 2, 9, 6, 10, 4);
        BinaryTree.root = new Node(5);
        list.forEach(x -> BinaryTree.create(root, x));

        System.out.println(BinaryTree.root);
        BinaryTree binaryTree = new BinaryTree();
        binaryTree.widthFind(BinaryTree.root);
//        binaryTree.firstRoot(BinaryTree.root);
//        binaryTree.secondRoot(BinaryTree.root);
//        binaryTree.lastRoot(BinaryTree.root);
    }
}
