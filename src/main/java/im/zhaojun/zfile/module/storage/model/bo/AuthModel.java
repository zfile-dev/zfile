
package im.zhaojun.zfile.module.storage.model.bo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 又拍云上传认证信息 model
 *
 * @author zhaojun
 */
@Data
@AllArgsConstructor
public class AuthModel {

    /**
     * 上传 url
     */
    private String url;

    /**
     * 上传签名
     */
    private String signature;

    /**
     * 上传策略 base64
     */
    private String policy;

}