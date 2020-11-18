package top.leejay.datasource;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;
import javax.sql.DataSource;

@Configuration
public abstract class AbstractDataSourceConfiguration {

    @Resource
    protected Environment environment;

    protected abstract DataSource dataSource();
}
