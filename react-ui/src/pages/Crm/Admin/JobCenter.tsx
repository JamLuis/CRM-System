import React, { useEffect, useState } from 'react';
import {
  Button,
  Card,
  Drawer,
  message,
  Modal,
  Popconfirm,
  Space,
  Table,
  Tag,
  Tooltip,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { PageContainer } from '@ant-design/pro-components';
import { DownloadOutlined, EyeOutlined, ReloadOutlined } from '@ant-design/icons';
import {
  confirmImport,
  getDataJob,
  getExportDownloadUrl,
  listDataJobs,
} from '@/services/crm/datajob';

const STATUS_META: Record<string, { color: string; text: string }> = {
  PENDING: { color: 'default', text: '待执行' },
  RUNNING: { color: 'processing', text: '执行中' },
  VALIDATED: { color: 'warning', text: '待确认' },
  SUCCESS: { color: 'success', text: '成功' },
  FAILED: { color: 'error', text: '失败' },
  EXPIRED: { color: 'default', text: '已过期' },
};

const TYPE_META: Record<string, { color: string; text: string }> = {
  IMPORT: { color: 'blue', text: '导入' },
  EXPORT: { color: 'green', text: '导出' },
};

/** 作业中心：导入导出作业列表、逐行结果与下载 */
const JobCenter: React.FC = () => {
  const [messageApi, contextHolder] = message.useMessage();
  const [jobs, setJobs] = useState<API.Crm.CrmDataJob[]>([]);
  const [loading, setLoading] = useState(false);
  const [detail, setDetail] = useState<API.Crm.CrmDataJob>();
  const [rowResults, setRowResults] = useState<API.Crm.ImportRowResult[]>([]);
  const [detailOpen, setDetailOpen] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      const resp = await listDataJobs();
      if (resp.code === 200) {
        setJobs(resp.data || []);
      } else {
        messageApi.error(resp.msg || '查询失败');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    // 有执行中作业时自动刷新
    const timer = setInterval(() => {
      setJobs((prev) => {
        const hasActive = prev.some(
          (j) => j.status === 'PENDING' || j.status === 'RUNNING',
        );
        if (hasActive) {
          load();
        }
        return prev;
      });
    }, 5000);
    return () => clearInterval(timer);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const openDetail = async (job: API.Crm.CrmDataJob) => {
    const resp = await getDataJob(job.jobId as number);
    if (resp.code === 200 && resp.data) {
      setDetail(resp.data);
      try {
        setRowResults(resp.data.rowResults ? JSON.parse(resp.data.rowResults) : []);
      } catch {
        setRowResults([]);
      }
      setDetailOpen(true);
    } else {
      messageApi.error(resp.msg || '查询失败');
    }
  };

  const doConfirmImport = (job: API.Crm.CrmDataJob) => {
    Modal.confirm({
      title: '确认执行导入',
      content: `共 ${job.totalCount ?? 0} 行，将仅执行预检通过的行，确认继续？`,
      onOk: async () => {
        const resp = await confirmImport(job.jobId as number);
        if (resp.code === 200 && resp.data) {
          messageApi.success(
            `导入完成：成功 ${resp.data.successCount ?? 0}，失败 ${resp.data.failedCount ?? 0}`,
          );
          load();
        } else {
          messageApi.error(resp.msg || '执行失败');
        }
      },
    });
  };

  const download = (job: API.Crm.CrmDataJob) => {
    if (job.status === 'EXPIRED') {
      messageApi.warning('导出文件已过期，请重新导出');
      return;
    }
    window.open(getExportDownloadUrl(job.jobId as number), '_blank');
  };

  const columns: ColumnsType<API.Crm.CrmDataJob> = [
    {
      title: '类型',
      dataIndex: 'jobType',
      width: 80,
      render: (v: string) => (
        <Tag color={TYPE_META[v]?.color}>{TYPE_META[v]?.text || v}</Tag>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 90,
      render: (v: string) => (
        <Tag color={STATUS_META[v]?.color}>{STATUS_META[v]?.text || v}</Tag>
      ),
    },
    {
      title: '文件 / 条件',
      dataIndex: 'fileName',
      ellipsis: true,
      render: (v: string, record) =>
        v || (record.jobType === 'EXPORT' ? '按当前筛选条件导出' : '-'),
    },
    {
      title: '总数',
      dataIndex: 'totalCount',
      width: 70,
      render: (v?: number) => (v ?? 0) as React.ReactNode,
    },
    {
      title: '成功',
      dataIndex: 'successCount',
      width: 70,
      render: (v?: number) => (v ?? 0) as React.ReactNode,
    },
    {
      title: '失败',
      dataIndex: 'failedCount',
      width: 70,
      render: (v?: number) => (v ?? 0) as React.ReactNode,
    },
    { title: '操作人', dataIndex: 'operatorName', width: 100 },
    { title: '提交时间', dataIndex: 'createTime', width: 170 },
    {
      title: '下载有效期至',
      dataIndex: 'expireTime',
      width: 170,
      render: (v?: string, record?: API.Crm.CrmDataJob) =>
        record?.jobType === 'EXPORT' ? v || '-' : '-',
    },
    {
      title: '操作',
      key: 'action',
      width: 180,
      render: (_: unknown, record: API.Crm.CrmDataJob) => (
        <Space size="small">
          <Tooltip title="查看详情">
            <Button
              type="link"
              size="small"
              icon={<EyeOutlined />}
              onClick={() => openDetail(record)}
            />
          </Tooltip>
          {record.jobType === 'IMPORT' && record.status === 'VALIDATED' && (
            <Popconfirm
              title="确认执行导入？"
              onConfirm={() => doConfirmImport(record)}
            >
              <Button type="link" size="small">
                确认执行
              </Button>
            </Popconfirm>
          )}
          {record.jobType === 'EXPORT' && record.status === 'SUCCESS' && (
            <Button
              type="link"
              size="small"
              icon={<DownloadOutlined />}
              onClick={() => download(record)}
            >
              下载
            </Button>
          )}
        </Space>
      ),
    },
  ];

  const rowResultColumns: ColumnsType<API.Crm.ImportRowResult> = [
    { title: '行号', dataIndex: 'rowNum', width: 60 },
    { title: '客户名称', dataIndex: 'name', ellipsis: true },
    {
      title: '预检',
      dataIndex: 'valid',
      width: 70,
      render: (v?: boolean) =>
        v ? <Tag color="success">通过</Tag> : <Tag color="error">不通过</Tag>,
    },
    {
      title: '执行结果',
      dataIndex: 'result',
      width: 90,
      render: (v?: string) => {
        if (v === 'SUCCESS') return <Tag color="success">成功</Tag>;
        if (v === 'FAILED') return <Tag color="error">失败</Tag>;
        if (v === 'SKIPPED') return <Tag>跳过</Tag>;
        return <Tag>待执行</Tag>;
      },
    },
    { title: '说明', dataIndex: 'message', ellipsis: true },
  ];

  return (
    <PageContainer>
      {contextHolder}
      <Card
        title="导入导出作业"
        extra={
          <Button icon={<ReloadOutlined />} onClick={load} loading={loading}>
            刷新
          </Button>
        }
      >
        <Table<API.Crm.CrmDataJob>
          rowKey="jobId"
          columns={columns}
          dataSource={jobs}
          loading={loading}
          pagination={{ pageSize: 20, showSizeChanger: false }}
          size="middle"
        />
      </Card>

      <Drawer
        title={`作业详情 #${detail?.jobId ?? ''}`}
        width={720}
        open={detailOpen}
        onClose={() => setDetailOpen(false)}
      >
        {detail && (
          <Space direction="vertical" style={{ width: '100%' }}>
            <p>
              <b>类型：</b>
              {TYPE_META[detail.jobType || '']?.text || detail.jobType}
              <span style={{ marginLeft: 16 }}>
                <b>状态：</b>
                {STATUS_META[detail.status || '']?.text || detail.status}
              </span>
              <span style={{ marginLeft: 16 }}>
                <b>操作人：</b>
                {detail.operatorName || '-'}
              </span>
            </p>
            <p>
              <b>文件：</b>
              {detail.fileName || '-'}
              <span style={{ marginLeft: 16 }}>
                <b>总数/成功/失败：</b>
                {detail.totalCount ?? 0} / {detail.successCount ?? 0} /{' '}
                {detail.failedCount ?? 0}
              </span>
            </p>
            {detail.errorMsg && (
              <p style={{ color: '#ff4d4f' }}>
                <b>错误：</b>
                {detail.errorMsg}
              </p>
            )}
            {detail.jobType === 'IMPORT' && rowResults.length > 0 && (
              <Table<API.Crm.ImportRowResult>
                rowKey="rowNum"
                columns={rowResultColumns}
                dataSource={rowResults}
                pagination={{ pageSize: 10, showSizeChanger: false }}
                size="small"
              />
            )}
          </Space>
        )}
      </Drawer>
    </PageContainer>
  );
};

export default JobCenter;
