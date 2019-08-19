package im.zhaojun.common.model;

import java.io.Serializable;

public class ResultBean implements Serializable {

    private static final long serialVersionUID = -8276264968757808344L;

    private static final int SUCCESS = 0;

    private static final int FAIL = -1;

    private String msg = "操作成功";

    private int code = SUCCESS;

    private Object data;

    private ResultBean() {
        super();
    }

    private ResultBean(String msg, Object data, int code) {
        this.msg = msg;
        this.data = data;
        this.code = code;
    }

    public static ResultBean success() {
        return success("操作成功");
    }

    public static ResultBean success(String msg) {
        return success(msg, null);
    }

    public static ResultBean successData(Object data) {
        return success("操作成功", data);
    }

    public static ResultBean success(Object data) {
        return success("操作成功", data);
    }

    public static ResultBean success(String msg, Object data) {
        return new ResultBean(msg, data, SUCCESS);
    }

    public static ResultBean error(String msg) {
        ResultBean resultBean = new ResultBean();
        resultBean.setCode(FAIL);
        resultBean.setMsg(msg);
        return resultBean;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
