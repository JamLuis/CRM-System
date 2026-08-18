import React, { useEffect, useState } from 'react';
import {
  Button,
  Checkbox,
  DatePicker,
  Form,
  Input,
  message,
  Modal,
  Select,
  Space,
  Table,
  Tag,
} from 'antd';
import { useAccess } from '@umijs/max';
import dayjs from 'dayjs';
import { createFollowUp, correctFollowUp, getFollowUpsByCustomer, voidFollowUp } from '@/services/crm/followup';
import { getContactsByCustomer } from '@/services/crm/contact';
import { FOLLOW_UP_METHOD_ENUM } from '../../constants';

export type FollowUpsTabProps = {
  customerId: number;
};

const methodOptions = Object.keys(FOLLOW_UP_METHOD_ENUM).map((k) => ({ label: k, value: k }));

/** 客户跟踪记录 */
const FollowUpsTab: React.FC<FollowUpsTabProps> = ({ customerId }) => {
  const [messageApi, contextHolder] = message.useMessage();
  const access = useAccess();
  const canWrite = access.hasPerms('crm:followup:write');

  const [followUps, setFollowUps] = useState<API.Crm.FollowUp[]>([]);
  const [contacts, setContacts] = useState<API.Crm.Contact[]>([]);
  const [loading, setLoading] = useState(false);

  const [formVisible, setFormVisible] = useState(false);
  const [correctingRow, setCorrectingRow] = useState<API.Crm.FollowUp>();
  const [voidingRow, setVoidingRow] = useState<API.Crm.FollowUp>();
  const [voidReason, setVoidReason] = useState('');
  const [form] = Form.useForm();

  const loadData = async () => {
    setLoading(true);
    try {
      const [fuResp, ctResp] = await Promise.all([
        getFollowUpsByCustomer(customerId),
        getContactsByCustomer(customerId),
      ]);
      if (fuResp.code === 200) setFollowUps(fuResp.data || []);
      if (ctResp.code === 200) setContacts((ctResp.data || []).filter((c) => c.status === '有效'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [customerId]);

  const openCreate = () => {
    setCorrectingRow(undefined);
    form.resetFields();
    form.setFieldsValue({ method: '电话', followUpAt: dayjs() });
    setFormVisible(true);
  };

  const openCorrect = (row: API.Crm.FollowUp) => {
    setCorrectingRow(row);
    form.resetFields();
    form.setFieldsValue({
      method: row.method,
      followUpAt: row.followUpAt ? dayjs(row.followUpAt) : undefined,
      content: row.content,
      outcome: row.outcome,
      nextAction: row.nextAction,
      nextFollowUpAt: row.nextFollowUpAt ? dayjs(row.nextFollowUpAt) : undefined,
      noNextFollowUpReason: row.noNextFollowUpReason,
      hasNewSigningProject: row.hasNewSigningProject,
    });
    setFormVisible(true);
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    const followUp: API.Crm.FollowUp = {
      customerId,
      method: values.method,
      followUpAt: values.followUpAt?.format('YYYY-MM-DD HH:mm:ss'),
      content: values.content,
      hasNewSigningProject: values.hasNewSigningProject,
      outcome: values.outcome,
      nextAction: values.nextAction,
      nextFollowUpAt: values.nextFollowUpAt?.format('YYYY-MM-DD HH:mm:ss'),
      noNextFollowUpReason: values.noNextFollowUpReason,
    };
    let resp: API.Crm.R<API.Crm.FollowUp>;
    if (correctingRow?.followUpId) {
      resp = await correctFollowUp(correctingRow.followUpId, {
        followUp,
        contactIds: values.contactIds,
        correctionReason: values.correctionReason,
      });
    } else {
      resp = await createFollowUp({ followUp, contactIds: values.contactIds });
    }
    if (resp.code === 200) {
      messageApi.success(correctingRow ? '更正成功' : '创建成功');
      setFormVisible(false);
      form.resetFields();
      loadData();
    } else {
      messageApi.error(resp.msg || '操作失败');
    }
  };

  const handleVoid = async () => {
    if (!voidingRow?.followUpId) return;
    if (!voidReason) {
      messageApi.error('请填写作废原因');
      return;
    }
    const resp = await voidFollowUp(voidingRow.followUpId, { voidedReason: voidReason });
    if (resp.code === 200) {
      messageApi.success('已作废');
      setVoidingRow(undefined);
      setVoidReason('');
      loadData();
    } else {
      messageApi.error(resp.msg || '操作失败');
    }
  };

  const columns = [
    { title: '方式', dataIndex: 'method', width: 80 },
    { title: '跟踪时间', dataIndex: 'followUpAt', width: 160 },
    { title: '内容', dataIndex: 'content', ellipsis: true },
    { title: '结果', dataIndex: 'outcome', ellipsis: true },
    { title: '下步动作', dataIndex: 'nextAction', ellipsis: true },
    { title: '下次跟进', dataIndex: 'nextFollowUpAt', width: 160 },
    { title: '记录人', dataIndex: 'createdByName', width: 100 },
    {
      title: '状态',
      width: 90,
      render: (_: any, r: API.Crm.FollowUp) => {
        if (r.isVoided) return <Tag color="red">已作废</Tag>;
        if (r.isCorrected) return <Tag color="orange">已更正</Tag>;
        if (r.correctionOfFollowUpId) return <Tag color="blue">更正记录</Tag>;
        return <Tag color="green">有效</Tag>;
      },
    },
    {
      title: '操作',
      width: 120,
      render: (_: any, record: API.Crm.FollowUp) =>
        canWrite && !record.isVoided ? (
          <Space>
            <a onClick={() => openCorrect(record)}>更正</a>
            <a onClick={() => setVoidingRow(record)}>作废</a>
          </Space>
        ) : null,
    },
  ];

  return (
    <>
      {contextHolder}
      {canWrite && (
        <Button type="primary" style={{ marginBottom: 16 }} onClick={openCreate}>
          新增跟踪
        </Button>
      )}
      <Table
        rowKey="followUpId"
        size="small"
        loading={loading}
        dataSource={followUps}
        columns={columns}
        pagination={{ defaultPageSize: 10 }}
      />

      <Modal
        title={correctingRow ? '更正跟踪' : '新增跟踪'}
        open={formVisible}
        onCancel={() => setFormVisible(false)}
        onOk={handleSubmit}
        width={640}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item name="method" label="跟踪方式" rules={[{ required: true }]}>
            <Select options={methodOptions} />
          </Form.Item>
          <Form.Item name="followUpAt" label="跟踪时间" rules={[{ required: true }]}>
            <DatePicker showTime style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="contactIds" label="关联联系人">
            <Select
              mode="multiple"
              allowClear
              placeholder="选择参与本次跟踪的联系人"
              options={contacts.map((c) => ({ label: c.name, value: c.contactId }))}
            />
          </Form.Item>
          <Form.Item name="content" label="跟踪内容" rules={[{ required: true, message: '请填写跟踪内容' }]}>
            <Input.TextArea rows={3} maxLength={1000} />
          </Form.Item>
          <Form.Item name="hasNewSigningProject" label="是否有新签项目" valuePropName="checked">
            <Checkbox />
          </Form.Item>
          <Form.Item name="outcome" label="跟踪结果">
            <Input.TextArea rows={2} maxLength={500} />
          </Form.Item>
          <Form.Item name="nextAction" label="下步动作">
            <Input maxLength={255} />
          </Form.Item>
          <Form.Item name="nextFollowUpAt" label="下次跟进时间">
            <DatePicker showTime style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="noNextFollowUpReason" label="无下次跟进原因（可选）">
            <Input maxLength={255} />
          </Form.Item>
          {correctingRow && (
            <Form.Item
              name="correctionReason"
              label="更正原因"
              rules={[{ required: true, message: '请填写更正原因' }]}
            >
              <Input.TextArea rows={2} maxLength={255} />
            </Form.Item>
          )}
        </Form>
      </Modal>

      <Modal
        title="作废跟踪"
        open={!!voidingRow}
        onCancel={() => setVoidingRow(undefined)}
        onOk={handleVoid}
        destroyOnClose
      >
        <Input.TextArea
          rows={3}
          maxLength={255}
          placeholder="请填写作废原因"
          value={voidReason}
          onChange={(e) => setVoidReason(e.target.value)}
        />
      </Modal>
    </>
  );
};

export default FollowUpsTab;
