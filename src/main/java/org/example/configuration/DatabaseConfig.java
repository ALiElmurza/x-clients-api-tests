package org.example.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class DatabaseConfig {
    @Value("${jdbc.datasource.url}")
    private String connectionString;

    @Value("${jdbc.datasource.username}")
    private String username;

    @Value("${jdbc.datasource.password}")
    private String password;

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(connectionString);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }
}
