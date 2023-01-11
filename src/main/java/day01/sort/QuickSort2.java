package day01.sort;

import java.util.Arrays;

import static day01.sort.Utils.swap;

// 双边循环法
@SuppressWarnings("all")
public class QuickSort2 {
    public static void main(String[] args) {
        int[] a = {5, 3, 7, 2, 9, 8, 1, 4};
        System.out.println(Arrays.toString(a));
        quick(a, 0, a.length - 1);
//                [5, 3, 7, 2, 9, 8, 1, 4]
//                [1, 3, 4, 2, 5, 8, 9, 7] j=4
//                [1, 3, 4, 2, 5, 8, 9, 7] j=0
//                [1, 2, 3, 4, 5, 8, 9, 7] j=2
//                [1, 2, 3, 4, 5, 7, 8, 9] j=6
//        int p = partition(a, 0, a.length - 1);
    }

    private static void quick(int[] a, int l, int h) {
        if (l >= h) {
            return;
        }
        int p = partition(a, l, h);
        quick(a, l, p - 1);
        quick(a, p + 1, h);
    }

    private static int partition(int[] a, int l, int h) {
        // pivot value
        int pv = a[l];
        int i = l;
        int j = h;
        // i != j
        while (i < j) {
            // j 从右找小的
            while (i < j && a[j] > pv) {
                j--;
            }
            // i 从左找大的
            while (i < j && a[i] <= pv) {
                i++;
            }
            swap(a, i, j);
        }
        swap(a, l, j);
        System.out.println(Arrays.toString(a) + " j=" + j);
        return j;
    }

    public static int partition2(int a[],int l, int h){
        int i = l, j = h;
        while (i != j) {
            while (i < j && a[j] > a[h])
                j--;
            while (i < j && a[i] <= a[h])
                i++;
            swap(a, i, j);
        }
        swap(a, h, j);
        return j;
    };

    public static void quick2(int a[],int l, int h) {
        if (l >= h) {
            return;
        }
        int partition2 = partition2(a, l, h);
        quick2(a,partition2+1,h);
        quick2(a, l ,partition2-1);
    }
}
