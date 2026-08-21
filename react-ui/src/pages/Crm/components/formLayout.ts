/** CRM 桌面端弹窗表单统一使用左侧标签、右侧控件的水平布局。 */
export const CRM_HORIZONTAL_FORM_PROPS = {
  layout: 'horizontal' as const,
  labelCol: { flex: '136px' },
  wrapperCol: { flex: 1 },
  labelAlign: 'left' as const,
  labelWrap: true,
  colon: false,
};
