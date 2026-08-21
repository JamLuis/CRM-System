import React, { useCallback, useEffect, useState } from 'react';
import { history } from '@umijs/max';
import { PlusOutlined } from '@ant-design/icons';
import { Button, Card, Input, Space, Table, Tag, message } from 'antd';
import {
  Button as MButton,
  DotLoading,
  InfiniteScroll,
  List,
  PullToRefresh,
  SearchBar,
  Tag as MTag,
} from 'antd-mobile';
import { addCustomer, getCustomerList, updateCustomer } from '@/services/crm/customer';
import { FOLLOW_UP_STATUS_ENUM, LIFECYCLE_STAGE_ENUM } from '../constants';
import CustomerForm from '../Customer/components/CustomerForm';
import H5Layout from './H5Layout';
import H5TabBar from './H5TabBar';
import MobileCustomerForm from './MobileCustomerForm';
import { useH5Auth } from './useH5Auth';
import { useIsMobile } from './useIsMobile';

const PAGE_SIZE = 20;
/** 桌面端一次性拉取（后端按数据范围返回），前端分页 */
const PC_PAGE_SIZE = 500;

const STAGE_TAG_COLOR: Record<string, string> = {
  新获取: 'default',
  待跟进: 'processing',
  初步意向: 'orange',
  商机客户: 'gold',
  成交客户: 'green',
};

/**
 * H5 我的客户：按当前用户数据范围分页查询，支持下拉刷新、滚动加载、关键字搜索。
 * 桌面/Pad 端渲染与 PC 一致的表格视图（新建/编辑/详情功能完整保留），
 * 移动端渲染卡片列表并保留新建/编辑入口（只做展示优化，不做功能阉割）。
 */
const H5Customers: React.FC = () => {
  const { state, currentUser, errorMsg, reLogin, gotoPcLogin } = useH5Auth();
  const isMobile = useIsMobile();

  const [keyword, setKeyword] = useState<string>('');
  const [list, setList] = useState<API.Crm.Customer[]>([]);
  const [hasMore, setHasMore] = useState<boolean>(true);
  const [page, setPage] = useState<number>(1);
  const [loading, setLoading] = useState<boolean>(false);

  // 桌面端：一次性拉全量后前端分页 + 搜索
  const [pcList, setPcList] = useState<API.Crm.Customer[]>([]);
  const [pcKeyword, setPcKeyword] = useState<string>('');
  const [pcLoading, setPcLoading] = useState<boolean>(false);
  const [formVisible, setFormVisible] = useState(false);
  const [editingCustomer, setEditingCustomer] = useState<API.Crm.Customer | undefined>();

  // 移动端新建/编辑
  const [mobileFormVisible, setMobileFormVisible] = useState(false);
  const [mobileEditing, setMobileEditing] = useState<API.Crm.Customer | undefined>();

  const loadData = useCallback(
    async (pageNum: number, reset: boolean) => {
      setLoading(true);
      try {
        const resp = await getCustomerList(
          {
            pageNum,
            pageSize: PAGE_SIZE,
            name: keyword || undefined,
          } as any,
          { skipErrorHandler: true },
        );
        if (resp.code === 200) {
          const rows: API.Crm.Customer[] = (resp as any)?.rows ?? resp.data ?? [];
          const total: number = (resp as any)?.total ?? rows.length;
          setList((prev) => (reset ? rows : [...prev, ...rows]));
          setHasMore(pageNum * PAGE_SIZE < total);
          setPage(pageNum);
        } else {
          setHasMore(false);
        }
      } catch (e) {
        console.error('load customers failed', e);
        setHasMore(false);
      } finally {
        setLoading(false);
      }
    },
    [keyword],
  );

  const loadPcData = useCallback(async () => {
    setPcLoading(true);
    try {
      const resp = await getCustomerList(
        { pageNum: 1, pageSize: PC_PAGE_SIZE } as any,
        { skipErrorHandler: true },
      );
      if (resp.code === 200) {
        const rows: API.Crm.Customer[] = (resp as any)?.rows ?? resp.data ?? [];
        setPcList(rows);
      }
    } catch (e) {
      console.error('load pc customers failed', e);
    } finally {
      setPcLoading(false);
    }
  }, []);

  useEffect(() => {
    if (state === 'ready') {
      if (isMobile) {
        loadData(1, true);
      } else {
        loadPcData();
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [state, isMobile]);

  const onSearch = (value: string) => {
    setKeyword(value);
    setList([]);
    setHasMore(true);
    setPage(1);
    // keyword 变化后重新加载
    setTimeout(() => loadData(1, true), 0);
  };

  const closeForms = () => {
    setFormVisible(false);
    setMobileFormVisible(false);
    setEditingCustomer(undefined);
    setMobileEditing(undefined);
  };

  /** 新建/编辑统一提交（编辑须携带 version 乐观锁） */
  const handleSubmit = async (values: API.Crm.Customer): Promise<boolean> => {
    try {
      if (values.customerId) {
        await updateCustomer(values);
        message.success('已保存');
      } else {
        await addCustomer(values);
        message.success('已创建');
      }
      closeForms();
      if (isMobile) {
        loadData(1, true);
      } else {
        loadPcData();
      }
      return true;
    } catch {
      return false;
    }
  };

  const openCreate = (mobile: boolean) => {
    if (mobile) {
      setMobileEditing(undefined);
      setMobileFormVisible(true);
    } else {
      setEditingCustomer(undefined);
      setFormVisible(true);
    }
  };

  const openEdit = (customer: API.Crm.Customer, mobile: boolean) => {
    if (mobile) {
      setMobileEditing(customer);
      setMobileFormVisible(true);
    } else {
      setEditingCustomer(customer);
      setFormVisible(true);
    }
  };

  // ---------- 桌面/Pad 端：完整表格 ----------
  const kw = pcKeyword.trim().toLowerCase();
  const pcRows = kw
    ? pcList.filter(
        (r) =>
          r.name?.toLowerCase().includes(kw) ||
          (r.primaryOwnerName || r.sourceOwnerName || '').toLowerCase().includes(kw) ||
          (r.lifecycleStage || '').includes(pcKeyword.trim()),
      )
    : pcList;

  const pcContent = (
    <Card>
      <div style={{ display: 'flex', gap: 12, marginBottom: 16, flexWrap: 'wrap' }}>
        <Input.Search
          allowClear
          placeholder="搜索客户名称/负责人/阶段"
          style={{ width: 320 }}
          value={pcKeyword}
          onChange={(e) => setPcKeyword(e.target.value)}
          onSearch={(v) => setPcKeyword(v)}
        />
        <Button type="primary" icon={<PlusOutlined />} onClick={() => openCreate(false)}>
          新建客户
        </Button>
        <Button onClick={loadPcData}>刷新</Button>
      </div>
      <Table
        rowKey="customerId"
        size="middle"
        loading={pcLoading}
        dataSource={pcRows}
        pagination={{ pageSize: 10, showTotal: (t) => `共 ${t} 条` }}
        columns={[
          {
            title: '客户名称',
            dataIndex: 'name',
            ellipsis: true,
            render: (name: string, r) => (
              <a onClick={() => history.push(`/crm/h5/customer/${r.customerId}`)}>{name}</a>
            ),
          },
          {
            title: '生命周期',
            dataIndex: 'lifecycleStage',
            width: 110,
            render: (v?: string) =>
              v ? <Tag color={STAGE_TAG_COLOR[v] ?? 'default'}>{v}</Tag> : '-',
          },
          {
            title: '跟进状态',
            dataIndex: 'followUpStatus',
            width: 100,
            render: (v?: string) =>
              v && FOLLOW_UP_STATUS_ENUM[v] ? (
                <Tag color={FOLLOW_UP_STATUS_ENUM[v].color}>{FOLLOW_UP_STATUS_ENUM[v].text}</Tag>
              ) : (
                '-'
              ),
          },
          { title: '重要程度', dataIndex: 'importance', width: 90, render: (v?: string) => v || '-' },
          {
            title: '负责人',
            width: 100,
            render: (_: unknown, r) => r.primaryOwnerName || r.sourceOwnerName || '-',
          },
          {
            title: '下次跟进',
            dataIndex: 'nextFollowUpAt',
            width: 130,
            render: (v?: string) => (v ? String(v).slice(5, 16) : '-'),
          },
          {
            title: '操作',
            width: 110,
            render: (_: unknown, r) => (
              <Space>
                <a onClick={() => openEdit(r, false)}>编辑</a>
                <a onClick={() => history.push(`/crm/h5/customer/${r.customerId}`)}>详情</a>
              </Space>
            ),
          },
        ]}
      />
      <CustomerForm
        current={editingCustomer}
        visible={formVisible}
        onCancel={closeForms}
        onSubmit={handleSubmit}
      />
    </Card>
  );

  // ---------- 移动端 ----------
  return (
    <H5Layout
      state={state}
      errorMsg={errorMsg}
      onReLogin={reLogin}
      onGotoPcLogin={gotoPcLogin}
      title={`我的客户${currentUser?.nickName ? ` · ${currentUser.nickName}` : ''}`}
      currentUser={currentUser}
      pcContent={pcContent}
    >
      <div style={{ padding: '8px 12px', background: '#fff' }}>
        <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
          <div style={{ flex: 1 }}>
            <SearchBar
              placeholder="搜索客户名称"
              value={keyword}
              onChange={setKeyword}
              onSearch={onSearch}
              onClear={() => onSearch('')}
            />
          </div>
          <MButton color="primary" size="small" onClick={() => openCreate(true)}>
            新建
          </MButton>
        </div>
      </div>

      <PullToRefresh onRefresh={async () => loadData(1, true)}>
        <List>
          {list.map((item) => (
            <List.Item
              key={item.customerId}
              onClick={() => history.push(`/crm/h5/customer/${item.customerId}`)}
              description={
                <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', marginTop: 4 }}>
                  {item.lifecycleStage && (
                    <MTag color="primary" fill="outline">
                      {LIFECYCLE_STAGE_ENUM[item.lifecycleStage]?.text ?? item.lifecycleStage}
                    </MTag>
                  )}
                  {item.followUpStatus && FOLLOW_UP_STATUS_ENUM[item.followUpStatus] && (
                    <MTag color={FOLLOW_UP_STATUS_ENUM[item.followUpStatus].color}>
                      {FOLLOW_UP_STATUS_ENUM[item.followUpStatus].text}
                    </MTag>
                  )}
                  {item.importance && item.importance !== '一般' && (
                    <MTag color="warning">{item.importance}</MTag>
                  )}
                </div>
              }
              extra={
                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 4 }}>
                  {item.nextFollowUpAt && (
                    <span style={{ fontSize: 12, color: '#999' }}>
                      下次跟踪
                      <br />
                      {String(item.nextFollowUpAt).slice(5, 16)}
                    </span>
                  )}
                  <MButton
                    size="mini"
                    fill="outline"
                    color="primary"
                    onClick={(e) => {
                      e.stopPropagation();
                      openEdit(item, true);
                    }}
                  >
                    编辑
                  </MButton>
                </div>
              }
            >
              <span style={{ fontWeight: 500 }}>{item.name}</span>
            </List.Item>
          ))}
        </List>

        {list.length === 0 && !loading && (
          <div style={{ textAlign: 'center', color: '#999', padding: 32 }}>暂无客户数据</div>
        )}

        <InfiniteScroll loadMore={() => loadData(page + 1, false)} hasMore={hasMore}>
          {hasMore ? (
            <span>
              加载中<DotLoading />
            </span>
          ) : (
            list.length > 0 ? '— 已加载全部 —' : ''
          )}
        </InfiniteScroll>
      </PullToRefresh>

      {/* 底部导航占位 */}
      <div style={{ height: 60 }} />
      <H5TabBar />

      <MobileCustomerForm
        visible={mobileFormVisible}
        current={mobileEditing}
        onCancel={closeForms}
        onSubmit={handleSubmit}
      />
    </H5Layout>
  );
};

export default H5Customers;
