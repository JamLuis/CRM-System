import React, { useRef, useState } from 'react';
import { history, useAccess } from '@umijs/max';
import { Button, Dropdown, message, Tag, Upload } from 'antd';
import { ActionType, PageContainer, ProColumns, ProTable } from '@ant-design/pro-components';
import { DownOutlined, ExportOutlined, ImportOutlined, PlusOutlined } from '@ant-design/icons';
import {
  addCustomer,
  archiveCustomer,
  getCustomerList,
  invalidateCustomer,
  pauseCustomer,
  restoreArchivedCustomer,
  restoreInvalidCustomer,
  resumeCustomer,
  updateCustomer,
} from '@/services/crm/customer';
import { submitExport, uploadImport } from '@/services/crm/datajob';
import {
  FOLLOW_UP_STATUS_ENUM,
  IMPORTANCE_ENUM,
  LIFECYCLE_STAGE_ENUM,
  OPERATING_STATUS_ENUM,
} from '../constants';
import CustomerForm from './components/CustomerForm';
import StatusCommandModal, {
  StatusCommandKey,
  StatusCommandResult,
} from './components/StatusCommandModal';

/** 根据经营状态计算可执行的状态命令 */
function getStatusCommands(record: API.Crm.Customer): { key: StatusCommandKey; label: string }[] {
  switch (record.operatingStatus) {
    case '正常':
      return [
        { key: 'pause', label: '暂停跟进' },
        { key: 'invalidate', label: '设为已失效' },
        { key: 'archive', label: '归档' },
      ];
    case '暂停跟进':
      return [
        { key: 'resume', label: '恢复跟进' },
        { key: 'invalidate', label: '设为已失效' },
        { key: 'archive', label: '归档' },
      ];
    case '已失效':
      return [{ key: 'restoreInvalid', label: '恢复为正常（主管/管理员）' }];
    case '已归档':
      return [{ key: 'restoreArchive', label: '恢复归档（仅管理员）' }];
    default:
      return [];
  }
}

const CustomerList: React.FC = () => {
  const [messageApi, contextHolder] = message.useMessage();
  const actionRef = useRef<ActionType>();
  const access = useAccess();

  const [formVisible, setFormVisible] = useState<boolean>(false);
  const [currentRow, setCurrentRow] = useState<API.Crm.Customer>();

  const [statusCommand, setStatusCommand] = useState<StatusCommandKey>();
  const [statusModalVisible, setStatusModalVisible] = useState<boolean>(false);
  const [statusRow, setStatusRow] = useState<API.Crm.Customer>();

  const canCreate = access.hasPerms('crm:customer:create');
  const canWrite = access.hasPerms('crm:customer:write');
  const canStatus = access.hasPerms('crm:customer:status');
  const canImport = access.hasPerms('crm:customer:import');
  const canExport = access.hasPerms('crm:customer:export');

  /** 最近一次列表查询条件（导出继承页面筛选） */
  const lastQueryRef = useRef<Partial<API.Crm.Customer>>({});
  const [exporting, setExporting] = useState<boolean>(false);

  /** 提交异步导出（继承当前筛选条件） */
  const handleExport = async () => {
    setExporting(true);
    try {
      const resp = await submitExport(lastQueryRef.current);
      if (resp.code === 200) {
        messageApi.success('导出任务已提交，请到「作业中心」查看进度并下载');
      } else {
        messageApi.error(resp.msg || '导出提交失败');
      }
    } finally {
      setExporting(false);
    }
  };

  /** 上传导入文件并预检 */
  const handleImport = async (file: File) => {
    const resp = await uploadImport(file);
    if (resp.code === 200 && resp.data) {
      messageApi.success(
        `预检完成：共 ${resp.data.totalCount ?? 0} 行，请到「作业中心」确认执行`,
      );
    } else {
      messageApi.error(resp.msg || '导入预检失败');
    }
    return false; // 阻止 antd 默认上传
  };

  /** 创建/编辑提交 */
  const handleSubmit = async (values: API.Crm.Customer): Promise<boolean> => {
    const isEdit = !!values.customerId;
    const resp = isEdit ? await updateCustomer(values) : await addCustomer(values);
    if (resp.code === 200) {
      messageApi.success(isEdit ? '编辑成功' : '创建成功');
      setFormVisible(false);
      actionRef.current?.reload();
      return true;
    }
    messageApi.error(resp.msg || '操作失败');
    return false;
  };

  /** 状态命令提交 */
  const handleStatusCommand = async (
    command: StatusCommandKey,
    result: StatusCommandResult,
  ): Promise<boolean> => {
    if (!statusRow?.customerId) return false;
    const id = statusRow.customerId;
    const calls: Record<StatusCommandKey, () => Promise<API.Crm.R<API.Crm.Customer>>> = {
      pause: () => pauseCustomer(id, result.reason, result.plannedResumeAt),
      resume: () => resumeCustomer(id, result.reason),
      invalidate: () => invalidateCustomer(id, result.reason),
      archive: () => archiveCustomer(id, result.reason),
      restoreArchive: () => restoreArchivedCustomer(id, result.reason),
      restoreInvalid: () => restoreInvalidCustomer(id, result.reason),
    };
    const resp = await calls[command]();
    if (resp.code === 200) {
      messageApi.success('操作成功');
      setStatusModalVisible(false);
      actionRef.current?.reload();
      return true;
    }
    messageApi.error(resp.msg || '操作失败');
    return false;
  };

  const columns: ProColumns<API.Crm.Customer>[] = [
    {
      title: '客户名称',
      dataIndex: 'name',
      ellipsis: true,
      render: (_, record) => (
        <a onClick={() => history.push(`/crm/customer/detail/${record.customerId}`)}>
          {record.name}
        </a>
      ),
    },
    { title: '客户编码', dataIndex: 'customerCode', hideInSearch: true, width: 110 },
    {
      title: '经营状态',
      dataIndex: 'operatingStatus',
      valueType: 'select',
      valueEnum: OPERATING_STATUS_ENUM,
      width: 110,
    },
    {
      title: '生命周期阶段',
      dataIndex: 'lifecycleStage',
      valueType: 'select',
      valueEnum: LIFECYCLE_STAGE_ENUM,
      width: 120,
    },
    {
      title: '重要程度',
      dataIndex: 'importance',
      valueType: 'select',
      valueEnum: IMPORTANCE_ENUM,
      width: 100,
    },
    { title: '客户来源', dataIndex: 'source', width: 100, hideInSearch: true },
    { title: '行业', dataIndex: 'industry', width: 100 },
    { title: '主负责人', dataIndex: 'primaryOwnerName', width: 100, hideInSearch: true },
    {
      title: '跟进健康度',
      dataIndex: 'followUpStatus',
      hideInSearch: true,
      width: 100,
      render: (_, record) => {
        const meta = FOLLOW_UP_STATUS_ENUM[record.followUpStatus || ''];
        return meta ? <Tag color={meta.color}>{meta.text}</Tag> : '-';
      },
    },
    {
      title: '下次跟进',
      dataIndex: 'nextFollowUpAt',
      valueType: 'dateTime',
      hideInSearch: true,
      width: 160,
    },
    {
      title: '最近有效跟进',
      dataIndex: 'lastEffectiveFollowUpAt',
      valueType: 'dateTime',
      hideInSearch: true,
      width: 160,
    },
    {
      title: '操作',
      dataIndex: 'option',
      valueType: 'option',
      width: 160,
      render: (_, record) => {
        const commands = getStatusCommands(record);
        return [
          <a
            key="detail"
            onClick={() => history.push(`/crm/customer/detail/${record.customerId}`)}
          >
            详情
          </a>,
          canWrite && record.operatingStatus !== '已归档' && (
            <a
              key="edit"
              onClick={() => {
                setCurrentRow(record);
                setFormVisible(true);
              }}
            >
              编辑
            </a>
          ),
          canStatus && commands.length > 0 && (
            <Dropdown
              key="status"
              menu={{
                items: commands.map((c) => ({ key: c.key, label: c.label })),
                onClick: ({ key }) => {
                  setStatusRow(record);
                  setStatusCommand(key as StatusCommandKey);
                  setStatusModalVisible(true);
                },
              }}
            >
              <a onClick={(e) => e.preventDefault()}>
                状态 <DownOutlined />
              </a>
            </Dropdown>
          ),
        ];
      },
    },
  ];

  return (
    <PageContainer>
      {contextHolder}
      <ProTable<API.Crm.Customer>
        headerTitle="客户列表"
        actionRef={actionRef}
        rowKey="customerId"
        search={{ labelWidth: 100 }}
        toolBarRender={() => [
          canImport && (
            <Upload
              key="import"
              accept=".xlsx,.xls"
              showUploadList={false}
              beforeUpload={(file) => handleImport(file)}
            >
              <Button icon={<ImportOutlined />}>导入客户</Button>
            </Upload>
          ),
          canExport && (
            <Button
              key="export"
              icon={<ExportOutlined />}
              loading={exporting}
              onClick={handleExport}
            >
              导出客户
            </Button>
          ),
          canCreate && (
            <Button
              type="primary"
              key="add"
              icon={<PlusOutlined />}
              onClick={() => {
                setCurrentRow(undefined);
                setFormVisible(true);
              }}
            >
              新建客户
            </Button>
          ),
        ]}
        request={async (params) => {
          const query = {
            name: params.name,
            operatingStatus: params.operatingStatus,
            lifecycleStage: params.lifecycleStage,
            importance: params.importance,
            source: params.source,
            industry: params.industry,
          };
          lastQueryRef.current = query;
          const resp = await getCustomerList(query);
          if (resp.code === 200) {
            // 后端按数据范围返回全量列表，前端分页
            return { data: resp.data || [], success: true, total: resp.data?.length || 0 };
          }
          messageApi.error(resp.msg || '查询失败');
          return { data: [], success: false, total: 0 };
        }}
        columns={columns}
        pagination={{ defaultPageSize: 10, showSizeChanger: true }}
      />
      <CustomerForm
        current={currentRow}
        visible={formVisible}
        onCancel={() => setFormVisible(false)}
        onSubmit={handleSubmit}
      />
      <StatusCommandModal
        command={statusCommand}
        customerName={statusRow?.name}
        visible={statusModalVisible}
        onCancel={() => setStatusModalVisible(false)}
        onSubmit={handleStatusCommand}
      />
    </PageContainer>
  );
};

export default CustomerList;
