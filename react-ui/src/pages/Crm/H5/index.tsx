import React, { useEffect } from 'react';
import { history } from '@umijs/max';
import H5Layout from './H5Layout';
import { useH5Auth } from './useH5Auth';

/**
 * H5 入口页：完成钉钉免登后跳转到「我的客户」。
 * 钉钉工作台微应用首页地址配置为本页即可。
 */
const H5Index: React.FC = () => {
  const { state, errorMsg, reLogin, gotoPcLogin } = useH5Auth();

  useEffect(() => {
    if (state === 'ready') {
      history.replace('/crm/h5/customers');
    }
  }, [state]);

  return (
    <H5Layout
      state={state}
      errorMsg={errorMsg}
      onReLogin={reLogin}
      onGotoPcLogin={gotoPcLogin}
    />
  );
};

export default H5Index;
