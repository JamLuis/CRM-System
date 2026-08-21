import { clearSessionToken } from '@/access';
import { logout } from '@/services/system/auth';
import { history } from '@umijs/max';
import { Avatar, Button, Dropdown, Layout, Menu, Space, Typography } from 'antd';
import {
  AppstoreOutlined,
  BellOutlined,
  LogoutOutlined,
  UserOutlined,
} from '@ant-design/icons';
import React from 'react';

export type H5PcShellProps = {
  /** 当前激活的底部导航 key：/crm/h5/customers | /crm/h5/todos */
  activeKey: string;
  currentUser?: API.CurrentUser;
  title?: string;
  /** 右上角操作区 */
  extra?: React.ReactNode;
  children?: React.ReactNode;
};

/**
 * H5 路由在桌面/Pad 打开时的外壳：
 * 提供与 PC 端一致的顶栏 + 导航 + 内容卡片布局，功能不阉割。
 */
const H5PcShell: React.FC<H5PcShellProps> = ({ activeKey, currentUser, title, extra, children }) => {
  return (
    <Layout style={{ minHeight: '100vh', background: '#f2f4f8' }}>
      <Layout.Header
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: 24,
          padding: '0 24px',
          background: '#fff',
          borderBottom: '1px solid #eef1f6',
          position: 'sticky',
          top: 0,
          zIndex: 20,
        }}
      >
        <Space
          size={8}
          style={{ cursor: 'pointer' }}
          onClick={() => history.push('/crm/h5/customers')}
        >
          <img src="/logo.svg" alt="logo" style={{ width: 28, height: 28 }} />
          <Typography.Text strong style={{ fontSize: 16 }}>
            CRM 管理系统
          </Typography.Text>
        </Space>
        <Menu
          mode="horizontal"
          selectedKeys={[activeKey]}
          style={{ flex: 1, minWidth: 0, borderBottom: 'none' }}
          items={[
            { key: '/crm/h5/customers', icon: <AppstoreOutlined />, label: '我的客户' },
            { key: '/crm/h5/todos', icon: <BellOutlined />, label: '我的待办' },
          ]}
          onClick={({ key }) => history.push(key)}
        />
        <Space size={16}>
          {extra}
          <Button size="small" onClick={() => history.push('/crm/customer')}>
            进入 PC 完整版
          </Button>
          <Dropdown
            menu={{
              items: [
                {
                  key: 'pc',
                  icon: <UserOutlined />,
                  label: '进入 PC 完整版',
                  onClick: () => history.push('/crm/customer'),
                },
                {
                  key: 'logout',
                  icon: <LogoutOutlined />,
                  label: '退出登录',
                  onClick: async () => {
                    try {
                      await logout();
                    } catch {
                      // 忽略登出接口异常，本地会话仍要清理
                    }
                    clearSessionToken();
                    history.push('/user/login');
                  },
                },
              ],
            }}
          >
            <Space style={{ cursor: 'pointer' }}>
              <Avatar size="small" src={currentUser?.avatar} icon={<UserOutlined />} />
              <Typography.Text>{currentUser?.nickName || currentUser?.userName}</Typography.Text>
            </Space>
          </Dropdown>
        </Space>
      </Layout.Header>
      <Layout.Content style={{ padding: 24, maxWidth: 1280, width: '100%', margin: '0 auto' }}>
        {title && (
          <Typography.Title level={4} style={{ marginBottom: 16 }}>
            {title}
          </Typography.Title>
        )}
        {children}
      </Layout.Content>
    </Layout>
  );
};

export default H5PcShell;
