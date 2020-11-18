package top.leejay.datasource;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.type.AnnotationMetadata;
import top.leejay.datasource.annotation.EnableDataSource;

import java.util.List;

public class DataSourceImportSelector implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        List<String> names = SpringFactoriesLoader.loadFactoryNames(EnableDataSource.class, Thread.currentThread().getContextClassLoader());
        return names.toArray(new String[0]);
    }
}
