package com.tolclin.qa.api_automation.base;

import org.junit.jupiter.api.BeforeAll;

import static io.restassured.RestAssured.given;
import io.restassured.response.Response;

// @TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BaseTest {

    protected String token;
    protected String baseUrl = "https://api.tolclin.com/pool/api";

    @BeforeAll
    public void authenticate() {

        String requestBody =
                "{\n" +
                "  \"username\": \"joetolah\",\n" +
                "  \"password\": \"admin\"\n" +
                "}";

        Response response =
                given()
                        .baseUri(baseUrl)
                        .header("Content-Type", "application/json")
                        .body(requestBody)
                .when()
                        .post("/auth/login");

        token = response.jsonPath().getString("token");

        System.out.println("AUTH TOKEN: " + token);
    }
}