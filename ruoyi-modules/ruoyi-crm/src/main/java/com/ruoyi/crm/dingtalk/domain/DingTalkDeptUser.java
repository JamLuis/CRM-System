package com.ruoyi.crm.dingtalk.domain;

import java.io.Serializable;
import java.util.List;

/**
 * 钉钉部门用户（组织同步用）
 *
 * @author ruoyi-crm
 */
public class DingTalkDeptUser implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 钉钉用户 ID */
    private String userid;
    /** 姓名 */
    private String name;
    /** 手机号 */
    private String mobile;
    /** 职位 */
    private String title;
    /** 部门 ID 列表 */
    private List<Long> deptIdList;
    /** 是否在职 */
    private Boolean active;
    /** 钉钉 UnionID */
    private String unionid;
    /** 邮箱 */
    private String email;

    public String getUserid()
    {
        return userid;
    }

    public void setUserid(String userid)
    {
        this.userid = userid;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getMobile()
    {
        return mobile;
    }

    public void setMobile(String mobile)
    {
        this.mobile = mobile;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public List<Long> getDeptIdList()
    {
        return deptIdList;
    }

    public void setDeptIdList(List<Long> deptIdList)
    {
        this.deptIdList = deptIdList;
    }

    public Boolean getActive()
    {
        return active;
    }

    public void setActive(Boolean active)
    {
        this.active = active;
    }

    public String getUnionid()
    {
        return unionid;
    }

    public void setUnionid(String unionid)
    {
        this.unionid = unionid;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }
}
