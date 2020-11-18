package top.leejay.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.context.annotation.Bean;
import top.leejay.datasource.annotation.ConditionalOnDriver;

import javax.sql.DataSource;


public class MysqlConfiguration extends AbstractDataSourceConfiguration {

    @Bean
    @ConditionalOnDriver
    public DataSource dataSource() {
        DruidDataSource source = new DruidDataSource();
        source.setUsername(environment.getProperty("spring.datasource.username"));
        source.setPassword(environment.getProperty("spring.datasource.password"));
        source.setUrl(environment.getProperty("spring.datasource.url"));
        source.setDriverClassName(DriverType.MYSQL.getDriver());
        return source;
    }
}
