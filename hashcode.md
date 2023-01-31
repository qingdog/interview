## GC复制存活对象，它内存地址变了么？
**前言**

前些天与一位朋友技术交流，朋友在招人面试时想到一个问题，JVM垃圾回收时，会复制存活的对象到不同的区域。比如从新生代复制到老年代，在此过程中，被复制的对象的地址是否变了呢？对他提出的这个问题很感兴趣，深入研究了一下，便有了这篇文章。

**更新引用是JVM的职责**

任何一款JVM的设计，采用任何一种GC算法进行对象的移动操作时，如何更新对象引用都是JVM的基本职责。也就是说，当移动对象时，必然会涉及到对象引用的变更，只不过这部分操作JVM已经帮我们做了。

作为开发者来说，可以将引用理解为存储对象的抽象句柄，而不必担心JVM是如何管理对象存储的。但如果做技术研究，好奇底层的实现，倒是值得深入研究一下。

当对象的实际地址发生变化时，简单来说，JVM会将指向该地址的一个或多个变量所使用的引用地址进行更新，从而达到在“不知不觉”中移动了对象的效果。

JVM规范中只规定了引用类型是指向对象的引用，并没有限制具体的实现。因此，不同虚拟机的实现方式可能不同。通常有两种实现形式：句柄访问和直接指针访问。

**句柄访问**

先来看一张图，句柄访问的形式是堆空间维护一个句柄池，对象引用中保存的是对象的句柄位置。在堆中的句柄包含对象的实例数据和类型数据的真实地址。
![image-20210831084325075](img/hashcode/1.jpg)

这种形式的实现好处很明显，引用中保存的对象句柄地址相对稳定（不变），当GC操作移动对象时只用维护句柄池中存储的信息即可，特别是多个变量都引用同一个句柄池中的句柄时，可以减少更新变量存储的引用，同时确保变量的地址不变。缺点就是多了一次中转，访问效率会有影响。

**直接指针访问**

直接指针访问省去了中间的句柄池，对象引用中保持的直接是对象地址。
![image-20210831084325075](img/hashcode/2.jpg)

这种方式很明显节省了一次指针定位的开销，访问速度快。但是当GC发生对象移动时，变量中保持的引用地址也需要维护，如果多个变量指向一个地址，需要更新多次。Hot Spot虚拟机便是基于这种方式实现的。

**如何查看引用地址？**

上面聊了对象引用的实现形式，那么在日常开发中是否可以通过打印等形式来查看对象的地址吗？有这样一个说法，通过对象默认的toString方法打印出来的信息中包含对象的引用地址。下面我们通过一个实例来看看：

```java
Bike bike = new Bike();
System.out.println(bike);
```
当我们执行上述程序时，控制台会打印出如下信息：
```java
com.secbro2.others.Bike@4dc63996
```
@后面的字符串是什么？是对象的地址吗？这种地址的说法其实在坊间流传了很久。我们先来看Object的toString源码：
```java
public String toString() {
    return getClass().getName() + "@" + Integer.toHexString(hashCode());
}
```
通过源码我们会发现，其实@符合后面并不是对象的地址，而只是hashcode的十六进制展现形式而已。

那么，如何打印对象的内存地址呢？我们需要依赖一个JOL（Java Object Layout）类库，在项目中添加如下Maven依赖：
```java
<dependency>
    <groupId>org.openjdk.jol</groupId>
    <artifactId>jol-core</artifactId>
    <version>0.10</version>
</dependency>
```
然后在程序中通过如下方法使用：
```java
String answer = "42";
System.out.println("The memory address is " + VM.current().addressOf(answer));
```
会发现打印的内容如下：
```java
The memory address is 31856221536
```
上面的便是真实的内存地址，虽然能够获取并打印出内存地址，但由于不同环境下的JVM采用了不同的指针压缩操作。因此，我们不要基于此地址来做一些本机内存相关的操作。但上面的打印，明确的证明了toString方法打印出来的信息并不包括对象的内存地址。

鉴于此，基于toString方法打印出来hashCode值只能保证两个对象的hashcode一样，却无法保证两个引用地址指向同一对象。

**小结**
通过与朋友的一个小交流，深挖一下，竟然发现不少底层的知识点，交流和探索的作用可见一斑。总结来说就是：JVM在GC操作时会自动维护引用地址，变量对应的应用地址是否变化要看采用的是基于句柄池方式还是直接指针指向的方式。同时，当我们通过toString方法打印时，输出的内容并不包含对象地址，只不过是对象hashcode的十六进制而已。

原创 二师兄 程序新视界 2021-04-06 22:50
https://mp.weixin.qq.com/s/rKEdTxXIc8F8ZwGW-hTakg

## GC时对象地址变了，hashCode如何保持不变？

**前言**

前几天跟朋友交流，引出了一篇文章《GC复制存活对象，它内存地址变了么？》，我们得知在Hotspot虚拟机下，当GC发生时，对象的地址是会发生变化的。

同时我们也知道，无论何时调用同一对象的hashCode方法，都必须返回相同的值，也就是说一个对象的hashCode值要在生命周期中保持不变。同时，网传“hashCode是根据对象地址生成的”。那么，问题来了，当对象地址变化了，hashCode如何保持不变呢？

**HashCode的约定**

在继续研究之前，我们先来看一下对hashCode方法的一些约定和说明。

在java.lang.Object的JavaDoc注释上对hashCode方法有三项约定，概括来说就是：

第一，当一个对象equals方法所使用的字段不变时，多次调用hashCode方法的值应保持不变。

第二，如果两个对象equals(Object o)方法是相等的，则hashCode方法值必须相等。

第三，如果两个对象equals(Object o)方法是不相等，则hashCode方法值不要求相等，但在这种情况下尽量确保hashCode不同，以提升性能。

同时注释部分还有这样一段描述：
```
As much as is reasonably practical, 
the hashCode method defined by class Object does return distinct integers for distinct objects. 
(This is typically implemented by converting the internal address of the object into an integer, 
but this implementation technique is not required by the Java™ programming language.)
```
**谷歌翻译：**
在相当实用的情况下，类 Object 定义的 hashCode 方法确实会为不同的对象返回不同的整数。 （这通常是通过将对象的内部地址转换为整数来实现的，但 Java™ 编程语言不需要这种实现技术。）

通过上面的描述我们知道，一般情况下hashCode是通过对象的内存地址映射过来的。这也应该就是开篇说的“hashCode是根据对象地址生成的”的来源吧。

但我们知道，JVM进行GC操作时，无论是标记复制算法还是标记整理算法，对象的内存地址都是会变的。但hashcode又要求保持不变，JVM到底是如何实现这一功能的呢？另外还有一个问题，如果一个对象被移动到了另外一个位置，而它原来的位置被其他对象填充了，那新填充的对象是否会与之前的对象hashCode相同呢？

下面我们就来探究一下JVM的实现。

**GC前后的hashCode值**

我们先通过一个实例来验证一下GC前后对象地址和hashcode值的变化。在项目中引入JOL依赖：
```java
<dependency>
    <groupId>org.openjdk.jol</groupId>
    <artifactId>jol-core</artifactId>
    <version>0.10</version>
</dependency>
```
验证代码如下：
```java
public class TestHashCode {
    public static void main(String[] args) {
        Object obj = new Object();
        long address = VM.current().addressOf(obj);
        long hashCode = obj.hashCode();
        System.out.println("before GC : The memory address is " + address);
        System.out.println("before GC : The hash code is " + hashCode);

        new Object();
        new Object();
        new Object();

        System.gc();

        long afterAddress = VM.current().addressOf(obj);
        long afterHashCode = obj.hashCode();
        System.out.println("after GC : The memory address is " + afterAddress);
        System.out.println("after GC : The hash code is " + afterHashCode);
        System.out.println("---------------------");

        System.out.println("memory address = " + (address == afterAddress));
        System.out.println("hash code = " + (hashCode == afterHashCode));
    }
}
```
上述代码执环境为Hotspot虚拟机，执行时如果未出现GC，则可将JVM参数设置的小一点，
比如可以设置为16M：-Xms16m -Xmx16m -XX:+PrintGCDetails。

执行上述代码，打印日志如下：

```
before GC : The memory address is 31856020608
before GC : The hash code is 2065530879
after GC : The memory address is 28991167544
after GC : The hash code is 2065530879
---------------------
memory address = false
hash code = true
```
上面的控制台信息可以看出，GC前后对象的地址的确变了，但hashCode却并未发生变化。同时也可以看出hashcode的值与内存地址的值是完全不一样的，根据hashCode方法的注解，我们暂时只能认为hashcode值与地址有关。

**hashCode不变的原理**

经过上述实例，很明显在GC操作时，原来的hashcode值被存储在了某个地方，以备再用。对此以Hotspot为例，最直接的实现方式就是在对象的header区域中划分出来一部分（32位机器上是占用25位，64位机器上占用31）用来存储hashcode值。但这种方式会添加额外信息到对象中，而在大多数情况下hashCode方法并不会被调用，这就造成空间浪费。

那么JVM是如何进行优化的呢？当hashCode方法未被调用时，object header中用来存储hashcode的位置为0，只有当hashCode方法（本质上是System#identityHashCode）首次被调用时，才会计算对应的hashcode值，并存储到object header中。当再次被调用时，则直接获取计算好hashcode即可。

上述实现方式就保证了即使GC发生，对象地址发生了变化，也不影响hashcode的值。比如在GC发生前调用了hashCode方法，hashcode值已经被存储，即使地址变了也没关系；在GC发生后调用hashCode方法更是如此。

**hashcode生成的方式**

不同的JVM对hashcode值的生成方式不同。Open JDK中提供了6中生成hash值的方法。

0：随机数生成器（A randomly generated number.）；
1：通过对象内存地址的函数生成（A function of memory address of the object.）；
2：硬编码1（用于敏感度测试）（A hardcoded 1 (used for sensitivity testing.)）；
3：通过序列（A sequence.）；
4：对象的内存地址，强制转换为int。（The memory address of the object, cast to int.）
5：线程状态与xorshift结合（Thread state combined with xorshift）；

其中在OpenJDK6、7中使用的是随机数生成器的（第0种）方式，OpenJDK8、9则采用第5种作为默认的生成方式。所以，单纯从OpenJDK的实现来说，其实hashcode的生成与对象内存地址没有什么关系。而Object类中hashCode方法上的注释，很有可能是早期版本中使用到了第4种方式。

**hashCode与identityHashCode**

上面我们多次提到hashCode方法，还提到identityHashCode方法，如果单纯以Object类中的hashCode方法来说，它与System类中提供了的identityHashCode方法是一致的。但在实践中我们往往会重写hashCode方法，此时object header中存储的hashcode值便有两种情况，一个是父类Object的，一个是实现类的。

在OpenJDK中，header中存储的是通过System#identityHashCode获得的hashcode，而重写的hashCode方法的子类的hashcode则是通过实时调用其实现方法获得的。

那么，如果一个类的hashCode方法被重写了，那么是否还可以获得原始的hashcode吗？System#identityHashCode方法可以返回一个不变的hascode值，无论当前对象是否重写了hashCode方法。

我们用一个实例来验证一下，先创建一个Person类，实现hashcode方法：

```java
public class Person {

    private int id;
    // 省略getter/setter 和equals方法

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
```
验证代码如下：
```java
Person person = new Person();
person.setId(1);

System.out.println("Hashcode = " + person.hashCode());
System.out.println("Identity Hashcode = " + System.identityHashCode(person));
```
执行验证程序，打印结果如下：
```
Hashcode = 32
Identity Hashcode = 1259475182
```
会发现System#identityHashCode方法采用了默认的hashCode方法，而不是Person对象重写的hashCode方法。本质上Object的hashCode方法也是调用的identityHashCode方法。

**还有一个问题**

如果JVM使用基于对象内存地址的方式生成hashcode值，那么是否会出现这样的问题：如果Object1被调用了hashCode方法，然后GC移动。此时，Object2被分配到Object1原来的位置，Object2也调用hashCode方法，那么这两个对象的hashcode值是否相等呢？的确，它们有可能是相等的，但这也没什么关系，hashcode值只是一个hashcode值，并不要求它是唯一的。当出现hash冲突时，同样会出现相同的值。

**再验证一下**

上面说了hashcode值的存储逻辑，那么是否可以从侧面证明一下呢？我们依旧采用JOL依赖类库，来写一个程序查看一下hashCode方法被调用之后，Object header中信息的变化。
```java
// 创建对象并打印JVM中对象的信息
Object person = new Object();
System.out.println(ClassLayout.parseInstance(person).toPrintable());
// 调用hashCode方法，如果重写了hashCode方法则调用System#identityHashCode方法
System.out.println(person.hashCode());
// System.out.println(System.identityHashCode(person));
// 再次打印对象JVM中的信息
System.out.println(ClassLayout.parseInstance(person).toPrintable());
```
执行上述程序，控制台打印如下：
```
java.lang.Object object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           01 00 00 00 (00000001 00000000 00000000 00000000) (1)
      4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4        (object header)                           e5 01 00 f8 (11100101 00000001 00000000 11111000) (-134217243)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

1898220577
java.lang.Object object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           01 21 8c 24 (00000001 00100001 10001100 00100100) (613163265)
      4     4        (object header)                           71 00 00 00 (01110001 00000000 00000000 00000000) (113)
      8     4        (object header)                           e5 01 00 f8 (11100101 00000001 00000000 11111000) (-134217243)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total
```
在调用hashCode方法前后，我们可以看到OFFSET为0的一行存储的值（Value），从原来的1变为613163265，也就是说将hashcode的值进行了存储。如果未调用对应方法，则不会进行存储 。

**小结**

经过本文的分析，我们会发现针对GC移动对象导致hashCode变化这个问题，在JVM未使用对象内存地址生成hashcode时已经不是什么问题了。但在探索这个问题的过程中，我们了解了hashcode的生成、存储以及与identityHashCode方法的关系，最后来实践了JOL的新用法，反而收获了更多。

————————————————

版权声明：本文为CSDN博主「程序新视界」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/wo541075754/article/details/115497869

## jdk中Hashcode方法的具体实现 及 如何在openjdk中查找native方法具体实现实例

1.查找java.lang.Object，切换到Object.class文件所在目录，
执行 javah -jni java.lang.Object，得到java_lang_Object.h文件，文件内容如下：
```h
/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class java_lang_Object */

#ifndef _Included_java_lang_Object
#define _Included_java_lang_Object
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     java_lang_Object
 * Method:    registerNatives
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Object_registerNatives
  (JNIEnv *, jclass);

/*
 * Class:     java_lang_Object
 * Method:    getClass
 * Signature: ()Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_Object_getClass
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_Object
 * Method:    hashCode
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_lang_Object_hashCode
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_Object
 * Method:    clone
 * Signature: ()Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Object_clone
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_Object
 * Method:    notify
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Object_notify
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_Object
 * Method:    notifyAll
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Object_notifyAll
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_Object
 * Method:    wait
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_java_lang_Object_wait
  (JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif
#endif
```
native源码：

http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/d2bbff5b7006/src/share/native

由Java_java_lang_Object
找到Obejcet.c,在c文件中可以看到hashcode具体实现被注册成由JVM_IHashCode方法指针来处理
```c
static JNINativeMethod methods[] = {  
    {"hashCode",    "()I",                    (void *)&JVM_IHashCode},//hashcode的方法指针JVM_IHashCode  
    {"wait",        "(J)V",                   (void *)&JVM_MonitorWait},  
    {"notify",      "()V",                    (void *)&JVM_MonitorNotify},  
    {"notifyAll",   "()V",                    (void *)&JVM_MonitorNotifyAll},  
    {"clone",       "()Ljava/lang/Object;",   (void *)&JVM_Clone},  
};  
```
而JVM_IHashCode方法指针在 openjdk\hotspot\src\share\vm\prims\jvm.cpp中定义为：
```cpp
JVM_ENTRY(jint, JVM_IHashCode(JNIEnv* env, jobject handle))  
  JVMWrapper("JVM_IHashCode");  
  // as implemented in the classic virtual machine; return 0 if object is NULL  
  return handle == NULL ? 0 : ObjectSynchronizer::FastHashCode (THREAD, JNIHandles::resolve_non_null(handle)) ;  
JVM_END 
```
真正计算获得hashCode的值是ObjectSynchronizer::FastHashCode
ObjectSynchronizer::FastHashCode的实现方法在
openjdk\hotspot\src\share\vm\runtime\synchronizer.cpp，具体如下
```cpp
intptr_t ObjectSynchronizer::FastHashCode (Thread * Self, oop obj) {
  if (UseBiasedLocking) {
    // NOTE: many places throughout the JVM do not expect a safepoint
    // to be taken here, in particular most operations on perm gen
    // objects. However, we only ever bias Java instances and all of
    // the call sites of identity_hash that might revoke biases have
    // been checked to make sure they can handle a safepoint. The
    // added check of the bias pattern is to avoid useless calls to
    // thread-local storage.
    if (obj->mark()->has_bias_pattern()) {
      // Box and unbox the raw reference just in case we cause a STW safepoint.
      Handle hobj (Self, obj) ;
      // Relaxing assertion for bug 6320749.
      assert (Universe::verify_in_progress() ||
              !SafepointSynchronize::is_at_safepoint(),
             "biases should not be seen by VM thread here");
      BiasedLocking::revoke_and_rebias(hobj, false, JavaThread::current());
      obj = hobj() ;
      assert(!obj->mark()->has_bias_pattern(), "biases should be revoked by now");
    }
  }

  // hashCode() is a heap mutator ...
  // Relaxing assertion for bug 6320749.
  assert (Universe::verify_in_progress() ||
          !SafepointSynchronize::is_at_safepoint(), "invariant") ;
  assert (Universe::verify_in_progress() ||
          Self->is_Java_thread() , "invariant") ;
  assert (Universe::verify_in_progress() ||
         ((JavaThread *)Self)->thread_state() != _thread_blocked, "invariant") ;

  ObjectMonitor* monitor = NULL;
  markOop temp, test;
  intptr_t hash;
  markOop mark = ReadStableMark (obj);

  // object should remain ineligible for biased locking
  assert (!mark->has_bias_pattern(), "invariant") ;

  if (mark->is_neutral()) {
    hash = mark->hash();              // this is a normal header
    if (hash) {                       // if it has hash, just return it
      return hash;
    }
    hash = get_next_hash(Self, obj);  // allocate a new hash code
    temp = mark->copy_set_hash(hash); // merge the hash code into header
    // use (machine word version) atomic operation to install the hash
    test = (markOop) Atomic::cmpxchg_ptr(temp, obj->mark_addr(), mark);
    if (test == mark) {
      return hash;
    }
    // If atomic operation failed, we must inflate the header
    // into heavy weight monitor. We could add more code here
    // for fast path, but it does not worth the complexity.
  } else if (mark->has_monitor()) {
    monitor = mark->monitor();
    temp = monitor->header();
    assert (temp->is_neutral(), "invariant") ;
    hash = temp->hash();
    if (hash) {
      return hash;
    }
    // Skip to the following code to reduce code size
  } else if (Self->is_lock_owned((address)mark->locker())) {
    temp = mark->displaced_mark_helper(); // this is a lightweight monitor owned
    assert (temp->is_neutral(), "invariant") ;
    hash = temp->hash();              // by current thread, check if the displaced
    if (hash) {                       // header contains hash code
      return hash;
    }
    // WARNING:
    //   The displaced header is strictly immutable.
    // It can NOT be changed in ANY cases. So we have
    // to inflate the header into heavyweight monitor
    // even the current thread owns the lock. The reason
    // is the BasicLock (stack slot) will be asynchronously
    // read by other threads during the inflate() function.
    // Any change to stack may not propagate to other threads
    // correctly.
  }

  // Inflate the monitor to set hash code
  monitor = ObjectSynchronizer::inflate(Self, obj);
  // Load displaced header and check it has hash code
  mark = monitor->header();
  assert (mark->is_neutral(), "invariant") ;
  hash = mark->hash();
  if (hash == 0) {
    hash = get_next_hash(Self, obj);
    temp = mark->copy_set_hash(hash); // merge hash code into header
    assert (temp->is_neutral(), "invariant") ;
    test = (markOop) Atomic::cmpxchg_ptr(temp, monitor, mark);
    if (test != mark) {
      // The only update to the header in the monitor (outside GC)
      // is install the hash code. If someone add new usage of
      // displaced header, please update this code
      hash = test->hash();
      assert (test->is_neutral(), "invariant") ;
      assert (hash != 0, "Trivial unexpected object/monitor header usage.");
    }
  }
  // We finally get the hash
  return hash;
}
```
hash值具体由get_next_hash方法生成
```cpp
  mark = monitor->header();
  assert (mark->is_neutral(), "invariant") ;
  hash = mark->hash();
  if (hash == 0) {
    hash = get_next_hash(Self, obj);
```
方法查看
http://hg.openjdk.java.net/jdk8u/jdk8u/hotspot/file/87ee5ee27509/src/share/vm/runtime/synchronizer.cpp#l555

**hashcode的具体生成方法：**
```cpp
static inline intptr_t get_next_hash(Thread * Self, oop obj) {
  intptr_t value = 0 ;
  if (hashCode == 0) {
  //这种生成算法，使用的一种Park-Miller RNG的随机数生成策略。
  //不过需要注意的是……这个随机算法在高并发的时候会出现自旋等待
     // This form uses an unguarded global Park-Miller RNG,
     // so it's possible for two threads to race and generate the same RNG.
     // On MP system we'll have lots of RW access to a global, so the
     // mechanism induces lots of coherency traffic.
     value = os::random() ;
  } else
  if (hashCode == 1) {
     // This variation has the property of being stable (idempotent)
     // between STW operations.  This can be useful in some of the 1-0
     // synchronization schemes.
     intptr_t addrBits = intptr_t(obj) >> 3 ;
     value = addrBits ^ (addrBits >> 5) ^ GVars.stwRandom ;
  } else
  if (hashCode == 2) {
  	//返回固定hashcode值1
     value = 1 ;            // for sensitivity testing
  } else
  if (hashCode == 3) {
  	 自增序列
     value = ++GVars.hcSequence ;
  } else
  if (hashCode == 4) {
    直接用对象的内存地址，直接获取对象的 intptr_t 类型指针
     value = intptr_t(obj) ;
  } else {
     // Marsaglia's xor-shift scheme with thread-specific state
     // This is probably the best overall implementation -- we'll
     // likely make this the default in future releases.
     unsigned t = Self->_hashStateX ;
     t ^= (t << 11) ;
     Self->_hashStateX = Self->_hashStateY ;
     Self->_hashStateY = Self->_hashStateZ ;
     Self->_hashStateZ = Self->_hashStateW ;
     unsigned v = Self->_hashStateW ;
     v = (v ^ (v >> 19)) ^ (t ^ (t >> 8)) ;
     Self->_hashStateW = v ;
     value = v ;
  }
 
  value &= markOopDesc::hash_mask;
  if (value == 0) value = 0xBAD ;
  assert (value != markOopDesc::no_hash, "invariant") ;
  TEVENT (hashCode: GENERATE) ;
  return value;
}
```
具体hash值的生成方法由这边一个全局变量hashcode决定
有五种生成策略，hashcode的定义在另一个头文件中，默认是5
生成处：http://hg.openjdk.java.net/jdk8u/jdk8u/hotspot/file/87ee5ee27509/src/share/vm/runtime/globals.hpp#l1127
```cpp
product(intx, hashCode, 5,                                            
         "(Unstable) select hashCode generation algorithm" ) 
```
java命令也可以决定hashcode值
```shell
//java命令
java -XX:+PrintFlagsFinal -version | grep hashCode
```
```
intx hashCode                                  = 5                                   {product}
openjdk version "1.8.0_282"
OpenJDK Runtime Environment (AdoptOpenJDK)(build 1.8.0_282-b08)
OpenJDK 64-Bit Server VM (AdoptOpenJDK)(build 25.282-b08, mixed mode)
```
前四种说明在前面代码注释查看，第五种解析如下：
```cpp
     unsigned t = Self->_hashStateX ;
     t ^= (t << 11) ;
     Self->_hashStateX = Self->_hashStateY ;
     Self->_hashStateY = Self->_hashStateZ ;
     Self->_hashStateZ = Self->_hashStateW ;
     unsigned v = Self->_hashStateW ;
     v = (v ^ (v >> 19)) ^ (t ^ (t >> 8)) ;
     Self->_hashStateW = v ;
     value = v ;

	可以看到由hashStateW  和 hashStateX进行移位和异或操作获得，
	
	根据Marsaglia's xorshift scheme随机数算法得到的一个随机数
	xorshift是由George Marsaglia发现的一类伪随机数生成器，
	其通过移位和与或计算，能够在计算机上以极快的速度生成伪随机数序列。
	
```
Marsaglia’s xorshift scheme随机数算法论文：

https://www.jstatsoft.org/article/view/v008i14/xorshift.pdf
```cpp
Marsaglia's xorshift scheme随机数算法实现
unsigned long xor128(){
static unsigned long x=123456789,y=362436069,z=521288629,w=88675123;
unsigned long t;
t=(xˆ(x<<11));x=y;y=z;z=w; 
return( w=(wˆ(w>>19))ˆ(tˆ(t>>8)) );
```
hashStateW 和 hashStateX的生成是在Thread的初始化中完成
定义处：http://hg.openjdk.java.net/jdk8u/jdk8u/hotspot/file/87ee5ee27509/src/share/vm/runtime/thread.cpp#I263
```cpp
具体生成代码：
_hashStateX = os::random() ;
_hashStateY = 842502087 ;
_hashStateZ = 0x8767 ;    // (int)(3579807591LL & 0xffff) ;
_hashStateW = 273326509 ;
```
可以看到object的hashcode方法最终生成的hashcode实际上由线程和jdk中的hashcode参数决定，和内存地址无必然关系。

————————————————

版权声明：本文为CSDN博主「fengting1995」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/fengting1995/article/details/121186166

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Chrome网页翻译测试</title>
</head>
<body>
<div>
    <div style="display:flex;column-gap:10px;">
        <div style="flex:1;">
            <label style="font-size:.85em;">※ 输入英文</label>
            <label>
                <textarea placeholder="输入英文" style="display:block;padding:10px;border:2px solid #666;border-radius:5px;box-sizing:border-box;width:100%;resize: vertical;">Hello World!</textarea>
            </label>
        </div>
        <div style="flex:1;">
            <label style="font-size:.85em;">※ 翻译结果</label>
            <label>
                <textarea placeholder="翻译结果" style="display:block;padding:10px;border:2px solid #666;border-radius:5px;box-sizing:border-box;width:100%;resize: vertical;"></textarea>
            </label>
        </div>
    </div>
    <div style="margin-top:10px;">
        <input type="submit" value="测试一下" style="margin:0 auto;padding:5px 10px;border:1px solid #ccc;border-radius:5px;width:100%;">
    </div>
</div>
</body>
<script>
    window.onload = function(){
        const input = document.querySelector('input');
        input.onclick = function (){
            const xhr = new XMLHttpRequest();

            const api = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=zh-CN&hl=en-US&dt=t&dt=bd&dj=1&source=icon&tk=294611.294611&q=";
            const url = new URL(api);
            url.searchParams.set('q', document.getElementsByTagName("textarea")[0].value);

            xhr.open('post', url.href, true);
            xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");

            document.getElementsByTagName("textarea")[1].value = "翻译中。。。";
            xhr.onload = function () {
                if (xhr.status === 200) {
                    const json = JSON.parse(xhr.responseText);
                    let value = "";
                    for(let i = 0; i < json.sentences.length; i++){
                        value += json.sentences[i].trans;
                    }
                    document.getElementsByTagName("textarea")[1].value = value;
                    console.log(json);
                } else {
                    document.getElementsByTagName("textarea")[1].value = "翻译出错。";
                }
            };
            // 处理net::ERR_CONNECTION_TIMED_OUT
            xhr.timeout = 5000; // 设置超时时间为 5000 毫秒
            xhr.ontimeout = function () {
                document.getElementsByTagName("textarea")[1].value = "请求 API 失败。";
            };
            xhr.send();
        }


        // input.addEventListener('click', (event) => {
        //     event.preventDefault();
        //
        //     const token = "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";
        //     send(token);
        // });
    }
    function send(token){
        document.getElementsByTagName("textarea")[1].value = "发送中。。。";

        const xhr = new XMLHttpRequest();
        xhr.open('post', "https://api.openai.com/v1/completions", true);

        xhr.setRequestHeader('Content-Type', 'application/json');
        xhr.setRequestHeader('Authorization', 'Bearer ' + token);

        xhr.onload = function () {
            if (xhr.status === 200) {
                const json = JSON.parse(xhr.responseText);
                document.getElementsByTagName("textarea")[1].value = json.choices[0].text;
                console.log(json);
            }
        };

        const value = document.getElementsByTagName("textarea")[0].value;
        const json = {model: 'text-davinci-003', max_tokens: 2048, prompt: value};
        //console.log(json);
        xhr.send(JSON.stringify(json));
    }
</script>
</html>