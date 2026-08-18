import React, { useEffect } from 'react';
import { DatePicker, Form, Input, Modal } from 'antd';
import dayjs from 'dayjs';

export type StatusCommandKey =
  | 'pause'
  | 'resume'
  | 'invalidate'
  | 'archive'
  | 'restoreArchive'
  | 'restoreInvalid';

const COMMAND_META: Record<StatusCommandKey, { title: string; needReason: boolean; needResumeAt: boolean }> = {
  pause: { title: '暂停跟进', needReason: true, needResumeAt: false },
  resume: { title: '恢复跟进', needReason: true, needResumeAt: false },
  invalidate: { title: '设为已失效', needReason: true, needResumeAt: false },
  archive: { title: '归档客户', needReason: true, needResumeAt: false },
  restoreArchive: { title: '恢复归档客户', needReason: true, needResumeAt: false },
  restoreInvalid: { title: '恢复失效客户', needReason: true, needResumeAt: false },
};

export type StatusCommandResult = {
  reason?: string;
  plannedResumeAt?: string;
};

export type StatusCommandModalProps = {
  command?: StatusCommandKey;
  customerName?: string;
  visible: boolean;
  onCancel: () => void;
  onSubmit: (command: StatusCommandKey, result: StatusCommandResult) => Promise<boolean>;
};

/** 客户状态命令弹窗（暂停需填写原因，可选计划恢复时间） */
const StatusCommandModal: React.FC<StatusCommandModalProps> = (props) => {
  const { command, customerName, visible, onCancel, onSubmit } = props;
  const [form] = Form.useForm();

  useEffect(() => {
    if (visible) {
      form.resetFields();
    }
  }, [visible, form]);

  if (!command) {
    return null;
  }
  const meta = COMMAND_META[command];

  const handleOk = async () => {
    const values = await form.validateFields();
    const result: StatusCommandResult = {
      reason: values.reason,
      plannedResumeAt: values.plannedResumeAt
        ? dayjs(values.plannedResumeAt).format('YYYY-MM-DD HH:mm:ss')
        : undefined,
    };
    const ok = await onSubmit(command, result);
    if (ok) {
      form.resetFields();
    }
  };

  return (
    <Modal
      title={`${meta.title} - ${customerName || ''}`}
      open={visible}
      onCancel={onCancel}
      onOk={handleOk}
      width={480}
      destroyOnClose
    >
      <Form form={form} layout="vertical">
        {meta.needReason && (
          <Form.Item
            name="reason"
            label="原因"
            rules={[{ required: true, message: '请填写原因' }]}
          >
            <Input.TextArea rows={3} maxLength={255} placeholder="请填写原因" />
          </Form.Item>
        )}
        {command === 'pause' && (
          <Form.Item name="plannedResumeAt" label="计划恢复时间（可选）">
            <DatePicker showTime style={{ width: '100%' }} placeholder="选择计划恢复时间" />
          </Form.Item>
        )}
      </Form>
    </Modal>
  );
};

export default StatusCommandModal;
