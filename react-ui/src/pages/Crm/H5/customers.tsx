import React, { useCallback, useEffect, useState } from 'react';
import { history } from '@umijs/max';
import { InfiniteScroll, List, PullToRefresh, SearchBar, Tag, DotLoading } from 'antd-mobile';
import { getCustomerList } from '@/services/crm/customer';
import { FOLLOW_UP_STATUS_ENUM, LIFECYCLE_STAGE_ENUM } from '../constants';
import H5Layout from './H5Layout';
import H5TabBar from './H5TabBar';
import { useH5Auth } from './useH5Auth';

const PAGE_SIZE = 20;

/**
 * H5 我的客户：按当前用户数据范围分页查询，支持下拉刷新、滚动加载、关键字搜索。
 */
const H5Customers: React.FC = () => {
  const { state, currentUser, errorMsg, reLogin, gotoPcLogin } = useH5Auth();

  const [keyword, setKeyword] = useState<string>('');
  const [list, setList] = useState<API.Crm.Customer[]>([]);
  const [hasMore, setHasMore] = useState<boolean>(true);
  const [page, setPage] = useState<number>(1);
  const [loading, setLoading] = useState<boolean>(false);

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

  useEffect(() => {
    if (state === 'ready') {
      loadData(1, true);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [state]);

  const onSearch = (value: string) => {
    setKeyword(value);
    setList([]);
    setHasMore(true);
    setPage(1);
    // keyword 变化后重新加载
    setTimeout(() => loadData(1, true), 0);
  };

  return (
    <H5Layout
      state={state}
      errorMsg={errorMsg}
      onReLogin={reLogin}
      onGotoPcLogin={gotoPcLogin}
      title={`我的客户${currentUser?.nickName ? ` · ${currentUser.nickName}` : ''}`}
    >
      <div style={{ padding: '8px 12px', background: '#fff' }}>
        <SearchBar
          placeholder="搜索客户名称"
          value={keyword}
          onChange={setKeyword}
          onSearch={onSearch}
          onClear={() => onSearch('')}
        />
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
                    <Tag color="primary" fill="outline">
                      {LIFECYCLE_STAGE_ENUM[item.lifecycleStage]?.text ?? item.lifecycleStage}
                    </Tag>
                  )}
                  {item.followUpStatus && FOLLOW_UP_STATUS_ENUM[item.followUpStatus] && (
                    <Tag color={FOLLOW_UP_STATUS_ENUM[item.followUpStatus].color}>
                      {FOLLOW_UP_STATUS_ENUM[item.followUpStatus].text}
                    </Tag>
                  )}
                  {item.importance && item.importance !== '一般' && (
                    <Tag color="warning">{item.importance}</Tag>
                  )}
                </div>
              }
              extra={
                item.nextFollowUpAt ? (
                  <span style={{ fontSize: 12, color: '#999' }}>
                    下次跟踪
                    <br />
                    {String(item.nextFollowUpAt).slice(5, 16)}
                  </span>
                ) : undefined
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
    </H5Layout>
  );
};

export default H5Customers;
