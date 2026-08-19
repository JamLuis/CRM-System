import React, { useCallback, useEffect, useState } from 'react';
import { history, useParams } from '@umijs/max';
import { Button, Card, Empty, List, PullToRefresh, Space, Tag, Toast } from 'antd-mobile';
import { getCustomer } from '@/services/crm/customer';
import { getContactsByCustomer } from '@/services/crm/contact';
import { getFollowUpsByCustomer } from '@/services/crm/followup';
import {
  FOLLOW_UP_METHOD_ENUM,
  FOLLOW_UP_STATUS_ENUM,
  LIFECYCLE_STAGE_ENUM,
  OPERATING_STATUS_ENUM,
} from '../constants';
import H5Layout from './H5Layout';
import { useH5Auth } from './useH5Auth';

/**
 * H5 客户详情：概览 + 联系人 + 最近跟踪，提供「快速跟踪」入口。
 */
const H5CustomerDetail: React.FC = () => {
  const { state, errorMsg, reLogin, gotoPcLogin } = useH5Auth();
  const params = useParams<{ id: string }>();
  const customerId = Number(params.id);

  const [customer, setCustomer] = useState<API.Crm.Customer>();
  const [contacts, setContacts] = useState<API.Crm.Contact[]>([]);
  const [followUps, setFollowUps] = useState<API.Crm.FollowUp[]>([]);

  const loadData = useCallback(async () => {
    if (!customerId) return;
    try {
      const [cResp, ctResp, fuResp] = await Promise.all([
        getCustomer(customerId, { skipErrorHandler: true }),
        getContactsByCustomer(customerId, { skipErrorHandler: true }),
        getFollowUpsByCustomer(customerId, { skipErrorHandler: true }),
      ]);
      if (cResp.code === 200) {
        setCustomer(cResp.data);
      } else {
        Toast.show({ content: cResp.msg || '加载客户失败' });
      }
      if (ctResp.code === 200) setContacts(ctResp.data ?? []);
      if (fuResp.code === 200) setFollowUps(fuResp.data ?? []);
    } catch (e) {
      console.error('load customer detail failed', e);
      Toast.show({ content: '加载失败，请重试' });
    }
  }, [customerId]);

  useEffect(() => {
    if (state === 'ready') {
      loadData();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [state, customerId]);

  return (
    <H5Layout
      state={state}
      errorMsg={errorMsg}
      onReLogin={reLogin}
      onGotoPcLogin={gotoPcLogin}
      title="客户详情"
    >
      <PullToRefresh onRefresh={loadData}>
        <div style={{ padding: 12 }}>
          {/* 概览 */}
          <Card title={customer?.name || '加载中…'} style={{ marginBottom: 12 }}>
            <Space wrap style={{ marginBottom: 8 }}>
              {customer?.lifecycleStage && (
                <Tag color="primary" fill="outline">
                  {LIFECYCLE_STAGE_ENUM[customer.lifecycleStage]?.text ?? customer.lifecycleStage}
                </Tag>
              )}
              {customer?.operatingStatus && (
                <Tag color={customer.operatingStatus === '正常' ? 'success' : 'warning'}>
                  {OPERATING_STATUS_ENUM[customer.operatingStatus]?.text ?? customer.operatingStatus}
                </Tag>
              )}
              {customer?.followUpStatus && FOLLOW_UP_STATUS_ENUM[customer.followUpStatus] && (
                <Tag color={FOLLOW_UP_STATUS_ENUM[customer.followUpStatus].color}>
                  {FOLLOW_UP_STATUS_ENUM[customer.followUpStatus].text}
                </Tag>
              )}
              {customer?.importance && <Tag color="warning">{customer.importance}</Tag>}
            </Space>
            <div style={{ fontSize: 13, color: '#333' }}>
              {[
                { label: '主负责人', value: customer?.primaryOwnerName || '-' },
                {
                  label: '最近有效跟踪',
                  value: customer?.lastEffectiveFollowUpAt?.slice(0, 16) || '-',
                },
                { label: '下次跟踪', value: customer?.nextFollowUpAt?.slice(0, 16) || '-' },
                { label: '来源', value: customer?.source || '-' },
                { label: '行业', value: customer?.industry || '-' },
                {
                  label: '地址',
                  value:
                    [
                      customer?.addressProvince,
                      customer?.addressCity,
                      customer?.addressDistrict,
                      customer?.addressDetail,
                    ]
                      .filter(Boolean)
                      .join(' ') || '-',
                },
              ].map((row) => (
                <div key={row.label} style={{ display: 'flex', padding: '4px 0' }}>
                  <span style={{ width: 90, color: '#999', flexShrink: 0 }}>{row.label}</span>
                  <span style={{ flex: 1 }}>{row.value}</span>
                </div>
              ))}
            </div>
            <Button
              block
              color="primary"
              style={{ marginTop: 12 }}
              onClick={() => history.push(`/crm/h5/customer/${customerId}/followup`)}
            >
              快速跟踪
            </Button>
          </Card>

          {/* 联系人 */}
          <Card title={`联系人（${contacts.length}）`} style={{ marginBottom: 12 }}>
            {contacts.length === 0 ? (
              <Empty description="暂无联系人" imageStyle={{ width: 60 }} />
            ) : (
              <List>
                {contacts.map((c) => (
                  <List.Item
                    key={c.contactId}
                    description={
                      <Space direction="vertical" style={{ fontSize: 12, color: '#666' }}>
                        <span>
                          {c.phoneMasked || c.phoneNumber || '无电话'}
                          {c.title ? ` · ${c.title}` : ''}
                        </span>
                        {c.wechatMasked && <span>微信：{c.wechatMasked}</span>}
                      </Space>
                    }
                    extra={
                      c.isDecisionMaker ? <Tag color="danger">决策人</Tag> : undefined
                    }
                  >
                    {c.name}
                  </List.Item>
                ))}
              </List>
            )}
          </Card>

          {/* 最近跟踪 */}
          <Card title={`跟踪记录（${followUps.length}）`}>
            {followUps.length === 0 ? (
              <Empty description="暂无跟踪记录" imageStyle={{ width: 60 }} />
            ) : (
              <List>
                {followUps.slice(0, 10).map((f) => (
                  <List.Item
                    key={f.followUpId}
                    description={
                      <Space direction="vertical" style={{ fontSize: 12, color: '#666' }}>
                        {f.content && <span>{f.content}</span>}
                        {f.outcome && <span>结果：{f.outcome}</span>}
                      </Space>
                    }
                    extra={
                      f.isVoided ? (
                        <Tag color="default">已作废</Tag>
                      ) : (
                        <span style={{ fontSize: 12, color: '#999' }}>
                          {f.followUpAt?.slice(5, 16)}
                        </span>
                      )
                    }
                  >
                    <Space>
                      <span>{FOLLOW_UP_METHOD_ENUM[f.method || '']?.text ?? f.method}</span>
                      {f.isCorrected && <Tag color="warning">已更正</Tag>}
                    </Space>
                  </List.Item>
                ))}
              </List>
            )}
          </Card>

          <div style={{ padding: '16px 0' }}>
            <Button block fill="outline" onClick={() => history.push('/crm/h5/customers')}>
              返回我的客户
            </Button>
          </div>
        </div>
      </PullToRefresh>
    </H5Layout>
  );
};

export default H5CustomerDetail;
