import React, { useCallback, useEffect, useState } from 'react';
import { history } from '@umijs/max';
import { Empty as PcEmpty, Button as PcButton, Card, Popconfirm, Space, Table, Tag as PcTag, message } from 'antd';
import { Button, Dialog, Empty, List, PullToRefresh, SwipeAction, Tag, Toast } from 'antd-mobile';
import { completeMyTodo, getMyTodos } from '@/services/crm/mobile';
import H5Layout from './H5Layout';
import H5TabBar from './H5TabBar';
import { useH5Auth } from './useH5Auth';

const DELIVERY_STATUS_TAG: Record<string, { text: string; color: string }> = {
  PENDING: { text: '待处理', color: 'primary' },
  RETRYING: { text: '重试中', color: 'warning' },
  SENT: { text: '已送达', color: 'success' },
};

/** PC 表格状态标签颜色 */
const PC_STATUS_TAG: Record<string, { text: string; color: string }> = {
  PENDING: { text: '待处理', color: 'blue' },
  RETRYING: { text: '重试中', color: 'orange' },
  SENT: { text: '已送达', color: 'green' },
};

/**
 * H5 我的待办：展示当前用户未完成的提醒投递，
 * 支持左滑完成、点击进入客户详情。
 */
const H5Todos: React.FC = () => {
  const { state, currentUser, errorMsg, reLogin, gotoPcLogin } = useH5Auth();
  const [todos, setTodos] = useState<API.Crm.ReminderDelivery[]>([]);
  const [loaded, setLoaded] = useState(false);

  const loadData = useCallback(async () => {
    try {
      const resp = await getMyTodos({ skipErrorHandler: true });
      if (resp.code === 200) {
        setTodos(resp.data ?? []);
      } else {
        Toast.show({ content: resp.msg || '加载待办失败' });
      }
    } catch (e) {
      console.error('load todos failed', e);
      Toast.show({ content: '加载失败，请重试' });
    } finally {
      setLoaded(true);
    }
  }, []);

  useEffect(() => {
    if (state === 'ready') {
      loadData();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [state]);

  const handleComplete = async (item: API.Crm.ReminderDelivery) => {
    const confirmed = await Dialog.confirm({ content: '确认完成该待办？' });
    if (!confirmed) return;
    try {
      const resp = await completeMyTodo(item.deliveryId as API.Crm.Id, { skipErrorHandler: true });
      if (resp.code === 200) {
        Toast.show({ icon: 'success', content: '已完成' });
        loadData();
      } else {
        Toast.show({ content: resp.msg || '操作失败' });
      }
    } catch (e) {
      console.error('complete todo failed', e);
      Toast.show({ content: '操作失败，请重试' });
    }
  };

  /** 桌面端完成待办（Popconfirm 确认） */
  const handleCompletePc = async (item: API.Crm.ReminderDelivery) => {
    try {
      const resp = await completeMyTodo(item.deliveryId as API.Crm.Id, { skipErrorHandler: true });
      if (resp.code === 200) {
        message.success('已完成');
        loadData();
      } else {
        message.error(resp.msg || '操作失败');
      }
    } catch (e) {
      console.error('complete todo failed', e);
      message.error('操作失败，请重试');
    }
  };

  // ---------- 桌面/Pad 端：完整表格 ----------
  const pcContent = (
    <Card>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <span style={{ fontWeight: 600 }}>我的待办</span>
        <PcButton onClick={loadData}>刷新</PcButton>
      </div>
      {todos.length === 0 && loaded ? (
        <PcEmpty description="暂无待办，太棒了" />
      ) : (
        <Table
          rowKey="deliveryId"
          size="middle"
          loading={!loaded}
          dataSource={todos}
          pagination={{ pageSize: 10, showTotal: (t) => `共 ${t} 条` }}
          columns={[
            {
              title: '客户',
              dataIndex: 'customerName',
              ellipsis: true,
              render: (name: string, r) => (
                <a onClick={() => history.push(`/crm/h5/customer/${r.customerId}`)}>
                  {name || '未知客户'}
                </a>
              ),
            },
            {
              title: '计划跟踪时间',
              dataIndex: 'plannedFollowUpAt',
              width: 160,
              render: (v?: string) => (v ? v.slice(0, 16) : '-'),
            },
            {
              title: '状态',
              dataIndex: 'status',
              width: 100,
              render: (v?: string) =>
                v && PC_STATUS_TAG[v] ? <PcTag color={PC_STATUS_TAG[v].color}>{PC_STATUS_TAG[v].text}</PcTag> : '-',
            },
            {
              title: '重试次数',
              dataIndex: 'retryCount',
              width: 90,
              render: (v?: number) => v ?? 0,
            },
            {
              title: '操作',
              width: 140,
              render: (_: unknown, r) => (
                <Space>
                  <a onClick={() => history.push(`/crm/h5/customer/${r.customerId}`)}>详情</a>
                  <Popconfirm title="确认完成该待办？" onConfirm={() => handleCompletePc(r)}>
                    <a>完成</a>
                  </Popconfirm>
                </Space>
              ),
            },
          ]}
        />
      )}
    </Card>
  );

  return (
    <H5Layout
      state={state}
      errorMsg={errorMsg}
      onReLogin={reLogin}
      onGotoPcLogin={gotoPcLogin}
      title={`我的待办${currentUser?.nickName ? ` · ${currentUser.nickName}` : ''}`}
      currentUser={currentUser}
      pcContent={pcContent}
    >
      <PullToRefresh onRefresh={loadData}>
        {todos.length === 0 && loaded ? (
          <div style={{ padding: 48 }}>
            <Empty description="暂无待办，太棒了" />
          </div>
        ) : (
          <List>
            {todos.map((item) => (
              <SwipeAction
                key={item.deliveryId}
                rightActions={[
                  {
                    key: 'complete',
                    text: '完成',
                    color: 'success',
                    onClick: () => handleComplete(item),
                  },
                ]}
              >
                <List.Item
                  onClick={() => history.push(`/crm/h5/customer/${item.customerId}`)}
                  description={
                    <div style={{ fontSize: 12, color: '#666', marginTop: 4 }}>
                      <div>
                        计划跟踪时间：
                        {item.plannedFollowUpAt ? item.plannedFollowUpAt.slice(0, 16) : '-'}
                      </div>
                      {item.retryCount ? <div>已重试 {item.retryCount} 次</div> : null}
                    </div>
                  }
                  extra={
                    item.status && DELIVERY_STATUS_TAG[item.status] ? (
                      <Tag color={DELIVERY_STATUS_TAG[item.status].color}>
                        {DELIVERY_STATUS_TAG[item.status].text}
                      </Tag>
                    ) : undefined
                  }
                >
                  <span style={{ fontWeight: 500 }}>{item.customerName || '未知客户'}</span>
                </List.Item>
              </SwipeAction>
            ))}
          </List>
        )}

        {todos.length > 0 && (
          <div style={{ padding: 16 }}>
            <Button block fill="outline" onClick={loadData}>
              刷新
            </Button>
          </div>
        )}
      </PullToRefresh>

      {/* 底部导航占位 */}
      <div style={{ height: 60 }} />
      <H5TabBar />
    </H5Layout>
  );
};

export default H5Todos;
