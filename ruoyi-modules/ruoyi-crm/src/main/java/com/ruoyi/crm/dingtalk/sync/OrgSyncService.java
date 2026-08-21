package com.ruoyi.crm.dingtalk.sync;

import com.ruoyi.crm.dingtalk.domain.DingTalkDeptUser;

import java.util.List;

/**
 * 组织架构同步服务接口
 * <p>
 * 支持事件增量同步 + 全量对账，映射 RuoYi 用户与部门。
 * 离职用户不能再被分配但历史显示完整。
 *
 * @author ruoyi-crm
 */
public interface OrgSyncService
{
    /**
     * 全量同步组织架构（部门 + 人员）
     * <p>
     * 从根部门开始递归拉取所有子部门和人员，与本地快照对账。
     * 适用于夜间全量对账或管理员手动触发。
     *
     * @param tenantId 租户 ID
     * @return 同步统计
     */
    SyncResult fullSync(String tenantId);

    /**
     * 增量同步（基于游标）
     * <p>
     * 从上次同步游标位置拉取变更数据。
     *
     * @param tenantId 租户 ID
     * @return 同步统计
     */
    SyncResult incrementalSync(String tenantId);

    /**
     * 刷新单个用户（管理员手动触发）
     *
     * @param tenantId 租户 ID
     * @param dingtalkUserId 钉钉用户 ID
     * @return 同步结果
     */
    boolean refreshSingleUser(String tenantId, String dingtalkUserId);

    /**
     * 查询同步状态
     *
     * @param tenantId 租户 ID
     * @return 游标信息
     */
    SyncCursorInfo getSyncStatus(String tenantId);

    /**
     * 同步统计结果
     */
    class SyncResult
    {
        public int deptCount;
        public int userCount;
        public int userUpdated;
        public int userDeactivated;
        public boolean success;
        public String error;

        public SyncResult(boolean success)
        {
            this.success = success;
        }
    }

    /**
     * 同步游标信息
     */
    class SyncCursorInfo
    {
        public String source;
        public String cursor;
        public java.util.Date lastSyncTime;
        public String status;
        public String errorSummary;
    }
}
