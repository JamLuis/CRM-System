import React, { useEffect, useState } from 'react';
import { Button, DatePicker, Form, Input, Popup, Selector, Space, TextArea } from 'antd-mobile';
import dayjs from 'dayjs';
import { IMPORTANCE_ENUM, LIFECYCLE_STAGE_ENUM } from '../constants';

const importanceOptions = Object.keys(IMPORTANCE_ENUM).map((k) => ({ label: k, value: k }));
const stageOptions = Object.keys(LIFECYCLE_STAGE_ENUM).map((k) => ({ label: k, value: k }));

export type MobileCustomerFormProps = {
  visible: boolean;
  /** 编辑时传入当前客户，创建时为 undefined */
  current?: API.Crm.Customer;
  onCancel: () => void;
  onSubmit: (values: API.Crm.Customer) => Promise<boolean>;
};

/** 移动端新建/编辑客户表单（底部弹层，字段与 PC CustomerForm 对齐） */
const MobileCustomerForm: React.FC<MobileCustomerFormProps> = ({
  visible,
  current,
  onCancel,
  onSubmit,
}) => {
  const [form] = Form.useForm();
  const isEdit = !!current?.customerId;
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (visible) {
      if (current) {
        form.setFieldsValue({
          ...current,
          importance: current.importance ? [current.importance] : [],
          lifecycleStage: current.lifecycleStage ? [current.lifecycleStage] : [],
          nextFollowUpAt: current.nextFollowUpAt ? new Date(current.nextFollowUpAt) : undefined,
        });
      } else {
        form.resetFields();
        form.setFieldsValue({ importance: ['一般'], lifecycleStage: ['新获取'] });
      }
    }
  }, [visible, current, form]);

  const handleOk = async () => {
    const values = await form.validateFields();
    const nextFollowUpAt: Date | undefined = values.nextFollowUpAt;
    const payload: API.Crm.Customer = {
      ...current,
      ...values,
      importance: values.importance?.[0],
      lifecycleStage: values.lifecycleStage?.[0],
      nextFollowUpAt: nextFollowUpAt ? dayjs(nextFollowUpAt).format('YYYY-MM-DD HH:mm:ss') : undefined,
    };
    setSubmitting(true);
    try {
      await onSubmit(payload);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Popup
      visible={visible}
      onMaskClick={onCancel}
      bodyStyle={{
        borderTopLeftRadius: 12,
        borderTopRightRadius: 12,
        maxHeight: '85vh',
        overflowY: 'auto',
      }}
    >
      <div style={{ padding: '12px 16px 24px' }}>
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            marginBottom: 12,
          }}
        >
          <span style={{ fontWeight: 600, fontSize: 16 }}>{isEdit ? '编辑客户' : '新建客户'}</span>
          <Space>
            <Button size="small" onClick={onCancel}>
              取消
            </Button>
            <Button size="small" color="primary" loading={submitting} onClick={handleOk}>
              保存
            </Button>
          </Space>
        </div>
        <Form form={form} layout="vertical" footer={null}>
          <Form.Item
            name="name"
            label="客户名称"
            rules={[{ required: true, message: '请输入客户名称' }]}
          >
            <Input placeholder="请输入客户名称" maxLength={128} />
          </Form.Item>
          <Form.Item
            name="importance"
            label="重要程度"
            rules={[{ required: true, message: '请选择重要程度' }]}
          >
            <Selector options={importanceOptions} />
          </Form.Item>
          <Form.Item name="lifecycleStage" label="生命周期阶段">
            <Selector options={stageOptions} disabled={isEdit} />
          </Form.Item>
          <Form.Item
            name="source"
            label="客户来源"
            rules={[{ required: true, message: '请输入客户来源' }]}
          >
            <Input placeholder="如：展会、转介绍、官网" maxLength={32} />
          </Form.Item>
          <Form.Item name="industry" label="行业" rules={[{ required: true, message: '请输入行业' }]}>
            <Input placeholder="请输入行业" maxLength={32} />
          </Form.Item>
          <Form.Item
            name="nextFollowUpAt"
            label="下次跟进时间"
            rules={[{ required: true, message: '正常客户必须设置下次跟进时间' }]}
          >
            <DatePicker precision="minute" />
          </Form.Item>
          <Form.Item name="addressProvince" label="省份">
            <Input placeholder="省份" maxLength={32} />
          </Form.Item>
          <Form.Item name="addressCity" label="城市">
            <Input placeholder="城市" maxLength={32} />
          </Form.Item>
          <Form.Item name="addressDistrict" label="区县">
            <Input placeholder="区县" maxLength={32} />
          </Form.Item>
          <Form.Item name="addressDetail" label="详细地址">
            <Input placeholder="详细地址" maxLength={256} />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <TextArea rows={2} maxLength={500} placeholder="备注" />
          </Form.Item>
        </Form>
      </div>
    </Popup>
  );
};

export default MobileCustomerForm;
