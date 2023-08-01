package common.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Asce
 */
@Data
public class LockChargingVo implements Serializable {

    /**
     * 接口id
     */
    private Long interfaceid;

    /**
     * 购买数量
     */
    private Long orderNum;
}
