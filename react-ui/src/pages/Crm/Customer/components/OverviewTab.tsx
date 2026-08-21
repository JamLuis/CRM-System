import React from 'react';
import { Card, Tag } from 'antd';
import {
  FOLLOW_UP_STATUS_ENUM,
  IMPORTANCE_ENUM,
  LIFECYCLE_STAGE_ENUM,
  OPERATING_STATUS_ENUM,
} from '../../constants';
import '../../components/CrmPage.less';

export type OverviewTabProps = {
  customer?: API.Crm.Customer;
};

type InfoItem = {
  label: string;
  value?: React.ReactNode;
  wide?: boolean;
};

/** 客户概览 */
const OverviewTab: React.FC<OverviewTabProps> = ({ customer }) => {
  if (!customer) return null;
  const followUpMeta = FOLLOW_UP_STATUS_ENUM[customer.followUpStatus || ''];

  const address = [
    customer.addressProvince,
    customer.addressCity,
    customer.addressDistrict,
    customer.addressStreet,
    customer.addressDetail,
  ]
    .filter(Boolean)
    .join(' ');

  const items: InfoItem[] = [
    { label: '客户名称', value: customer.name },
    { label: '客户编码', value: customer.customerCode },
    { label: '客户状态', value: customer.sourceCustomerStatus },
    {
      label: '客户跟进状态',
      value: customer.sourceFollowUpStatus || customer.lifecycleStage,
    },
    {
      label: 'CRM 经营状态',
      value:
        OPERATING_STATUS_ENUM[customer.operatingStatus || '']?.text || customer.operatingStatus,
    },
    {
      label: 'CRM 生命周期阶段',
      value: LIFECYCLE_STAGE_ENUM[customer.lifecycleStage || '']?.text || customer.lifecycleStage,
    },
    { label: '跟进力度', value: customer.followUpIntensity },
    { label: '客户群', value: customer.customerGroup },
    {
      label: '重要程度',
      value: IMPORTANCE_ENUM[customer.importance || '']?.text || customer.importance,
    },
    {
      label: '跟进健康度',
      value: followUpMeta ? <Tag color={followUpMeta.color}>{followUpMeta.text}</Tag> : undefined,
    },
    {
      label: '客户来源',
      value: [customer.source, customer.sourceOther].filter(Boolean).join(' / '),
    },
    {
      label: '客户行业',
      value: [customer.industry, customer.industryOther].filter(Boolean).join(' / '),
    },
    { label: '介绍客户名称', value: customer.referredCustomerName },
    { label: '负责人', value: customer.sourceOwnerName || customer.primaryOwnerName },
    { label: '协同人', value: customer.sourceCollaboratorNames || customer.collaboratorNames },
    { label: '归属部门', value: customer.ownerDeptId },
    { label: '下次跟进时间', value: customer.nextFollowUpAt },
    { label: '最近有效跟进', value: customer.lastEffectiveFollowUpAt },
    { label: '创建人', value: customer.sourceCreatorName || customer.createBy },
    { label: '创建时间', value: customer.createTime },
    { label: '更新时间', value: customer.updateTime },
    { label: '掉保时间', value: customer.droppedProtectionAt },
    { label: '计划恢复时间', value: customer.plannedResumeAt },
    { label: '归档时间', value: customer.archivedAt },
    { label: '地址', value: address, wide: true },
    { label: '状态变更原因', value: customer.statusChangeReason, wide: true },
    { label: '备注', value: customer.remark, wide: true },
  ];

  return (
    <Card styles={{ body: { padding: '4px 20px 12px' } }}>
      <div className="crmInfoGrid">
        {items.map((item) => (
          <div key={item.label} className={`crmInfoItem${item.wide ? ' crmInfoItemWide' : ''}`}>
            <span className="crmInfoLabel">{item.label}</span>
            <span className="crmInfoValue">{item.value || '-'}</span>
          </div>
        ))}
      </div>
    </Card>
  );
};

export default OverviewTab;
