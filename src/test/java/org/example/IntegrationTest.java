package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.configuration.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;

@ExtendWith(SpringExtension.class)
@SpringBootTest
//@Import(TestConfig.class)
@TestPropertySource(locations = "classpath:application.yaml")
public abstract class IntegrationTest {
    @Value("${http.clientUrl}")
    protected String clientUrl;
    @Value("${query.employee}")
    protected String employee;
    @Value("${query.company}")
    protected String company;
    protected ObjectMapper objectMapper;

    @BeforeEach
    public void setup() throws IOException {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

}
