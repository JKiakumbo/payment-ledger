package me.jkiakumbo.payment_ledger.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class PostgresTestConfig {
    @Bean
    @ServiceConnection
    PostgreSQLContainer postgreSQLContainer() {
       return new PostgreSQLContainer("postgres:17-alpine")
               .withDatabaseName("ledger_db")
               .withUsername("ledger_user")
               .withPassword("ledger_password");
   }
}
