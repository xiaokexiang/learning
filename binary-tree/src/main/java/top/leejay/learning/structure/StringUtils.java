package top.leejay.learning.structure;

public final class StringUtils {

    public static boolean contain(String source, String target) {
        final char[] sources = source.toCharArray(), targets = target.toCharArray();
        return contain(sources, targets) != -1;
    }

    // 基于KMP算法实现字符串快速匹配
    // 基于`最大公共前后缀`，在母串与子串不同时指示该从子串的哪个位置开始继续匹配
    private static int contain(char[] source, char[] target) {
        final int sLen = source.length, tLen = target.length;
        if (sLen < tLen) return -1;
        final int[] next = getNext(target);
        int s = 0, t = 0;
        while (s < sLen && t < tLen) {
            if (t == -1 || source[s] == target[t]) {
                s++;
                t++;
            } else {
                // 失配，查询next数组找到下一个需要查询的子串位置
                t = next[t];
            }
        }
        return t == tLen ? s - t : -1;
    }


    /**
     * 计算子串的next数组
     * 通过子串的前缀和后缀进行匹配得出最长前后缀
     *         j       i                        j       i
     *         ↓       ↓                        ↓       ↓
     * index 0 1 2 3 4 5 6 7 8 9            0 1 2 3 4 5 6 7 8 9
     * 子串   A B A D A B D A B A            A B A D A B D A B A
     * 子串           A B A D A B D A B A            A B A D A B D A B A
     * next -1 0 0 1 0 1 2                 -1 0 0 1 0 1 2
     *                 ↑                            ↑ ← ↑
     *                 j                            j   j
     * if ( target[i] == target[j] ) {
     *   next[i+1] = next[i] + 1 = j + 1;
     * }
     * else {
     *   j回溯到next[j]所在位置保证前后缀一致从而跳过无用的比对
     * }
     */
    private static int[] getNext(char[] target) {
        int length = target.length;
        int[] next = new int[length];
        // next[0]比较特殊，需要移动母串
        next[0] = -1;
        int i = 0, j = -1;
        while (i < length - 1) {
            if (j == -1 || target[i] == target[j]) {
                i++;
                j++;
                // 针对next查询后的值仍是之前的值，如果next查询出来的值仍是之前的值，那么再next一次
                next[i] = (target[i] != target[j] ? j : next[j]);
            } else {
                j = next[j];
            }
        }
        return next;
    }
}
