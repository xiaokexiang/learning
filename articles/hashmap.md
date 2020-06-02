## HashMap源码解析

>  什么是HashMap?
>
>  HashMap是基于哈希表的Map接口的`非同步实现`，其本质上是`数组 + 链表(或红黑树)`的结合，它允许使用null值和null键，且`不保证映射顺序`。
>
>  HashMap采用的`链地址法的Hash算法`，即存在相同的hash值时使用next指针将Node节点构建成链表结构进行保存。

![HashMap结构](https://image.leejay.top/image/20200519/7iYthJhj1l38.png?imageslim)

### 1. 构造函数

```java
// 在构造函数未指定时使用的负载因子
static final float DEFAULT_LOAD_FACTOR = 0.75f;

// 最大容量， 初始容量范围：0 <= initialCapacity <= 1<<30
static final int MAXIMUM_CAPACITY = 1 << 30;

// 下一次扩容的阈值，默认(capacity * load factor)计算得出
int threshold;

/**
 * new HashMap<>(16)初始化入口
 */
public HashMap(int initialCapacity) {
    // 调用了初始容量和默认负载因子(用于扩容)
    this(initialCapacity, DEFAULT_LOAD_FACTOR);
}

/**
 * 带有初始容量和负载因子的构造函数
 */
public HashMap(int initialCapacity, float loadFactor) {
    // 校验initialCapacity参数是否合法
    if (initialCapacity < 0)
        throw new IllegalArgumentException("Illegal initial capacity: " +
                                           initialCapacity);
    // 超过最大容量就设置成最大容量
    if (initialCapacity > MAXIMUM_CAPACITY)
        initialCapacity = MAXIMUM_CAPACITY;
    // 判断负载因子是否合法
    if (loadFactor <= 0 || Float.isNaN(loadFactor))
        throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
    this.loadFactor = loadFactor;
    // 计算扩容阈值，赋值后构造函数执行完成
    this.threshold = tableSizeFor(initialCapacity);
}

/**
 * 基于给定的容量，返回大于等于initialCapacity的最小的2的幂次方。
 * cap的值必定 > 0
 * 如果 cap = 1，n = 0进行或运算结果还是0，最终返回1(0+1)
 * |：位或运算 n |= n >>> 1 即 n = n | n >>> 1
 */
static final int tableSizeFor(int cap) {
    int n = cap - 1;
    n |= n >>> 1;
    n |= n >>> 2;
    n |= n >>> 4;
    n |= n >>> 8;
    n |= n >>> 16;
    return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
}
```

> 总结：
>
> 1. HashMap的`initCapacity`范围：`[0, 1<<30]`。
> 2. HashMap默认负载因子为`0.75F`。
> 3. 初始化时扩容阈值为大于或等于`initialCapacity`的最小的2的幂次方(15 -> 16)。
> 4. HashMap此时只是`设置了扩容阈值，还没有初始化数组(在put时初始化)`，目的是防止创建了HashMap却不用导致占内存。

#### tableSizeFor()

1. 首先我们需要确认两个前提，`cap: [0, 1<<30] `以及如果`cap=0或1`时，返回1，结论仍成立。
2. `int n = cap - 1`，如果`cap=2^*(*>=0)`，此时如果不减1，则在最后返回的时候会返回`cap+1(即2^(*+1))`。
3. 我们假设`n>0`，所以n的二进制必定存在一个或者多个bit值为1，我们用`x来表示bit值1`的情况，则有可能`n = 0x00 0000 0000 0000 0000 0000 0000 0000`。
4. `n | n >>> 1`即为`n = n | n >>> 1`，就是n 与 n无符号右移1位的值进行或运算，`n | n >>> 1`结果为x 和x-1位都为1，`n | n >>> 2`即为x、x-1、x-2和x-3位都是1，`n|n >>>4` 即为x、x-1、x-2、x-3、...、x-7都是1，依次类推最终x及x后的所有位都为1。
5. 最终如果除了`cap=0或1`及超过`MAXIMUM_CAPACITY`的情况，其他都返回n + 1`(0010 0000 0000 ... 0000)`这样的数据，也就是2^*幂次方。

---

### 2. put()

```java
public V put(K key, V value) {
    return putVal(hash(key), key, value, false, true);
}

// 计算object的hash值
static final int hash(Object key) {
    int h;
    // key.hashCode()是nactive方法，不同的JVM有不同的实现
    // 将h的高16位和低16位进行异或运算，这样保证每个bit都能参与运算
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}

// 构成HashMap数组中每一项的链表数据结构
static class Node<K,V> implements Map.Entry<K,V> {
    // 当前node的hash值
    final int hash;
    // key
    final K key;
    // value
    V value;
    // 当前node的下一个node
    Node<K,V> next;

    Node(int hash, K key, V value, Node<K,V> next) {
        this.hash = hash;
        this.key = key;
        this.value = value;
        this.next = next;
    }
    ....
}

final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    // 如果当前table数组为空或null
    if ((tab = table) == null || (n = tab.length) == 0)
        // 初始化table[]数组并设置相关参数，具体的跳转下面resize()解析
        n = (tab = resize()).length;
    // 计算数据保存的索引，如果table[i] = null就创建Node<k,v>并保存
    if ((p = tab[i = (n - 1) & hash]) == null)
        tab[i] = newNode(hash, key, value, null);
    else {
        // table[i]存在数据，定义e为临时节点，k为临时key
        Node<K,V> e; K k;
        // p=table[i]（即当前位置已占位的节点）
        // 1. 判断新增node与p的hash、key是否相同，相同则需要用新值覆盖旧值
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
            e = p;
        // 2. 判断p是否是TreeNode的子类，说明此处已经是红黑树
        else if (p instanceof TreeNode)
            // 红黑树添加，如果添加成功就返回null，如果树存在相同的key就返回该新增节点
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        else {
            // jdk1.8之前是头插法，会导致链表循环，1.8换成尾插法
            // 循环判断新增节点e的next节点是否为null
            for (int binCount = 0; ; ++binCount) {
                if ((e = p.next) == null) {
                    // 如果为null，直接尾插
                    p.next = newNode(hash, key, value, null);
                    // 判断链表数量是否超过阈值(默认8)，超过转为红黑树
                    if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                        treeifyBin(tab, hash);
                    break;
                }
                // 如果存在相同的key，结束循环，在外层进行值替换
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    break;
                // 设置p为下一个节点
                p = e;
            }
        }
        // 如果e不为null，说明此次的key是重复的，所以需要新值覆盖旧值
        if (e != null) { // existing mapping for key
            V oldValue = e.value;
            if (!onlyIfAbsent || oldValue == null)
                e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
    }
    // modCount：HashMap结构修改次数+1
    ++modCount;
    // 如果超过扩容阈值就调用resize()扩容
    if (++size > threshold)
        resize();
    afterNodeInsertion(evict);
    // 返回null表明添加成功
    return null;
}
```

> 总结：
>
> 1. 如果调用put()方法时，table[]桶还没有初始化，调用`resize()`进行初始化。
> 2.  put()通过`index = (capicity - 1) & key.hashCode()`计算脚标存放node。
> 3. jdk8之后put()使用的是`尾插法`，之前使用的是`头插法`，`头插法在多线程情况下会导致环形链表，出现get()的时候导致死循环`。使用尾插法在多线程情况下最多是丢失数据，不会出现死循环。
> 4.  如果`node链表的数量大于等于8`时，会将`node链表转成红黑树`。
> 5. put()方法包含三种情况：key相同的值覆盖 & TreeNode插入红黑树 & Node插入链表。
> 6. 红黑树：查询快，插入慢，链表相反：查询慢，插入快。
> 7. 每put一次都会计算是否需要扩容，默认cap:16，threshold:12，当插入第12个值完成时会扩容。



### 3. resize()

```java
/**
 * 1. 如果table为null就初始化表并设置扩容阈值。
 * 2. table不为null则对表进行2次幂扩容，扩容后元素索引与扩容前一致或者2次幂偏移。
 */
final Node<K,V>[] resize() {
    // 将当前table赋值给oldTab(简称旧表)
    Node<K,V>[] oldTab = table;
    // 设置旧表的容量，旧表不存在就为0(初始化状态)
    int oldCap = (oldTab == null) ? 0 : oldTab.length;
    // 将当前的扩容阈值设置给oldThr(简称旧阈值)
    int oldThr = threshold;
    // 初始化新表容量和新阈值
    int newCap, newThr = 0;
    // 说明此时的resize()是非初始化扩容
    if (oldCap > 0) {
        // 如果oldCap超过最大值就设置阈值并返回，因为无法再扩容
        if (oldCap >= MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return oldTab;
        }
        // 如果扩容两倍小于max_cap且old_cap大于16
        else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                 oldCap >= DEFAULT_INITIAL_CAPACITY)
            // 将threshold 设置为原来两倍
            newThr = oldThr << 1;
    }
    // 此时说明oldCap.length = 0，table没有初始化且之前代码设置过threshold，
    // 优先将threshold赋值给newCap(有参构造会出现该情况)
    else if (oldThr > 0) 
        newCap = oldThr;
    // 到这里说明没有设置过threshold且table没有初始化(无参构造会有这情况)
    else {               
        // 将cap&threshold赋予默认值
        newCap = DEFAULT_INITIAL_CAPACITY;// 16
        newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);// 16 * 0.75
    }
    // 有参构造才会走到这步，继续设置newThr
    if (newThr == 0) {
        // 计算newThr = newCap * loadFactor
        float ft = (float)newCap * loadFactor;
        newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                  (int)ft : Integer.MAX_VALUE);
    }
    // 赋值给threshold
    threshold = newThr;
    @SuppressWarnings({"rawtypes","unchecked"})
    // 至此：新的cap & threshold计算完毕,创建新的table[]
    Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
    table = newTab;
    // 如果oldTab != null，要进行rehash()操作
    if (oldTab != null) {
        // 遍历获取里面每一个table[j](hashMap允许存在null键)
        for (int j = 0; j < oldCap; ++j) {
            Node<K,V> e;
            if ((e = oldTab[j]) != null) {
                // 将oldTab对应位置置为null
                oldTab[j] = null;
                // 如果next不存在node
                if (e.next == null)
                    // 计算新的index并设置值
                    newTab[e.hash & (newCap - 1)] = e;
                // 判断e是否是树节点
                else if (e instanceof TreeNode)
                    // 将红黑树节点rehash后放到新的地址
                    ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                // 如果不是红黑树结构且e.next不为null，进行链表复制
                else {
                    // 操作逻辑: 判断某个index上是否存在链表，如果存在，根据(e.hash & oldCap)
                    // 进行判断， 不移位则index不变，移位则index + oldCap作为新的脚标
                    // 具体可看下图3.1
                    Node<K,V> loHead = null, loTail = null;
                    Node<K,V> hiHead = null, hiTail = null;
                    Node<K,V> next;
                    do {
                        next = e.next;
                        // 判断是否需要移位 具体可看下图3.2
                        if ((e.hash & oldCap) == 0) {
                            // 将不需要移位的node转成链表(这里不处理红黑树是因为最多8个)
                            if (loTail == null)
                                loHead = e;
                            else
                                loTail.next = e;
                            loTail = e;
                        }
                        else {
                            // 需要处理移位，并将需要处理的node转成链表
                            if (hiTail == null)
                                hiHead = e;
                            else
                                hiTail.next = e;
                            hiTail = e;
                        }
                    } while ((e = next) != null);
                    if (loTail != null) {
                        loTail.next = null;
                        // 不移位：将不需要移位构成的链表赋值给table[j]
                        newTab[j] = loHead;
                    }
                    if (hiTail != null) {
                        hiTail.next = null;
                        // 移位: 将需要处理移位构成的新的链表赋值给table[j + oldCap]
                        newTab[j + oldCap] = hiHead;
                    }
                }
            }
        }
    }
    return newTab;
}
```

![图3.1](https://image.leejay.top/image/20200602/wASwgukyaekV.png?imageslim)

![](https://image.leejay.top/image/20200602/9p82dKyqCGPA.png?imageslim)

> 总结：
>
> 1. resize()包括`计算扩容后cap & threshold、rehash对元素进行重新分布`两个部分。
> 2. 如果HashMap还没有初始化，resize()会将其初始化。如果没有通过构造函数设置cap & threshold，则会设置默认的(cap=16 & threshold=12)。
> 3. 扩容遵守`newCap = oldCap * 2`公式，扩容过程中有的节点要么不会移位，要么按照(oldCap + i)进行平移。

### 4. get()

```java
public V get(Object key) {
    Node<K,V> e;
    // 传入key.hashCode & key
    return (e = getNode(hash(key), key)) == null ? null : e.value;
}

static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
// HashMap中table[]桶
transient Node<K,V>[] table;

// 传入key.hashCode & key
final Node<K,V> getNode(int hash, Object key) {
        Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
    	// table[]不为null且存在元素 && table[(n-1)&hash]不为null
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (first = tab[(n - 1) & hash]) != null) {
            // 判断table[i]的hash & key 是否相等，相等返回该节点
            if (first.hash == hash && // always check first node
                ((k = first.key) == key || (key != null && key.equals(k))))
                return first;
            // 如果不相等继续查看first是否有next节点
            if ((e = first.next) != null) {
                // 如果是红黑树，则进行红黑树find
                if (first instanceof TreeNode)
                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                // 如果是链表，进行循环查找
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        return e;
                } while ((e = e.next) != null);
            }
        }
    	// 找不到直接返回null
        return null;
    }
```

> 总结:
>
> 1. 相比put()，get()理解简单，就是通过`key.hash & (cap -1)`计算，如果table[i]没有，就获取table[i].next，按照红黑树或链表进行查找。



