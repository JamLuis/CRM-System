import React, { useState } from 'react';
import { Select, Spin } from 'antd';
import { listDingTalkDirectoryUsers } from '@/services/crm/admin';

export type UserSelectProps = {
  value?: number;
  onChange?: (userId: number, user: API.Crm.SysUserItem) => void;
  placeholder?: string;
};

type AuthorizedUserOption = API.Crm.SysUserItem & {
  title?: string;
  roleNames?: string;
};

/** 仅搜索已显式分配 CRM 访问权的企业通讯录人员。 */
const UserSelect: React.FC<UserSelectProps> = ({ value, onChange, placeholder }) => {
  const [options, setOptions] = useState<AuthorizedUserOption[]>([]);
  const [fetching, setFetching] = useState(false);

  const doSearch = async (keyword: string) => {
    if (!keyword) {
      setOptions([]);
      return;
    }
    setFetching(true);
    try {
      const resp = await listDingTalkDirectoryUsers({ keyword, accessStatus: 'GRANTED' });
      if (resp.code === 200) {
        setOptions(
          (resp.data || [])
            .filter((person) => person.sysUserId)
            .map((person) => ({
              userId: person.sysUserId,
              userName: person.dingtalkUserId,
              nickName: person.name,
              phonenumber: person.mobile,
              deptId: person.sysDeptId,
              dept: { deptId: person.sysDeptId, deptName: person.deptNames },
              dingtalkUserId: person.dingtalkUserId,
              title: person.title,
              roleNames: person.roleNames,
            })),
        );
      }
    } finally {
      setFetching(false);
    }
  };

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
        label: `${u.nickName || u.userName}｜${u.title || '无职位'}｜${u.dept?.deptName || '无组织'}｜${u.roleNames || '无角色'}`,
        value: u.userId,
      }))}
    />
  );
};

export default UserSelect;
