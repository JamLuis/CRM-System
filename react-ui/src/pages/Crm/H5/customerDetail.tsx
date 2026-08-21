import React, { useCallback, useEffect, useState } from 'react';
import { history, useParams } from '@umijs/max';
import { Button as PcButton, Space as PcSpace, Tabs as PcTabs, Tag as PcTag, message } from 'antd';
import {
  Button,
  Card,
  Empty,
  Form as MForm,
  Input as MInput,
  List,
  Popup,
  PullToRefresh,
  Selector,
  Space,
  Switch,
  Tag,
  TextArea as MTextArea,
  Toast,
} from 'antd-mobile';
import { getCustomer, updateCustomer } from '@/services/crm/customer';
import { addContact, getContactsByCustomer, updateContact } from '@/services/crm/contact';
import { getFollowUpsByCustomer } from '@/services/crm/followup';
import {
  FOLLOW_UP_METHOD_ENUM,
  FOLLOW_UP_STATUS_ENUM,
  LIFECYCLE_STAGE_ENUM,
  OPERATING_STATUS_ENUM,
  PHONE_TYPE_ENUM,
} from '../constants';
import CustomerForm from '../Customer/components/CustomerForm';
import ContactsTab from '../Customer/components/ContactsTab';
import FollowUpsTab from '../Customer/components/FollowUpsTab';
import MembersTab from '../Customer/components/MembersTab';
import OverviewTab from '../Customer/components/OverviewTab';
import RemindersTab from '../Customer/components/RemindersTab';
import TimelineTab from '../Customer/components/TimelineTab';
import '../components/CrmPage.less';
import H5Layout from './H5Layout';
import MobileCustomerForm from './MobileCustomerForm';
import { useH5Auth } from './useH5Auth';

const phoneTypeOptions = Object.keys(PHONE_TYPE_ENUM).map((k) => ({ label: k, value: k }));

/** 移动端联系人新建/编辑表单（字段与 PC ContactsTab 对齐） */
const MobileContactForm: React.FC<{
  visible: boolean;
  customerId: API.Crm.Id;
  current?: API.Crm.Contact;
  onCancel: () => void;
  onSaved: () => void;
}> = ({ visible, customerId, current, onCancel, onSaved }) => {
  const [form] = MForm.useForm();
  const isEdit = !!current?.contactId;
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (visible) {
      if (current) {
        form.setFieldsValue({
          ...current,
          phoneType: current.phoneType ? [current.phoneType] : [],
          isDecisionMaker: !!current.isDecisionMaker,
        });
      } else {
        form.resetFields();
        form.setFieldsValue({ phoneType: ['手机'], isDecisionMaker: false });
      }
    }
  }, [visible, current, form]);

  const handleOk = async () => {
    const values = await form.validateFields();
    const payload: API.Crm.Contact = {
      ...current,
      ...values,
      customerId,
      phoneType: values.phoneType?.[0],
    };
    setSubmitting(true);
    try {
      if (isEdit) {
        await updateContact(payload);
        Toast.show({ content: '已保存' });
      } else {
        await addContact(payload);
        Toast.show({ content: '已创建' });
      }
      onSaved();
    } catch {
      // 错误提示由全局 request 拦截器处理
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Popup
      visible={visible}
      onMaskClick={onCancel}
      bodyStyle={{
        borderTopLeftRadius: 12,
        borderTopRightRadius: 12,
        maxHeight: '85vh',
        overflowY: 'auto',
      }}
    >
      <div style={{ padding: '12px 16px 24px' }}>
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            marginBottom: 12,
          }}
        >
          <span style={{ fontWeight: 600, fontSize: 16 }}>{isEdit ? '编辑联系人' : '新增联系人'}</span>
          <Space>
            <Button size="small" onClick={onCancel}>
              取消
            </Button>
            <Button size="small" color="primary" loading={submitting} onClick={handleOk}>
              保存
            </Button>
          </Space>
        </div>
        <MForm form={form} layout="vertical" footer={null}>
          <MForm.Item
            name="name"
            label="姓名"
            rules={[{ required: true, message: '请输入姓名' }]}
          >
            <MInput placeholder="请输入姓名" maxLength={64} />
          </MForm.Item>
          <MForm.Item name="phoneType" label="电话类型">
            <Selector options={phoneTypeOptions} />
          </MForm.Item>
          <MForm.Item name="countryCode" label="国家码">
            <MInput placeholder="如 +86" maxLength={8} />
          </MForm.Item>
          <MForm.Item
            name="phoneNumber"
            label="电话号码"
            rules={[{ required: true, message: '请输入电话号码' }]}
          >
            <MInput placeholder="请输入电话号码" maxLength={32} />
          </MForm.Item>
          <MForm.Item name="email" label="邮箱">
            <MInput placeholder="邮箱" maxLength={128} />
          </MForm.Item>
          <MForm.Item name="wechatId" label="微信号">
            <MInput placeholder="微信号" maxLength={64} />
          </MForm.Item>
          <MForm.Item name="responsibility" label="负责事项">
            <MInput placeholder="负责事项" maxLength={128} />
          </MForm.Item>
          <MForm.Item name="title" label="职务">
            <MInput placeholder="职务" maxLength={64} />
          </MForm.Item>
          <MForm.Item name="isDecisionMaker" label="是否决策人" childElementPosition="right">
            <Switch />
          </MForm.Item>
          <MForm.Item name="remark" label="备注">
            <MTextArea rows={2} maxLength={500} placeholder="备注" />
          </MForm.Item>
        </MForm>
      </div>
    </Popup>
  );
};

/**
 * H5 客户详情：概览 + 联系人 + 最近跟踪，提供「快速跟踪」入口。
 */
const H5CustomerDetail: React.FC = () => {
  const { state, currentUser, errorMsg, reLogin, gotoPcLogin } = useH5Auth();
  const params = useParams<{ id: string }>();
  const customerId = params.id || '';

  const [customer, setCustomer] = useState<API.Crm.Customer>();
  const [contacts, setContacts] = useState<API.Crm.Contact[]>([]);
  const [followUps, setFollowUps] = useState<API.Crm.FollowUp[]>([]);

  // 编辑客户（桌面端复用 PC CustomerForm；移动端底部弹层）
  const [editVisible, setEditVisible] = useState(false);
  const [mobileEditVisible, setMobileEditVisible] = useState(false);
  // 移动端联系人新增/编辑
  const [contactFormVisible, setContactFormVisible] = useState(false);
  const [editingContact, setEditingContact] = useState<API.Crm.Contact | undefined>();

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

  /** 编辑客户提交（携带 version 乐观锁） */
  const handleEditCustomer = async (values: API.Crm.Customer): Promise<boolean> => {
    try {
      await updateCustomer({ ...customer, ...values });
      message.success('已保存');
      setEditVisible(false);
      setMobileEditVisible(false);
      loadData();
      return true;
    } catch {
      return false;
    }
  };

  // ---------- 桌面/Pad 端：复用 PC 详情页全部功能 ----------
  const pcContent = customer && (
    <div>
      <div className="crmDetailHeader" style={{ marginBottom: 16 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', flexWrap: 'wrap', gap: 8 }}>
          <div>
            <div className="crmDetailName">{customer.name}</div>
            <div className="crmDetailMeta">
              主负责人：{customer.primaryOwnerName || customer.sourceOwnerName || '-'}
            </div>
          </div>
          <PcSpace>
            <PcTag color="blue">{customer.lifecycleStage || '-'}</PcTag>
            <PcTag color={customer.operatingStatus === '正常' ? 'green' : 'orange'}>
              {OPERATING_STATUS_ENUM[customer.operatingStatus || '']?.text ??
                customer.operatingStatus ??
                '-'}
            </PcTag>
            <PcTag color="gold">{customer.importance || '-'}</PcTag>
            <PcButton size="small" type="primary" ghost onClick={() => setEditVisible(true)}>
              编辑客户
            </PcButton>
          </PcSpace>
        </div>
      </div>

      <PcTabs
        className="crmDetailTabs"
        defaultActiveKey="overview"
        items={[
          { key: 'overview', label: '概览', children: <OverviewTab customer={customer} /> },
          { key: 'members', label: '成员', children: <MembersTab customerId={customerId} /> },
          { key: 'contacts', label: '联系人', children: <ContactsTab customerId={customerId} /> },
          { key: 'followups', label: '跟踪记录', children: <FollowUpsTab customerId={customerId} /> },
          { key: 'reminders', label: '提醒计划', children: <RemindersTab customerId={customerId} /> },
          { key: 'timeline', label: '时间线', children: <TimelineTab customerId={customerId} /> },
        ]}
      />

      <CustomerForm
        current={customer}
        visible={editVisible}
        onCancel={() => setEditVisible(false)}
        onSubmit={handleEditCustomer}
      />
    </div>
  );

  return (
    <H5Layout
      state={state}
      errorMsg={errorMsg}
      onReLogin={reLogin}
      onGotoPcLogin={gotoPcLogin}
      title="客户详情"
      currentUser={currentUser}
      pcContent={pcContent}
      pcExtra={
        <PcButton
          size="small"
          type="primary"
          ghost
          onClick={() => history.push(`/crm/h5/customer/${customerId}/followup`)}
        >
          快速跟踪
        </PcButton>
      }
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
            <div style={{ display: 'flex', gap: 8, marginTop: 12 }}>
              <Button
                block
                color="primary"
                onClick={() => history.push(`/crm/h5/customer/${customerId}/followup`)}
              >
                快速跟踪
              </Button>
              <Button block fill="outline" onClick={() => setMobileEditVisible(true)}>
                编辑客户
              </Button>
            </div>
          </Card>

          {/* 联系人 */}
          <Card
            title={
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span>{`联系人（${contacts.length}）`}</span>
                <Button
                  size="mini"
                  color="primary"
                  fill="outline"
                  onClick={(e) => {
                    e.stopPropagation();
                    setEditingContact(undefined);
                    setContactFormVisible(true);
                  }}
                >
                  新增
                </Button>
              </div>
            }
            style={{ marginBottom: 12 }}
          >
            {contacts.length === 0 ? (
              <Empty description="暂无联系人" imageStyle={{ width: 60 }} />
            ) : (
              <List>
                {contacts.map((c) => (
                  <List.Item
                    key={c.contactId}
                    onClick={() => {
                      setEditingContact(c);
                      setContactFormVisible(true);
                    }}
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
                      <Space align="center" style={{ gap: 6 }}>
                        {c.status === '已停用' && <Tag color="default">已停用</Tag>}
                        {c.isDecisionMaker && <Tag color="danger">决策人</Tag>}
                        <span style={{ fontSize: 12, color: '#1677ff' }}>编辑</span>
                      </Space>
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

      {/* 移动端编辑客户 */}
      <MobileCustomerForm
        visible={mobileEditVisible}
        current={customer}
        onCancel={() => setMobileEditVisible(false)}
        onSubmit={handleEditCustomer}
      />

      {/* 移动端新增/编辑联系人 */}
      <MobileContactForm
        visible={contactFormVisible}
        customerId={customerId}
        current={editingContact}
        onCancel={() => {
          setContactFormVisible(false);
          setEditingContact(undefined);
        }}
        onSaved={() => {
          setContactFormVisible(false);
          setEditingContact(undefined);
          loadData();
        }}
      />
    </H5Layout>
  );
};

export default H5CustomerDetail;
