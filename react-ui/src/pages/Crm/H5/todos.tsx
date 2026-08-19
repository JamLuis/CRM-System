import React, { useCallback, useEffect, useState } from 'react';
import { history } from '@umijs/max';
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
      const resp = await completeMyTodo(item.deliveryId as number, { skipErrorHandler: true });
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

  return (
    <H5Layout
      state={state}
      errorMsg={errorMsg}
      onReLogin={reLogin}
      onGotoPcLogin={gotoPcLogin}
      title={`我的待办${currentUser?.nickName ? ` · ${currentUser.nickName}` : ''}`}
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
