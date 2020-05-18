package top.leejay.learning.entity;

import lombok.Data;

/**
 * @author xiaokexiang
 * @date 5/6/2020
 */
@Data
public class TreeNode {
    /**
     * 左节点
     */
    TreeNode left;
    /**
     * 节点存储的值
     */
    int value;
    /**
     * 右节点
     */
    TreeNode right;

    /**
     * 用于处理值相等的情况
     */
    int count = 1;

    public TreeNode(int value) {
        this.left = null;
        this.value = value;
        this.right = null;
    }
}
