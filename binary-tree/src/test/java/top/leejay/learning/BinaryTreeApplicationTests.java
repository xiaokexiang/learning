package top.leejay.learning;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.leejay.learning.structure.TreeNode;
import top.leejay.learning.structure.TreeNodeUtil;

import java.util.List;

@Data
@SpringBootTest
class BinaryTreeApplicationTests {

    private static List<Integer> NUMS;
    private static TreeNodeUtil TREE_NODE_UTIL = new TreeNodeUtil();

    static {
        NUMS = Lists.newArrayList(15, 7, 4, 10, 8, 1, 20, 8);
    }

    @Test
    void addNodesSorted() {
        TreeNode treeNode = TREE_NODE_UTIL.addNodesSorted(NUMS);
        System.out.println(JSONObject.toJSONString(treeNode, true));
    }

    @Test
    void addNodes() {
        TreeNode treeNode = TREE_NODE_UTIL.addNodes(NUMS);
        System.out.println(JSONObject.toJSONString(treeNode, true));
    }

    @Test
    void add() {
        TreeNode treeNode = TREE_NODE_UTIL.addByNonRecursion(NUMS);
        System.out.println(JSONObject.toJSONString(treeNode, true));
    }

    @Test
    void preQuery() {
        TreeNode treeNode = TREE_NODE_UTIL.addNodesSorted(NUMS);
        List<Integer> integers = TREE_NODE_UTIL.preOrderQuery(treeNode);
        System.out.println(integers);
    }

    @Test
    void preOrder2() {
        TreeNode treeNode = TREE_NODE_UTIL.addNodesSorted(NUMS);
        List<Integer> integers = TREE_NODE_UTIL.preOrderQuery2(treeNode);
        System.out.println(integers);
    }

    @Test
    void middleQuery() {
        TreeNode treeNode = TREE_NODE_UTIL.addNodesSorted(NUMS);
        TREE_NODE_UTIL.middleOrderQuery(treeNode);
    }

    @Test
    void middleQuery2() {
        TreeNode treeNode = TREE_NODE_UTIL.addNodesSorted(NUMS);
        List<Integer> integers = TREE_NODE_UTIL.middleOrderQuery2(treeNode);
        System.out.println(integers);
    }

    @Test
    void postQuery() {
        TreeNode treeNode = TREE_NODE_UTIL.addNodesSorted(NUMS);
        TREE_NODE_UTIL.postQuery(treeNode);
    }

    @Test
    void postQuery2() {
        TreeNode treeNode = TREE_NODE_UTIL.addNodesSorted(NUMS);
        List<Integer> integers = TREE_NODE_UTIL.postQuery2(treeNode);
        System.out.println(integers);
    }

    @Test
    void levelQuery() {
        TreeNode treeNode = TREE_NODE_UTIL.addNodesSorted(NUMS);
        List<Integer> integers = TREE_NODE_UTIL.levelQuery(treeNode);
        System.out.println(integers);
    }
}
