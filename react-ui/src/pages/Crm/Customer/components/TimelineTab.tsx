import React, { useEffect, useState } from 'react';
import { Empty, message, Spin, Timeline } from 'antd';
import { getCustomerTimeline } from '@/services/crm/admin';
import { TIMELINE_EVENT_TYPE_ENUM } from '../../constants';

export type TimelineTabProps = {
  customerId: number;
};

/** 客户动态时间线（只读） */
const TimelineTab: React.FC<TimelineTabProps> = ({ customerId }) => {
  const [messageApi, contextHolder] = message.useMessage();
  const [events, setEvents] = useState<API.Crm.CustomerTimeline[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const resp = await getCustomerTimeline(customerId);
        if (resp.code === 200) setEvents(resp.data || []);
        else messageApi.error(resp.msg || '查询失败');
      } finally {
        setLoading(false);
      }
    };
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [customerId]);

  if (loading) return <Spin />;
  if (events.length === 0) return <Empty description="暂无动态" />;

  return (
    <>
      {contextHolder}
      <Timeline
        items={events.map((e) => {
          let detail = '';
          try {
            const data = e.eventData ? JSON.parse(e.eventData) : null;
            if (data && typeof data === 'object') {
              detail = Object.entries(data)
                .map(([k, v]) => `${k}: ${v}`)
                .join('，');
            }
          } catch {
            detail = e.eventData || '';
          }
          return {
            children: (
              <div>
                <div style={{ fontWeight: 500 }}>
                  {TIMELINE_EVENT_TYPE_ENUM[e.eventType || '']?.text || e.eventType}
                </div>
                {detail && <div style={{ color: '#666', fontSize: 12 }}>{detail}</div>}
                <div style={{ color: '#999', fontSize: 12 }}>
                  {e.operatorName || '系统'} · {e.createTime}
                </div>
              </div>
            ),
          };
        })}
      />
    </>
  );
};

export default TimelineTab;
