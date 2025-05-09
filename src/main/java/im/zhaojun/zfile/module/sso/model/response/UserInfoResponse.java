package im.zhaojun.zfile.module.sso.model.response;

import lombok.Data;

@Data
public class UserInfoResponse
{
    private String sub;
    private String name;
    private String picture;
    private String email;
}

