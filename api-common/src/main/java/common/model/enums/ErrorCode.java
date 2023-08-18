package common.model.enums;

/**
 * 错误码
 *
 * 
 */
public enum ErrorCode {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    ACCOUNT_OR_PASSWORD_ERROR(40102, "用户名或密码错误"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    ILLEGAL_ERROR(40301, "请求非法"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    AK_NOT_FOUND(40401, "ak不存在"),
    SK_ERROR(40402, "sk错误"),
    API_NOT_FOUND(40403, "接口不存在或已下线"),
    API_UNDER_CNT(40404, "接口调用次数不足"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    SMS_CODE_ERROR(50002,"验证码或手机号错误"),
    OPERATION_ERROR(50001, "操作失败"),
    API_INVOKE_ERROR(50003, "接口异常");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
