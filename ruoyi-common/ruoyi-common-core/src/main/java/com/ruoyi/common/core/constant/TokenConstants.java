package com.ruoyi.common.core.constant;

/**
 * Token的Key常量
 * 
 * @author ruoyi
 */
public class TokenConstants
{
    /**
     * 令牌自定义标识
     */
    public static final String AUTHENTICATION = "Authorization";

    /**
     * 令牌前缀
     */
    public static final String PREFIX = "Bearer ";

    /** JWT 签名密钥的运行时环境变量名。 */
    public static final String SECRET_ENV = "JWT_SECRET";

    /** HS512 密钥的最低安全长度（字符）。 */
    public static final int SECRET_MIN_LENGTH = 64;

}
