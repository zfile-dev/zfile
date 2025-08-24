package im.zhaojun.zfile.module.storage.model.result;

import lombok.Data;

@Data
public class Open115GetStatusResult {

    private String status;

    private String msg;

    private String accessToken;

    private String refreshToken;

    private Integer expiredAt;

    public static Open115GetStatusResult error(String msg) {
        Open115GetStatusResult open115GetStatusResult = new Open115GetStatusResult();
        open115GetStatusResult.setStatus("error");
        open115GetStatusResult.setMsg(msg);
        return open115GetStatusResult;
    }

    public static Open115GetStatusResult waiting() {
        Open115GetStatusResult open115GetStatusResult = new Open115GetStatusResult();
        open115GetStatusResult.setStatus("waiting");
        return open115GetStatusResult;
    }

    public static Open115GetStatusResult scanning(String msg) {
        Open115GetStatusResult open115GetStatusResult = new Open115GetStatusResult();
        open115GetStatusResult.setStatus("scanning");
        open115GetStatusResult.setMsg(msg);
        return open115GetStatusResult;
    }

    public static Open115GetStatusResult success(String accessToken, String refreshToken, Integer expiredAt) {
        Open115GetStatusResult open115GetStatusResult = new Open115GetStatusResult();
        open115GetStatusResult.setStatus("success");
        open115GetStatusResult.setAccessToken(accessToken);
        open115GetStatusResult.setRefreshToken(refreshToken);
        open115GetStatusResult.setExpiredAt(expiredAt);
        return open115GetStatusResult;
    }


}
