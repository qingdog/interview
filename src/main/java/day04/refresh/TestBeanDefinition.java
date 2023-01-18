package day04.refresh;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.core.io.ClassPathResource;

import java.util.Arrays;

// 演示各种 BeanDefinition 的来源
public class TestBeanDefinition {
    public static void main(String[] args) {
        System.out.println("========================> 一开始");
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        System.out.println(Arrays.toString(beanFactory.getBeanDefinitionNames()));

        System.out.println("========================> 1) 从 xml 获取 ");
        // Xml Bean 定义读取器
        XmlBeanDefinitionReader reader1 = new XmlBeanDefinitionReader(beanFactory);
        //      加载 Bean 定义            类路径资源
        reader1.loadBeanDefinitions(new ClassPathResource("bd.xml"));
        System.out.println(Arrays.toString(beanFactory.getBeanDefinitionNames()));

        System.out.println("========================> 2) 从配置类获取 ");
        //          注册 Bean 定义                                Bean 定义生成器        通用 Bean 定义                         获取 Bean 定义
        beanFactory.registerBeanDefinition("config1", BeanDefinitionBuilder.genericBeanDefinition(Config1.class).getBeanDefinition());

        // 配置类后处理器
        ConfigurationClassPostProcessor postProcessor = new ConfigurationClassPostProcessor();
        //            后处理 Bean 定义注册表（bean2）
        postProcessor.postProcessBeanDefinitionRegistry(beanFactory);
        System.out.println(Arrays.toString(beanFactory.getBeanDefinitionNames()));

        System.out.println("========================> 3) 扫描获取 ");
        // 类路径 Bean 定义扫描器
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(beanFactory);
        scanner.scan("day04.refresh.sub");
        System.out.println(Arrays.toString(beanFactory.getBeanDefinitionNames()));
    }

    static class Bean1 {

    }

    static class Bean2 {

    }

    static class Config1 {
        @Bean
        public Bean2 bean2() {
            return new Bean2();
        }
    }
}
