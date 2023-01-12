package day02;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static day02.LoggerUtils.*;

// --add-opens java.base/java.util.concurrent=ALL-UNNAMED
public class TestThreadPoolExecutor {

    public static void main(String[] args) throws InterruptedException {
        AtomicInteger c = new AtomicInteger(1);
        // 数组阻塞队列
        ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(2);
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
                2,
                3,
                0, // 救急线程的生存时间，生存时间内没有新任务，此线程资源会释放
                TimeUnit.MILLISECONDS,
                queue,
                // ThreadFactory 是一个接口，它提供了一个 newThread 方法，用来创建新的线程。 lambda 表达式实际上就是实现了这个方法。
                // 其中 new Thread(r, "myThread" + c.getAndIncrement()) 将runnable r作为参数传入，创建了一个新线程并启动了它。
                r -> new Thread(r, "myThread" + c.getAndIncrement()),
                new ThreadPoolExecutor.DiscardOldestPolicy());
        Thread thread = new Thread("myThread" + c.getAndIncrement());

        // Thread(Runnable target, String name)

        // 声明具体接口类型 确定lambda表达式所表示的具体抽象方法
        Runnable rr = () -> {};

        // 错误的写法，传入的参数Runnable r没有起到任何作用。实现该接口时应该使用方法里的固定的参数（Runnable r）实现约定的抽象方法（返回Thread对象）
        ThreadFactory threadFactory1 = r -> new Thread(() -> {},"");
        // 同样是错误的
        ThreadFactory threadFactory11 = r -> new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println();
            }
        }, "");


        // 正确的写法t2和t22。对于有参数的抽象方法可省略小括号（直接定义参数名r箭头指向->方法体），一行代码返回时可省略大括号{}和里面的return。
        ThreadFactory t2 = r -> {System.out.println(true);return new Thread(r, "");};
        ThreadFactory t22 = new ThreadFactory() {
            @Override
            public Thread newThread(@NotNull Runnable r) {
                return new Thread(r ,"");
            }
        };

        Thread thread1 = new Thread("");
        ThreadFactory threadFactory3 = r -> new Thread("");

        // Thread(Runnable target, String name) {
        Thread t222 = new Thread(() -> {logger1.debug("before waiting"); },"");

        showState(queue, threadPool);
        threadPool.submit(new MyTask("1", 3600000));
        showState(queue, threadPool);
        threadPool.submit(new MyTask("2", 3600000));
        showState(queue, threadPool);
        threadPool.submit(new MyTask("3"));
        showState(queue, threadPool);
        threadPool.submit(new MyTask("4"));
        showState(queue, threadPool);
        threadPool.submit(new MyTask("5", 3600000));
        showState(queue, threadPool);
        threadPool.submit(new MyTask("6"));
        showState(queue, threadPool);
    }

    private static void showState(ArrayBlockingQueue<Runnable> queue, ThreadPoolExecutor threadPool) {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<Object> tasks = new ArrayList<>();
        for (Runnable runnable : queue) {
            try {
                Field callable = FutureTask.class.getDeclaredField("callable");
                callable.setAccessible(true);
                Object adapter = callable.get(runnable);
                Class<?> clazz = Class.forName("java.util.concurrent.Executors$RunnableAdapter");
                Field task = clazz.getDeclaredField("task");
                task.setAccessible(true);
                Object o = task.get(adapter);
                tasks.add(o);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        main.debug("pool size: {}, queue: {}", threadPool.getPoolSize(), tasks);
    }

    static class MyTask implements Runnable {
        private final String name;
        private final long duration;

        public MyTask(String name) {
            this(name, 0);
        }

        public MyTask(String name, long duration) {
            this.name = name;
            this.duration = duration;
        }

        @Override
        public void run() {
            try {
                LoggerUtils.get("myThread").debug("running..." + this);
                Thread.sleep(duration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public String toString() {
            return "MyTask(" + name + ")";
        }
    }
}
