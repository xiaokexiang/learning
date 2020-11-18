package top.leejay.datasource.annotation;


import org.springframework.context.annotation.Import;
import top.leejay.datasource.DataSourceImportSelector;

import java.lang.annotation.*;

/**
 * 数据源自动注入
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(DataSourceImportSelector.class)
public @interface EnableDataSource {
}
