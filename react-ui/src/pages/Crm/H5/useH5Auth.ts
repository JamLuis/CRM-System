import { clearSessionToken, getAccessToken, getTokenExpireTime, setSessionToken } from '@/access';
import { dingtalkLogin, getDingtalkConfig } from '@/services/crm/mobile';
import { getUserInfo } from '@/services/session';
import { history } from '@umijs/max';
import * as dd from 'dingtalk-jsapi';
import { useCallback, useEffect, useState } from 'react';

export type H5AuthState = 'loading' | 'ready' | 'pending-activation' | 'error';

class H5AuthError extends Error {
  constructor(message: string) {
    super(message);
    this.name = 'H5AuthError';
  }
}

/** 兼容钉钉 JSAPI 的非标准错误对象。 */
function extractErrorMessage(error: any): string {
  if (!error) return '未知错误';
  if (typeof error === 'string') return error;
  return (
    error?.message ||
    error?.errorMessage ||
    error?.msg ||
    `errorCode=${error?.errorCode}, ${JSON.stringify(error)}`
  );
}

/**
 * 开发模式下 React 可能重复挂载页面。免登码只能消费一次，因此整个
 * “取码 + 后端换 token”过程必须保持单航班，所有调用者共享同一个结果。
 */
let dingTalkLoginFlight: Promise<API.Crm.DingTalkLoginResult> | undefined;

async function requestDingTalkSession(): Promise<API.Crm.DingTalkLoginResult> {
  console.log('[H5Auth] 步骤1: 获取钉钉配置...');
  let configResp;
  try {
    configResp = await getDingtalkConfig({
      skipErrorHandler: true,
      headers: { isToken: false },
    });
  } catch (error) {
    throw new H5AuthError(
      `步骤1-获取钉钉配置异常: ${extractErrorMessage(error)}\n` +
        '可能原因：前端代理无法访问网关 8080，或后端 CRM 服务未启动。',
    );
  }

  const { corpId, clientId } = configResp?.data || {};
  if (configResp?.code !== 200 || !corpId) {
    throw new H5AuthError(
      `获取钉钉配置失败 (code=${configResp?.code})。\n` +
        `${configResp?.msg || ''}\n` +
        '请检查后端 CRM 服务、网关及钉钉应用配置。',
    );
  }

  console.log('[H5Auth] 步骤1 完成:', {
    hasCorpId: !!corpId,
    hasClientId: !!clientId,
    platform: dd?.env?.platform,
  });

  let authCode: string | undefined;
  try {
    console.log('[H5Auth] 步骤2: 获取钉钉免登授权码...');

    // 3.x SDK 的统一 API 会调用 requestAuthCodeV2，并用 clientId 将授权码
    // 绑定到当前应用，避免调试器中选择的应用与后端 AppKey 不一致。
    const requestAuthCode = (dd as any)?.requestAuthCode;
    const authResult =
      clientId && typeof requestAuthCode === 'function'
        ? await requestAuthCode({ corpId, clientId })
        : await dd.runtime.permission.requestAuthCode({ corpId });

    authCode = (authResult as any)?.code;
  } catch (error) {
    throw new H5AuthError(
      `步骤2-获取钉钉授权码失败: ${extractErrorMessage(error)}\n` +
        '请从钉钉工作台打开微应用，并确认应用首页地址和安全域名配置正确。',
    );
  }

  if (!authCode) {
    throw new H5AuthError(
      '步骤2-获取钉钉授权码失败：JSAPI 返回为空。\n' +
        '请确认当前页面由钉钉微应用打开，且应用配置与后端一致。',
    );
  }

  console.log('[H5Auth] 步骤3: 后端免登换取 CRM 会话...');
  let loginResp;
  try {
    loginResp = await dingtalkLogin(authCode, {
      skipErrorHandler: true,
      headers: { isToken: false },
    });
  } catch (error) {
    throw new H5AuthError(
      `步骤3-后端免登异常: ${extractErrorMessage(error)}\n` +
        '可能原因：授权码已过期、已被消费，或后端钉钉 API 调用失败。',
    );
  }

  if (loginResp?.code !== 200 || !loginResp?.data) {
    throw new H5AuthError(
      `步骤3-钉钉免登失败 (code=${loginResp?.code})。\n${loginResp?.msg || ''}`,
    );
  }
  return loginResp.data;
}

function getDingTalkSession(): Promise<API.Crm.DingTalkLoginResult> {
  if (!dingTalkLoginFlight) {
    dingTalkLoginFlight = requestDingTalkSession().finally(() => {
      dingTalkLoginFlight = undefined;
    });
  }
  return dingTalkLoginFlight;
}

/**
 * H5 钉钉免登 Hook
 * <p>
 * 流程：本地 token 有效 → 直接拉取用户信息；
 * 否则通过钉钉 JSAPI requestAuthCode 获取授权码，
 * 调后端免登接口换取 CRM 会话（access_token），再拉取用户信息。
 * <p>
 * 免登失败时保留在 H5 错误页，不自动跳转账号密码登录。
 */
export function useH5Auth() {
  const [state, setState] = useState<H5AuthState>('loading');
  const [currentUser, setCurrentUser] = useState<API.CurrentUser>();
  const [errorMsg, setErrorMsg] = useState<string>('');

  const fetchUserInfo = useCallback(async () => {
    const resp = await getUserInfo({ skipErrorHandler: true });
    const user = (resp as any)?.user;
    if (user) {
      if (user.avatar === '') {
        user.avatar = '/logo.svg';
      }
      setCurrentUser({
        ...user,
        permissions: (resp as any)?.permissions,
        roles: (resp as any)?.roles,
      } as API.CurrentUser);
      return true;
    }
    return false;
  }, []);

  /** 本地 token 是否有效 */
  const hasValidToken = useCallback(() => {
    const token = getAccessToken();
    const expireTime = getTokenExpireTime();
    if (!token || !expireTime) return false;
    return Number(expireTime) > new Date().getTime();
  }, []);

  const doLogin = useCallback(async () => {
    let result: any;
    try {
      result = await getDingTalkSession();
    } catch (e: any) {
      console.error('[H5Auth] 免登失败:', e);
      setErrorMsg(extractErrorMessage(e));
      setState('error');
      return;
    }

    if (result.status === 'PENDING_ACTIVATION') {
      console.log('[H5Auth] 身份待激活');
      setState('pending-activation');
      return;
    }

    // 步骤4：保存会话并拉取用户信息
    try {
      console.log('[H5Auth] 步骤4: 保存会话, 拉取用户信息...');
      if (!result.access_token) {
        throw new H5AuthError('步骤4-后端未返回 access_token');
      }
      const expiresIn = result.expires_in ?? 720;
      const expireTime = new Date().getTime() + expiresIn * 60 * 1000;
      setSessionToken(result.access_token, result.access_token, expireTime);

      const ok = await fetchUserInfo();
      if (ok) {
        console.log('[H5Auth] 免登完成');
        setState('ready');
      } else {
        setErrorMsg('步骤4-获取用户信息失败');
        setState('error');
      }
    } catch (e: any) {
      console.error('[H5Auth] 步骤4 异常:', e);
      setErrorMsg(`步骤4-获取用户信息异常: ${extractErrorMessage(e)}`);
      setState('error');
    }
  }, [fetchUserInfo]);

  useEffect(() => {
    (async () => {
      // 已有有效会话：直接拉取用户信息
      if (hasValidToken()) {
        try {
          const ok = await fetchUserInfo();
          if (ok) {
            setState('ready');
            return;
          }
          clearSessionToken();
        } catch (e) {
          // token 无效，继续走免登
          console.warn('Existing token invalid, fallback to DingTalk login', e);
          clearSessionToken();
        }
      }
      await doLogin();
    })();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  /** 重新免登（token 失效时可调用） */
  const reLogin = useCallback(() => {
    setState('loading');
    doLogin();
  }, [doLogin]);

  /** 跳转 PC 登录页（本地联调兜底，登录后自动跳回 H5） */
  const gotoPcLogin = useCallback(() => {
    history.push('/user/login?redirect=' + encodeURIComponent('/crm/h5'));
  }, []);

  return { state, currentUser, errorMsg, reLogin, gotoPcLogin };
}
