import React from 'react';
import { useLocation } from '@umijs/max';
import { Button, ErrorBlock, SpinLoading } from 'antd-mobile';
import { Result, Spin } from 'antd';
import type { H5AuthState } from './useH5Auth';
import { useIsMobile } from './useIsMobile';
import H5PcShell from './H5PcShell';

export type H5LayoutProps = {
  state: H5AuthState;
  errorMsg?: string;
  onReLogin?: () => void;
  onGotoPcLogin?: () => void;
  title?: string;
  currentUser?: API.CurrentUser;
  /** 桌面/Pad 端渲染的完整版内容；不传则复用 children */
  pcContent?: React.ReactNode;
  /** 桌面端右上角操作区 */
  pcExtra?: React.ReactNode;
  children?: React.ReactNode;
};

const centerStyle: React.CSSProperties = {
  minHeight: '100vh',
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  justifyContent: 'center',
  background: '#f5f5f5',
  padding: 24,
};

/**
 * H5 页面外壳：统一处理免登加载/待激活/失败状态，
 * ready 后渲染业务内容。
 */
/**
 * H5 页面外壳：统一处理免登加载/待激活/失败状态，ready 后渲染业务内容。
 * 移动端（手机）渲染 antd-mobile 优化版；桌面/Pad 渲染 PC 完整版（功能不阉割）。
 */
const H5Layout: React.FC<H5LayoutProps> = ({
  state,
  errorMsg,
  onReLogin,
  onGotoPcLogin,
  title,
  currentUser,
  pcContent,
  pcExtra,
  children,
}) => {
  const isMobile = useIsMobile();
  const location = useLocation();
  const activeKey = location.pathname.startsWith('/crm/h5/todos')
    ? '/crm/h5/todos'
    : '/crm/h5/customers';

  if (state === 'loading') {
    return (
      <div style={centerStyle}>
        {isMobile ? <SpinLoading color="primary" /> : <Spin size="large" />}
        <div style={{ marginTop: 12, color: '#999' }}>正在登录…</div>
      </div>
    );
  }

  if (state === 'pending-activation') {
    return (
      <div style={centerStyle}>
        {isMobile ? (
          <ErrorBlock
            status="empty"
            title="账号待激活"
            description="您的钉钉身份尚未关联 CRM 账号，请联系管理员完成授权。"
          />
        ) : (
          <Result
            status="403"
            title="账号待激活"
            subTitle="您的钉钉身份尚未关联 CRM 账号，请联系管理员完成授权。"
          />
        )}
        {onGotoPcLogin && (
          <Button
            color="primary"
            fill="outline"
            onClick={onGotoPcLogin}
            style={{ marginTop: 16 }}
          >
            使用账号密码登录
          </Button>
        )}
      </div>
    );
  }

  if (state === 'error') {
    return (
      <div style={centerStyle}>
        {isMobile ? (
          <ErrorBlock status="default" title="登录失败" description={errorMsg || '请稍后重试'} />
        ) : (
          <Result status="error" title="登录失败" subTitle={errorMsg || '请稍后重试'} />
        )}
        <div style={{ display: 'flex', gap: 12, marginTop: 16 }}>
          {onReLogin && (
            <Button color="primary" onClick={onReLogin}>
              重新免登
            </Button>
          )}
          {onGotoPcLogin && (
            <Button fill="outline" onClick={onGotoPcLogin}>
              账号密码登录
            </Button>
          )}
        </div>
      </div>
    );
  }

  if (!isMobile) {
    return (
      <H5PcShell activeKey={activeKey} currentUser={currentUser} title={title} extra={pcExtra}>
        {pcContent ?? children}
      </H5PcShell>
    );
  }

  return (
    <div style={{ minHeight: '100vh', background: '#f5f5f5' }}>
      {title && (
        <div
          style={{
            position: 'sticky',
            top: 0,
            zIndex: 10,
            padding: '12px 16px',
            background: '#fff',
            fontWeight: 600,
            fontSize: 16,
            borderBottom: '1px solid #f0f0f0',
          }}
        >
          {title}
        </div>
      )}
      {children}
    </div>
  );
};

export default H5Layout;
