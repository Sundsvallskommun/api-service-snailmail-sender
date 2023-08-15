package apptest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.snailmail.Application;

import static apptest.CommonStubs.stubForAccessToken;

@WireMockAppTestSuite(
        files = "classpath:/EmailSenderIT/",
        classes = Application.class
)
class EmailSenderIT extends AbstractAppTest {

    @BeforeEach
    void setUp() {
        stubForAccessToken();
    }

    @Test
    void test1_successful() {
        setupCall()
                .withServicePath("/send/snailmail")
                .withHttpMethod(HttpMethod.POST)
                .withRequest("request.json")
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse();
    }


    @Test
    void test2_emailSenderThrowsException() {
        setupCall()
                .withServicePath("/send/snailmail")
                .withHttpMethod(HttpMethod.POST)
                .withRequest("request.json")
                .withExpectedResponseStatus(HttpStatus.BAD_GATEWAY)
                .sendRequestAndVerifyResponse();
    }

}
