package common.constant;

/**
 * 通用常量
 *
 * 
 */
public interface CommonConstant {

    /**
     * 升序
     */
    String SORT_ORDER_ASC = "ascend";

    /**
     * 降序
     */
    String SORT_ORDER_DESC = "descend";

    /**
     * 网关接口调用通用返回异常
     */
    String INVOKE_ERROR = "{\"code\":50003,\"message\":\"接口异常，请稍后再试或联系管理员处理！\"}";

    String JWT_CACHE_PREFIX = "login:";

    String TOKEN_EXP_TIME = "final";
}
