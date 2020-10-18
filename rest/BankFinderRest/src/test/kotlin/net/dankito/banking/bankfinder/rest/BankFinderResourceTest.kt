package net.dankito.banking.bankfinder.rest

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.Test


@QuarkusTest
class BankFinderResourceTest {

    @Test
    fun testSparkasse() {
        given()
            .`when`().get("/bankfinder/Sparkasse")
            .then()
                .statusCode(200)
                .body(containsString("Berliner Sparkasse"))
                .body(containsString("\"bankCode\":\"10050000\""))
    }

}