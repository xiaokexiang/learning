## 函数式编程
### lambda表达式
*一种紧凑的,传递行为的方式*

* 原有的代码编写方式(匿名内部类)
``` java
new runnable() {
    @override
    public void run() {
        system.out.println("匿名内部类");
    }
}.run();
```

* lambda表达式编写方式
``` java
// ()表示不需要参数
runnable runnable = () -> system.out.println("lambda");

// 
((runnable) () -> system.out.println("lambda")).run();
```

* 编写方式对比
  * lambda比传统的代码更简洁,代码量更少
  * lambda相对传统代码来说不容易进行代码阅读
  * lambda表达式需要学习时间和成本

### 函数接口
*函数接口是只有一个抽象方法的接口,用作lambda表达式的类型*

* 函数接口转换
``` java
public interface actionlistener extends eventlistener {
    public void actionperformed(actionevent e);
}

// 参数 -> 返回类型
actionevent -> actionlistener

```
* 常用的函数接口

接口 | 参数 | 返回类型 | 示例 |
---- | --- | ------ | ---- |
predicate<t> | t | boolean | 这张唱片已经发行了吗 |
consumer<t> | t | void | 输出一个值 |
function<t> | t | r | 获取artist对象的名字 |
supplier<t> | none | t | 工厂方法 |
unaryoperator<t> | t | t | 逻辑非(!) |
binaryoperator<t> | (t, t) | t | 求两个数的乘积 |

### 类型推断

* 代码展示
``` java
// 当前lambda表达式实现了predicate<t>接口
predicate<integer> predicate = x -> x > 5;
system.out.println(predicate.test(5));
// 当前lambda表达式实现了binaryoperator<t>接口
binaryoperator<long> operator = (x, y) -> x * y;
long apply = operator.apply(1l, 2l);
system.out.println(apply);
```

* 自定义lambda接口

``` java
public class customtest {
    public static void main(string[] args) {
        // 定义lambda表达式
        mathoperation<integer> mathoperation = (a, b) -> a * b;
        // 创建customtest对象调用方法
        customtest customtest = new customtest();
        int i = customtest.mathoperation(1, 2, mathoperation);
        system.out.println(i);
    }

    /**
     * 自定义计算方法操作参数和lambda表达式
     */
    private int mathoperation(integer a, integer b, mathoperation<integer> mathoperation) {
        return mathoperation.operation(a, b);
    }
}

interface mathoperation<t> {
    /**
     * 返回计算结果
     */
    int operation(t a, t b);
}
```

### 拓展: `Function<t>`解析
*使用`Function<t>实现函数 y = (a+10)*10 / 2`的计算*

``` java
// 自定义lambda表达式a*10
function<integer, integer> function = (a -> a * 10);

//  先于apply方法执行
function = function.compose(b -> b + 10);
log.info("function compose result: {}", function);

// 后于apply方法执行
function = function.andthen(c -> c / 2);
log.info("function andthen result: {}", function);

//  r apply(t t);
integer result = function.apply(2);
log.info("function apply result: {}", result);

// 使用function.identity()返回相同的实例,而a -> a会创建其新的实例
list<string> list = lists.newarraylist("a", "b", "c");
map<string, string> collect = list.stream().collect(collectors.tomap(function.identity(), a -> a));
// result: (a,a) (b,b) (c,c)
collect.foreach((k, v) -> log.info("key: {}, value: {}", k, v));
```

### stream
*stream用函数式编程方式在集合类上进行复杂操作的工具*

* collect()
*将Stream中的值生成一个列表*

``` java
 List<String> list = Stream.of("a", "b", "c").collect(Collectors.toList());
```

* map()
*将stream中值的类型转换成另一个类型的值*

``` java
 List<String> list2 = Stream.of("a", "b", "c").map(String::toUpperCase).collect(Collectors.toList());
```

* filter()
*遍历并检查其中的元素,保留一些并过滤掉一些*

``` java
 List<String> list3 = Stream.of("a", "B", "c", "D").filter(v -> v.equals(v.toUpperCase())).collect(Collectors.toList());
```


