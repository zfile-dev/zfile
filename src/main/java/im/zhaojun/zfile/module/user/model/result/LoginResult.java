package im.zhaojun.zfile.module.user.model.result;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoginResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private String token;

    private boolean admin;

    public LoginResult(String tokenValue, boolean isAdmin) {
        this.token = tokenValue;
        this.admin = isAdmin;
    }
}
