package top.leejay.datasource.annotation;

import org.springframework.context.annotation.Import;
import top.leejay.datasource.ConditionalOnDriverName;
import top.leejay.datasource.DriverType;

import java.lang.annotation.*;

/**
 * 根据classpath下的jar包决定加载的数据源
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Import(ConditionalOnDriverName.class)
public @interface ConditionalOnDriver {
    DriverType value() default DriverType.MYSQL;
}
