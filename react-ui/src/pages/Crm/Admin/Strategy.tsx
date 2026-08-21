import {
  getActiveStrategy,
  getStrategies,
  recalculateFollowUpStatusBatch,
  saveFollowUpStatusStrategy,
} from '@/services/crm/health';
import { PlusOutlined, ThunderboltOutlined } from '@ant-design/icons';
import { ActionType, PageContainer, ProColumns, ProTable } from '@ant-design/pro-components';
import { Button, Card, Form, InputNumber, message, Modal, Space, Tag } from 'antd';
import React, { useEffect, useRef, useState } from 'react';
import { CRM_HORIZONTAL_FORM_PROPS } from '../components/formLayout';

/** 跟进健康度策略管理 */
const Strategy: React.FC = () => {
  const [messageApi, contextHolder] = message.useMessage();
  const actionRef = useRef<ActionType>();
  const [active, setActive] = useState<API.Crm.FollowUpStatusStrategy>();
  const [formVisible, setFormVisible] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm();

  const loadActive = async () => {
    const resp = await getActiveStrategy();
    if (resp.code === 200) {
      setActive(resp.data);
    }
  };

  useEffect(() => {
    loadActive();
  }, []);

  const handleSave = async () => {
    const values = await form.validateFields();
    if (values.severeThreshold <= values.insufficientThreshold) {
      messageApi.error('严重不足阈值必须大于跟进不足阈值');
      return;
    }
    setSubmitting(true);
    try {
      const resp = await saveFollowUpStatusStrategy({
        insufficientThreshold: values.insufficientThreshold,
        severeThreshold: values.severeThreshold,
      });
      if (resp.code === 200) {
        messageApi.success('策略已保存并生效');
        setFormVisible(false);
        loadActive();
        actionRef.current?.reload();
      } else {
        messageApi.error(resp.msg || '保存失败');
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleRecalculate = () => {
    Modal.confirm({
      title: '批量重算健康度',
      content: '将对全部客户重新计算跟进健康度，可能耗时较长，确认执行？',
      onOk: async () => {
        const resp = await recalculateFollowUpStatusBatch();
        if (resp.code === 200) {
          messageApi.success(`重算完成，共处理 ${resp.data ?? 0} 个客户`);
        } else {
          messageApi.error(resp.msg || '重算失败');
        }
      },
    });
  };

  const columns: ProColumns<API.Crm.FollowUpStatusStrategy>[] = [
    { title: '策略 ID', dataIndex: 'strategyId', width: 120 },
    { title: '跟进不足阈值（天）', dataIndex: 'insufficientThreshold', width: 160 },
    { title: '严重不足阈值（天）', dataIndex: 'severeThreshold', width: 160 },
    {
      title: '状态',
      dataIndex: 'status',
      width: 120,
      render: (_, record) =>
        record.status === 'ACTIVE' ? (
          <Tag color="success">生效中</Tag>
        ) : (
          <Tag color="default">已替代</Tag>
        ),
    },
    { title: '生效时间', dataIndex: 'effectiveFrom', width: 170 },
    { title: '创建时间', dataIndex: 'createTime', width: 170 },
  ];

  return (
    <PageContainer>
      {contextHolder}
      <Card style={{ marginBottom: 16 }}>
        <Space size="large" wrap>
          <span>
            当前生效策略：
            {active ? (
              <>
                跟进不足 ≥ <b>{active.insufficientThreshold}</b> 天，严重不足 ≥{' '}
                <b>{active.severeThreshold}</b> 天
              </>
            ) : (
              '暂无'
            )}
          </span>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              form.setFieldsValue({
                insufficientThreshold: active?.insufficientThreshold ?? 7,
                severeThreshold: active?.severeThreshold ?? 15,
              });
              setFormVisible(true);
            }}
          >
            新建策略
          </Button>
          <Button icon={<ThunderboltOutlined />} onClick={handleRecalculate}>
            批量重算健康度
          </Button>
        </Space>
      </Card>

      <ProTable<API.Crm.FollowUpStatusStrategy>
        headerTitle="策略历史"
        actionRef={actionRef}
        rowKey="strategyId"
        columns={columns}
        search={false}
        request={async () => {
          const resp = await getStrategies();
          if (resp.code === 200) {
            return { data: resp.data || [], success: true, total: resp.data?.length || 0 };
          }
          messageApi.error(resp.msg || '查询失败');
          return { data: [], success: false, total: 0 };
        }}
      />

      <Modal
        title="新建健康度策略"
        open={formVisible}
        onOk={handleSave}
        onCancel={() => setFormVisible(false)}
        confirmLoading={submitting}
        destroyOnClose
      >
        <Form form={form} {...CRM_HORIZONTAL_FORM_PROPS}>
          <Form.Item
            name="insufficientThreshold"
            label="跟进不足阈值（天）"
            rules={[{ required: true, message: '请输入跟进不足阈值' }]}
            extra="超过该天数未跟踪则标记为「跟进不足」"
          >
            <InputNumber min={1} max={365} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item
            name="severeThreshold"
            label="严重不足阈值（天）"
            rules={[{ required: true, message: '请输入严重不足阈值' }]}
            extra="超过该天数未跟踪则标记为「严重不足」，须大于跟进不足阈值"
          >
            <InputNumber min={1} max={365} style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default Strategy;
