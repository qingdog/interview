package day01.sort;

import java.util.Arrays;

import static day01.sort.Utils.swap;

// 单边循环法 (lomuto)
public class QuickSort1 {
    public static void main(String[] args) {
//        int[] a = {5, 3, 7, 2, 9, 8, 1, 4};
        int[] a = {5, 3, 7, 2, 9, 8, 1, 4, 21,22,35,11,6};
        System.out.println(Arrays.toString(a));
        quick(a, 0, a.length - 1);
    }

    public static void quick(int[] a, int l, int h) {
        // 双指针
        if (l >= h) {
            return;
        }

        int p = partition2(a, l, h); // p 索引值
        quick(a, l, p - 1); // 左边分区的范围确定
        quick(a, p + 1, h); // 右边分区的范围确定
    }

    private static int partition(int[] a, int l, int h) {
        int pv = a[h]; // 基准点元素
        int i = l;
        for (int j = l; j < h; j++) {
            if (a[j] < pv) {
                if (i != j) {
                    swap(a, i, j);
                }
                i++;
            }
        }
        if (i != h) {
            swap(a, h, i);
        }
        System.out.println(Arrays.toString(a) + " i=" + i);
        // 返回值代表了基准点元素所在的正确索引，用它确定下一轮分区的边界
        return i;
    }

    private static int partition2(int[] a, int l, int h) {
        // i 定位，j 循环比较
        int i  = l, j = l;
        for (; j < a.length - 1; j++) {
            if (a[j] < a[h]) {
                if (i != j) {
                    swap(a, i ,j);
                }
                i++;
            }
        }
        if (i != h) {
            // 分区的最后一步，最大索引的值和 i（左分区最大索引右边的值）交换
            //[3, 2, 1, 4, 9, 8, 7, 5] i=3
            //[3, 2, 1, 5, 9, 8, 7, 4] i=3
            swap(a, h, i);
        }
        System.out.println(Arrays.toString(a) + " i=" + i);

        return i;
    }
}
