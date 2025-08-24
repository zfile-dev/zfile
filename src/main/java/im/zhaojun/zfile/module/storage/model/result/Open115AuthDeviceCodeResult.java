package im.zhaojun.zfile.module.storage.model.result;

import lombok.Data;

@Data
public class Open115AuthDeviceCodeResult {

    private String uid;

    private Integer time;

    private String sign;

    private String codeVerifier;

    private String qrcode;

}
