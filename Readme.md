## UUID的实现
UUID的格式是这样的：xxxxxxxx-xxxx-Mxxx-Nxxx-xxxxxxxxxxxx，一共为32个16进制数。
M那个位置，代表版本号，目前UUID的标准实现有5个版本，所以只会是1,2,3,4,5
N那个位置，只会是8,9,a,b

UUID的具体实现存在多个版本，分别为：

### 1. 基于时间的UUID(Generators.timeBasedGenerator())
基于时间的UUID通过计算当前时间戳、随机数和机器MAC地址得到。由于在算法中使用了MAC地址，这个版本的UUID可以保证在全球范围的唯一性。但与此同时，使用MAC地址会带来安全性问题，这就是这个版本UUID受到批评的地方。如果应用只是在局域网中使用，也可以使用退化的算法，以IP地址来代替MAC地址。

### 2. DCE（Distributed Computing Environment）安全的UUID
和基于时间的UUID算法相同，但会把时间戳的前4位置换为POSIX的UID或GID，这个版本的UUID在实际中较少用到。

### 3. 基于名称空间的UUID（MD5,Generators.nameBasedGenerator(null, MessageDigest.getInstance("MD5"))
）
基于名称的UUID通过计算名称和名称空间的MD5散列值得到，这个版本的UUID保证了：相同名称空间中不同名称生成的UUID的唯一性；不同名称空间中的UUID的唯一性；相同名称空间中相同名称的UUID重复生成是相同的。

### 4. 基于随机数的UUID(Generators.randomBasedGenerator();)
根据随机数，或者伪随机数生成UUID。这种UUID产生重复的概率是可以计算出来的，但随机的东西就像是买彩票：你指望它发财是不可能的，但狗屎运通常会在不经意中到来。可能在测试的时候多线程并发也不见得出现重复，但是却不能保证系统正式上线之后不会出现不重复的UUID，特别是在分布式系统中。

### 5. 基于名称空间的UUID（SHA1,Generators.nameBasedGenerator(null, MessageDigest.getInstance(" SHA-1"))
）
和版本3的UUID算法类似，只是散列值计算使用SHA1（Secure Hash Algorithm 1）算法。


## 雪花算法实现SnowFlake
* <br>
 * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
 * <br>
 * 1位标识，由于long基本类型在Java中是带符号的，最高位是符号位，正数是0，负数是1，所以id一般是正数，最高位是0
 * <br>
 * 41位时间截(毫秒级)，注意，41位时间截不是存储当前时间的时间截，而是存储时间截的差值（当前时间截 - 开始时间截)
 * <br>
 * 这里的的开始时间截，一般是我们的id生成器开始使用的时间，由我们程序来指定的（如下下面程序IdWorker类的startTime属性）。
 * 41位的时间截，可以使用69年，年T = (1L << 41) / (1000L * 60 * 60 * 24 * 365) = 69
 * <br>
 * 10位的数据机器位，可以部署在1024个节点，包括5位datacenterId和5位workerId<br>
 * <br>
 * 12位序列，毫秒内的计数，12位的计数顺序号支持每个节点每毫秒(同一机器，同一时间截)产生4096个ID序号<br>
 * <br>
 * <br>
 * 加起来刚好64位，为一个Long型。<br>
### 1.单机性能每秒假设达到最佳，能够达到 4096*1000=400w左右

## 雪花算法，百度版本
 * +------+----------------------+----------------+-----------+
 * | sign |     delta seconds    | worker node id | sequence  |
 * +------+----------------------+----------------+-----------+
 *   1bit          28bits              22bits         13bits

### 1.百度实现了，两种算法，一种是双buffer缓存的，一种就是原生的。不过这里要注意的是百度的实现时间只够使用8.7年，机器倒是可以使用很多。这种像是各个机器自己去实现自增的思维，非集中式的想法。

### 2.双buffer缓存 就是提前先计算好，然后直接去取就好了。用到一个阈值，就会重新生成。

## 美团的版本，依赖了数据库，或者是zk
Leaf数据库中的号段表格式如下：
+-------------+--------------+------+-----+-------------------+-----------------------------+
| Field       | Type         | Null | Key | Default           | Extra                       |
+-------------+--------------+------+-----+-------------------+-----------------------------+
| biz_tag     | varchar(128) | NO   | PRI |                   |                             |
| max_id      | bigint(20)   | NO   |     | 1                 |                             |
| step        | int(11)      | NO   |     | NULL              |                             |
| desc        | varchar(256) | YES  |     | NULL              |                             |
| update_time | timestamp    | NO   |     | CURRENT_TIMESTAMP | on update CURRENT_TIMESTAMP |
+-------------+--------------+------+-----+-------------------+-----------------------------+
Leaf Server加载号段的SQL语句如下：

```
Begin
UPDATE table SET max_id=max_id+step WHERE biz_tag=xxx
SELECT tag, max_id, step FROM table WHERE biz_tag=xxx
Commit
```

### 解决毛刺问题
为了解决这两个问题，Leaf采用了异步更新的策略，同时通过双Buffer的方式，保证无论何时DB出现问题，都能有一个Buffer的号段可以正常对外提供服务，只要DB在一个Buffer的下发的周期内恢复，就不会影响整个Leaf的可用性。

### Leaf动态调整Step
假设服务QPS为Q，号段长度为L，号段更新周期为T，那么Q * T = L。最开始L长度是固定的，导致随着Q的增长，T会越来越小。但是Leaf本质的需求是希望T是固定的。那么如果L可以和Q正相关的话，T就可以趋近一个定值了。所以Leaf每次更新号段的时候，根据上一次更新号段的周期T和号段长度step，来决定下一次的号段长度nextStep：


### 1.版本1分段式发号器，为每个业务有自增的使用。不能当做订单使用，会暴露自家订单量。

### 2.版本2 基于zk版本的雪花算法。
对Zookeeper生成机器号做了弱依赖处理
