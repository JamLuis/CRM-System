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
import { confirmImport, submitExport, uploadImport } from '@/services/crm/datajob';
import { DownOutlined, ExportOutlined, ImportOutlined, PlusOutlined } from '@ant-design/icons';
import { ActionType, PageContainer, ProColumns, ProTable } from '@ant-design/pro-components';
import { history, useAccess } from '@umijs/max';
import { Button, Dropdown, message, Tag } from 'antd';
import React, { useRef, useState } from 'react';
import '../components/CrmPage.less';
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

const displayText = (value?: string | number | null) =>
  value === undefined || value === null || value === '' ? '-' : String(value);

const getCustomerAddress = (record: API.Crm.Customer) =>
  [
    record.addressProvince,
    record.addressCity,
    record.addressDistrict,
    record.addressStreet,
    record.addressDetail,
  ]
    .filter(Boolean)
    .join(' ');

const getCustomerTags = (tags?: string) => {
  if (!tags) return [];
  try {
    const parsed = JSON.parse(tags);
    if (Array.isArray(parsed)) return parsed.map(String).filter(Boolean);
  } catch {
    // 兼容旧数据中的普通分隔文本
  }
  return tags
    .split(/[,，;；、]/)
    .map((tag) => tag.trim())
    .filter(Boolean);
};

/** 列表、统计统一采用导入源客户状态；没有导入值时回退到系统经营状态。 */
const getCustomerDisplayStatus = (record: API.Crm.Customer) =>
  record.sourceCustomerStatus || record.operatingStatus || '未设置';

const getCustomerStatusColor = (status: string) => {
  if (/失效|流失|终止|红灯/.test(status)) return 'error';
  if (/暂停|待跟进|风险|黄灯/.test(status)) return 'warning';
  if (/成交|正常|有效|绿灯/.test(status)) return 'success';
  if (/归档|未设置|无亮灯/.test(status)) return 'default';
  return 'processing';
};

const getImportanceColor = (importance?: string) => {
  if (importance === '非常重要') return 'error';
  if (importance === '重要') return 'warning';
  return 'blue';
};

const CUSTOMER_STATUS_ORDER = [
  '成交客户',
  '正常',
  '待跟进',
  '暂停跟进',
  '已失效',
  '已归档',
  '未设置',
];

const CustomerStatusSummary: React.FC<{
  customers: API.Crm.Customer[];
  activeStatus?: string;
  onSelect: (status?: string) => void;
}> = ({ customers, activeStatus, onSelect }) => {
  const statusCounts = customers.reduce<Record<string, number>>((counts, customer) => {
    const status = getCustomerDisplayStatus(customer);
    counts[status] = (counts[status] || 0) + 1;
    return counts;
  }, {});
  const statusEntries = Object.entries(statusCounts).sort(([left], [right]) => {
    const leftIndex = CUSTOMER_STATUS_ORDER.indexOf(left);
    const rightIndex = CUSTOMER_STATUS_ORDER.indexOf(right);
    if (leftIndex === -1 && rightIndex === -1) return left.localeCompare(right, 'zh-CN');
    if (leftIndex === -1) return 1;
    if (rightIndex === -1) return -1;
    return leftIndex - rightIndex;
  });

  return (
    <div className="crmStatCard" aria-label="客户状态统计">
      <div
        className={`crmStatItem crmStatItemClickable${activeStatus ? '' : ' crmStatItemActive'}`}
        role="button"
        title="点击显示全部客户"
        onClick={() => onSelect(undefined)}
      >
        <span className="crmStatLabel">客户总数</span>
        <strong className="crmStatValue crmStatValuePrimary">{customers.length}</strong>
      </div>
      {statusEntries.map(([status, count]) => (
        <div
          key={status}
          className={`crmStatItem crmStatItemClickable${
            activeStatus === status ? ' crmStatItemActive' : ''
          }`}
          role="button"
          title={`点击筛选「${status}」客户`}
          onClick={() => onSelect(activeStatus === status ? undefined : status)}
        >
          <Tag color={getCustomerStatusColor(status)} style={{ marginInlineEnd: 0 }}>
            {status}
          </Tag>
          <strong className="crmStatValue">{count}</strong>
        </div>
      ))}
    </div>
  );
};

const IMPORT_LABELS: Record<API.Crm.DataImportType, string> = {
  CUSTOMER: '客户',
  CONTACT: '联系人',
  FOLLOW_UP: '跟进记录',
};

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
  const [importing, setImporting] = useState<API.Crm.DataImportType>();

  /** 隐藏文件选择框：一个入口承载三种导入类型 */
  const importInputRef = useRef<HTMLInputElement>(null);
  const importTypeRef = useRef<API.Crm.DataImportType>('CUSTOMER');
  const openImportPicker = (type: API.Crm.DataImportType) => {
    importTypeRef.current = type;
    importInputRef.current?.click();
  };

  /** 统计卡片点击筛选：全量数据前端过滤 */
  const [allCustomers, setAllCustomers] = useState<API.Crm.Customer[]>([]);
  const [statusFilter, setStatusFilter] = useState<string>();
  const statusFilterRef = useRef<string>();
  const handleStatusFilter = (status?: string) => {
    statusFilterRef.current = status;
    setStatusFilter(status);
    actionRef.current?.reload();
  };

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

  /** 上传预检后立即确认入库，并刷新客户列表 */
  const handleImport = async (file: File, importType: API.Crm.DataImportType) => {
    setImporting(importType);
    try {
      const resp = await uploadImport(file, importType, { timeout: 300000 });
      if (resp.code !== 200 || !resp.data?.jobId) {
        messageApi.error(resp.msg || '导入预检失败');
        return false;
      }

      let rowResults: API.Crm.ImportRowResult[] = [];
      try {
        rowResults = resp.data.rowResults ? JSON.parse(resp.data.rowResults) : [];
      } catch {
        rowResults = [];
      }
      const totalCount = resp.data.totalCount ?? rowResults.length;
      const validCount = rowResults.length
        ? rowResults.filter((row) => row.valid).length
        : totalCount;

      if (validCount === 0) {
        messageApi.warning(`预检完成：共 ${totalCount} 行，没有可导入数据，请检查作业明细`);
        return false;
      }

      const confirmed = await confirmImport(resp.data.jobId, { timeout: 300000 });
      if (confirmed.code !== 200 || !confirmed.data) {
        messageApi.error(confirmed.msg || '导入执行失败');
        return false;
      }

      const successCount = confirmed.data.successCount ?? 0;
      const failedCount = confirmed.data.failedCount ?? 0;
      const skippedCount = Math.max(totalCount - successCount - failedCount, 0);
      if (failedCount > 0 || skippedCount > 0) {
        messageApi.warning(
          `导入完成：成功 ${successCount} 行，失败 ${failedCount} 行，跳过 ${skippedCount} 行`,
        );
      } else {
        messageApi.success(`导入完成：成功处理 ${successCount} 条${IMPORT_LABELS[importType]}`);
      }
      actionRef.current?.reload();
    } catch (error: any) {
      messageApi.error(error?.data?.msg || error?.message || '导入失败，请稍后重试');
    } finally {
      setImporting(undefined);
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
      width: 220,
      fixed: 'left',
      render: (_, record) => (
        <a onClick={() => history.push(`/crm/customer/detail/${record.customerId}`)}>
          {record.name}
        </a>
      ),
    },
    {
      title: '客户状态',
      dataIndex: 'operatingStatus',
      valueType: 'select',
      valueEnum: OPERATING_STATUS_ENUM,
      width: 130,
      fixed: 'left',
      render: (_, record) => {
        const status = getCustomerDisplayStatus(record);
        return <Tag color={getCustomerStatusColor(status)}>{status}</Tag>;
      },
    },
    {
      title: '重要程度',
      dataIndex: 'importance',
      valueType: 'select',
      valueEnum: IMPORTANCE_ENUM,
      width: 120,
      fixed: 'left',
      render: (_, record) => (
        <Tag color={getImportanceColor(record.importance)}>{displayText(record.importance)}</Tag>
      ),
    },
    {
      title: '跟进力度',
      dataIndex: 'followUpStatus',
      hideInSearch: true,
      width: 110,
      render: (_, record) => {
        const meta = FOLLOW_UP_STATUS_ENUM[record.followUpStatus || ''];
        if (!meta) return displayText(record.followUpIntensity);
        return <Tag color={meta.color}>{meta.text}</Tag>;
      },
    },
    {
      title: '负责人',
      dataIndex: 'sourceOwnerName',
      hideInSearch: true,
      ellipsis: true,
      width: 180,
      render: (_, record) => displayText(record.sourceOwnerName || record.primaryOwnerName),
    },
    {
      title: '客户标签',
      dataIndex: 'tags',
      hideInSearch: true,
      width: 180,
      render: (_, record) => {
        const tags = getCustomerTags(record.tags);
        return tags.length ? tags.map((tag) => <Tag key={tag}>{tag}</Tag>) : '-';
      },
    },
    {
      title: '跟进记录',
      dataIndex: 'followUpRecordCount',
      hideInSearch: true,
      width: 100,
      render: (_, record) => (
        <a onClick={() => history.push(`/crm/customer/detail/${record.customerId}`)}>
          {record.followUpRecordCount || 0} 条
        </a>
      ),
    },
    {
      title: '客户群',
      dataIndex: 'customerGroup',
      hideInSearch: true,
      ellipsis: true,
      width: 150,
      render: (_, record) => displayText(record.customerGroup),
    },
    {
      title: '客户跟进状态',
      dataIndex: 'lifecycleStage',
      valueType: 'select',
      valueEnum: LIFECYCLE_STAGE_ENUM,
      width: 130,
      render: (_, record) => displayText(record.sourceFollowUpStatus || record.lifecycleStage),
    },
    {
      title: '地址',
      dataIndex: 'addressDetail',
      hideInSearch: true,
      ellipsis: true,
      width: 260,
      render: (_, record) => displayText(getCustomerAddress(record)),
    },
    { title: '客户来源', dataIndex: 'source', width: 160 },
    {
      title: '介绍客户名称',
      dataIndex: 'referredCustomerName',
      hideInSearch: true,
      ellipsis: true,
      width: 180,
      render: (_, record) => displayText(record.referredCustomerName),
    },
    {
      title: '客户来源（其他）',
      dataIndex: 'sourceOther',
      hideInSearch: true,
      ellipsis: true,
      width: 180,
      render: (_, record) => displayText(record.sourceOther),
    },
    { title: '客户行业', dataIndex: 'industry', width: 160 },
    {
      title: '客户行业（其他）',
      dataIndex: 'industryOther',
      hideInSearch: true,
      ellipsis: true,
      width: 180,
      render: (_, record) => displayText(record.industryOther),
    },
    {
      title: '备注',
      dataIndex: 'remark',
      hideInSearch: true,
      ellipsis: true,
      width: 220,
      render: (_, record) => displayText(record.remark),
    },
    {
      title: '创建人',
      dataIndex: 'sourceCreatorName',
      hideInSearch: true,
      width: 130,
      render: (_, record) => displayText(record.sourceCreatorName || record.createBy),
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      valueType: 'dateTime',
      hideInSearch: true,
      width: 170,
    },
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      valueType: 'dateTime',
      hideInSearch: true,
      width: 170,
    },
    {
      title: '掉保时间',
      dataIndex: 'droppedProtectionAt',
      valueType: 'dateTime',
      hideInSearch: true,
      width: 170,
    },
    {
      title: '最近动态时间',
      dataIndex: 'latestActivityAt',
      valueType: 'dateTime',
      hideInSearch: true,
      width: 170,
    },
    {
      title: '最近跟进时间',
      dataIndex: 'lastEffectiveFollowUpAt',
      valueType: 'dateTime',
      hideInSearch: true,
      width: 170,
    },
    {
      title: '最近拜访时间',
      dataIndex: 'latestVisitAt',
      valueType: 'dateTime',
      hideInSearch: true,
      width: 170,
    },
    {
      title: '最近分配时间',
      dataIndex: 'latestAssignmentAt',
      valueType: 'dateTime',
      hideInSearch: true,
      width: 170,
    },
    {
      title: '提醒',
      dataIndex: 'nextFollowUpAt',
      valueType: 'dateTime',
      hideInSearch: true,
      width: 170,
    },
    {
      title: '协同人',
      dataIndex: 'sourceCollaboratorNames',
      hideInSearch: true,
      ellipsis: true,
      width: 220,
      render: (_, record) =>
        displayText(record.sourceCollaboratorNames || record.collaboratorNames),
    },
    {
      title: '操作',
      dataIndex: 'option',
      valueType: 'option',
      width: 160,
      fixed: 'right',
      render: (_, record) => {
        const commands = getStatusCommands(record);
        return [
          <a key="detail" onClick={() => history.push(`/crm/customer/detail/${record.customerId}`)}>
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
            <React.Fragment key="import">
              <input
                ref={importInputRef}
                type="file"
                accept=".xlsx,.xls"
                style={{ display: 'none' }}
                onChange={(e) => {
                  const file = e.target.files?.[0];
                  if (file) handleImport(file, importTypeRef.current);
                  e.target.value = '';
                }}
              />
              <Dropdown
                disabled={!!importing}
                menu={{
                  items: [
                    { key: 'CUSTOMER', label: '导入客户', icon: <ImportOutlined /> },
                    { key: 'CONTACT', label: '导入联系人', icon: <ImportOutlined /> },
                    { key: 'FOLLOW_UP', label: '导入跟进记录', icon: <ImportOutlined /> },
                  ],
                  onClick: ({ key }) => openImportPicker(key as API.Crm.DataImportType),
                }}
              >
                <Button icon={<ImportOutlined />} loading={!!importing}>
                  导入数据 <DownOutlined />
                </Button>
              </Dropdown>
            </React.Fragment>
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
            const all = resp.data || [];
            setAllCustomers(all);
            const filtered = statusFilterRef.current
              ? all.filter((c) => getCustomerDisplayStatus(c) === statusFilterRef.current)
              : all;
            return { data: filtered, success: true, total: filtered.length };
          }
          messageApi.error(resp.msg || '查询失败');
          return { data: [], success: false, total: 0 };
        }}
        tableExtraRender={() => (
          <CustomerStatusSummary
            customers={allCustomers}
            activeStatus={statusFilter}
            onSelect={handleStatusFilter}
          />
        )}
        columns={columns}
        pagination={{ defaultPageSize: 10, showSizeChanger: true }}
        scroll={{ x: 4100 }}
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
