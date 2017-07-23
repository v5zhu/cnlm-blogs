package me.cnlm.commons.response;

/**
 * Created by cnlm.me@qq.com on 2017/7/23.
 */
public class Res {
    private int code;
    private String msg;
    private Object obj;

    public static Res success() {
        return success("操作成功");
    }

    public static Res success(Object obj) {
        if (obj instanceof String) {
            return success((String) obj, null);
        } else {
            return success("操作成功", obj);
        }
    }

    public static Res success(String msg, Object obj) {
        return new Res(200000, msg, obj);
    }

    public static Res failure() {
        return failure("操作失败");
    }

    public static Res failure(Object obj) {
        if (obj instanceof String) {
            return failure((String) obj, null);
        } else {
            return failure("操作成功", obj);
        }
    }

    public static Res failure(String msg, Object obj) {
        return new Res(400000, msg, obj);
    }


    public static Res forbid() {
        return failure("操作禁止");
    }

    public static Res forbid(Object obj) {
        if (obj instanceof String) {
            return failure((String) obj, null);
        } else {
            return failure("操作禁止", obj);
        }
    }

    public static Res forbid(String msg, Object obj) {
        return new Res(403403, msg, obj);
    }


    private Res(int code, String msg, Object obj) {
        this.code = code;
        this.msg = msg;
        this.obj = obj;
    }

    public Res() {
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}
