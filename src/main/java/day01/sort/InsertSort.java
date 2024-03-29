package day01.sort;

import java.util.Arrays;

public class InsertSort {
    public static void main(String[] args) {
        int[] a = {7, 5, 19, 8, 4, 1};
//        insert(a);
        insert2(a);
    }

    // 修改了代码与希尔排序一致
    public static void insert(int[] a) {
        // i 代表待插入元素的索引
        for (int i = 1; i < a.length; i++) {
            int t = a[i]; // 代表待插入的元素值
            int j = i;
            System.out.println(j);
            while (j >= 1) {
                if (t < a[j - 1]) { // j-1 是上一个元素索引，如果 > t，后移
                    a[j] = a[j - 1];
                    j--;
                } else { // 如果 j-1 已经 <= t, 则 j 就是插入位置
                    break;
                }
            }
            a[j] = t;
            System.out.println(Arrays.toString(a) + " " + j);
        }
    }

    public static void insert2(int[] a){
        String payRecord = "";
//        String s = "{" +
//                " \"out_trade_no\":\""+payRecord+"\"," +
//                " \"total_amount\":\""+payRecord"\"," +
//                " \"subject\":\""+payRecord+"\"," +
//                " \"product_code\":\"QUICK_WAP_PAY\"" +
//                " }";
//        alipayRequest.setBizContent();//填充业务参数

        String bizContent="{\"out_trade_no\":\""+ payRecord +"\","
                + "\"total_amount\":\""+ payRecord +"\","
                + "\"subject\":\""+ payRecord +"\","
                + "\"body\":\""+ payRecord +"\","
                + "\"goods_type\":\""+ payRecord +"\","
                + "\"merchant_order_no\":\""+ payRecord +"\","
                //     + "\"goods_detail\":[{\"goods_id\":\""+ id +"\",\"goods_name\":\""+ kind +"\"}],"
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}";
        new String("{" +
                " \"out_trade_no\":\""+payRecord+"\"," +
                " \"total_amount\":\""+payRecord+"\"," +
                " \"subject\":\""+payRecord+"\"," +
                " \"product_code\":\"QUICK_WAP_PAY\"" +
                " }");//填充业务参数\

        new String("{" +
                " \"out_trade_no\":\""+payRecord+"\"," +
                " \"total_amount\":\""+payRecord+"\"," +
                " \"subject\":\""+payRecord+"\"," +
                " \"product_code\":\"QUICK_WAP_PAY\"" +
                " }");//填充业务参数\

//        alipayRequest.setBizContent(bizContent);


        for (int i = 1; i< a.length; i++) {
            int in = a[i];
            int j = i;
            for (; j > 0; j--) {
                if (in < a[j-1]) {
                    // 交换（插入）
                    a[j] = a[j-1];
                } else {
                    // 稳定
                    break;
                }
            }
            a[j] = in;
            System.out.println(Arrays.toString(a) + " " + j);
        }
    }
}
