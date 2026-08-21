package com.ruoyi.crm.dingtalk.client;

import com.ruoyi.crm.dingtalk.config.DingTalkProperties;
import com.ruoyi.crm.dingtalk.domain.DingTalkDept;
import com.ruoyi.crm.dingtalk.domain.DingTalkDeptUser;
import com.ruoyi.crm.dingtalk.domain.DingTalkUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 钉钉 API 客户端
 * <p>
 * 封装钉钉服务端 API 调用，包括 access_token 获取与缓存、
 * 免登授权码换取用户身份、部门列表、部门用户列表。
 * <p>
 * 安全约束：
 * <ul>
 *   <li>Client Secret 从环境变量读取，不写日志</li>
 *   <li>access_token 缓存在内存中，不持久化</li>
 *   <li>所有 API 调用失败不暴露原始响应体</li>
 * </ul>
 *
 * @author ruoyi-crm
 */
@Component
public class DingTalkClient
{
    private static final Logger log = LoggerFactory.getLogger(DingTalkClient.class);

    @Autowired
    private DingTalkProperties properties;

    private final RestTemplate restTemplate = new RestTemplate();

    /** access_token 缓存 */
    private volatile String cachedAccessToken;
    /** token 过期时间戳（毫秒） */
    private volatile long tokenExpireAt = 0;
    /** token 刷新锁 */
    private final ReentrantLock tokenLock = new ReentrantLock();

    /**
     * 获取 access_token（带缓存）
     * <p>
     * 钉钉 access_token 有效期 7200 秒，提前 200 秒刷新。
     *
     * @return access_token
     */
    public String getAccessToken()
    {
        long now = System.currentTimeMillis();
        if (cachedAccessToken != null && now < tokenExpireAt)
        {
            return cachedAccessToken;
        }

        tokenLock.lock();
        try
        {
            // double-check
            now = System.currentTimeMillis();
            if (cachedAccessToken != null && now < tokenExpireAt)
            {
                return cachedAccessToken;
            }

            String secret = properties.getClientSecret();
            if (secret == null || secret.isEmpty())
            {
                throw new IllegalStateException("DingTalk client secret not configured in environment: "
                        + properties.getClientSecretEnv());
            }

            String url = properties.getApiBaseUrl() + properties.getGetTokenPath()
                    + "?appkey=" + properties.getClientId()
                    + "&appsecret=" + secret;

            @SuppressWarnings("unchecked")
            Map<String, Object> resp = restTemplate.getForObject(url, Map.class);

            if (resp == null || !Integer.valueOf(0).equals(resp.get("errcode")))
            {
                String errmsg = resp != null ? String.valueOf(resp.get("errmsg")) : "unknown";
                throw new RuntimeException("DingTalk gettoken failed: " + errmsg);
            }

            cachedAccessToken = (String) resp.get("access_token");
            int expiresIn = (Integer) resp.get("expires_in");
            // 提前 200 秒过期
            tokenExpireAt = System.currentTimeMillis() + (expiresIn - 200) * 1000L;

            log.debug("DingTalk access_token refreshed, expires_in={}s", expiresIn);
            return cachedAccessToken;
        }
        finally
        {
            tokenLock.unlock();
        }
    }

    /**
     * 免登授权码换取用户身份
     * <p>
     * H5 微应用通过 JSAPI 获取一次性 authCode，后端用此接口换取钉钉用户 ID。
     * 授权码一次消费，不可重放。
     *
     * @param authCode 钉钉免登授权码
     * @return 用户信息
     */
    public DingTalkUserInfo getUserInfoByAuthCode(String authCode)
    {
        String token = getAccessToken();
        String url = properties.getApiBaseUrl() + properties.getGetUserInfoPath()
                + "?access_token=" + token;

        Map<String, Object> body = new HashMap<>();
        body.put("code", authCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        @SuppressWarnings("unchecked")
        Map<String, Object> resp = restTemplate.postForObject(url, entity, Map.class);

        if (resp == null || !Integer.valueOf(0).equals(resp.get("errcode")))
        {
            String errmsg = resp != null ? String.valueOf(resp.get("errmsg")) : "unknown";
            throw new RuntimeException("DingTalk getuserinfo failed: " + errmsg);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) resp.get("result");

        DingTalkUserInfo info = new DingTalkUserInfo();
        if (result != null)
        {
            info.setUserid((String) result.get("userid"));
            info.setUnionid((String) result.get("unionid"));
            info.setDeviceId((String) result.get("device_id"));
            Object sysLevel = result.get("sys_level");
            if (sysLevel != null)
            {
                info.setSysLevel(Boolean.valueOf(sysLevel.toString()));
            }
        }
        return info;
    }

    /**
     * 获取子部门列表
     *
     * @param parentDeptId 父部门 ID（根部门传 1）
     * @return 部门列表
     */
    @SuppressWarnings("unchecked")
    public List<DingTalkDept> getDeptList(Long parentDeptId)
    {
        String token = getAccessToken();
        String url = properties.getApiBaseUrl() + properties.getGetDeptListPath()
                + "?access_token=" + token;

        Map<String, Object> body = new HashMap<>();
        body.put("dept_id", parentDeptId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        Map<String, Object> resp = restTemplate.postForObject(url, entity, Map.class);

        if (resp == null || !Integer.valueOf(0).equals(resp.get("errcode")))
        {
            String errmsg = resp != null ? String.valueOf(resp.get("errmsg")) : "unknown";
            throw new RuntimeException("DingTalk getdeptlist failed: " + errmsg);
        }

        List<Map<String, Object>> rawList = (List<Map<String, Object>>) resp.get("result");
        if (rawList == null)
        {
            return Collections.emptyList();
        }

        return rawList.stream().map(m -> {
            DingTalkDept dept = new DingTalkDept();
            dept.setDeptId(toLong(m.get("dept_id")));
            dept.setParentId(toLong(m.get("parent_id")));
            dept.setName((String) m.get("name"));
            dept.setOrder(toLong(m.get("order")));
            dept.setCreateTime(toLong(m.get("create_time")));
            return dept;
        }).collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取部门用户列表
     *
     * @param deptId 部门 ID
     * @param cursor  分页游标
     * @param size    每页大小（最大 100）
     * @return 用户列表
     */
    @SuppressWarnings("unchecked")
    public List<DingTalkDeptUser> getDeptUserList(Long deptId, Long cursor, Long size)
    {
        String token = getAccessToken();
        String url = properties.getApiBaseUrl() + properties.getGetDeptUserListPath()
                + "?access_token=" + token;

        Map<String, Object> body = new HashMap<>();
        body.put("dept_id", deptId);
        body.put("cursor", cursor);
        body.put("size", size);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        Map<String, Object> resp = restTemplate.postForObject(url, entity, Map.class);

        if (resp == null || !Integer.valueOf(0).equals(resp.get("errcode")))
        {
            String errmsg = resp != null ? String.valueOf(resp.get("errmsg")) : "unknown";
            throw new RuntimeException("DingTalk getdeptuserlist failed: " + errmsg);
        }

        Map<String, Object> result = (Map<String, Object>) resp.get("result");
        if (result == null)
        {
            return Collections.emptyList();
        }

        List<Map<String, Object>> rawList = (List<Map<String, Object>>) result.get("list");
        if (rawList == null)
        {
            return Collections.emptyList();
        }

        return rawList.stream().map(m -> {
            DingTalkDeptUser user = new DingTalkDeptUser();
            user.setUserid((String) m.get("userid"));
            user.setName((String) m.get("name"));
            user.setMobile((String) m.get("mobile"));
            user.setTitle((String) m.get("title"));
            user.setActive((Boolean) m.get("active"));
            user.setUnionid((String) m.get("unionid"));
            user.setEmail((String) m.get("email"));
            List<Number> deptIds = (List<Number>) m.get("dept_id_list");
            if (deptIds != null)
            {
                user.setDeptIdList(deptIds.stream()
                        .map(Number::longValue)
                        .collect(java.util.stream.Collectors.toList()));
            }
            return user;
        }).collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取部门用户列表的分页 has_more 标志
     */
    @SuppressWarnings("unchecked")
    public boolean hasMore(Long deptId, Long cursor, Long size)
    {
        String token = getAccessToken();
        String url = properties.getApiBaseUrl() + properties.getGetDeptUserListPath()
                + "?access_token=" + token;

        Map<String, Object> body = new HashMap<>();
        body.put("dept_id", deptId);
        body.put("cursor", cursor);
        body.put("size", size);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        Map<String, Object> resp = restTemplate.postForObject(url, entity, Map.class);
        if (resp != null)
        {
            Map<String, Object> result = (Map<String, Object>) resp.get("result");
            return result != null && Boolean.TRUE.equals(result.get("has_more"));
        }
        return false;
    }

    /** 发送钉钉企业内部工作通知（文本消息）。 */
    public Long sendWorkNotification(String dingtalkUserId, String content)
    {
        String token = getAccessToken();
        String url = properties.getApiBaseUrl() + properties.getSendWorkNotificationPath()
                + "?access_token=" + token;

        Map<String, Object> textBody = new HashMap<>();
        textBody.put("content", content);
        Map<String, Object> msg = new HashMap<>();
        msg.put("msgtype", "text");
        msg.put("text", textBody);

        Map<String, Object> body = new HashMap<>();
        body.put("agent_id", Long.valueOf(properties.getAgentId()));
        body.put("userid_list", dingtalkUserId);
        body.put("to_all_user", false);
        body.put("msg", msg);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        @SuppressWarnings("unchecked")
        Map<String, Object> resp = restTemplate.postForObject(
                url, new HttpEntity<>(body, headers), Map.class);
        if (resp == null || !Integer.valueOf(0).equals(resp.get("errcode")))
        {
            String errmsg = resp != null ? String.valueOf(resp.get("errmsg")) : "unknown";
            throw new RuntimeException("DingTalk asyncsend failed: " + errmsg);
        }
        return toLong(resp.get("task_id"));
    }

    private Long toLong(Object obj)
    {
        if (obj == null)
        {
            return null;
        }
        if (obj instanceof Number)
        {
            return ((Number) obj).longValue();
        }
        return Long.valueOf(obj.toString());
    }
}
