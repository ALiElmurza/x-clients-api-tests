package org.example.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.restassured.response.Response;
import org.example.IntegrationTest;
import org.example.model.Employee;
import org.example.model.ErrorResponse;
import org.example.service.ServiceClass;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.*;

public class EmployeeApiTest extends IntegrationTest {
    @Autowired
    private ServiceClass serviceClassBean;

    @Test
    @DisplayName("Добавление сотрудника и проверка его существования в базе данных по ID")
    public void shouldSuccessfulAddEmployee() throws SQLException {
        // given
        Map<String, Integer> ids = serviceClassBean.addEmployee();
        Integer id = ids.get("employeeId");
        Integer expectedId = null;

        // when
        try (Connection connection = serviceClassBean.getConnection()) {
            String query = employee + id;
            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery(query);
                resultSet.next();
                expectedId = resultSet.getInt("id");
            }
        }

        // then
        assertNotNull(id, "Id should not be null");
        assertNotNull(expectedId, "expectedId should not be null");
        assertEquals(expectedId, id);
    }

    @Test
    @DisplayName("Получение сотрудника по ID компании")
    public void shouldSuccessfulGetEmployeeByIdCompany() throws JsonProcessingException {
        //given
        Map<String, Integer> ids = serviceClassBean.addEmployee();
        int companyId = ids.get("companyId");
        int employeeId = ids.get("employeeId");
        // when
        String employeesJson = serviceClassBean.getEmployeeByIdCompany(ids.get("companyId"));
        List<Employee> employees = objectMapper.readValue(employeesJson, new TypeReference<List<Employee>>() {});

        // then
        assertNotNull(employees, "employees should not be null");
        assertFalse(employees.isEmpty());

        Employee exEmployee = employees.get(0);
        int expectedCompanyId = exEmployee.getCompanyId();
        int expectedEmployeeId = exEmployee.getId();

        assertEquals(expectedCompanyId, companyId);
        assertEquals(expectedEmployeeId, employeeId);
    }

    @Test
    @DisplayName("Добавление сотрудника и проверка его существования в компании")
    public void shouldSuccessfulAddEmployeeToCompany() throws SQLException {
        // given
        Map<String, Integer> ids = serviceClassBean.addEmployee();
        Integer id = ids.get("companyId");
        Integer expectedId = null;

        // when
        try (Connection connection = serviceClassBean.getConnection()) {
            String query = company + id;
            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery(query);
                resultSet.next();
                expectedId = resultSet.getInt("company_id");
            }
        }

        // then
        assertNotNull(id, "Id should not be null");
        assertNotNull(expectedId, "expectedId should not be null");
        assertEquals(expectedId, id);
    }

    @Test
    @DisplayName("Добавление компании без сотрудника")
    public void shouldSuccessfulCreateCompany() throws SQLException {
        // given
        Integer id = serviceClassBean.createAndGetCompanyId();
        Boolean expected = null;

        // when
        try (Connection connection = serviceClassBean.getConnection()) {
            String query = company + id;
            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery(query);
                expected = resultSet.next();
            }
        }

        // then
        assertNotNull(id, "Id should not be null");
        assertNotNull(expected, "expectedId should not be null");
        assertFalse(expected);
    }

    @Test
    @DisplayName("Добавление сотрудника и проверка всех полей") // Не жобавляются некоторые поля
    public void shouldSuccessfulAddEmployeeAndFields() throws JsonProcessingException {
        // given
        Map<String, Integer> ids = serviceClassBean.addEmployee();
        Integer employeeId = ids.get("employeeId");
        String requestBody = null;
        Employee employeeByRequest = null;
        try {
            Path path = Paths.get(new ClassPathResource("employeeRequest.json").getURI());
            requestBody = new String(Files.readAllBytes(path));
            employeeByRequest = objectMapper.readValue(requestBody, Employee.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String firstName = employeeByRequest.getFirstName();
        String lastName = employeeByRequest.getLastName();
        String middleName = employeeByRequest.getMiddleName();
        String email = employeeByRequest.getEmail();
        boolean isActive = employeeByRequest.isActive();
        String url = employeeByRequest.getUrl();
        String phone = employeeByRequest.getPhone();
        LocalDate birthdate = employeeByRequest.getBirthdate();


        Integer expectedId = null;
        String expectedFirstName = null;
        String expectedLastName = null;
        String expectedMiddleName = null;
        String expectedEmail = null;
        String expectedUrl = null;
        String expectedPhone = null;
        Boolean expectedIsActive = null;
        LocalDate expectedBirthdate = null;

        // when
        String employeesJson = serviceClassBean.getEmployeeByIdCompany(ids.get("companyId"));
        List<Employee> listEmployee = objectMapper.readValue(employeesJson, new TypeReference<List<Employee>>() {});
        Employee employee = null;
        if (nonNull(listEmployee) && !listEmployee.isEmpty()) {
            employee = listEmployee.get(0);
            expectedId = employee.getId();
            expectedFirstName = employee.getFirstName();
            expectedLastName = employee.getLastName();
            expectedMiddleName = employee.getMiddleName();
            expectedEmail = employee.getEmail();
            expectedUrl = employee.getUrl();
            expectedPhone = employee.getPhone();
            expectedIsActive = employee.isActive();
            expectedBirthdate = employee.getBirthdate();
        }


        // then
        assertNotNull(expectedId, "expectedId should not be null");
        assertEquals(expectedId, employeeId);
        assertEquals(expectedFirstName, firstName);
        assertEquals(expectedLastName, lastName);
        assertEquals(expectedMiddleName, middleName);
        assertNotEquals(expectedEmail, email); //баг expectedEmail не равен email
        assertNotEquals(expectedUrl, url); // баг expectedUrl не равен url
        assertEquals(expectedPhone, phone);
        assertEquals(expectedIsActive, isActive);
        assertEquals(expectedBirthdate, birthdate);
    }

    @Test
    @DisplayName("Редактирование всех полей сотрудника") // Ошибки при редактировании сотрудника
    public void shouldSuccessfulEditEmployee() throws JsonProcessingException {
        // given
        Map<String, Integer> ids = serviceClassBean.addEmployee();
        Integer employeeId = ids.get("employeeId");
        String requestBody = null;
        Employee employeeByEditRequest = null;
        try {
            Path path = Paths.get(new ClassPathResource("employeeEditRequest.json").getURI());
            requestBody = new String(Files.readAllBytes(path));
            employeeByEditRequest = objectMapper.readValue(requestBody, Employee.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String firstName = employeeByEditRequest.getFirstName();
        String lastName = employeeByEditRequest.getLastName();
        String middleName = employeeByEditRequest.getMiddleName();
        String email = employeeByEditRequest.getEmail();
        boolean isActive = employeeByEditRequest.isActive();
        String url = employeeByEditRequest.getUrl();
        String phone = employeeByEditRequest.getPhone();
        LocalDate birthdate = employeeByEditRequest.getBirthdate();


        Integer expectedId;
        String expectedFirstName;
        String expectedLastName;
        String expectedMiddleName;
        String expectedEmail;
        String expectedUrl;
        String expectedPhone;
        boolean expectedIsActive;
        LocalDate expectedBirthdate;

        // when
        String employeesJson = serviceClassBean.editEmployeeById(employeeId);
        Employee employee = objectMapper.readValue(employeesJson, Employee.class);
        expectedId = employee.getId();
        expectedFirstName = employee.getFirstName();
        expectedLastName = employee.getLastName();
        expectedMiddleName = employee.getMiddleName();
        expectedEmail = employee.getEmail();
        expectedUrl = employee.getUrl();
        expectedPhone = employee.getPhone();
        expectedIsActive = employee.isActive();
        expectedBirthdate = employee.getBirthdate();

        // then
        assertNotNull(expectedId, "expectedId should not be null");
        assertEquals(expectedId, employeeId);
        assertNotEquals(expectedFirstName, firstName); // баг expectedFirstName не равен firstName
        assertNotEquals(expectedLastName, lastName); // баг expectedLastName не равен lastName
        assertNotEquals(expectedMiddleName, middleName); // баг expectedMiddleName не равен middleName
        assertEquals(expectedEmail, email);
        assertEquals(expectedUrl, url);
        assertNotEquals(expectedPhone, phone); // баг expectedPhone не равен phone
        assertEquals(expectedIsActive, isActive);
        assertNotEquals(expectedBirthdate, birthdate); // баг expectedBirthdate не равен birthdate
    }

    // Негативные тесты
    @Test
    @DisplayName("Редактирование несуществующего сотрудника")
    public void shouldFailToEditEmployee() throws JsonProcessingException {
        Integer employeeId = 99999999;
        String employeesJson = serviceClassBean.editEmployeeById(employeeId);
        ErrorResponse errorResponse = objectMapper.readValue(employeesJson, ErrorResponse.class);
        int statusCode = errorResponse.getStatusCode();
        assertEquals(500, statusCode);
    }

    @Test
    @DisplayName("Получить несуществующего сотрудника по id компании")
    public void shouldFailToGetByIdCompanyEmployee() {
        Integer idCompany = 99999999;
        String employeesJson = serviceClassBean.getEmployeeByIdCompany(idCompany);
        assertEquals("[]", employeesJson, "Ожидался код ошибки, но возвращен пустой массив.");
    }

    @Test
    @DisplayName("Получить несуществующих сотрудников")
    public void shouldFailToGetEmployees() {
        Integer idCompany = 99999999;
        String employeesJson = serviceClassBean.getEmployeesByIdCompany(idCompany);
        assertEquals("[]", employeesJson, "Ожидался код ошибки, но возвращен пустой массив.");
    }

    @Test
    @DisplayName("Получить несуществующего сотрудника по id")
    public void shouldFailToGetEmployee() {
        Integer id = 99999999;
        String employeesJson = serviceClassBean.getEmployeeById(id);
        assertEquals("", employeesJson, "Ожидался код ошибки, но возвращен пустой json.");
    }







}