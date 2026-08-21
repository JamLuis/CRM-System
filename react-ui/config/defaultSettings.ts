import { ProLayoutProps } from '@ant-design/pro-components';

/**
 * @name
 */
const Settings: ProLayoutProps & {
  pwa?: boolean;
  logo?: string;
} = {
  navTheme: 'light',
  colorPrimary: '#1677ff',
  layout: 'mix',
  contentWidth: 'Fluid',
  fixedHeader: true,
  fixSiderbar: true,
  splitMenus: true,
  colorWeak: false,
  title: 'CRM 管理系统',
  pwa: false,
  logo: '/logo.svg',
  iconfontUrl: '',
  token: {
    header: {
      colorBgHeader: '#ffffff',
    },
    sider: {
      colorMenuBackground: '#ffffff',
      colorBgMenuItemSelected: '#e8f1ff',
      colorTextMenuSelected: '#1677ff',
      colorTextMenuItemHover: '#1677ff',
    },
    pageContainer: {
      paddingBlockPageContainerContent: 20,
      paddingInlinePageContainerContent: 24,
    },
  },
};

export default Settings;
