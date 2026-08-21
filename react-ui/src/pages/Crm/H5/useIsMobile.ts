import { useEffect, useState } from 'react';

/** 与 PC 端断点一致：小于 768px 视为移动端 */
const MOBILE_QUERY = '(max-width: 768px)';

/**
 * 设备形态检测：移动端（手机）返回 true，桌面/Pad 返回 false。
 * H5 页面据此决定渲染移动端优化版还是 PC 完整版。
 */
export function useIsMobile(): boolean {
  const [isMobile, setIsMobile] = useState<boolean>(
    () => typeof window !== 'undefined' && window.matchMedia(MOBILE_QUERY).matches,
  );

  useEffect(() => {
    const mql = window.matchMedia(MOBILE_QUERY);
    const onChange = (e: MediaQueryListEvent) => setIsMobile(e.matches);
    mql.addEventListener('change', onChange);
    return () => mql.removeEventListener('change', onChange);
  }, []);

  return isMobile;
}
