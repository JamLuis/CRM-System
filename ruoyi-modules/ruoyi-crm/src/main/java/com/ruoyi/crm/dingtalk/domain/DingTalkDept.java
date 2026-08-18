package com.ruoyi.crm.dingtalk.domain;

import java.io.Serializable;
import java.util.List;

/**
 * 钉钉部门信息（组织同步用）
 *
 * @author ruoyi-crm
 */
public class DingTalkDept implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 部门 ID */
    private Long deptId;
    /** 父部门 ID */
    private Long parentId;
    /** 部门名称 */
    private String name;
    /** 部门排序 */
    private Long order;
    /** 创建时间戳 */
    private Long createTime;
    /** 是否包含子部门 */
    private Boolean containSub;

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    public Long getParentId()
    {
        return parentId;
    }

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Long getOrder()
    {
        return order;
    }

    public void setOrder(Long order)
    {
        this.order = order;
    }

    public Long getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Long createTime)
    {
        this.createTime = createTime;
    }

    public Boolean getContainSub()
    {
        return containSub;
    }

    public void setContainSub(Boolean containSub)
    {
        this.containSub = containSub;
    }
}
