import { useCallback, useEffect, useState } from 'react';
import { history } from '@umijs/max';
import * as dd from 'dingtalk-jsapi';
import { setSessionToken, getAccessToken, getTokenExpireTime } from '@/access';
import { getUserInfo } from '@/services/session';
import { dingtalkLogin, getDingtalkConfig } from '@/services/crm/mobile';

export type H5AuthState = 'loading' | 'ready' | 'pending-activation' | 'error';

/**
 * H5 钉钉免登 Hook
 * <p>
 * 流程：本地 token 有效 → 直接拉取用户信息；
 * 否则通过钉钉 JSAPI requestAuthCode 获取授权码，
 * 调后端免登接口换取 CRM 会话（access_token），再拉取用户信息。
 * <p>
 * 非钉钉容器内（如 PC 浏览器调试）无法免登，返回 error 状态。
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
        user.avatar =
          'https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png';
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
    // 非钉钉容器：无法免登
    if (!dd.env.platform || dd.env.platform === 'notInDingTalk') {
      setErrorMsg('请在钉钉内打开本应用完成免登');
      setState('error');
      return;
    }

    try {
      // 1. 获取企业配置（corpId）
      const configResp = await getDingtalkConfig({ skipErrorHandler: true });
      const corpId = configResp?.data?.corpId;
      if (configResp?.code !== 200 || !corpId) {
        setErrorMsg(configResp?.msg || '获取钉钉配置失败');
        setState('error');
        return;
      }

      // 2. JSAPI 获取免登授权码（一次消费，5 分钟有效）
      const authRes = await dd.runtime.permission.requestAuthCode({ corpId });
      const authCode = (authRes as any)?.code;
      if (!authCode) {
        setErrorMsg('获取钉钉授权码失败');
        setState('error');
        return;
      }

      // 3. 后端免登换取 CRM 会话
      const loginResp = await dingtalkLogin(authCode, { skipErrorHandler: true });
      if (loginResp?.code !== 200 || !loginResp.data) {
        setErrorMsg(loginResp?.msg || '钉钉免登失败');
        setState('error');
        return;
      }

      const result = loginResp.data;
      if (result.status === 'PENDING_ACTIVATION') {
        setState('pending-activation');
        return;
      }

      // 4. 保存会话并拉取用户信息
      const expiresIn = result.expires_in ?? 720;
      const expireTime = new Date().getTime() + expiresIn * 60 * 1000;
      setSessionToken(result.access_token, result.access_token, expireTime);

      const ok = await fetchUserInfo();
      if (ok) {
        setState('ready');
      } else {
        setErrorMsg('获取用户信息失败');
        setState('error');
      }
    } catch (e: any) {
      console.error('H5 DingTalk login failed', e);
      setErrorMsg(e?.message || '钉钉免登异常');
      setState('error');
    }
  }, [fetchUserInfo]);

  useEffect(() => {
    (async () => {
      // 已有有效会话：直接拉取用户信息
      if (hasValidToken()) {
        try {
          const ok = await fetchUserInfo();
          setState(ok ? 'ready' : 'error');
          if (!ok) setErrorMsg('获取用户信息失败');
          return;
        } catch (e) {
          // token 无效，继续走免登
          console.warn('Existing token invalid, fallback to DingTalk login', e);
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

  /** 跳转 PC 登录页（兜底） */
  const gotoPcLogin = useCallback(() => {
    history.push('/user/login');
  }, []);

  return { state, currentUser, errorMsg, reLogin, gotoPcLogin };
}
