import React from 'react';
import { Card, Descriptions, Tag } from 'antd';
import {
  FOLLOW_UP_STATUS_ENUM,
  IMPORTANCE_ENUM,
  LIFECYCLE_STAGE_ENUM,
  OPERATING_STATUS_ENUM,
} from '../../constants';

export type OverviewTabProps = {
  customer?: API.Crm.Customer;
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

  return (
    <Card>
      <Descriptions column={2} bordered size="small">
        <Descriptions.Item label="客户名称">{customer.name}</Descriptions.Item>
        <Descriptions.Item label="客户编码">{customer.customerCode}</Descriptions.Item>
        <Descriptions.Item label="经营状态">
          {OPERATING_STATUS_ENUM[customer.operatingStatus || '']?.text || customer.operatingStatus}
        </Descriptions.Item>
        <Descriptions.Item label="生命周期阶段">
          {LIFECYCLE_STAGE_ENUM[customer.lifecycleStage || '']?.text || customer.lifecycleStage}
        </Descriptions.Item>
        <Descriptions.Item label="重要程度">
          {IMPORTANCE_ENUM[customer.importance || '']?.text || customer.importance}
        </Descriptions.Item>
        <Descriptions.Item label="跟进健康度">
          {followUpMeta ? <Tag color={followUpMeta.color}>{followUpMeta.text}</Tag> : '-'}
        </Descriptions.Item>
        <Descriptions.Item label="客户来源">{customer.source || '-'}</Descriptions.Item>
        <Descriptions.Item label="行业">{customer.industry || '-'}</Descriptions.Item>
        <Descriptions.Item label="主负责人">{customer.primaryOwnerName || '-'}</Descriptions.Item>
        <Descriptions.Item label="归属部门">{customer.ownerDeptId || '-'}</Descriptions.Item>
        <Descriptions.Item label="下次跟进时间">{customer.nextFollowUpAt || '-'}</Descriptions.Item>
        <Descriptions.Item label="最近有效跟进">{customer.lastEffectiveFollowUpAt || '-'}</Descriptions.Item>
        <Descriptions.Item label="地址" span={2}>
          {address || '-'}
        </Descriptions.Item>
        <Descriptions.Item label="状态变更原因" span={2}>
          {customer.statusChangeReason || '-'}
        </Descriptions.Item>
        <Descriptions.Item label="计划恢复时间">{customer.plannedResumeAt || '-'}</Descriptions.Item>
        <Descriptions.Item label="归档时间">{customer.archivedAt || '-'}</Descriptions.Item>
        <Descriptions.Item label="备注" span={2}>
          {customer.remark || '-'}
        </Descriptions.Item>
      </Descriptions>
    </Card>
  );
};

export default OverviewTab;
