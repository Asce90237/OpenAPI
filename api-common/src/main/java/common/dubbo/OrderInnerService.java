package common.dubbo;

import common.model.BaseResponse;

import java.util.List;

public interface OrderInnerService {

    /**
     * 获取echarts图中最近7天的交易数
     * @return
     */
    BaseResponse getOrderEchartsData(List<String> dateList);
}
