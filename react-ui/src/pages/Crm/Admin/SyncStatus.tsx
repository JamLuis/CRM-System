import React, { useEffect, useState } from 'react';
import { Button, Card, Descriptions, message, Modal, Space, Spin, Tag } from 'antd';
import { PageContainer } from '@ant-design/pro-components';
import { ReloadOutlined, SyncOutlined } from '@ant-design/icons';
import {
  getOrgSyncStatus,
  triggerFullSync,
  triggerIncrementalSync,
} from '@/services/crm/admin';

/** 钉钉组织同步状态 */
const SyncStatus: React.FC = () => {
  const [messageApi, contextHolder] = message.useMessage();
  const [status, setStatus] = useState<API.Crm.OrgSyncCursorInfo>();
  const [loading, setLoading] = useState(false);
  const [syncing, setSyncing] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      const resp = await getOrgSyncStatus();
      if (resp.code === 200) {
        setStatus(resp.data);
      } else {
        messageApi.error(resp.msg || '查询失败');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const runSync = (type: 'full' | 'incremental') => {
    const isFull = type === 'full';
    Modal.confirm({
      title: isFull ? '全量同步' : '增量同步',
      content: isFull
        ? '将从钉钉拉取全部部门与人员并对账，耗时较长，确认执行？'
        : '将基于游标拉取上次同步后的变更数据，确认执行？',
      onOk: async () => {
        setSyncing(true);
        try {
          const resp = isFull ? await triggerFullSync() : await triggerIncrementalSync();
          if (resp.code === 200 && resp.data) {
            const r = resp.data;
            messageApi.success(
              `同步完成：部门 ${r.deptCount ?? 0} 个，人员 ${r.userCount ?? 0} 人，更新 ${
                r.userUpdated ?? 0
              }，停用 ${r.userDeactivated ?? 0}`,
            );
            load();
          } else {
            messageApi.error(resp.msg || '同步失败');
          }
        } finally {
          setSyncing(false);
        }
      },
    });
  };

  return (
    <PageContainer>
      {contextHolder}
      <Card
        title="组织同步状态"
        extra={
          <Button icon={<ReloadOutlined />} onClick={load} loading={loading}>
            刷新
          </Button>
        }
      >
        <Spin spinning={loading}>
          <Descriptions column={2} bordered size="small">
            <Descriptions.Item label="同步来源">{status?.source || '-'}</Descriptions.Item>
            <Descriptions.Item label="状态">
              {status?.status ? (
                <Tag color="success">{status.status}</Tag>
              ) : (
                <Tag>未同步</Tag>
              )}
            </Descriptions.Item>
            <Descriptions.Item label="最近同步时间">
              {status?.lastSyncTime || '从未同步'}
            </Descriptions.Item>
            <Descriptions.Item label="同步游标">{status?.cursor || '-'}</Descriptions.Item>
          </Descriptions>
        </Spin>

        <div style={{ marginTop: 24 }}>
          <Space>
            <Button
              type="primary"
              icon={<SyncOutlined />}
              loading={syncing}
              onClick={() => runSync('incremental')}
            >
              增量同步
            </Button>
            <Button icon={<SyncOutlined />} loading={syncing} onClick={() => runSync('full')}>
              全量对账同步
            </Button>
          </Space>
        </div>
      </Card>
    </PageContainer>
  );
};

export default SyncStatus;
