import Footer from '@/components/Footer';
import RightContent from '@/components/RightContent';
import type { Settings as LayoutSettings } from '@ant-design/pro-components';
import type { RunTimeLayoutConfig } from '@umijs/max';
import { history } from '@umijs/max';
import defaultSettings from '../config/defaultSettings';
import { errorConfig } from './requestErrorConfig';
import { clearSessionToken, getAccessToken, getRefreshToken, getTokenExpireTime } from './access';
import { getRemoteMenu, getRoutersInfo, getUserInfo, patchRouteWithRemoteMenus, setRemoteMenu } from './services/session';
import { PageEnum } from './enums/pagesEnums';


/** H5（钉钉内）页面路径前缀：自行完成免登，不走 PC 登录重定向 */
const H5_PATH_PREFIX = '/crm/h5';



/**
 * @see  https://umijs.org/zh-CN/plugins/plugin-initial-state
 * */
export async function getInitialState(): Promise<{
  settings?: Partial<LayoutSettings>;
  currentUser?: API.CurrentUser;
  loading?: boolean;
  fetchUserInfo?: () => Promise<API.CurrentUser | undefined>;
}> {
  const fetchUserInfo = async () => {
    try {
      const response = await getUserInfo({
        skipErrorHandler: true,
      });
      if (response.user.avatar === '') {
        response.user.avatar = '/logo.svg';
      }
      return {
        ...response.user,
        permissions: response.permissions,
        roles: response.roles,
      } as API.CurrentUser;
    } catch (error) {
      console.log(error);
      history.push(PageEnum.LOGIN);
    }
    return undefined;
  };
  // 如果不是登录页面，执行
  const { location } = history;
  // H5（钉钉内）页面由 H5 自行完成免登，不在此拉取用户、不重定向 PC 登录页
  if (location.pathname !== PageEnum.LOGIN && !location.pathname.startsWith(H5_PATH_PREFIX)) {
    const currentUser = await fetchUserInfo();
    return {
      fetchUserInfo,
      currentUser,
      settings: defaultSettings as Partial<LayoutSettings>,
    };
  }
  return {
    fetchUserInfo,
    settings: defaultSettings as Partial<LayoutSettings>,
  };
}

// ProLayout 支持的api https://procomponents.ant.design/components/layout
export const layout: RunTimeLayoutConfig = ({ initialState }) => {
  return {
    rightContentRender: () => <RightContent />,
    waterMarkProps: {
      // content: initialState?.currentUser?.nickName,
    },
    menu: {
      locale: false,
      // 每当 initialState?.currentUser?.userid 发生修改时重新执行 request
      params: {
        userId: initialState?.currentUser?.userId,
      },
      request: async () => {
        if (!initialState?.currentUser?.userId) {
          return [];
        }
        // console.log('get menus')
        // initialState.currentUser 中包含了所有用户信息
        // console.log('get routers')
        return getRemoteMenu();
      },
    },
    footerRender: () => <Footer />,
    onPageChange: () => {
      const { location } = history;
      // H5（钉钉内）页面自行免登，不重定向 PC 登录页
      if (location.pathname.startsWith(H5_PATH_PREFIX)) {
        return;
      }
      // 如果没有登录，重定向到 login
      if (!initialState?.currentUser && location.pathname !== PageEnum.LOGIN) {
        history.push(PageEnum.LOGIN);
      }
    },
    menuHeaderRender: undefined,
    // 自定义 403 页面
    // unAccessible: <div>unAccessible</div>,
    ...initialState?.settings,
  };
};

/**
 * antd v5 运行时主题：统一 CRM 视觉基调（主色 / 圆角 / 表格 / 卡片）
 */
export function antd(memo: any) {
  memo.theme = {
    token: {
      colorPrimary: '#1677ff',
      colorInfo: '#1677ff',
      colorLink: '#1677ff',
      borderRadius: 6,
      colorBgLayout: '#f2f4f8',
      colorTextBase: '#0f172a',
    },
    components: {
      Card: {
        borderRadiusLG: 10,
      },
      Table: {
        headerBg: '#f7f9fc',
        headerColor: '#475569',
        headerSplitColor: 'transparent',
        rowHoverBg: '#f5f9ff',
      },
      Layout: {
        headerBg: '#ffffff',
        siderBg: '#ffffff',
        bodyBg: 'transparent',
      },
    },
  };
  return memo;
}

export async function onRouteChange({ clientRoutes, location }) {
  const menus = getRemoteMenu();
 // console.log('onRouteChange', clientRoutes, location, menus);
  // H5（钉钉内）页面自行免登，不检查远程菜单、不触发刷新
  if (location.pathname.startsWith(H5_PATH_PREFIX)) {
    return;
  }
  if(menus === null && location.pathname !== PageEnum.LOGIN) {
    console.log('refresh')
    history.go(0);
  }
}

// export function patchRoutes({ routes, routeComponents }) {
//   console.log('patchRoutes', routes, routeComponents);
// }


export async function patchClientRoutes({ routes }) {
  // console.log('patchClientRoutes', routes);
  patchRouteWithRemoteMenus(routes);
}

export function render(oldRender: () => void) {
  // console.log('render get routers', oldRender)
  const token = getAccessToken();
  if(!token || token?.length === 0) {
    // 无 token 时设置空菜单，避免 onRouteChange 因 menus===null 触发无限刷新
    setRemoteMenu([]);
    oldRender();
    return;
  }
  getRoutersInfo().then(res => {
    setRemoteMenu(res);
    oldRender()
  }).catch(() => {
    // 获取路由信息失败（如 token 过期、后端不可用），
    // 仍需正常渲染，否则页面会永远卡在 loading
    setRemoteMenu([]);
    oldRender()
  });
}

/**
 * @name request 配置，可以配置错误处理
 * 它基于 axios 和 ahooks 的 useRequest 提供了一套统一的网络请求和错误处理方案。
 * @doc https://umijs.org/docs/max/request#配置
 */
const checkRegion = 5 * 60 * 1000;

export const request = {
  ...errorConfig,
  requestInterceptors: [
    (url: any, options: { headers: any }) => {
      const headers = options.headers ? options.headers : [];
      console.log('request ====>:', url);
      const authHeader = headers['Authorization'];
      const isToken = headers['isToken'];
      if (!authHeader && isToken !== false) {
        const expireTime = getTokenExpireTime();
        if (expireTime) {
          const left = Number(expireTime) - new Date().getTime();
          const refreshToken = getRefreshToken();
          if (left < checkRegion && refreshToken) {
            if (left < 0) {
              clearSessionToken();
            }
          } else {
            const accessToken = getAccessToken();
            if (accessToken) {
              headers['Authorization'] = `Bearer ${accessToken}`;
            }
          }
        } else {
          clearSessionToken();
        }
      }
      return { url, options };
    },
  ],
  responseInterceptors: [
    // (response) =>
    // {
    //   // // 不再需要异步处理读取返回体内容，可直接在data中读出，部分字段可在 config 中找到
    //   // const { data = {} as any, config } = response;
    //   // // do something
    //   // console.log('data: ', data)
    //   // console.log('config: ', config)
    //   return response
    // },
  ],
};
