import { listRoleScopes, saveRoleScope } from '@/services/crm/admin';
import { getRoleList } from '@/services/system/role';
import { EditOutlined } from '@ant-design/icons';
import { ActionType, PageContainer, ProColumns, ProTable } from '@ant-design/pro-components';
import { Form, message, Modal, Select, Tag } from 'antd';
import React, { useEffect, useRef, useState } from 'react';
import { CRM_HORIZONTAL_FORM_PROPS } from '../components/formLayout';

/** CRM 数据范围类型 */
const SCOPE_TYPE_OPTIONS = [
  { label: '全部客户（ALL）', value: 'ALL' },
  { label: '本部门及下级部门（DEPT）', value: 'DEPT' },
  { label: '本人主负责或协同（SELF_CREATED_OR_MEMBER）', value: 'SELF_CREATED_OR_MEMBER' },
];

const SCOPE_TYPE_TAG: Record<string, { text: string; color: string }> = {
  ALL: { text: '全部客户', color: 'blue' },
  DEPT: { text: '本部门及下级', color: 'green' },
  SELF_CREATED_OR_MEMBER: { text: '本人负责/协同', color: 'default' },
};

type RoleScopeRow = {
  roleId: number;
  roleName?: string;
  roleKey?: string;
  scopeType?: string;
};

/** CRM 角色数据范围授权 */
const RoleScope: React.FC = () => {
  const [messageApi, contextHolder] = message.useMessage();
  const actionRef = useRef<ActionType>();

  const [roles, setRoles] = useState<API.System.Role[]>([]);
  const [editVisible, setEditVisible] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm();

  /** 加载 CRM 相关角色（role_key 以 crm_ 开头） */
  const loadRoles = async () => {
    const resp = await getRoleList({ current: '1', pageSize: '100' });
    if (resp.code === 200) {
      const all = resp.rows || [];
      setRoles(all.filter((r) => r.roleKey?.startsWith('crm_')));
    }
  };

  useEffect(() => {
    loadRoles();
  }, []);

  const handleSave = async () => {
    const values = await form.validateFields();
    setSubmitting(true);
    try {
      const resp = await saveRoleScope({
        roleId: values.roleId,
        scopeType: values.scopeType,
      });
      if (resp.code === 200) {
        messageApi.success('数据范围已保存');
        setEditVisible(false);
        actionRef.current?.reload();
      } else {
        messageApi.error(resp.msg || '保存失败');
      }
    } finally {
      setSubmitting(false);
    }
  };

  const columns: ProColumns<RoleScopeRow>[] = [
    { title: '角色 ID', dataIndex: 'roleId', width: 100 },
    { title: '角色名称', dataIndex: 'roleName', width: 160 },
    { title: '角色标识', dataIndex: 'roleKey', width: 160 },
    {
      title: 'CRM 数据范围',
      dataIndex: 'scopeType',
      width: 180,
      render: (_, record) => {
        const tag = SCOPE_TYPE_TAG[record.scopeType || ''];
        return tag ? <Tag color={tag.color}>{tag.text}</Tag> : <Tag>未配置</Tag>;
      },
    },
    {
      title: '操作',
      valueType: 'option',
      width: 120,
      render: (_, record) => [
        <a
          key="edit"
          onClick={() => {
            form.setFieldsValue({ roleId: record.roleId, scopeType: record.scopeType });
            setEditVisible(true);
          }}
        >
          <EditOutlined /> 设置范围
        </a>,
      ],
    },
  ];

  return (
    <PageContainer>
      {contextHolder}
      <ProTable<RoleScopeRow>
        headerTitle="CRM 角色数据范围"
        actionRef={actionRef}
        rowKey="roleId"
        columns={columns}
        search={false}
        request={async () => {
          const resp = await listRoleScopes();
          if (resp.code === 200) {
            const scopes = resp.data || [];
            const rows: RoleScopeRow[] = roles.map((role) => {
              const scope = scopes.find((s) => s.roleId === role.roleId);
              return {
                roleId: role.roleId as number,
                roleName: role.roleName,
                roleKey: role.roleKey,
                scopeType: scope?.scopeType,
              };
            });
            // 追加已配置范围但不在角色列表中的记录
            scopes.forEach((s) => {
              if (!rows.some((r) => r.roleId === s.roleId)) {
                rows.push({ roleId: s.roleId as number, scopeType: s.scopeType });
              }
            });
            return { data: rows, success: true, total: rows.length };
          }
          messageApi.error(resp.msg || '查询失败');
          return { data: [], success: false, total: 0 };
        }}
      />

      <Modal
        title="设置 CRM 数据范围"
        open={editVisible}
        onOk={handleSave}
        onCancel={() => setEditVisible(false)}
        confirmLoading={submitting}
        destroyOnClose
      >
        <Form form={form} {...CRM_HORIZONTAL_FORM_PROPS}>
          <Form.Item name="roleId" label="角色" rules={[{ required: true, message: '请选择角色' }]}>
            <Select
              placeholder="选择 CRM 角色"
              options={roles.map((r) => ({
                label: `${r.roleName}（${r.roleKey}）`,
                value: r.roleId,
              }))}
            />
          </Form.Item>
          <Form.Item
            name="scopeType"
            label="数据范围"
            rules={[{ required: true, message: '请选择数据范围' }]}
          >
            <Select placeholder="选择数据范围" options={SCOPE_TYPE_OPTIONS} />
          </Form.Item>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default RoleScope;
