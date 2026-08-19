import React, { useRef } from 'react';
import { Button, message, Popconfirm, Tag, Typography } from 'antd';
import { ActionType, PageContainer, ProColumns, ProTable } from '@ant-design/pro-components';
import { ReloadOutlined } from '@ant-design/icons';
import { getDeadLetters, replayDeadLetter } from '@/services/crm/admin';

/** Outbox 死信查询与重放 */
const DeadLetter: React.FC = () => {
  const [messageApi, contextHolder] = message.useMessage();
  const actionRef = useRef<ActionType>();

  const handleReplay = async (id?: number) => {
    if (!id) return;
    const resp = await replayDeadLetter(id);
    if (resp.code === 200) {
      messageApi.success('已重置为待发送，将由后台任务重新投递');
      actionRef.current?.reload();
    } else {
      messageApi.error(resp.msg || '重放失败');
    }
  };

  const columns: ProColumns<API.Crm.OutboxDeadLetter>[] = [
    { title: 'ID', dataIndex: 'id', width: 100 },
    { title: '主题', dataIndex: 'topic', width: 180 },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (_, record) => <Tag color="error">{record.status || 'DEAD'}</Tag>,
    },
    { title: '重试次数', dataIndex: 'retryCount', width: 100 },
    { title: '创建时间', dataIndex: 'createTime', width: 170 },
    {
      title: '消息内容',
      dataIndex: 'payload',
      ellipsis: true,
      render: (_, record) => (
        <Typography.Paragraph
          style={{ marginBottom: 0 }}
          ellipsis={{ rows: 1, expandable: true, symbol: '展开' }}
        >
          {record.payload}
        </Typography.Paragraph>
      ),
    },
    {
      title: '操作',
      valueType: 'option',
      width: 100,
      render: (_, record) => [
        <Popconfirm
          key="replay"
          title="确认重放该死信？"
          onConfirm={() => handleReplay(record.id)}
        >
          <a>重放</a>
        </Popconfirm>,
      ],
    },
  ];

  return (
    <PageContainer>
      {contextHolder}
      <ProTable<API.Crm.OutboxDeadLetter>
        headerTitle="Outbox 死信"
        actionRef={actionRef}
        rowKey="id"
        columns={columns}
        search={false}
        toolBarRender={() => [
          <Button
            key="reload"
            icon={<ReloadOutlined />}
            onClick={() => actionRef.current?.reload()}
          >
            刷新
          </Button>,
        ]}
        request={async () => {
          const resp = await getDeadLetters();
          if (resp.code === 200) {
            return { data: resp.data || [], success: true, total: resp.data?.length || 0 };
          }
          messageApi.error(resp.msg || '查询失败');
          return { data: [], success: false, total: 0 };
        }}
      />
    </PageContainer>
  );
};

export default DeadLetter;
