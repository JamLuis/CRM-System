package com.ruoyi.crm.permission;

/**
 * 权限拒绝异常
 *
 * @author ruoyi-crm
 */
public class PermissionDeniedException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    /** 拒绝原因 */
    private final String reason;

    public PermissionDeniedException(String reason)
    {
        super(reason);
        this.reason = reason;
    }

    public PermissionDeniedException(String reason, Throwable cause)
    {
        super(reason, cause);
        this.reason = reason;
    }

    public String getReason()
    {
        return reason;
    }
}
