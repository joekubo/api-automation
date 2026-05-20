package com.tolclin.qa.api_automation.services;

import org.junit.jupiter.api.Test;

import com.tolclin.qa.api_automation.base.BaseTest;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class GetUsersTest extends BaseTest {

    @Test
    public void testGetAllUsers() {

        given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")

        .when()
                .get("/users/all-users/1")

        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data", notNullValue());

        System.out.println("GET USERS TEST PASSED");
    }
}