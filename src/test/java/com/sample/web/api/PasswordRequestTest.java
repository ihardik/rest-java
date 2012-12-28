package com.sample.web.api;

import com.sample.web.service.exception.ValidationException;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;

/**
 *
 * @version 1.0
 * @author: Iain Porter iain.porter@incept5.com
 * @since 30/09/2012
 */
public class PasswordRequestTest {

    @Test
    public void validPassword() {
        PasswordRequest request = new PasswordRequest("password");
        request.validate();
    }

    @Test(expected = ValidationException.class)
    public void passwordTooShort() {
        PasswordRequest request = new PasswordRequest(RandomStringUtils.random(7));
        request.validate();
    }

    @Test(expected = ValidationException.class)
    public void passwordTooLong() {
        PasswordRequest request = new PasswordRequest(RandomStringUtils.random(36));
        request.validate();
    }
}
