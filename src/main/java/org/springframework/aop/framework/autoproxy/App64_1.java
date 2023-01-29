package org.springframework.aop.framework.autoproxy;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.context.support.GenericApplicationContext;

public class App64_1 {
    public static void main(String[] args) {
        GenericApplicationContext genericApplicationContext = new GenericApplicationContext();
        genericApplicationContext.registerBean("aspect1", Ascpect1.class);
        genericApplicationContext.registerBean(AnnotationAwareAspectJAutoProxyCreator.class);

        genericApplicationContext.registerBean("target1", Target1.class);
        genericApplicationContext.registerBean("target2", Target2.class);

        genericApplicationContext.refresh();

        Target1 target1 = genericApplicationContext.getBean(Target1.class);
        target1.foo();
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>");

        Target2 target2 = genericApplicationContext.getBean(Target2.class);
        target2.bar();

        AnnotationAwareAspectJAutoProxyCreator creator = genericApplicationContext.getBean(AnnotationAwareAspectJAutoProxyCreator.class);
        // wrapIfNecessary为保护方法，改为同包下调用 package org.springframework.aop.framework.autoproxy;

        Object o = creator.wrapIfNecessary(new Ascpect1(), "ascpect1", "ascpect1");
        // wrapIfNecessary会检查是否需要创建代理对象，如果没有切点匹配则不创建代理（类型为springaop的$内部类，而不是CGLIB代理对象）
        // class org.springframework.aop.framework.autoproxy.App64_1$Ascpect1
        // isInfrastructureClass()是否是基础设施类型，排除切点切面通知类
        // getAdvicesAndAdvisorsForBean放回切面
        // createProxy创建代理使用ProxyFactory
        System.out.println(o.getClass());
    }

    static class Target1{
        public void foo(){
            System.out.println("target1 foo");
        }
    }
    static class Target2{
        public void bar(){
            System.out.println("target1 foo");
        }
    }

    @Aspect
    static class Ascpect1{
        @Around("execution(* foo())") // 一个advisor切面
//      @Around() 和 @After() @Before都是实现了MethodInterceptor
        public Object arround(ProceedingJoinPoint pjp) throws Throwable{
            System.out.println("aspect1 around");
            return pjp.proceed();
        }
    }
}
