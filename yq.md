            方法名 参数列表    放回值 返回修饰符   抛出异常
            
方法重写    相同  相同  相同或是其子类 不能比父类更严格    不能比父类更宽泛

方法重载    相同  相同  无关  无关  无关

父类：protected void method() throws Exception()
子类：public/protected void

父类的方法 public void method()
子类

## 9.超过long 整型的数据会报错吗
基本类型
是一个循环，不能报错
```java
public class Test02{
    public static void main(String[] args) {
        long l = Long.MAX_VALUE;
        System.out.println(l + 1);
        // 条件 'l +1 == Long.MIN_VALUE' 始终为 'true' 
        System.out.println(l +1 == Long.MIN_VALUE); // true
    }
}
```
`BigInteger`内部使用int[]数组来存储任意大小的整形数据，
BigInteger
基本数据类型（如果不够用，精度不够 或者 范围不够）-> 包装类（拥有更多的方法）->BigXxx（准确、精确、范围）

## 10.数组的使用
**一维数组**
给 67,87,88,98,99 数组中插入一个元素 90，要求保持从小到大的顺序。（不使用JDK或工具类中已有的算法）
* 扩容一位
* 1.寻找90的位置
* 2.向右平移
```java
public class Test03{
    public static void main(String[] args) {
        int nums[] = {67,87,88,98,99};
        int newNums[] = new Integer[nums.length+1];
        for(int i = 0; i < nums.length; i++){
            newNums[i] = nums[i];
        }
        
        int insert = 90; // 待插入元素
        // 设置位置为最后默认值
        int position = nums.length - 1; // 待插入的位置
        // newNums数组中第一个>90的位置，就是待插入元素的位置
        for (int i = 0; i < newNums.length; i++){
            if (newNums[i] > insert){
                position = i;
                break;
            }
        }
        System.out.println(position);
        
        // 2.向右平移
        for (int i = newNums.length-2; i >= position; i-- ){ // position, newNums.length-2
            newNums[i+1] = newNums[i];
        }
        newNums[position] = insert;
        for(int i = 0; i < newNums.length; i++){
            System.out.println(newNums[i]);
        }
    }
}
```

**二维数组**
为"陕西,关西,四川"三个省份制作二级菜单
例如，输入 陕西，输出 西安，

```java
public class Test04{
    public static void main(String[] args) {
        String[] provinces = {"陕西","山西","四川"};
        // 约定：二维数组中的一维数组 需要和provinces 的数据保持一致
        // 陕西 在 pronvinces 中的下标0；
        // 同时是 cities数组 对应的下标
        String[][] cities = {{"西安","咸阳",""},{"太原","大同",""},{"成都","广元"}};
        int position = -1; // 暂存待查省份的下标
        String pro = "山西"; // 输出 太原 大同
        // 山西 -> position -> cities
        for (int i = 0; i < provinces.length; i++){
            if(pro.equals(provinces[i])){
                position = i;
                break;
            }
        }
        if(position == -1){
            System.out.println("输入有误！！");
        }else{
            for(int i = 0; i < cities.length; i++){
                System.out.println(cities[i]);
            }
        }
    }
}
```



## 24.内部类
在类中定义的类
### 1.成员内部类
核心：在外部类中 定义内部类，可以在内部类中直接访问外部类的属性或方法
成员内部类是直接定义在类内部的类，成员内部类与类的属性及方法属于同一层级。成员内部类
并且成员内部类中不能包括静态的属性和方法。

提示：成员内部类InnerClass可以**直接访问**外部类OuterClass中的属性和方法
```java
public class OuterClass{
    String name;
    public void method(){
        
    }
    Class InnerClass{
        
    }
    
}
```

### 2.静态内部类
静态内部类就是用static修饰的成员内部类，
静态内部类只能访问外部静态的方法和成员
### 3.局部内部类


局部内部类是指定义在方法中的内部类，局部内部类的特定，只有在定义它的方法中才能使用
final修饰：在jdk8.0之后，JDK会变量增加final修饰
```java
public class OuterClass{
    private static void method(){
        String name = "Hello";
        
        class InnerClass{
            public void innerMethod(){
                // 在jdk8.0以前，需要给变量增加final修饰
                System.out.println(name);
            }
        }
    }
}
```

### 4.匿名内部类
是局部内部类中的一种

匿名方法（lambda）、匿名接口、匿名类、匿名对象
匿名：只使用一次

当一个内部类需要继承或者实现某个类，并且这个内部类只会被使用一次的时候，可以考虑使用匿名内部
类，匿名内部类实际是内部类的一种特殊形式

1.省略了实现类的编写（匿名类）2.省略了对象的生成
```java
interface MyInterface {
    public void method();


}

public class OuterClass {
    public void something() {
        new MyInterface() {
            @Override
            public void method() {
                System.out.println("...");
            }
        }
    }
}
```

## 11.Object 类的常用方法有些
```java
interface i{
public final native Class<?> getClass(){}

public native int hashCode(){}
        
public void finalize() throws Throwable{}
    // System.gc() 建议-> finalize()
}
```
## 12.一个类的声明是否能否 即是 abstract，又是
不能。语义矛盾。
抽象类 必须 和 子类 一起使用
final 修饰的类 不能继承 -> final 修饰的类不能有子类

## 17.

```java
/*
        1.但可以有以下形式：
        new 接口(){
            重写接口所有方法
        }
        
        2.new 接口[N]（new 接口数组）
        
 */
interface InterfaceA{}
public class Test05{
    // 对象的堆地址，0xABC123，0x11EEBB
    InterfaceA[] a = new InterfaceA[2];
    // 接口不能实例化new
    // 错误理解：对象本身（由于接口不能new，该理解错误）
    
}
```

## 13.String为什么是不可变的

  






