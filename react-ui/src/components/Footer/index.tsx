import React from 'react';

const Footer: React.FC = () => {
  const currentYear = new Date().getFullYear();

  return (
    <div style={{ padding: '20px 16px', textAlign: 'center', color: 'rgba(0, 0, 0, 0.45)' }}>
      © {currentYear} CRM 管理系统
    </div>
  );
};

export default Footer;
