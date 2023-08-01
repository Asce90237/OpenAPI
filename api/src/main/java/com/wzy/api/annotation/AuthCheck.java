package com.wzy.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验
 *@Target 和 @Retention 都是 Java 中的元注解，用于给注解类型添加注解属性。它们可以用于自定义注解的定义中，用来描述注解的作用范围和生命周期。
 *
 * @Target(ElementType.METHOD) 表示该注解只能用于方法上。这表示，使用该注解的时候，只能将其作用于方法上，而不能作用于类、字段或其他元素上。
 * 这有助于限制注解的作用范围，使得注解更加精确。
 *
 * @Retention(RetentionPolicy.RUNTIME) 表示该注解在运行时仍然可用。Java 中的注解有三种生命周期：源代码级别、编译时期和运行时期。
 * 其中，@Retention 用于指定注解的生命周期，RetentionPolicy.RUNTIME 表示该注解在运行时仍然可用，可以通过反射 API 来获取它的信息。
 * 这意味着，通过该注解标注的代码可以在程序运行时获取并进行处理。
 *
 * 因此，@Target 和 @Retention 的作用分别是限制注解的作用范围和指定注解的生命周期，这两个注解都是自定义注解时必备的元注解。
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {

    /**
     * 有任何一个角色
     *
     * @return
     */
    String[] anyRole() default "";

    /**
     * 必须有某个角色
     *
     * @return
     */
    String mustRole() default "";

}

