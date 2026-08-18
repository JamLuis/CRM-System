import React, { useEffect, useState } from 'react';
import { Button, Card, Checkbox, Form, Input, message, Modal, Popconfirm, Space, Table, Tabs, Tag } from 'antd';
import { useAccess } from '@umijs/max';
import {
  addCollaborator,
  getCustomerOwners,
  getOwnerChangeHistory,
  removeCollaborator,
  transferOwner,
} from '@/services/crm/customer';
import { OWNER_CHANGE_TYPE_ENUM, OWNER_ROLE_TYPE_ENUM } from '../../constants';
import UserSelect from '../../components/UserSelect';

export type MembersTabProps = {
  customerId: number;
};

/** 客户成员管理 */
const MembersTab: React.FC<MembersTabProps> = ({ customerId }) => {
  const [messageApi, contextHolder] = message.useMessage();
  const access = useAccess();
  const canAssign = access.hasPerms('crm:customer:assign');

  const [owners, setOwners] = useState<API.Crm.CustomerOwner[]>([]);
  const [changes, setChanges] = useState<API.Crm.OwnerChange[]>([]);
  const [loading, setLoading] = useState(false);

  const [transferVisible, setTransferVisible] = useState(false);
  const [transferForm] = Form.useForm();
  const [transferTarget, setTransferTarget] = useState<API.Crm.SysUserItem>();

  const [collabVisible, setCollabVisible] = useState(false);
  const [collabTarget, setCollabTarget] = useState<API.Crm.SysUserItem>();

  const loadData = async () => {
    setLoading(true);
    try {
      const [ownerResp, changeResp] = await Promise.all([
        getCustomerOwners(customerId),
        getOwnerChangeHistory(customerId),
      ]);
      if (ownerResp.code === 200) setOwners(ownerResp.data || []);
      if (changeResp.code === 200) setChanges(changeResp.data || []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [customerId]);

  const handleTransfer = async () => {
    const values = await transferForm.validateFields();
    if (!transferTarget?.userId) {
      messageApi.error('请选择新负责人');
      return;
    }
    const resp = await transferOwner(customerId, {
      targetOwnerId: transferTarget.userId,
      targetOwnerName: transferTarget.nickName || transferTarget.userName || '',
      targetOwnerDeptId: transferTarget.deptId,
      keepPreviousAsCollaborator: values.keepPreviousAsCollaborator,
      reason: values.reason,
    });
    if (resp.code === 200) {
      messageApi.success('移交成功');
      setTransferVisible(false);
      transferForm.resetFields();
      setTransferTarget(undefined);
      loadData();
    } else {
      messageApi.error(resp.msg || '移交失败');
    }
  };

  const handleAddCollaborator = async () => {
    if (!collabTarget?.userId) {
      messageApi.error('请选择协同人');
      return;
    }
    const resp = await addCollaborator(customerId, {
      userId: collabTarget.userId,
      userName: collabTarget.nickName || collabTarget.userName || '',
    });
    if (resp.code === 200) {
      messageApi.success('添加成功');
      setCollabVisible(false);
      setCollabTarget(undefined);
      loadData();
    } else {
      messageApi.error(resp.msg || '添加失败');
    }
  };

  const handleRemoveCollaborator = async (ownerId: number) => {
    const resp = await removeCollaborator(customerId, ownerId);
    if (resp.code === 200) {
      messageApi.success('移除成功');
      loadData();
    } else {
      messageApi.error(resp.msg || '移除失败');
    }
  };

  const ownerColumns = [
    { title: '成员', dataIndex: 'userName' },
    {
      title: '角色',
      dataIndex: 'roleType',
      render: (v: string) => {
        const meta = OWNER_ROLE_TYPE_ENUM[v];
        return meta ? <Tag color={meta.color}>{meta.text}</Tag> : v;
      },
    },
    { title: '状态', dataIndex: 'status' },
    {
      title: '操作',
      render: (_: any, record: API.Crm.CustomerOwner) =>
        canAssign && record.roleType === 'COLLABORATOR' ? (
          <Popconfirm title="确认移除该协同人？" onConfirm={() => handleRemoveCollaborator(record.id!)}>
            <a>移除</a>
          </Popconfirm>
        ) : null,
    },
  ];

  const changeColumns = [
    {
      title: '变更类型',
      dataIndex: 'changeType',
      render: (v: string) => OWNER_CHANGE_TYPE_ENUM[v]?.text || v,
    },
    { title: '原负责人', dataIndex: 'previousPrimaryOwnerName' },
    { title: '新负责人', dataIndex: 'targetPrimaryOwnerName' },
    { title: '原因', dataIndex: 'reason', ellipsis: true },
    { title: '操作人', dataIndex: 'operatorName' },
    { title: '时间', dataIndex: 'createTime' },
  ];

  return (
    <Card>
      {contextHolder}
      <Tabs
        items={[
          {
            key: 'members',
            label: '当前成员',
            children: (
              <>
                {canAssign && (
                  <Space style={{ marginBottom: 16 }}>
                    <Button type="primary" onClick={() => setTransferVisible(true)}>
                      移交负责人
                    </Button>
                    <Button onClick={() => setCollabVisible(true)}>新增协同人</Button>
                  </Space>
                )}
                <Table
                  rowKey="id"
                  size="small"
                  loading={loading}
                  dataSource={owners}
                  columns={ownerColumns}
                  pagination={false}
                />
              </>
            ),
          },
          {
            key: 'history',
            label: '变更历史',
            children: (
              <Table
                rowKey="id"
                size="small"
                loading={loading}
                dataSource={changes}
                columns={changeColumns}
                pagination={{ defaultPageSize: 10 }}
              />
            ),
          },
        ]}
      />

      <Modal
        title="移交负责人"
        open={transferVisible}
        onCancel={() => setTransferVisible(false)}
        onOk={handleTransfer}
        destroyOnClose
      >
        <Form form={transferForm} layout="vertical">
          <Form.Item label="新负责人" required>
            <UserSelect
              value={transferTarget?.userId}
              onChange={(_, user) => setTransferTarget(user)}
            />
          </Form.Item>
          <Form.Item name="keepPreviousAsCollaborator" valuePropName="checked" label="原负责人保留为协同人">
            <Checkbox />
          </Form.Item>
          <Form.Item name="reason" label="移交原因" rules={[{ required: true, message: '请填写移交原因' }]}>
            <Input.TextArea rows={3} maxLength={255} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="新增协同人"
        open={collabVisible}
        onCancel={() => setCollabVisible(false)}
        onOk={handleAddCollaborator}
        destroyOnClose
      >
        <UserSelect value={collabTarget?.userId} onChange={(_, user) => setCollabTarget(user)} />
      </Modal>
    </Card>
  );
};

export default MembersTab;
