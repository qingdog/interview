package day04.mypatch.early;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;

import javax.annotation.PostConstruct;

// set循环依赖被Spring处理了
public class App60 {
    static class A {
        private static final Logger log = LoggerFactory.getLogger("A");
        private B b;

        public A() {
            log.debug("A()...");
        }

        @Autowired
        public void setB(B b){
            log.debug("setB(){}");
            this.b = b;
        }
        @PostConstruct
        public void init(){
            log.debug("init()...");
        }

    }

    static class B{
        private static final Logger log = LoggerFactory.getLogger("B");
        private A a;

        public B(){
            log.debug("B()...");
        }

        @Autowired
        public void setA(A a){
            log.debug("setA(){}");
            this.a =a;
        }

        @PostConstruct
        public void init(){
            log.debug("init()...");
        }
    }

    public static void main(String[] args) {
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("a", A.class);
        context.registerBean("b", B.class);
        // 后处理处理注解（AutowiredAnnotationBeanPostProcessor 、CommonAnnotationBeanPostProcessor ）@Autowired @PostConstruct
        AnnotationConfigUtils.registerAnnotationConfigProcessors(context.getDefaultListableBeanFactory());

        context.refresh();
    }
}
