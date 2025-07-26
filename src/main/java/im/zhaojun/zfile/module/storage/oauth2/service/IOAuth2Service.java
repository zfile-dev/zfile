package im.zhaojun.zfile.module.storage.oauth2.service;

import im.zhaojun.zfile.module.storage.model.dto.OAuth2TokenDTO;

public interface IOAuth2Service {

    /**
     * 访问令牌字段名称
     */
    String ACCESS_TOKEN_FIELD_NAME = "access_token";

    /**
     * 刷新令牌字段名称
     */
    String REFRESH_TOKEN_FIELD_NAME = "refresh_token";

    /**
     * 过期时间字段名称
     */
    String EXPIRES_IN_FIELD_NAME = "expires_in";


    String generateAuthorizationUrl(String clientId, String clientSecret, String redirectUri);

    OAuth2TokenDTO getTokenByCode(String code, String clientId, String clientSecret, String redirectUri);

}
