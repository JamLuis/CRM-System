import React, { useEffect, useState } from 'react';
import { Button, Form, Input, message, Modal, Popconfirm, Select, Switch, Table, Tag } from 'antd';
import { useAccess } from '@umijs/max';
import { addContact, deactivateContact, getContactsByCustomer, updateContact } from '@/services/crm/contact';
import { PHONE_TYPE_ENUM } from '../../constants';

export type ContactsTabProps = {
  customerId: number;
};

const phoneTypeOptions = Object.keys(PHONE_TYPE_ENUM).map((k) => ({ label: k, value: k }));

/** 客户联系人管理（敏感字段展示后端返回的脱敏值） */
const ContactsTab: React.FC<ContactsTabProps> = ({ customerId }) => {
  const [messageApi, contextHolder] = message.useMessage();
  const access = useAccess();
  const canWrite = access.hasPerms('crm:contact:write');

  const [contacts, setContacts] = useState<API.Crm.Contact[]>([]);
  const [loading, setLoading] = useState(false);
  const [formVisible, setFormVisible] = useState(false);
  const [currentRow, setCurrentRow] = useState<API.Crm.Contact>();
  const [form] = Form.useForm();

  const loadData = async () => {
    setLoading(true);
    try {
      const resp = await getContactsByCustomer(customerId);
      if (resp.code === 200) setContacts(resp.data || []);
      else messageApi.error(resp.msg || '查询失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [customerId]);

  const handleSubmit = async () => {
    const values = await form.validateFields();
    const payload: API.Crm.Contact = { ...currentRow, ...values, customerId };
    const resp = payload.contactId ? await updateContact(payload) : await addContact(payload);
    if (resp.code === 200) {
      messageApi.success(payload.contactId ? '编辑成功' : '创建成功');
      setFormVisible(false);
      form.resetFields();
      loadData();
    } else {
      messageApi.error(resp.msg || '操作失败');
    }
  };

  const handleDeactivate = async (contactId: number) => {
    const resp = await deactivateContact(contactId);
    if (resp.code === 200) {
      messageApi.success('已停用');
      loadData();
    } else {
      messageApi.error(resp.msg || '操作失败');
    }
  };

  const columns = [
    { title: '姓名', dataIndex: 'name' },
    {
      title: '电话',
      render: (_: any, r: API.Crm.Contact) => r.phoneMasked || r.phoneNumber || '-',
    },
    {
      title: '邮箱',
      render: (_: any, r: API.Crm.Contact) => r.emailMasked || r.email || '-',
    },
    {
      title: '微信',
      render: (_: any, r: API.Crm.Contact) => r.wechatMasked || r.wechatId || '-',
    },
    { title: '职责', dataIndex: 'responsibility' },
    { title: '职务', dataIndex: 'title' },
    {
      title: '决策人',
      dataIndex: 'isDecisionMaker',
      render: (v: boolean) => (v ? <Tag color="blue">是</Tag> : '否'),
    },
    {
      title: '状态',
      dataIndex: 'status',
      render: (v: string) => (v === '有效' ? <Tag color="green">有效</Tag> : <Tag>已停用</Tag>),
    },
    {
      title: '操作',
      render: (_: any, record: API.Crm.Contact) =>
        canWrite ? (
          <>
            {record.status === '有效' && (
              <>
                <a
                  style={{ marginRight: 8 }}
                  onClick={() => {
                    setCurrentRow(record);
                    form.setFieldsValue(record);
                    setFormVisible(true);
                  }}
                >
                  编辑
                </a>
                <Popconfirm title="确认停用该联系人？" onConfirm={() => handleDeactivate(record.contactId!)}>
                  <a>停用</a>
                </Popconfirm>
              </>
            )}
          </>
        ) : null,
    },
  ];

  return (
    <>
      {contextHolder}
      {canWrite && (
        <Button
          type="primary"
          style={{ marginBottom: 16 }}
          onClick={() => {
            setCurrentRow(undefined);
            form.resetFields();
            form.setFieldsValue({ phoneType: '手机', countryCode: '+86' });
            setFormVisible(true);
          }}
        >
          新建联系人
        </Button>
      )}
      <Table
        rowKey="contactId"
        size="small"
        loading={loading}
        dataSource={contacts}
        columns={columns}
        pagination={false}
      />
      <Modal
        title={currentRow?.contactId ? '编辑联系人' : '新建联系人'}
        open={formVisible}
        onCancel={() => setFormVisible(false)}
        onOk={handleSubmit}
        width={560}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="姓名" rules={[{ required: true, message: '请输入姓名' }]}>
            <Input maxLength={64} />
          </Form.Item>
          <Form.Item name="phoneType" label="电话类型">
            <Select options={phoneTypeOptions} />
          </Form.Item>
          <Form.Item name="countryCode" label="国家码">
            <Input maxLength={8} placeholder="+86" />
          </Form.Item>
          <Form.Item name="phoneNumber" label="电话号码" rules={[{ required: true, message: '请输入电话号码' }]}>
            <Input maxLength={32} />
          </Form.Item>
          <Form.Item name="email" label="邮箱">
            <Input maxLength={128} />
          </Form.Item>
          <Form.Item name="wechatId" label="微信号">
            <Input maxLength={64} />
          </Form.Item>
          <Form.Item name="responsibility" label="职责">
            <Input maxLength={64} placeholder="如：采购、技术" />
          </Form.Item>
          <Form.Item name="title" label="职务">
            <Input maxLength={64} />
          </Form.Item>
          <Form.Item name="isDecisionMaker" label="是否决策人" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={2} maxLength={255} />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
};

export default ContactsTab;
