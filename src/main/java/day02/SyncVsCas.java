package day02;

// 在JDK9之后，sun.misc.Unsafe被移动到jdk.unsupported模块中，同时在java.base模块克隆了一个jdk.internal.misc.Unsafe类
// 代替了JDK8以前的sun.misc.Unsafe的功能，jdk.internal包不开放给开发者调用。
//import jdk.internal.misc.Unsafe;
//jdk8
import sun.misc.Unsafe;

import java.util.concurrent.atomic.AtomicReference;


// --add-opens java.base/jdk.internal.misc=ALL-UNNAMED
public class SyncVsCas {
    static final Unsafe U = Unsafe.getUnsafe();
    // CAS 操作需要知道要操作的字段的内存地址
    /*static final long BALANCE = U.objectFieldOffset(Account.class, "balance");
    // 在指定的类中查找指定的字段，并返回该字段在内存中的偏移量
    Unsafe.getUnsafe().objectFieldOffset(Account.class, "balance")

    static class Account {
        // 进行 CAS 操作时通常需要加 volatile 修饰变量，保证该字段的可见性和有序性。
        volatile int balance = 10; // 余额
    }

    private static void showResult(Account account, Thread t1, Thread t2) {
        try {
            t1.start();
            t2.start();
            t1.join();
            t2.join();
            LoggerUtils.get().debug("{}", account.balance);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void sync(Account account) {
        Thread t1 = new Thread(() -> {
            synchronized (account) {
                int old = account.balance;
                int n = old - 5;
                account.balance = n;
            }
        },"t1");

        Thread t2 = new Thread(() -> {
            synchronized (account) {
                int o = account.balance;
                int n = o + 5;
                account.balance = n;
            }
        },"t2");

        showResult(account, t1, t2);
    }

    public static void cas(Account account) {
        Thread t1 = new Thread(() -> {
            // 乐观锁
            while (true) {
                int o = account.balance;
                int n = o - 5;
                // 比较和设置CAS
                if (U.compareAndSetInt(account, BALANCE, o, n)) {
                    break;
                }
            }
        },"t1");

        Thread t2 = new Thread(() -> {
            while (true) {
                int o = account.balance;
                int n = o + 5;
                if (U.compareAndSetInt(account, BALANCE, o, n)) {
                    break;
                }
            }
        },"t2");

        showResult(account, t1, t2);
    }

    private static void basicCas(Account account) {
        while (true) {
            int o = account.balance;
            int n = o + 5;
            if(U.compareAndSetInt(account, BALANCE, o, n)){
                break;
            }
        }
        System.out.println(account.balance);
    }

    public static void main(String[] args) {
        Account account = new Account();
        cas(account);
    }*/

    private AtomicReference<Object> value = new AtomicReference<>(new Object());
    public Object getValue() {
        return value.get();
    }
    public boolean update(Object expectedValue, Object newValue) {
        return value.compareAndSet(expectedValue, newValue);
    }

}
