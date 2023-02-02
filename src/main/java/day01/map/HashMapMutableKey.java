package day01.map;

import java.util.HashMap;
import java.util.Objects;

public class HashMapMutableKey {
    public static void main(String[] args) {
        HashMap<Student, Object> map = new HashMap<>();
        Student stu = new Student("张三", 18);
        map.put(stu, new Object());

        System.out.println(map.get(stu));

        stu.age = 19;

        Object obj = stu;
//        stu = null;
        System.out.println(map.get(stu));

        System.out.println(map.get(obj));
        System.out.println("Identity Hashcode = " + System.identityHashCode(obj));

        // 重写hashcode不是必须的，之所以要重写该方法是因为重写了equals方法需要
        // 在对象地址不相同的情况下继续比较两个对象的值是否相等
        Student stu2 = new Student("张三", 18);
        System.out.println(stu.equals(stu2));
    }

    static class Student {
        String name;
        int age;

        public Student(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Student student = (Student) o;
            return age == student.age && Objects.equals(name, student.name);
        }

        // 若没有重写hashcode的方法，对象的值的变化不会导致hashcode改变（可以使用对象作为Map的key）
        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }
    }
}
