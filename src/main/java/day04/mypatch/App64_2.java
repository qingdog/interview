package day04.mypatch;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.openjdk.jol.vm.VM;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;

public class App64_2 {
    public static void main(String[] args) {
        // aspect = 通知（advice）+切点（pointcut），一个切面类中可能有一个到多个通知方法
        // advisor = 更细粒度的切面，包含一个通知和切点

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTarget(new Target1()); // 设置目标对象

        /*proxyFactory.addAdvice(new MethodInterceptor() { // 类似环绕通知
            @Override
            public Object invoke(MethodInvocation methodInvocation) throws Throwable {
                try {
                    System.out.println("before...");
                    return methodInvocation.proceed();
                }finally {
                    System.out.println("after...");
                }

            }
        });*/
        // 不要每个方法都做功能增强，增加切点实现类
        AspectJExpressionPointcut aspectJExpressionPointcut = new AspectJExpressionPointcut();
        aspectJExpressionPointcut.setExpression("execution(* foo())");

        MethodInterceptor methodInterceptor = new MethodInterceptor() { // 类似环绕通知
            @Override
            public Object invoke(MethodInvocation methodInvocation) throws Throwable {
                try {
                    System.out.println("before...");
                    return methodInvocation.proceed();
                } finally {
                    System.out.println("after...");
                }

            }
        };
        proxyFactory.addAdvisor(new DefaultPointcutAdvisor(aspectJExpressionPointcut, methodInterceptor));


        Target1 target1 = (Target1)proxyFactory.getProxy();
        // 生成子类作为代理（CGLIB），org.springframework.aop.framework.autoproxy.App64_2$Target$$EnhancerBySpringCGLIB$$0674e78
        System.out.println(target1.getClass());
        target1.bar();
        target1.foo();

//        proxyFactory.addInterface(I1.class);
//        I1 I1 = (I1)proxyFactory.getProxy();
//        org.springframework.aop.framework.autoproxy.$Proxy0（jdk代理）
        // 统一用CGLIB生成代理对象
//        proxyFactory.setExposeProxy(true);
    }

    interface I1{
        void foo();
        void bar();
    }

    static class Target1 implements I1{
        public void foo(){
            System.out.println("target1 foo");
        }

        public void bar() {
            System.out.println("target1 bar");
        }
    }
}
