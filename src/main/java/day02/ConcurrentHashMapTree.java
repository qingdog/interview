package day02;

import javax.swing.tree.TreeNode;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashMapTree {
    public static void main(String[] args) throws Exception{
        // tableSizeFor(initialCapacity + (initialCapacity >>> 1) + 1)) = i + i/2 + 1
        // long size = (long)(1.0 + (long)initialCapacity / loadFactor); = i / l + 1
        // int cap = (size >= (long)MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : tableSizeFor((int)size);
        ConcurrentHashMap<String, String> concurrentHashMap = new ConcurrentHashMap<>(1,0.75f);
        concurrentHashMap.put("1","3");
        concurrentHashMap.put("a","3");

        Field tableField = concurrentHashMap.getClass().getDeclaredField("table");
        tableField.setAccessible(true);
        Object[] table = (Object[]) tableField.get(concurrentHashMap);
        System.out.println("ConcurrentHashMap actual array length: " + table.length);
        long size = (long)(1.0 + (long)11 / 0.75);
        System.out.println(size);

        System.out.println(concurrentHashMap.size());

        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>(16, 0.05f, 2);
        for (int i = 0; i < 30; i++) {
            map.put(String.valueOf(i), "val" + i);
        }
        map.put("a","");



        tableField = map.getClass().getDeclaredField("table");
        tableField.setAccessible(true);
        table = (Object[]) tableField.get(map);
        System.out.println("ConcurrentHashMap actual array length: " + table.length);
        for (int i = 0; i < table.length; i++) {
            if(table[i] instanceof Object) {
                Object o = table[i];
            }
        }

        Object ooo = table[0];
        map.put("a","");

    }
}
