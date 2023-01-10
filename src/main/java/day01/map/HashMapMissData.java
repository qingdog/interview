package day01.map;

import java.util.HashMap;

public class HashMapMissData {
    public static void main(String[] args) throws InterruptedException {

        HashMap<String, Object> map = new HashMap<>();
        Thread t1 = new Thread(() -> {
            map.put("a", new Object()); // 97  => 1
        }, "t1");

        // if ((p = tab[i = (n - 1) & hash]) == null)
        // tab[i] = newNode(hash, key, value, null);
        // Thread.currentThread().getName().equals("t1")||Thread.currentThread().getName().equals("t2")
        Thread t2 = new Thread(() -> {
            map.put("1", new Object()); // 49 => 1
        }, "t2");

        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println(map);
    }
}
