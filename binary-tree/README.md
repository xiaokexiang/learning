### 树

#### 概念

`在了解二叉树之前，我们需要了解什么是树？在生活中树是一种植物，而在算法中，树代表着一种数据结构，如下图所示：`

<img src="https://image.leejay.top/image/20200509/eM2dSyb6pFvq.png"/>

#### 特征

树包含两个特征：

- 有且`只有一个称为根的节点`(根节点没有父节点)。
- 有若干个不相交的子树，这些子树本身也是一颗树。

#### 名词

树中包含一些特殊的名词，这些名词代表不同的含义：

- 节点
  `包含了数据项和指向其他结点的分支(包括根节点A、分支节点BC和叶子节点DE)。`
- 边
  `一个节点到另一个节点的距离。`
- 度
  `节点所拥有的子树的个数称为该结点的度。B节点的度为2，C节点的度为1。`
- 叶子节点
  `度为0的节点即为叶子节点，DE都是叶子节点。`
- 树的深度
  `节点的层数，根节点默认为第一层，上图深度为4层。`

---

### 二叉树

#### 概念

`二叉树是树的一种特殊形态。二叉树的特点是每个结点最多拥有两个子女（就是不存在度大于2的结点），分别称为左子女和右子女，并且二叉树的子树有左右之分，且子树次序不能颠倒。`
<img src="https://image.leejay.top/image/20200506/TtwqosDYLHyX.png"/>

#### 分类

- 满二叉树
  `在一颗二叉树中，如果所以的分支节点都存在左子树和右子树，并且所有的叶子节点都在同一层。`
  <img src="https://image.leejay.top/image/20200507/ayAyJAKvjwWE.png" />

- 完全二叉树
  `一棵二叉树中，只有最下面两层结点的度可以小于2，并且最下层的叶子节点集中在靠左的若干位置上。`
  <img src="https://image.leejay.top/image/20200507/3w37G2j09Hd7.png" />
  > 满二叉树一定是完全二叉树，完全二叉树不一定是满二叉树

#### 特性

- 树的深度为 k 的二叉树，最多存在`2^k-1`个节点。
- 任意一个二叉树的第 x 层最多存在`2^(x-1)`个节点。
- 任意一颗二叉树中，`叶子节点的数量比度为 2 的分支节点数量多 1`。

  ```java
    证明：
    1. 假设任意一个二叉树一共有 n 个节点，其中
    n0 个度为 0 的节点,
    n1 个度为 1 的节点，
    n2 个度为 2 的节点。
    那么 n = n0 + n1 + n2 ①

    2. 已知任意二叉树中:
    除了根节点外的其他节点都有一个父节点(意味着其他节点分别对应着一个边)。
    我们假设边的数目为b，可得：
    n = b + 1 ②

    3. 所有边都是由度不为0的节点产生。
    度为0的产生0个分支，
    度为1的产生1个分支，
    度为2的产生2个分支。
    可得：
    b = n0 * 0 + n1 * 1 + n2 * 2 ③

    ① = ③ + 1
    计算可得：
    n0 = n2 + 1
    即 任意一个二叉树中：叶子节点数量 = 度为2的节点数量 + 1
  ```

- 在同样深度的二叉树中，`满二叉树`节点数最多，且叶子节点全部在最下面一层
- 当一颗`完全二叉树`节点总数为 n 时，那么叶子节点数量等于 n/2 或 (n+1)/2

  ```java
  证明：
  1. 由之前特征可知任意二叉树：
  n0 = n2 + 1 ①
  n = n0 + n1 + n2 ②

  2. 完全二叉树只会存在0个或1个度为1的节点：
  n = n1(0|1) + 2*n2 + 1 ③

  3. 假设已知节点总数为112个，那么叶子节点数量为？
  112 = n1(0|1) + 2*n2 + 1 ④
  存在两种情况：
  a. 假设存在0个度为1的节点，那么n2 = 55.5，不是整数，不符合
  b. 假设存在1个度为1的节点，那么n2 = 55 n0 = 56 n1 = 1

  可得：
  当节点总数n为偶数时，n0 = n / 2，节点为奇数时， n0 = (n + 1) / 2
  ```

#### 存储

- 二分查找树

`通常二叉树是二分查找树，即每个节点它的值大于或者等于在它左子树节点上的值，小于或者等于在它右子树节点上的值。如下图所示`

<img src="https://image.leejay.top/image/20200507/CJX2vtcQOpCj.png"/>

- 顺序存储

`如下图所示，无论是何种树，我们都会转换成完全二叉树并一层一层从左开始对二叉树进行编号并存储。此方法的缺点是浪费空间，因为没有的节点也要补充完全。`

<img src="https://image.leejay.top/image/20200507/HVwDwYzGBwtD.png"/>

- 链式存储

`节点除了保存值，还会保存左子和右子地址的指针。`

<img src="https://image.leejay.top/image/20200507/5ncSOLgCSFTd.png">

#### 遍历算法

`我们以下图的二叉树为例，解释三种算法的遍历规则。`
<img src="https://image.leejay.top/image/20200507/0PzvUs0UW7t3.png"/>

- 先序遍历
  `先访问根节点，再访问当前节点的左子树，若当前节点无左子树，则访问当前节点的右子树。`

  ```java
  1. 先访问二叉树的根节点，找到1，输出1;
  2. 遍历节点1的左节点，找到节点2，输出2;
  3. 遍历节点2的左节点，找到4，且4没有左右节点，输出4;
  4. 遍历节点2的右节点，找到5，5没有左右节点，输出5;
  5. 至此节点2遍历完成，回到节点1，找到右节点3，输出3;
  6. 寻找节点3的左节点6，6没有左右节点，输出6;
  7. 寻找节点3的右节点7，7没有左右节点，输出7;
  8. 执行顺序：1-2-4-5-3-6-7
  ```

- 中序遍历
  `先访问当前节点的左子树，再访问根节点，然后访问当前节点的右子树。`

  ```java
  1. 先访问二叉树的根节点，找到1;
  2. 遍历节点1的左子树，找到2;
  3. 遍历节点2的左子树，找到4;
  4. 节点4无左子树后，遍历节点4右子树也没有，至此节点2左子树遍历完成，输出4和2;
  5. 遍历节点2的右子树，找到节点5，且节点5和节点4一样都，至此节点2右子树遍历完成，输出5;
  6. 回到节点1，至此节点1的左子树遍历完成，输出1，开始遍历节点1的右子树，找到节点3;
  7. 遍历节点3的左子树，找到节点6，其没有左右子树，输出6，因此节点3左子树遍历完成，输出3;
  8. 遍历节点3找到右子树节点7，其没有左右子树，输出7，节点1的右子树遍历完成，整棵树完成遍历;
  9. 执行顺序：4-2-5-1-6-3-7
  ```

- 后序遍历
  `从根节点出发，依次遍历各节点的左右子树，直到当前左右子树遍历完成后，才访问该节点元素。`

  ```java
  执行流程不再赘述，执行顺序：4-5-2-6-7-3-1
  ```

---

### 二叉树 Java 代码实现

#### 定义 TreeNode

```java
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
```

#### 添加值到二叉树中

```java
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
    *      4     10
    *   1     8
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
```

<a href="https://github.com/xiaokexiang/learning">Github</a>