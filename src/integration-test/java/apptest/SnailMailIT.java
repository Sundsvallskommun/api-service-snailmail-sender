package apptest;

import static apptest.CommonStubs.stubForAccessToken;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.snailmail.Application;

@WireMockAppTestSuite(
        files = "classpath:/EmailSenderIT/",
        classes = Application.class
)
class EmailSenderIT extends AbstractAppTest {

    @BeforeEach
    void setUp() {
        stubForAccessToken();
    }

    //TODO, disabled tests since we need a samba container to run.
    @Test
    @Disabled
    void test1_successful() {
        setupCall()
                .withServicePath("/send/snailmail")
                .withHttpMethod(HttpMethod.POST)
                .withRequest("request.json")
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse();
    }

    @Test
    @Disabled
    void test2_emailSenderThrowsException() {
        setupCall()
                .withServicePath("/send/snailmail")
                .withHttpMethod(HttpMethod.POST)
                .withRequest("request.json")
                .withExpectedResponseStatus(HttpStatus.BAD_GATEWAY)
                .sendRequestAndVerifyResponse();
    }

}
