import React, { useEffect, useState } from 'react';
import { Form, Input, Modal, message } from 'antd';
import { mapDingtalkIdentity } from '@/services/crm/admin';
import UserSelect from '../../components/UserSelect';

export type IdentityMapModalProps = {
  visible: boolean;
  /** 预填的钉钉用户 ID（从人员搜索页带入） */
  initialDingtalkUserId?: string;
  /** 预填的系统用户 ID */
  initialSysUserId?: number;
  onClose: () => void;
  onSuccess: () => void;
};

/**
 * 钉钉身份映射授权弹窗
 * <p>
 * 将钉钉用户映射到系统用户，映射后该钉钉用户即可免登访问 CRM H5。
 */
const IdentityMapModal: React.FC<IdentityMapModalProps> = ({
  visible,
  initialDingtalkUserId,
  initialSysUserId,
  onClose,
  onSuccess,
}) => {
  const [form] = Form.useForm();
  const [messageApi, contextHolder] = message.useMessage();
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (visible) {
      form.setFieldsValue({
        dingtalkUserId: initialDingtalkUserId || '',
        sysUserId: initialSysUserId,
        unionId: undefined,
      });
    }
  }, [visible, initialDingtalkUserId, initialSysUserId, form]);

  const handleSubmit = async () => {
    const values = await form.validateFields();
    setSubmitting(true);
    try {
      const resp = await mapDingtalkIdentity({
        dingtalkUserId: values.dingtalkUserId.trim(),
        sysUserId: values.sysUserId,
        unionId: values.unionId?.trim() || undefined,
      });
      if (resp.code === 200) {
        messageApi.success('授权成功');
        onSuccess();
      } else {
        messageApi.error(resp.msg || '授权失败');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal
      title="钉钉身份授权"
      open={visible}
      onOk={handleSubmit}
      onCancel={onClose}
      confirmLoading={submitting}
      destroyOnClose
    >
      {contextHolder}
      <Form form={form} layout="vertical">
        <Form.Item
          name="dingtalkUserId"
          label="钉钉用户 ID"
          rules={[{ required: true, message: '请输入钉钉用户 ID' }]}
        >
          <Input placeholder="钉钉 userid" />
        </Form.Item>
        <Form.Item
          name="sysUserId"
          label="映射到的系统用户"
          rules={[{ required: true, message: '请选择系统用户' }]}
        >
          <UserSelect placeholder="输入姓名/账号搜索系统用户" />
        </Form.Item>
        <Form.Item name="unionId" label="UnionID（可选）">
          <Input placeholder="钉钉 UnionID，可留空" />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default IdentityMapModal;
