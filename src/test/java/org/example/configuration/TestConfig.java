package org.example.configuration;

import org.example.service.AuthenticationService;
import org.example.service.ServiceClass;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;

@TestConfiguration
@Import({
        DatabaseConfig.class,
        AuthenticationService.class,
        ServiceClass.class
})
@Profile("test")
public class TestConfig {
    @Bean
    @Primary
    @Scope("prototype")
    public ServiceClass serviceClassBean(ServiceClass serviceClass) {
        ServiceClass clonedServiceClass = new ServiceClass(serviceClass);
//        BeanUtils.copyProperties(serviceClass, clonedServiceClass);
        return clonedServiceClass;
    }
}
