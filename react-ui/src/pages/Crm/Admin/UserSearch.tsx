import React, { useEffect, useRef, useState } from 'react';
import { Button, Modal, Space, Tag, Tooltip, message } from 'antd';
import { ActionType, PageContainer, ProColumns, ProTable } from '@ant-design/pro-components';
import { SyncOutlined } from '@ant-design/icons';
import {
  listCrmAccessRoles,
  listDingTalkDirectoryUsers,
  revokeCrmAccess,
  triggerFullSync,
} from '@/services/crm/admin';
import AccessGrantModal from './components/AccessGrantModal';

const splitValues = (value?: string) => (value || '').split(',').filter(Boolean);

/** 企业通讯录、身份映射和角色权限的一站式 CRM 访问授权页。 */
const UserSearch: React.FC = () => {
  const [messageApi, contextHolder] = message.useMessage();
  const actionRef = useRef<ActionType>();
  const [roles, setRoles] = useState<API.Crm.CrmRoleOption[]>([]);
  const [selectedPerson, setSelectedPerson] = useState<API.Crm.DingTalkDirectoryUser>();
  const [syncing, setSyncing] = useState(false);

  useEffect(() => {
    listCrmAccessRoles().then((response) => {
      if (response.code === 200) setRoles(response.data || []);
      else messageApi.error(response.msg || '加载 CRM 角色失败');
    });
  }, [messageApi]);

  const syncDirectory = async () => {
    setSyncing(true);
    try {
      const response = await triggerFullSync();
      if (response.code !== 200) {
        messageApi.error(response.msg || '组织同步失败');
        return;
      }
      messageApi.success(`同步完成：${response.data?.userCount || 0} 人`);
      actionRef.current?.reload();
    } finally {
      setSyncing(false);
    }
  };

  const revoke = (person: API.Crm.DingTalkDirectoryUser) => {
    Modal.confirm({
      title: `撤销 ${person.name || '该人员'} 的 CRM 访问权？`,
      content: '将清除其 CRM 角色和钉钉免登映射，但保留企业通讯录资料。',
      okText: '撤销授权',
      okButtonProps: { danger: true },
      onOk: async () => {
        const response = await revokeCrmAccess(person.dingtalkUserId);
        if (response.code !== 200) {
          messageApi.error(response.msg || '撤销失败');
          return Promise.reject();
        }
        messageApi.success('已撤销 CRM 访问权');
        actionRef.current?.reload();
        return undefined;
      },
    });
  };

  const columns: ProColumns<API.Crm.DingTalkDirectoryUser>[] = [
    { title: '姓名 / 手机 / 职位 / 组织', dataIndex: 'keyword', hideInTable: true },
    { title: '姓名', dataIndex: 'name', width: 110, search: false },
    { title: '手机号', dataIndex: 'mobile', width: 130, search: false },
    { title: '职位', dataIndex: 'title', width: 130, search: false, renderText: (v) => v || '-' },
    {
      title: '组织机构',
      dataIndex: 'deptNames',
      width: 200,
      search: false,
      ellipsis: true,
      renderText: (value) => value || '-',
    },
    {
      title: '在职状态',
      dataIndex: 'active',
      width: 90,
      search: false,
      render: (_, record) =>
        record.active === false ? <Tag>已停用</Tag> : <Tag color="success">在职</Tag>,
    },
    {
      title: '授权状态',
      dataIndex: 'accessStatus',
      width: 110,
      valueType: 'select',
      valueEnum: {
        GRANTED: { text: '已授权', status: 'Success' },
        UNGRANTED: { text: '未授权', status: 'Default' },
      },
      render: (_, record) =>
        record.accessGranted ? <Tag color="success">已授权</Tag> : <Tag>未授权</Tag>,
    },
    {
      title: '已分配角色',
      dataIndex: 'roleNames',
      width: 190,
      search: false,
      render: (_, record) => {
        const names = (record.roleNames || '').split('、').filter(Boolean);
        return names.length ? (
          <Space size={[0, 4]} wrap>
            {names.map((name) => <Tag key={name}>{name}</Tag>)}
          </Space>
        ) : '-';
      },
    },
    {
      title: '权限',
      dataIndex: 'permissionCodes',
      width: 150,
      search: false,
      render: (_, record) => {
        const permissions = splitValues(record.permissionCodes);
        if (!permissions.length) return '-';
        return (
          <Tooltip title={permissions.join('、')}>
            <Tag color="blue">{permissions.length} 项权限</Tag>
          </Tooltip>
        );
      },
    },
    {
      title: '操作',
      valueType: 'option',
      width: 150,
      fixed: 'right',
      render: (_, record) => [
        <a
          key="grant"
          onClick={() => record.active !== false && setSelectedPerson(record)}
          aria-disabled={record.active === false}
        >
          {record.accessGranted ? '调整角色' : '分配权限'}
        </a>,
        record.accessGranted ? (
          <a key="revoke" onClick={() => revoke(record)} style={{ color: '#ff4d4f' }}>
            撤销
          </a>
        ) : null,
      ],
    },
  ];

  return (
    <PageContainer>
      {contextHolder}
      <ProTable<API.Crm.DingTalkDirectoryUser>
        headerTitle="企业人员 CRM 访问授权"
        actionRef={actionRef}
        rowKey="dingtalkUserId"
        columns={columns}
        scroll={{ x: 1250 }}
        options={{ reload: true, density: true, setting: true }}
        toolBarRender={() => [
          <Button
            key="sync"
            icon={<SyncOutlined spin={syncing} />}
            loading={syncing}
            onClick={syncDirectory}
          >
            同步企业通讯录
          </Button>,
        ]}
        request={async (params) => {
          const response = await listDingTalkDirectoryUsers({
            keyword: params.keyword,
            accessStatus: params.accessStatus,
          });
          if (response.code !== 200) {
            messageApi.error(response.msg || '查询企业人员失败');
            return { data: [], success: false, total: 0 };
          }
          return { data: response.data || [], success: true, total: response.data?.length || 0 };
        }}
      />
      <AccessGrantModal
        open={Boolean(selectedPerson)}
        person={selectedPerson}
        roles={roles}
        onClose={() => setSelectedPerson(undefined)}
        onSuccess={() => {
          setSelectedPerson(undefined);
          actionRef.current?.reload();
        }}
      />
    </PageContainer>
  );
};

export default UserSearch;
