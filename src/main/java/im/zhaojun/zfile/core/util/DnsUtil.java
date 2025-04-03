package im.zhaojun.zfile.core.util;

import com.alibaba.dcm.DnsCacheManipulator;
import com.alibaba.fastjson2.JSONArray;
import org.springframework.lang.Nullable;

public class DnsUtil {

    /**
     * 通过 HTTP DNS 获取域名对应的 IP 地址
     *
     * @param   domain
     *          域名
     *
     * @return  IP 地址数组
     */
    public static @Nullable String[] getDomainIpByHttpDns(String domain) {
        String jsonArrayStr = cn.hutool.http.HttpUtil.get("http://223.5.5.5/resolve?name=" + domain + "&short=1", 3000);
        JSONArray jsonArray = JSONArray.parseArray(jsonArrayStr);
        if (!jsonArray.isEmpty()) {
            String[] result = new String[jsonArray.size()];
            for (int i = 0; i < jsonArray.size(); i++) {
                result[i] = jsonArray.getString(i);
            }
            return result;
        } else {
            return null;
        }
    }


    /**
     * 通过 HTTP DNS 获取域名对应的 IP 地址, 并设置 DNS 缓存.
     *
     * @param   domain
     *          域名
     *
     * @param   cacheTime
     *          缓存时间, 单位: 毫秒
     *
     * @return  IP 地址数组
     */
    public static String[] getDomainIpByHttpDnsAndCache(String domain, int cacheTime) {
        String[] domainIpByHttpDns = getDomainIpByHttpDns(domain);
        if (domainIpByHttpDns != null) {
            // 设置 DNS 缓存
            DnsCacheManipulator.setDnsCache(cacheTime, domain, domainIpByHttpDns);
        }
        return domainIpByHttpDns;
    }

}