package top.leejay.learning.sort;


/**
 * @author xiaokexiang
 */
public abstract class Sort {
    protected int[] array;

    public Sort(int[] array) {
        this.array = array;
    }

    public int[] getArray() {
        return array;
    }

    protected abstract void sort();
}
