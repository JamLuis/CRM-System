package com.ruoyi.crm.customer.domain;

/**
 * 负责人变更类型枚举
 *
 * @author ruoyi-crm
 */
public enum OwnerChangeType
{
    /** 分配 */
    ASSIGN,
    /** 移交 */
    TRANSFER,
    /** 协同人新增 */
    COLLABORATOR_ADD,
    /** 协同人移除 */
    COLLABORATOR_REMOVE
}
