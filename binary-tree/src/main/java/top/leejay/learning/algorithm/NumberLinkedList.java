package top.leejay.learning.algorithm;

import java.util.Objects;

/**
 * 链表的增加、移除和查询。
 */
public class NumberLinkedList {
    private Node head;
    private Node tail;
    private int count;

    public NumberLinkedList() {
        head = tail = null;
    }

    /**
     * 添加元素到队尾
     * O(1)
     *
     * @param value value
     */
    void addLast(int value) {
        Node node = new Node(value);
        // 此时还没有元素，刚初始化
        if (head == tail && tail == null) {
            head = tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
        count++;
    }

    /**
     * 添加到指定位置，考虑查询操作？O(n)：O(1)
     *
     * @param index index
     * @param value value
     */
    void addSpecify(int index, int value) {
        if (index > count) {
            throw new IllegalArgumentException("index over limit");
        } else if (index == count) {
            // 插入元素到队尾
            addLast(value);
        } else {
            Node node = new Node(value);
            Node h = head;
            int i = 1;
            do {
                if (i == index - 1) {
                    Node n = h.next;
                    h.next = node;
                    node.next = n;
                }
                i++;
            } while ((h = h.next) != null);
            count++;
        }
    }

    /**
     * 查找指定index的值
     * O(n)
     * @param index index
     * @return Node
     */
    Node find(int index) {
        if (index >= count) {
            throw new IllegalArgumentException("index over limit");
        }
        Node h = head;
        for (int i = 0; i < index; i++) {
            h = h.next;
        }
        return h;
    }

    /**
     * 移除指定index的元素
     * 考虑查询操作?O(n):O(1)
     *
     * @param index index
     * @return 被删除的Node
     */
    Node remove(int index) {
        if (index >= count) {
            throw new IllegalArgumentException("index over limit");
        }
        Node middle = find(index);
        // 如果移除的是队首元素
        if (index == 0) {
            head = find(index + 1);
            return middle;
        }
        // 被删除的正好是最后一个节点
        if (index == (count - 1)) {
            Node node = find(index - 1);
            node.next = null;
            count--;
            return middle;
        }
        Node prev = find(index - 1);
        prev.next = find(index + 1);
        count--;
        return middle;
    }

    /**
     * toString()返回
     * 注意队列未初始化的情况
     *
     * @return String
     */
    @Override
    public String toString() {
        if (null == tail && null == head)
            return "[], count = 0";
        StringBuffer result;
        result = new StringBuffer();
        result.append("[");
        Node n = head;
        do {
            result.append(n.value);
            if (null != n.next) {
                result.append(",");
            }
        } while ((n = n.next) != null);
        result.append("], count = ").append(count);
        return result.toString();
    }

    /**
     * 单向节点构成队列
     */
    static class Node {
        Node next;
        int value;

        public Node(int value) {
            this.value = value;
            this.next = null;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "next=" + next +
                    ", value=" + value +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Node)) return false;
            Node node = (Node) o;
            return value == node.value &&
                    Objects.equals(next, node.next);
        }

        @Override
        public int hashCode() {
            return Objects.hash(next, value);
        }
    }

    public static void main(String[] args) {
        NumberLinkedList linkedList = new NumberLinkedList();
        System.out.println(linkedList.toString());
        linkedList.addLast(2);
        System.out.println(linkedList.toString());
        linkedList.addLast(3);
        System.out.println(linkedList.toString());
        linkedList.addLast(18);
        System.out.println(linkedList.toString());
        linkedList.addSpecify(2, 22);
        System.out.println(linkedList.toString());
        linkedList.addSpecify(4, 1);
        System.out.println(linkedList.toString());
        System.out.println(linkedList.find(2));
        System.out.println(linkedList.find(3));
        System.out.println(linkedList.remove(3));
        System.out.println(linkedList.toString());
        System.out.println(linkedList.remove(0));
        System.out.println(linkedList.toString());
    }
}
