package day03.reference;

import day02.LoggerUtils;

import java.io.IOException;
import java.lang.ref.*;
import java.util.ArrayList;
import java.util.List;

public class TestPhantomReference {
    public static void main(String[] args) throws IOException, InterruptedException {
        ReferenceQueue<String> queue = new ReferenceQueue<>();// 引用队列
        List<MyResource> list = new ArrayList<>();
        list.add(new MyResource(new String("a"), queue));
        list.add(new MyResource("b", queue));
        list.add(new MyResource(new String("c"), queue));

        System.gc(); // 垃圾回收
        Thread.sleep(100);
        Object ref;
        while ((ref = queue.poll()) != null) {
            // 语言级别 '8' 不支持 'instanceof' 中的模式 升级到16
//            if (ref instanceof MyResource resource) {
//                resource.clean();
//            }
        }
    }

    static class MyResource extends PhantomReference<String> {
        public MyResource(String referent, ReferenceQueue<? super String> q) {
            super(referent, q);
        }
        // 释放外部资源的方法
        public void clean() {
            LoggerUtils.get().debug("clean");
        }
    }
}
