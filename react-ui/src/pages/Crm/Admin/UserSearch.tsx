import React, { useRef, useState } from 'react';
import { Button, message, Tag } from 'antd';
import { ActionType, PageContainer, ProColumns, ProTable } from '@ant-design/pro-components';
import { SearchOutlined, UserAddOutlined } from '@ant-design/icons';
import { listDingtalkIdentities, refreshDingTalkUser, searchUsers } from '@/services/crm/admin';
import IdentityMapModal from './components/IdentityMapModal';

/** 钉钉人员搜索与身份授权 */
const UserSearch: React.FC = () => {
  const [messageApi, contextHolder] = message.useMessage();
  const actionRef = useRef<ActionType>();
  const [keyword, setKeyword] = useState<string>('');

  const [mapVisible, setMapVisible] = useState(false);
  const [mapDingtalkUserId, setMapDingtalkUserId] = useState<string>();
  const [mapSysUserId, setMapSysUserId] = useState<number>();

  // 已映射的 dingtalkUserId -> sysUserId 集合
  const [mappedIds, setMappedIds] = useState<Record<string, number>>({});

  const loadMapped = async () => {
    const resp = await listDingtalkIdentities();
    if (resp.code === 200 && resp.data) {
      const map: Record<string, number> = {};
      resp.data.forEach((it) => {
        if (it.dingtalkUserId) map[it.dingtalkUserId] = it.sysUserId || 0;
      });
      setMappedIds(map);
    }
  };

  React.useEffect(() => {
    loadMapped();
  }, []);

  const handleRefresh = async (dingtalkUserId?: string) => {
    if (!dingtalkUserId) return;
    const resp = await refreshDingTalkUser(dingtalkUserId);
    if (resp.code === 200) {
      messageApi.success('刷新成功');
      actionRef.current?.reload();
    } else {
      messageApi.error(resp.msg || '刷新失败');
    }
  };

  const columns: ProColumns<API.Crm.SysUserItem>[] = [
    { title: '姓名', dataIndex: 'nickName', width: 120 },
    { title: '账号', dataIndex: 'userName', width: 120 },
    { title: '手机号', dataIndex: 'phonenumber', width: 130 },
    {
      title: '部门',
      dataIndex: ['dept', 'deptName'],
      width: 140,
      render: (_, record) => record.dept?.deptName || '-',
    },
    {
      title: '钉钉用户 ID',
      dataIndex: 'dingtalkUserId',
      width: 160,
      render: (_, record) => record.dingtalkUserId || '-',
    },
    {
      title: '授权状态',
      width: 110,
      render: (_, record) => {
        const dt = record.dingtalkUserId;
        if (dt && mappedIds[dt] !== undefined) {
          return <Tag color="success">已授权</Tag>;
        }
        return <Tag color="default">未授权</Tag>;
      },
    },
    {
      title: '操作',
      valueType: 'option',
      width: 180,
      render: (_, record) => [
        <a
          key="map"
          onClick={() => {
            setMapDingtalkUserId(record.dingtalkUserId);
            setMapSysUserId(record.userId);
            setMapVisible(true);
          }}
        >
          <UserAddOutlined /> 授权
        </a>,
        record.dingtalkUserId ? (
          <a key="refresh" onClick={() => handleRefresh(record.dingtalkUserId)}>
            刷新
          </a>
        ) : null,
      ],
    },
  ];

  return (
    <PageContainer>
      {contextHolder}
      <ProTable<API.Crm.SysUserItem>
        headerTitle="钉钉人员搜索"
        actionRef={actionRef}
        rowKey="userId"
        columns={columns}
        search={false}
        toolBarRender={() => [
          <Button
            key="search"
            type="primary"
            icon={<SearchOutlined />}
            onClick={() => actionRef.current?.reload()}
          >
            搜索
          </Button>,
        ]}
        request={async () => {
          if (!keyword.trim()) {
            return { data: [], success: true, total: 0 };
          }
          const resp = await searchUsers(keyword.trim());
          if (resp.code === 200) {
            return { data: resp.data || [], success: true, total: resp.data?.length || 0 };
          }
          messageApi.error(resp.msg || '搜索失败');
          return { data: [], success: false, total: 0 };
        }}
        toolbar={{
          search: {
            placeholder: '输入姓名 / 账号 / 手机号搜索',
            onSearch: (value) => {
              setKeyword(value);
              actionRef.current?.reload();
            },
          },
        }}
        options={{ reload: true }}
      />
      <IdentityMapModal
        visible={mapVisible}
        initialDingtalkUserId={mapDingtalkUserId}
        initialSysUserId={mapSysUserId}
        onClose={() => setMapVisible(false)}
        onSuccess={() => {
          setMapVisible(false);
          loadMapped();
        }}
      />
    </PageContainer>
  );
};

export default UserSearch;
