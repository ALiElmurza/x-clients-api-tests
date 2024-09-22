package org.example.service;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class AuthenticationService {

    @Value("${http.clientUrl}")
    private String clientUrl;

    @Value("${http.loginEndpoint}")
    private String loginEndpoint;


    public String getAuthenticateToken() throws IOException {
        Path path = Paths.get(new ClassPathResource("loginRequest.json").getURI());
        String requestBody = new String(Files.readAllBytes(path));

        Response response = RestAssured.given()
                .baseUri(clientUrl)
                .contentType(io.restassured.http.ContentType.JSON)
                .accept(io.restassured.http.ContentType.JSON)
                .body(requestBody)
                .when()
                .post(loginEndpoint)
                .then()
                .statusCode(201)
                .extract().response();

        return response.jsonPath().getString("userToken");
    }
}