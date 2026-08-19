import React from 'react';
import { history, useLocation } from '@umijs/max';
import { TabBar } from 'antd-mobile';
import { AppOutline, UnorderedListOutline } from 'antd-mobile-icons';

/**
 * H5 底部导航：我的客户 / 我的待办
 */
const H5TabBar: React.FC = () => {
  const location = useLocation();
  const activeKey = location.pathname.startsWith('/crm/h5/todos')
    ? '/crm/h5/todos'
    : '/crm/h5/customers';

  return (
    <div
      style={{
        position: 'fixed',
        bottom: 0,
        left: 0,
        right: 0,
        background: '#fff',
        borderTop: '1px solid #f0f0f0',
        zIndex: 100,
        paddingBottom: 'env(safe-area-inset-bottom)',
      }}
    >
      <TabBar activeKey={activeKey} onChange={(key) => history.push(key)}>
        <TabBar.Item key="/crm/h5/customers" icon={<AppOutline />} title="我的客户" />
        <TabBar.Item key="/crm/h5/todos" icon={<UnorderedListOutline />} title="我的待办" />
      </TabBar>
    </div>
  );
};

export default H5TabBar;
