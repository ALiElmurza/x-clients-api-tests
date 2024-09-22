package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.example.service.AuthenticationService;
import org.example.service.ServiceClass;
import org.springframework.beans.factory.annotation.Value;

import static io.restassured.RestAssured.given;

public class Service {
    private AuthenticationService authenticationService;
    private String token;
    private Integer companyId;
    @Value("${user.details}")
    private String[] userDetails;

    public Service(AuthenticationService authenticationService, ServiceClass serviceClass) {
        this.authenticationService = authenticationService;
        try {
            token = authenticationService.getAuthenticateToken();
            companyId = serviceClass.createAndGetCompanyId();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize Service", e);
        }
    }

    public Integer addEmployee() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode request = objectMapper.createObjectNode();
        String firstName = userDetails[0];
        String lastName = userDetails[1];
        String middleName = userDetails[2];
        String email = userDetails[3];
        String url = userDetails[4];
        String phone = userDetails[5];
        String birthdate = userDetails[6];

        boolean isActive = Boolean.parseBoolean(userDetails[7]);

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
                .header("x-client-token", token)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(request)
                .when()
                .post("/employee")
                .then()
                .statusCode(201)
                .extract().response();

        return response.jsonPath().getInt("id");
    }
}
