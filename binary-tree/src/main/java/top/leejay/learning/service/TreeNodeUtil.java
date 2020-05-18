package top.leejay.learning.service;

import com.google.common.collect.Lists;
import top.leejay.learning.entity.TreeNode;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * @author xiaokexiang
 * @date 5/6/2020
 */
public class TreeNodeUtil {

    /**
     * 根节点
     */
    private TreeNode rootNode;


    /**
     * 返回根节点
     *
     * @return rootNode
     */
    public TreeNode getRootNode() {
        return rootNode;
    }

    /**
     * 前序查询
     *
     * @param node 开始查询的节点
     */
    public List<Integer> preOrderQuery(TreeNode node) {
        List<Integer> nodes = Lists.newArrayList();
        if (node != null) {
            nodes.add(node.getValue());
            nodes.addAll(preOrderQuery(node.getLeft()));
            nodes.addAll(preOrderQuery(node.getRight()));
        }
        return nodes;
    }

    /**
     * 前序查询 非递归
     *
     * @param node node
     */
    public List<Integer> preOrderQuery2(TreeNode node) {
        // 类似栈结构 先进后出
        Stack<TreeNode> stack = new Stack<>();
        List<Integer> nodes = Lists.newArrayList();
        TreeNode current = node;
        while (current != null || !stack.empty()) {
            if (current != null) {
                nodes.add(current.getValue());
                // 放入栈首
                stack.push(current);
                current = current.getLeft();
            } else {
                // 从栈首获取 并移除
                TreeNode pop = stack.pop();
                current = pop.getRight();
            }
        }
        return nodes;
    }

    /**
     * 中序查询
     *
     * @param node 开始查询的节点
     */
    public List<Integer> middleOrderQuery(TreeNode node) {
        List<Integer> nodes = Lists.newArrayList();
        if (node != null) {
            nodes.addAll(middleOrderQuery(node.getLeft()));
            nodes.add(node.getValue());
            nodes.addAll(middleOrderQuery(node.getRight()));
        }
        return nodes;
    }

    /**
     * 中序非递归
     *
     * @param node node
     */
    public List<Integer> middleOrderQuery2(TreeNode node) {
        // 类似栈结构 先进后出
        Stack<TreeNode> stack = new Stack<>();
        List<Integer> nodes = Lists.newArrayList();
        TreeNode current = node;
        while (current != null || !stack.empty()) {
            if (current != null) {
                // 放入栈首
                stack.push(current);
                current = current.getLeft();
            } else {
                // 从栈首获取 并移除
                TreeNode pop = stack.pop();
                nodes.add(pop.getValue());
                current = pop.getRight();
            }
        }
        return nodes;
    }

    /**
     * 后序查询
     *
     * @param node 开始查询的节点
     */
    public List<Integer> postQuery(TreeNode node) {
        List<Integer> nodes = Lists.newArrayList();
        if (node != null) {
            nodes.addAll(postQuery(node.getLeft()));
            nodes.addAll(postQuery(node.getRight()));
            nodes.add(node.getValue());
        }
        return nodes;
    }

    /**
     * 后序非递归
     *
     * @param node node
     */
    public List<Integer> postQuery2(TreeNode node) {
        List<Integer> nodes = Lists.newArrayList();
        Stack<TreeNode> stack = new Stack<>();
        TreeNode current = node;
        TreeNode prev = null;
        while (current != null || !stack.isEmpty()) {
            if (current != null) {
                // 这步将会将所有的左节点加入stack
                stack.push(current);
                current = current.getLeft();
            } else {
                // 从stack中读取节点
                current = stack.pop();
                // 如果右节点为null 或 右节点之前处理过
                if (current.getRight() == null || current.getRight() == prev) {
                    nodes.add(current.getValue());
                    prev = current;
                    current = null;
                } else {
                    // 这里说明current 当前节点的右节点有值
                    stack.push(current);
                    current = current.getRight();
                    stack.push(current);
                    current = current.getLeft();
                }
            }
        }
        return nodes;
    }

    /**
     * 层级查询
     *
     * @param node node
     */
    public List<Integer> levelQuery(TreeNode node) {
        List<Integer> nodes = Lists.newArrayList();
        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(node);
        TreeNode current;
        while (!queue.isEmpty()) {
            current = queue.poll();
            nodes.add(current.getValue());

            if (null != current.getLeft()) {
                queue.offer(current.getLeft());
            }

            if (null != current.getRight()) {
                queue.offer(current.getRight());
            }
        }
        return nodes;
    }

    /**
     * 使用链表存储 & 顺序存储(不符合二分法查找)
     *
     * @param data [15, 7, 4, 10, 8, 1, 20]
     *             15
     *        7         4
     *    10    8     1     20
     * @return rootNode
     */
    public TreeNode addNodes(List<Integer> data) {

        if (data.isEmpty()) {
            return null;
        }
        if (data.size() == 1) {
            return new TreeNode(data.get(0));
        }
        // List<Integer> -> List<TreeNode>
        List<TreeNode> nodes = data.stream().map(TreeNode::new).collect(Collectors.toList());
        rootNode = nodes.get(0);
        for (int i = 0; i < nodes.size() / 2; i++) {
            // 当前节点的左子索引 = 当前节点索引 * 2 + 1
            nodes.get(i).setLeft(nodes.get(2 * i + 1));
            // 当前节点的右子索引 = 当前节点索引 * 2 + 2
            if ((2 * i + 2) < nodes.size()) {
                nodes.get(i).setRight(nodes.get(2 * i + 2));
            }
        }
        return rootNode;
    }

    /**
     * 使用链表存储 & 采用二叉树特性(二分法查找) 批量添加节点
     *
     * @param data [15, 7, 4, 10, 8, 1, 20]
     *             15
     *         7        20
     *     4      10
     *  1       8
     * @return rootNode
     */
    public TreeNode addNodesSorted(List<Integer> data) {
        if (data.isEmpty()) {
            return null;
        }
        if (data.size() == 1) {
            return new TreeNode(data.get(0));
        }
        // List<Integer> -> List<TreeNode>
        if (null == rootNode) {
            rootNode = new TreeNode(data.get(0));
        }
        // 默认current是根节点
        data.forEach(d -> addNode(rootNode, d));
        return rootNode;
    }

    /**
     * 采用递归的方法实现添加节点
     *
     * @param current current
     * @param value   value
     */
    private void addNode(TreeNode current, Integer value) {
        // 二分法，小于当前节点值，就放在左边
        if (value < current.getValue()) {
            if (null != current.getLeft()) {
                addNode(current.getLeft(), value);
            } else {
                current.setLeft(new TreeNode(value));
            }
        }

        // 大于当前节点值，就放在右边
        if (value > current.getValue()) {
            if (null != current.getRight()) {
                addNode(current.getRight(), value);
            } else {
                current.setRight(new TreeNode(value));
            }
        }
        // 处理值相等的情况
        if (value == current.getValue()) {
            int count = current.getCount();
            current.setCount(++count);
        }
    }

    /**
     * 使用非递归方法实现
     *
     * @param data data
     * @return rootNode
     */
    public TreeNode addByNonRecursion(List<Integer> data) {
        rootNode = new TreeNode(data.get(0));
        TreeNode current = rootNode;
        int index = 1;
        while (index < data.size()) {
            if (data.get(index) < current.getValue()) {
                if (null != current.getLeft()) {
                    current = current.getLeft();
                } else {
                    current.setLeft(new TreeNode(data.get(index++)));
                    current = rootNode;
                }
            }

            if (data.get(index) > current.getValue()) {
                if (null != current.getRight()) {
                    current = current.getRight();
                } else {
                    current.setRight(new TreeNode(data.get(index++)));
                    current = rootNode;
                }
            }

            if (data.get(index) == current.getValue()) {
                index++;
                int count = current.getCount();
                current.setCount(++count);
            }
        }
        return rootNode;
    }
}
