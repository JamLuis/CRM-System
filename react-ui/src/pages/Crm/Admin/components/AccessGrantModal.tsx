import { grantCrmAccess } from '@/services/crm/admin';
import { Descriptions, Form, Modal, Select, Tag, message } from 'antd';
import React, { useEffect } from 'react';
import { CRM_HORIZONTAL_FORM_PROPS } from '../../components/formLayout';

export type AccessGrantModalProps = {
  open: boolean;
  person?: API.Crm.DingTalkDirectoryUser;
  roles: API.Crm.CrmRoleOption[];
  onClose: () => void;
  onSuccess: () => void;
};

const parseRoleIds = (value?: string) =>
  (value || '')
    .split(',')
    .map((item) => Number(item))
    .filter((item) => Number.isFinite(item));

const AccessGrantModal: React.FC<AccessGrantModalProps> = ({
  open,
  person,
  roles,
  onClose,
  onSuccess,
}) => {
  const [form] = Form.useForm<{ roleIds: number[] }>();
  const [messageApi, contextHolder] = message.useMessage();

  useEffect(() => {
    if (open) {
      form.setFieldsValue({ roleIds: parseRoleIds(person?.roleIds) });
    }
  }, [form, open, person]);

  const submit = async () => {
    if (!person?.dingtalkUserId) return;
    const values = await form.validateFields();
    const response = await grantCrmAccess(person.dingtalkUserId, values.roleIds);
    if (response.code !== 200) {
      messageApi.error(response.msg || '授权失败');
      return;
    }
    messageApi.success('CRM访问权限已更新');
    onSuccess();
  };

  return (
    <Modal title="分配 CRM 访问权限" open={open} onCancel={onClose} onOk={submit} destroyOnClose>
      {contextHolder}
      <Descriptions column={1} size="small" bordered style={{ marginBottom: 20 }}>
        <Descriptions.Item label="人员">{person?.name || '-'}</Descriptions.Item>
        <Descriptions.Item label="手机号">{person?.mobile || '-'}</Descriptions.Item>
        <Descriptions.Item label="职位">{person?.title || '-'}</Descriptions.Item>
        <Descriptions.Item label="组织">{person?.deptNames || '-'}</Descriptions.Item>
        <Descriptions.Item label="状态">
          {person?.active === false ? <Tag>已停用</Tag> : <Tag color="success">在职</Tag>}
        </Descriptions.Item>
      </Descriptions>
      <Form form={form} {...CRM_HORIZONTAL_FORM_PROPS}>
        <Form.Item
          name="roleIds"
          label="CRM角色"
          rules={[{ required: true, type: 'array', min: 1, message: '至少选择一个 CRM 角色' }]}
          extra="只有包含 crm:access 的 CRM 角色可被选择；组织同步不会自动授予访问权。"
        >
          <Select
            mode="multiple"
            optionFilterProp="label"
            options={roles.map((role) => ({ label: role.roleName, value: role.roleId }))}
            placeholder="选择 CRM 角色"
          />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default AccessGrantModal;
