package com.ruoyi.system.api;

import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.domain.SysDept;
import com.ruoyi.system.api.factory.RemoteDeptFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门服务
 *
 * @author ruoyi
 */
@FeignClient(contextId = "remoteDeptService", value = ServiceNameConstants.SYSTEM_SERVICE, fallbackFactory = RemoteDeptFallbackFactory.class)
public interface RemoteDeptService
{
    /**
     * 内部调用：查询部门列表
     *
     * @param source 请求来源
     * @return 部门列表
     */
    @GetMapping("/dept/inner/list")
    public R<List<SysDept>> innerList(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 内部调用：新增部门（钉钉组织同步用）
     *
     * @param sysDept 部门信息
     * @param source 请求来源
     * @return 部门 ID
     */
    @PostMapping("/dept/inner/add")
    public R<Long> innerAddDept(@RequestBody SysDept sysDept, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 内部调用：修改部门（钉钉组织同步用）
     *
     * @param sysDept 部门信息
     * @param source 请求来源
     * @return 结果
     */
    @PutMapping("/dept/inner/edit")
    public R<Boolean> innerEditDept(@RequestBody SysDept sysDept, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
