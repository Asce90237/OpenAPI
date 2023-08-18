package common.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * echarts需要返回的数据
 * @author Asce
 */
@Data
public class EchartsVo implements Serializable {
    private Long count;

    private String date;
}
