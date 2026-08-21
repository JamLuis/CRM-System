package com.ruoyi.common.core.utils;

import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.TokenConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtUtilsTest
{
    @AfterEach
    void reset() throws Exception
    {
        System.clearProperty(TokenConstants.SECRET_ENV);
        setCachedSecret(null);
    }

    @Test
    void rejectsMissingOrShortSecret() throws Exception
    {
        System.setProperty(TokenConstants.SECRET_ENV, "too-short");
        setCachedSecret(null);
        assertThrows(IllegalStateException.class,
                () -> JwtUtils.createToken(new HashMap<>()));
    }

    @Test
    void signsAndParsesWithRuntimeSecret() throws Exception
    {
        System.setProperty(TokenConstants.SECRET_ENV,
                "test-only-runtime-secret-0123456789-ABCDEFGHIJKLMNOPQRSTUVWXYZ-abcdefghijk");
        setCachedSecret(null);
        Map<String, Object> claims = new HashMap<>();
        claims.put(SecurityConstants.DETAILS_USERNAME, "uat-user");

        String token = JwtUtils.createToken(claims);

        assertEquals("uat-user", JwtUtils.getUserName(token));
    }

    private void setCachedSecret(String value) throws Exception
    {
        Field field = JwtUtils.class.getDeclaredField("secret");
        field.setAccessible(true);
        field.set(null, value);
    }
}
