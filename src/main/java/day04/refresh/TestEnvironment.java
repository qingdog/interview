package day04.refresh;

import org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;

// 如何获得和解析 @Value 内容
public class TestEnvironment {
    public static void main(String[] args) throws NoSuchFieldException, IOException {
        // 1) 获得 @Value 的值
        System.out.println("=======================> 仅获取 @Value 值");
        // 限定符注解自动装配候选解析器
        QualifierAnnotationAutowireCandidateResolver resolver = new QualifierAnnotationAutowireCandidateResolver();
        //                      获取建议值             依赖描述符                      获取声明字段
        Object name = resolver.getSuggestedValue(new DependencyDescriptor(Bean1.class.getDeclaredField("name"), false));
        System.out.println(name);

        // 2) 解析 @Value 的值
        System.out.println("=======================> 获取 @Value 值, 并解析${}");
        Object javaHome = resolver.getSuggestedValue(new DependencyDescriptor(Bean1.class.getDeclaredField("javaHome"), false));
        System.out.println(javaHome);
        //                                  解析占位符
        System.out.println(getEnvironment().resolvePlaceholders(javaHome.toString()));

        // 3) 解析 SpEL 表达式（spring el）
        System.out.println("=======================> 获取 @Value 值, 并解析#{}");
        Object expression = resolver.getSuggestedValue(new DependencyDescriptor(Bean1.class.getDeclaredField("expression"), false));
        System.out.println(expression);
        String resolvePlaceholders = getEnvironment().resolvePlaceholders(expression.toString());
        System.out.println(resolvePlaceholders);
        //                    标准bean表达式解析器                                                  Bean 表达式上下文            默认可列出的 Bean 工厂
        Object evaluate = new StandardBeanExpressionResolver().evaluate(resolvePlaceholders, new BeanExpressionContext(new DefaultListableBeanFactory(), null));
        System.out.println(evaluate);
    }

    private static Environment getEnvironment() throws IOException {
        StandardEnvironment env = new StandardEnvironment();
        //  获取属性来源            添加最后一个   资源属性来源
        env.getPropertySources().addLast(new ResourcePropertySource("jdbc", new ClassPathResource("jdbc.properties")));
        return env;
    }

    static class Bean1 {
        @Value("hello")
        private String name;


//        @Value("${JAVA_HOME}")
//        @Value("${Path}") // 系统变量+用户变量
        @Value("${jdbc.username}")
//        @Value("class version:${java.class.version}")
        private String javaHome;

        @Value("#{'class version:' + '${java.class.version}'}")
        private String expression;
    }
}
