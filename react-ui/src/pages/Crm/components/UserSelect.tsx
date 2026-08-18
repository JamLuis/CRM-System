import React, { useEffect, useState } from 'react';
import { Select, Spin } from 'antd';
import { searchUsers } from '@/services/crm/admin';

export type UserSelectProps = {
  value?: number;
  onChange?: (userId: number, user: API.Crm.SysUserItem) => void;
  placeholder?: string;
};

/** 系统用户搜索选择器（钉钉/系统用户） */
const UserSelect: React.FC<UserSelectProps> = ({ value, onChange, placeholder }) => {
  const [options, setOptions] = useState<API.Crm.SysUserItem[]>([]);
  const [fetching, setFetching] = useState(false);

  const doSearch = async (keyword: string) => {
    if (!keyword) {
      setOptions([]);
      return;
    }
    setFetching(true);
    try {
      const resp = await searchUsers(keyword);
      if (resp.code === 200) {
        setOptions(resp.data || []);
      }
    } finally {
      setFetching(false);
    }
  };

  useEffect(() => {
    // 初始不加载，按关键字搜索
  }, []);

  return (
    <Select
      showSearch
      value={value}
      placeholder={placeholder || '输入姓名/账号搜索'}
      filterOption={false}
      onSearch={doSearch}
      onChange={(val) => {
        const user = options.find((u) => u.userId === val);
        if (user && onChange) {
          onChange(val, user);
        }
      }}
      notFoundContent={fetching ? <Spin size="small" /> : null}
      options={options.map((u) => ({
        label: `${u.nickName || u.userName}（${u.dept?.deptName || '-'}）`,
        value: u.userId,
      }))}
    />
  );
};

export default UserSelect;
