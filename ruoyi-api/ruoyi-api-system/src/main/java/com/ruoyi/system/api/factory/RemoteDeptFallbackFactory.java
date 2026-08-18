package com.ruoyi.system.api.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteDeptService;
import com.ruoyi.system.api.domain.SysDept;

import java.util.Collections;
import java.util.List;

/**
 * 部门服务降级处理
 *
 * @author ruoyi
 */
@Component
public class RemoteDeptFallbackFactory implements FallbackFactory<RemoteDeptService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteDeptFallbackFactory.class);

    @Override
    public RemoteDeptService create(Throwable throwable)
    {
        log.error("部门服务调用失败:{}", throwable.getMessage());
        return new RemoteDeptService()
        {
            @Override
            public R<List<SysDept>> innerList(String source)
            {
                return R.fail("内部查询部门列表失败:" + throwable.getMessage());
            }

            @Override
            public R<Long> innerAddDept(SysDept sysDept, String source)
            {
                return R.fail("内部新增部门失败:" + throwable.getMessage());
            }

            @Override
            public R<Boolean> innerEditDept(SysDept sysDept, String source)
            {
                return R.fail("内部修改部门失败:" + throwable.getMessage());
            }
        };
    }
}
