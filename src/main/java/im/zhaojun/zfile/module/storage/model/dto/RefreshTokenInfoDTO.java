package im.zhaojun.zfile.module.storage.model.dto;

import lombok.Data;

import java.util.Date;

@Data
public class RefreshTokenInfoDTO {

    /**
     * 访问令牌，用于访问受保护的资源
     */
    private String accessToken;

    /**
     * 刷新令牌，用于获取新的访问令牌
     */
    private String refreshToken;

    /**
     * 会话令牌，通常用于 AWS 等云存储服务的临时凭证
     */
    private String sessionToken;

    /**
     * 过期时间戳(单位: 秒)
     */
    private Integer expiredAt;

    public Date getExpiredAtDate() {
        if (expiredAt == null) {
            return null;
        }
        // 如果 expiredAt 是 10 位时间戳(秒)
        if (expiredAt > 1_000_000_000) {
            return new Date(expiredAt * 1000L);
        } else {
            // 否则认为 expiredAt 是过期时间(单位: 秒)
            return new Date((expiredAt + System.currentTimeMillis() / 1000) * 1000L);
        }
    }

    public static RefreshTokenInfoDTO success(String accessToken, String refreshToken, String sessionToken, Integer expiredAt) {
        RefreshTokenInfoDTO token = new RefreshTokenInfoDTO();
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setSessionToken(sessionToken);

        if (expiredAt != null) {
            // 如果 expiredAt 是 10 位时间戳(秒)
            if (expiredAt > 1_000_000_000) {
                token.setExpiredAt(expiredAt);
            } else {
                // 否则认为 expiredAt 是过期时间(单位: 秒)
                token.setExpiredAt(expiredAt + (int) (System.currentTimeMillis() / 1000));
            }
        }
        return token;
    }

    public static RefreshTokenInfoDTO success(String accessToken, String refreshToken, Integer expiredAt) {
        return success(accessToken, refreshToken, null, expiredAt);
    }

}
