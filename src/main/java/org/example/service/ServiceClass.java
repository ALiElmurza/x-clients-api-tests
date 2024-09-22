package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.example.configuration.UserProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@Service
public class ServiceClass {
    @Value("${http.clientUrl}")
    private String clientUrl;
    private List<String> details;
    private AuthenticationService authenticationService;
    private DataSource dataSource;

    @Autowired
    public ServiceClass(AuthenticationService authenticationService, UserProperties userProperties, DataSource dataSource) {
        this.authenticationService = authenticationService;
        this.details = userProperties.getDetails();
        this.dataSource = dataSource;
    }

    public ServiceClass(ServiceClass original) {
        this.authenticationService = original.authenticationService;
        this.details = original.details;
        this.clientUrl = original.clientUrl;
        this.dataSource = original.dataSource;
    }

    public Integer createAndGetCompanyId() {
        RestAssured.baseURI = clientUrl;
        String requestBody = null;
        try {
            Path path = Paths.get(new ClassPathResource("companyRequest.json").getURI());
            requestBody = new String(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Response response = given()
                .header("x-client-token", getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/company")
                .then()
                .statusCode(201)
                .extract().response();

        return response.jsonPath().getInt("id");

    }

    public Map<String, Integer> addEmployee() {
        RestAssured.baseURI = clientUrl;
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode request = objectMapper.createObjectNode();
        String firstName = details.get(0);
        String lastName = details.get(1);
        String middleName = details.get(2);
        String email = details.get(3);
        String url = details.get(4);
        String phone = details.get(5);
        String birthdate = details.get(6);

        boolean isActive = Boolean.parseBoolean(details.get(7));
        Integer companyId = createAndGetCompanyId();

        request.put("firstName", firstName);
        request.put("lastName", lastName);
        request.put("middleName", middleName);
        request.put("companyId", companyId);
        request.put("email", email);
        request.put("url", url);
        request.put("phone", phone);
        request.put("birthdate", birthdate);
        request.put("isActive", isActive);

        Response response = given()
                .header("x-client-token", getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(request)
                .when()
                .post("/employee")
                .then()
                .statusCode(201)
                .extract().response();

        Integer employeeId = response.jsonPath().getInt("id");

        Map<String, Integer> map = new HashMap<>();
        map.put("companyId", companyId);
        map.put("employeeId", employeeId);
        return map;
    }

    public String getEmployeeByIdCompany(Integer idCompany) {
        RestAssured.baseURI = clientUrl;
        Response response = given()
                .accept(ContentType.JSON)
                .when()
                .get("/employee?company=" + idCompany)
                .then()
                .statusCode(200)
                .body("", notNullValue())
                .extract()
                .response();
        return response.getBody().asString();
    }

    public String getEmployeesByIdCompany(Integer idCompany) {
        RestAssured.baseURI = clientUrl;
        Response response = given()
                .accept(ContentType.JSON)
                .when()
                .get("/employee?company=" + idCompany)
                .then()
                .statusCode(200)
                .body("", notNullValue())
                .extract()
                .response();
        return response.getBody().asString();
    }

    public String getEmployeeById(Integer id) {
        RestAssured.baseURI = clientUrl;
        Response response = given()
                .accept(ContentType.JSON)
                .when()
                .get("/employee/" + id)
                .then()
                .statusCode(200)
                .extract()
                .response();
        return response.getBody().asString();
    }

    public String editEmployeeById(Integer idEmployee) {
        RestAssured.baseURI = clientUrl;
        String requestBody = null;
        try {
            Path path = Paths.get(new ClassPathResource("employeeEditRequest.json").getURI());
            requestBody = new String(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return given()
                .header("x-client-token", getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(requestBody)
                .when()
                .patch("/employee/" + idEmployee)
                .then()
                .extract().response().getBody().asString();
    }

    public String getToken() {
        try {
            return authenticationService.getAuthenticateToken();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

}
