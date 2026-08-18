import React, { useEffect, useState } from 'react';
import { useParams } from '@umijs/max';
import { Button, Result, Spin, Tabs, Tag } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import { history } from '@umijs/max';
import { getCustomer } from '@/services/crm/customer';
import { FOLLOW_UP_STATUS_ENUM, OPERATING_STATUS_ENUM } from '../constants';
import OverviewTab from './components/OverviewTab';
import MembersTab from './components/MembersTab';
import ContactsTab from './components/ContactsTab';
import FollowUpsTab from './components/FollowUpsTab';
import RemindersTab from './components/RemindersTab';
import TimelineTab from './components/TimelineTab';

/** 客户 360 详情页 */
const CustomerDetail: React.FC = () => {
  const params = useParams<{ id: string }>();
  const customerId = Number(params.id);

  const [customer, setCustomer] = useState<API.Crm.Customer>();
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string>();

  const loadCustomer = async () => {
    if (!customerId) return;
    setLoading(true);
    setErrorMsg(undefined);
    try {
      const resp = await getCustomer(customerId);
      if (resp.code === 200) {
        setCustomer(resp.data);
      } else {
        setErrorMsg(resp.msg || '查询失败');
      }
    } catch (e: any) {
      setErrorMsg(e?.message || '查询失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCustomer();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [customerId]);

  if (!customerId) {
    return <Result status="404" title="缺少客户 ID" />;
  }

  const statusMeta = customer ? OPERATING_STATUS_ENUM[customer.operatingStatus || ''] : undefined;
  const followUpMeta = customer ? FOLLOW_UP_STATUS_ENUM[customer.followUpStatus || ''] : undefined;

  return (
    <PageContainer
      header={{
        title: (
          <span>
            <Button
              type="text"
              icon={<ArrowLeftOutlined />}
              onClick={() => history.push('/crm/customer')}
            />
            {customer?.name || '客户详情'}
            {statusMeta && (
              <Tag style={{ marginLeft: 8 }} color="blue">
                {statusMeta.text}
              </Tag>
            )}
            {followUpMeta && <Tag color={followUpMeta.color}>{followUpMeta.text}</Tag>}
          </span>
        ),
      }}
    >
      <Spin spinning={loading}>
        {errorMsg ? (
          <Result
            status="error"
            title="无法加载客户"
            subTitle={errorMsg}
            extra={
              <Button type="primary" onClick={loadCustomer}>
                重试
              </Button>
            }
          />
        ) : (
          customer && (
            <Tabs
              defaultActiveKey="overview"
              items={[
                { key: 'overview', label: '概览', children: <OverviewTab customer={customer} /> },
                { key: 'members', label: '成员', children: <MembersTab customerId={customerId} /> },
                { key: 'contacts', label: '联系人', children: <ContactsTab customerId={customerId} /> },
                { key: 'followups', label: '跟踪', children: <FollowUpsTab customerId={customerId} /> },
                { key: 'reminders', label: '计划', children: <RemindersTab customerId={customerId} /> },
                { key: 'timeline', label: '动态', children: <TimelineTab customerId={customerId} /> },
              ]}
            />
          )
        )}
      </Spin>
    </PageContainer>
  );
};

export default CustomerDetail;
