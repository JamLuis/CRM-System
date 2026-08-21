import { PageContainer } from '@ant-design/pro-components';
import { Card, Typography } from 'antd';
import React from 'react';

const Welcome: React.FC = () => (
  <PageContainer title="CRM 管理系统">
    <Card>
      <Typography.Title level={3}>企业客户关系管理平台</Typography.Title>
      <Typography.Paragraph type="secondary">
        请通过客户管理菜单进入客户、联系人、跟进及后台授权功能。
      </Typography.Paragraph>
    </Card>
  </PageContainer>
);

export default Welcome;
