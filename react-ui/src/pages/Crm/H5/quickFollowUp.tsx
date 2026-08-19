import React, { useCallback, useEffect, useState } from 'react';
import { history, useParams } from '@umijs/max';
import {
  Button,
  Card,
  DatePicker,
  Dialog,
  Form,
  ImageUploader,
  Selector,
  TextArea,
  Toast,
} from 'antd-mobile';
import type { ImageUploadItem } from 'antd-mobile';
import dayjs from 'dayjs';
import { getContactsByCustomer } from '@/services/crm/contact';
import { createFollowUp } from '@/services/crm/followup';
import { confirmUpload, preSignUpload } from '@/services/crm/attachment';
import { FOLLOW_UP_METHOD_ENUM } from '../constants';
import H5Layout from './H5Layout';
import { useH5Auth } from './useH5Auth';

const METHOD_OPTIONS = Object.keys(FOLLOW_UP_METHOD_ENUM).map((key) => ({
  label: FOLLOW_UP_METHOD_ENUM[key].text,
  value: key,
}));

/** 电话/微信跟进必须上传至少一张可用图片附件（后端 validateImageAttachments） */
const IMAGE_REQUIRED_METHODS = ['电话', '微信'];

/**
 * H5 快速跟踪：选择方式、填写内容与结果、可选联系人、
 * 电话/微信须上传图片附件；提交后创建跟进并返回客户详情。
 */
const H5QuickFollowUp: React.FC = () => {
  const { state, errorMsg, reLogin, gotoPcLogin } = useH5Auth();
  const params = useParams<{ id: string }>();
  const customerId = Number(params.id);

  const [form] = Form.useForm();
  const [contacts, setContacts] = useState<API.Crm.Contact[]>([]);
  const [fileList, setFileList] = useState<ImageUploadItem[]>([]);
  const [submitting, setSubmitting] = useState(false);
  const method = Form.useWatch('method', form);
  const hasNextWatch = Form.useWatch('hasNextFollowUp', form);
  const needImage = IMAGE_REQUIRED_METHODS.includes(method?.[0]);
  const hasNext = hasNextWatch?.[0] === 'yes';

  useEffect(() => {
    form.setFieldsValue({ method: ['电话'], followUpAt: new Date(), hasNextFollowUp: ['yes'] });
  }, [form]);

  useEffect(() => {
    if (state !== 'ready' || !customerId) return;
    getContactsByCustomer(customerId, { skipErrorHandler: true })
      .then((resp) => {
        if (resp.code === 200) {
          setContacts((resp.data ?? []).filter((c) => c.status !== '已停用'));
        }
      })
      .catch((e) => console.error('load contacts failed', e));
  }, [state, customerId]);

  /** 图片上传：preSignUpload → confirmUpload（模拟存储，确认后即 AVAILABLE） */
  const uploadImage = useCallback(async (file: File): Promise<ImageUploadItem> => {
    const preResp = await preSignUpload(
      {
        ownerType: 'FOLLOW_UP',
        fileName: file.name,
        contentType: file.type || 'image/jpeg',
        sizeBytes: file.size,
      },
      { skipErrorHandler: true },
    );
    const attachmentId = preResp?.data?.attachmentId;
    if (preResp?.code !== 200 || !attachmentId) {
      throw new Error(preResp?.msg || '申请上传失败');
    }
    const confirmResp = await confirmUpload(attachmentId, { skipErrorHandler: true });
    if (confirmResp?.code !== 200) {
      throw new Error(confirmResp?.msg || '确认上传失败');
    }
    return {
      url: URL.createObjectURL(file),
      extra: { attachmentId },
    };
  }, []);

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const methodValue = values.method?.[0];

      if (IMAGE_REQUIRED_METHODS.includes(methodValue) && fileList.length === 0) {
        Toast.show({ content: '电话/微信跟进必须上传至少一张图片' });
        return;
      }

      const followUpAt: Date = values.followUpAt;
      if (followUpAt > new Date()) {
        Toast.show({ content: '跟踪时间不能是未来时间' });
        return;
      }

      const attachmentIds = fileList
        .map((f) => f.extra?.attachmentId)
        .filter((id: number | undefined) => !!id) as number[];

      const hasNextFollowUp = values.hasNextFollowUp?.[0] === 'yes';

      const followUp: API.Crm.FollowUp = {
        customerId,
        method: methodValue,
        followUpAt: dayjs(followUpAt).format('YYYY-MM-DD HH:mm:ss'),
        content: values.content,
        outcome: values.outcome,
        nextAction: values.nextAction,
        nextFollowUpAt: hasNextFollowUp
          ? values.nextFollowUpAt && dayjs(values.nextFollowUpAt).format('YYYY-MM-DD HH:mm:ss')
          : undefined,
        noNextFollowUpReason: hasNextFollowUp ? undefined : values.noNextFollowUpReason,
      };

      setSubmitting(true);
      const resp = await createFollowUp(
        {
          followUp,
          contactIds: values.contactIds ?? [],
          attachmentIds: attachmentIds.length > 0 ? attachmentIds : undefined,
        },
        { skipErrorHandler: true },
      );
      setSubmitting(false);

      if (resp.code === 200) {
        Toast.show({ icon: 'success', content: '跟踪已提交' });
        history.replace(`/crm/h5/customer/${customerId}`);
      } else {
        Dialog.alert({ title: '提交失败', content: resp.msg || '请稍后重试' });
      }
    } catch (e: any) {
      setSubmitting(false);
      if (e?.errorFields) return; // 表单校验失败，antd-mobile 已提示
      console.error('submit follow-up failed', e);
      Dialog.alert({ title: '提交失败', content: e?.message || '请稍后重试' });
    }
  };

  return (
    <H5Layout
      state={state}
      errorMsg={errorMsg}
      onReLogin={reLogin}
      onGotoPcLogin={gotoPcLogin}
      title="快速跟踪"
    >
      <div style={{ padding: 12 }}>
        <Form
          form={form}
          layout="vertical"
          footer={
            <Button block color="primary" loading={submitting} onClick={handleSubmit}>
              提交跟踪
            </Button>
          }
        >
          <Card style={{ marginBottom: 12 }}>
            <Form.Item
              name="method"
              label="跟踪方式"
              rules={[{ required: true, message: '请选择跟踪方式' }]}
            >
              <Selector options={METHOD_OPTIONS} />
            </Form.Item>

            <Form.Item
              name="followUpAt"
              label="跟踪时间"
              trigger="onConfirm"
              onClick={(_, datePickerRef) => datePickerRef.current?.open()}
              rules={[{ required: true, message: '请选择跟踪时间' }]}
            >
              <DatePicker precision="minute" max={new Date()}>
                {(value) => (value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '请选择')}
              </DatePicker>
            </Form.Item>

            <Form.Item
              name="content"
              label="跟踪内容"
              rules={[{ required: true, message: '请填写跟踪内容' }]}
            >
              <TextArea placeholder="本次沟通的主要内容" rows={3} maxLength={500} showCount />
            </Form.Item>

            <Form.Item name="outcome" label="沟通结果">
              <TextArea placeholder="客户反馈与结果（选填）" rows={2} maxLength={500} showCount />
            </Form.Item>

            <Form.Item name="nextAction" label="下步动作">
              <TextArea placeholder="下一步计划（选填）" rows={2} maxLength={200} showCount />
            </Form.Item>
          </Card>

          {needImage && (
            <Card title="图片附件（电话/微信必填）" style={{ marginBottom: 12 }}>
              <ImageUploader
                value={fileList}
                onChange={setFileList}
                upload={uploadImage}
                accept="image/*"
                maxCount={6}
                multiple
              />
            </Card>
          )}

          <Card title="联系人（选填）" style={{ marginBottom: 12 }}>
            {contacts.length === 0 ? (
              <div style={{ color: '#999', fontSize: 13 }}>该客户暂无有效联系人</div>
            ) : (
              <Form.Item name="contactIds" noStyle>
                <Selector
                  multiple
                  options={contacts.map((c) => ({
                    label: c.title ? `${c.name}（${c.title}）` : c.name || '',
                    value: c.contactId as number,
                  }))}
                />
              </Form.Item>
            )}
          </Card>

          <Card title="下次跟踪安排" style={{ marginBottom: 12 }}>
            <Form.Item name="hasNextFollowUp" label="是否安排下次跟踪" trigger="onChange">
              <Selector
                options={[
                  { label: '安排', value: 'yes' },
                  { label: '暂不安排', value: 'no' },
                ]}
              />
            </Form.Item>

            {hasNext && (
              <Form.Item
                name="nextFollowUpAt"
                label="下次跟踪时间"
                trigger="onConfirm"
                onClick={(_, datePickerRef) => datePickerRef.current?.open()}
              >
                <DatePicker precision="minute" min={new Date()}>
                  {(value) => (value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '请选择')}
                </DatePicker>
              </Form.Item>
            )}

            {hasNext === false && (
              <Form.Item
                name="noNextFollowUpReason"
                label="暂不安排原因"
                rules={[{ required: true, message: '请填写暂不安排的原因' }]}
              >
                <TextArea placeholder="请说明原因" rows={2} maxLength={200} showCount />
              </Form.Item>
            )}
          </Card>
        </Form>

        <Button block fill="outline" onClick={() => history.back()}>
          取消
        </Button>
        <div style={{ height: 24 }} />
      </div>
    </H5Layout>
  );
};

export default H5QuickFollowUp;
