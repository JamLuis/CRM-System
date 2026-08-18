import React, { useEffect } from 'react';
import { Form, Input, Modal, Select } from 'antd';
import { IMPORTANCE_ENUM, LIFECYCLE_STAGE_ENUM } from '../../constants';

export type CustomerFormProps = {
  /** 编辑时传入当前客户，创建时为 undefined */
  current?: API.Crm.Customer;
  visible: boolean;
  onCancel: () => void;
  onSubmit: (values: API.Crm.Customer) => Promise<boolean>;
};

const importanceOptions = Object.keys(IMPORTANCE_ENUM).map((k) => ({ label: k, value: k }));
const stageOptions = Object.keys(LIFECYCLE_STAGE_ENUM).map((k) => ({ label: k, value: k }));

/** 客户创建/编辑表单 */
const CustomerForm: React.FC<CustomerFormProps> = (props) => {
  const { current, visible, onCancel, onSubmit } = props;
  const [form] = Form.useForm();
  const isEdit = !!current?.customerId;

  useEffect(() => {
    if (visible) {
      if (current) {
        form.setFieldsValue({ ...current });
      } else {
        form.resetFields();
        form.setFieldsValue({ importance: '一般', lifecycleStage: '新获取' });
      }
    }
  }, [visible, current, form]);

  const handleOk = async () => {
    const values = await form.validateFields();
    const payload: API.Crm.Customer = {
      ...current,
      ...values,
    };
    const ok = await onSubmit(payload);
    if (ok) {
      form.resetFields();
    }
  };

  return (
    <Modal
      title={isEdit ? '编辑客户' : '创建客户'}
      open={visible}
      onCancel={onCancel}
      onOk={handleOk}
      width={640}
      destroyOnClose
    >
      <Form form={form} layout="vertical">
        <Form.Item
          name="name"
          label="客户名称"
          rules={[{ required: true, message: '请输入客户名称' }]}
        >
          <Input placeholder="请输入客户名称" maxLength={128} />
        </Form.Item>
        <Form.Item name="importance" label="重要程度" rules={[{ required: true }]}>
          <Select options={importanceOptions} />
        </Form.Item>
        <Form.Item name="lifecycleStage" label="生命周期阶段">
          <Select options={stageOptions} disabled={isEdit} />
        </Form.Item>
        <Form.Item name="source" label="客户来源" rules={[{ required: true, message: '请输入客户来源' }]}>
          <Input placeholder="如：展会、转介绍、官网" maxLength={32} />
        </Form.Item>
        <Form.Item name="industry" label="行业" rules={[{ required: true, message: '请输入行业' }]}>
          <Input placeholder="请输入行业" maxLength={32} />
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
          <Input placeholder="详细地址" maxLength={255} />
        </Form.Item>
        <Form.Item name="remark" label="备注">
          <Input.TextArea rows={3} maxLength={500} placeholder="备注" />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default CustomerForm;
