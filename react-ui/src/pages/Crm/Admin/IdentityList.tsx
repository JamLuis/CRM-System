import React, { useRef, useState } from 'react';
import { Button, message, Tag } from 'antd';
import { ActionType, PageContainer, ProColumns, ProTable } from '@ant-design/pro-components';
import { UserAddOutlined } from '@ant-design/icons';
import { listDingtalkIdentities } from '@/services/crm/admin';
import IdentityMapModal from './components/IdentityMapModal';

/** 钉钉身份映射列表（系统访问授权） */
const IdentityList: React.FC = () => {
  const [messageApi, contextHolder] = message.useMessage();
  const actionRef = useRef<ActionType>();
  const [mapVisible, setMapVisible] = useState(false);
  const [editRow, setEditRow] = useState<API.Crm.CrmDingtalkIdentity>();

  const columns: ProColumns<API.Crm.CrmDingtalkIdentity>[] = [
    { title: '钉钉用户 ID', dataIndex: 'dingtalkUserId', width: 180 },
    { title: '系统用户 ID', dataIndex: 'sysUserId', width: 140 },
    { title: 'UnionID', dataIndex: 'unionId', width: 200, render: (_, r) => r.unionId || '-' },
    {
      title: '状态',
      width: 100,
      render: () => <Tag color="success">已授权</Tag>,
    },
    { title: '创建时间', dataIndex: 'createTime', width: 170 },
    {
      title: '操作',
      valueType: 'option',
      width: 120,
      render: (_, record) => [
        <a
          key="edit"
          onClick={() => {
            setEditRow(record);
            setMapVisible(true);
          }}
        >
          修改映射
        </a>,
      ],
    },
  ];

  return (
    <PageContainer>
      {contextHolder}
      <ProTable<API.Crm.CrmDingtalkIdentity>
        headerTitle="钉钉身份映射"
        actionRef={actionRef}
        rowKey="id"
        columns={columns}
        search={false}
        toolBarRender={() => [
          <Button
            key="add"
            type="primary"
            icon={<UserAddOutlined />}
            onClick={() => {
              setEditRow(undefined);
              setMapVisible(true);
            }}
          >
            新增授权
          </Button>,
        ]}
        request={async () => {
          const resp = await listDingtalkIdentities();
          if (resp.code === 200) {
            return { data: resp.data || [], success: true, total: resp.data?.length || 0 };
          }
          messageApi.error(resp.msg || '查询失败');
          return { data: [], success: false, total: 0 };
        }}
      />
      <IdentityMapModal
        visible={mapVisible}
        initialDingtalkUserId={editRow?.dingtalkUserId}
        initialSysUserId={editRow?.sysUserId}
        onClose={() => setMapVisible(false)}
        onSuccess={() => {
          setMapVisible(false);
          actionRef.current?.reload();
        }}
      />
    </PageContainer>
  );
};

export default IdentityList;
