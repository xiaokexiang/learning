### 基本参数

| 参数                               | 作用                                             |
| :--------------------------------- | ------------------------------------------------ |
| `-XX:+PrintGCDetails`              | 打印详细的GC日志                                 |
| -XX:+PrintGCTimeStamps             | GC开头的时间为虚拟机启动时间的偏移量             |
| -XX:+PrintGCApplicationStoppedTime | 打印引用程序由于GC而产生停顿的时间               |
| `-Xloggc:D://log.txt`              | 输出GC日志到D盘下log.txt文件中                   |
| -XX:+PrintVMOptions                | 打印显示传递的参数                               |
| `-XX:+PrintCommandLineFlags`       | 打印传递给虚拟机的`显式和隐式`参数               |
| -XX:+PrintFlagsFinal               | 打印全部参数`(包括虚拟机自身的参数)`             |
| -Xss1m                             | 指定栈大小为1m                                   |
| -Xms10m                            | 初始堆空间大小                                   |
| -Xmx20m                            | 最大堆空间大小                                   |
| `-Xmn2m`                           | 新生代大小                                       |
| `-XX:SurvivorRatio`                | 新生代中eden/s0/s1比例，默认`8:1:1`              |
| `-XX:NewRatio`                     | 老年代/新生代的比例，默认2:1                     |
| -XX:NewSize                        | 新生代初始大小                                   |
| -XX:MaxNewSize                     | 新生代大小最大值                                 |
| `-XX:+HeapDumpOnOutOfMemoryError`  | 堆OOM时导出堆的信息                              |
| `-XX:HeapDumpPath=D://log.dump`    | 将OOM信息导入到D盘下log.dump文件中               |
| -XX:MetaspaceSize=1m               | 设置元数据区初始大小为1m                         |
| `-XX:MaxMetaspaceSize=2m`          | 设置元数据区大小最大为2m                         |
| `-XX:MaxDirectMemorySize=2m`       | 本机直接内存(堆外内存)最大2m，默认等于-Xmx       |
| -XX:+UseTLAB                       | 开启TLAB，默认开启                               |
| `-XX:+PrintTLAB`                   | 打印TLAB信息                                     |
| -XX:TLABSize=1024                  | 设置TLAB大小为1kb                                |
| `-XX:TLABRefillWasteFraction=64`   | 允许TLAB空间浪费的比例                           |
| -XX:-ResizeTLAB                    | 禁止TLAB自动调整大小和浪费比例                   |
| -XX:PretenureSizeThreshold=5242880 | 大于5m对象直接进入老年代，只对Serial、ParNew有用 |
| -XX:MaxTenuringThreshold=15        | 晋升到老年代的年龄大小                           |
| -XX:TargetSurvivorRation=50        | 用于`动态对象年龄`判断的s0的使用率参数，默认50   |

---

### 收集器选择参数

| 参数                                | 新生代            | 老年代       |
| ----------------------------------- | ----------------- | ------------ |
| -XX:+UseSerialGC                    | Serial            | Serial Old   |
| `-XX:+UseParallelGC`(JDK8默认)      | Parallel Scavenge | Parallel Old |
| -XX:+UseParNewGC(JDK8过期)          | ParNew            | Serial Old   |
| `-XX:+UseConcMarkSweepGC`(JDK9过期) | ParNew            | CMS          |
| `-XX:UseG1GC`                       | G1                | G1           |

---

### 收集相关参数

| 收集器                                          | 相关参数                                                     | 注释                                                         |
| :---------------------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| ParNew<br>Parallel<br>CMS<br>G1<br>Parallel Old | -XX:ParallelGCThreads=n                                      | 指定并行回收线程数<br>`n=cores<8?cores:3+((5*cores))/8)`     |
| Parallel                                        | -XX:MaxGCPauseMillis=n<br>-XX:GCTimeRatio=n<br>-XX:+UseAdaptiveSizePolicy | 最大回收停顿时长<br>不超过`1/1+n`时间进行回收<br>自适应GC策略 |
| CMS                                             | -XX:CMSInitiatingOccupancyFraction=n<br>-XX:CMSFullGCBeforeCompaction=n<br>-XX:+UseCMSCompactAtFullCollection | 老年代容量到n时CMS开始工作，默认92<br>CMSn次FullGC后开启碎片整理，默认0<br>CMS进行FullGC时开启碎片整理 |
| G1                                              | -XX:G1HeapRegionSize<br>-XX:MaxGCPauseMillis<br>-XX:InitiatingHeapOccupancyPercent | 指定Region大小，默认1MB，最大32MB<br>垃圾收集时停顿时长，默认200ms<br>堆使用率达到n后开启并发标记，默认45 |

---

