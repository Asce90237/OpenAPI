package com.wzy.order.dubbo;

import common.dubbo.OrderInnerService;
import com.wzy.order.mapper.ApiOrderMapper;
import common.model.BaseResponse;
import common.Utils.ResultUtils;
import common.model.vo.EchartsVo;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.util.List;

@DubboService
public class OrderInnerServiceImpl implements OrderInnerService {

    @Resource
    private ApiOrderMapper apiOrderMapper;

    /**
     * 获取echarts图中最近7天的交易数
     * @param dateList
     * @return
     */
    @Override
    public BaseResponse getOrderEchartsData(List<String> dateList) {
        List<EchartsVo> list = apiOrderMapper.getOrderEchartsData(dateList);
        return ResultUtils.success(list);
    }
}
