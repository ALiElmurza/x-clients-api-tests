package org.example.integration;

import org.example.IntegrationTest;
import org.example.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ConfigTest extends IntegrationTest {
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private DataSource dataSource;

    @Test
    public void testGetToken() throws IOException {
        String token = authenticationService.getAuthenticateToken();

        assertNotNull(token, "Token should not be null");
    }

    @Test
    public void testConnection() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "Connection should not be null");
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }


}
