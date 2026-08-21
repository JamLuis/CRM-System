import React, { useEffect, useState } from 'react';
import { Button, message, Popconfirm, Table, Tag } from 'antd';
import { useAccess } from '@umijs/max';
import { cancelRemindersByCustomer, getRemindersByCustomer } from '@/services/crm/reminder';
import { REMINDER_STATUS_ENUM } from '../../constants';

export type RemindersTabProps = {
  customerId: API.Crm.Id;
};

/** 客户提醒计划 */
const RemindersTab: React.FC<RemindersTabProps> = ({ customerId }) => {
  const [messageApi, contextHolder] = message.useMessage();
  const access = useAccess();
  const canWrite = access.hasPerms('crm:reminder:write');

  const [plans, setPlans] = useState<API.Crm.ReminderPlan[]>([]);
  const [loading, setLoading] = useState(false);

  const loadData = async () => {
    setLoading(true);
    try {
      const resp = await getRemindersByCustomer(customerId);
      if (resp.code === 200) setPlans(resp.data || []);
      else messageApi.error(resp.msg || '查询失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [customerId]);

  const handleCancelAll = async () => {
    const resp = await cancelRemindersByCustomer(customerId);
    if (resp.code === 200) {
      messageApi.success('已取消活动计划');
      loadData();
    } else {
      messageApi.error(resp.msg || '操作失败');
    }
  };

  const hasActive = plans.some((p) => p.status === 'ACTIVE');

  const columns = [
    { title: '计划跟进时间', dataIndex: 'plannedFollowUpAt', width: 180 },
    { title: '投递时间', dataIndex: 'scheduledAt', width: 180 },
    { title: '来源跟踪', dataIndex: 'sourceFollowUpId', width: 120 },
    {
      title: '状态',
      dataIndex: 'status',
      render: (v: string) => {
        const meta = REMINDER_STATUS_ENUM[v];
        return meta ? <Tag color={meta.color}>{meta.text}</Tag> : v;
      },
    },
    { title: '创建时间', dataIndex: 'createTime' },
  ];

  return (
    <>
      {contextHolder}
      {canWrite && hasActive && (
        <Popconfirm title="确认取消该客户所有活动计划？" onConfirm={handleCancelAll}>
          <Button danger style={{ marginBottom: 16 }}>
            取消活动计划
          </Button>
        </Popconfirm>
      )}
      <Table
        rowKey="planId"
        size="small"
        loading={loading}
        dataSource={plans}
        columns={columns}
        pagination={{ defaultPageSize: 10 }}
      />
    </>
  );
};

export default RemindersTab;
