package com.wzy.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wzy.order.model.to.ApiOrderCancelDto;
import com.wzy.order.model.to.ApiOrderDto;
import com.wzy.order.model.to.ApiOrderStatusInfoDto;
import com.wzy.order.model.vo.ApiOrderStatusVo;
import com.wzy.order.service.ApiOrderService;
import common.model.BaseResponse;
import common.model.vo.OrderSnVo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ExecutionException;

/**
 * @author Asce
 */
@RestController
@Api("订单接口")
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private ApiOrderService apiOrderService;

    /**
     * 获取全站成功交易数
     * @return
     */
    @GetMapping("/getSuccessOrder")
    public BaseResponse getSuccessOrder(){
        return apiOrderService.getSuccessOrderCnt();
    }

    /**
     * 获取当前登录用户的status订单信息
     * @param statusInfoDto
     * @param request
     * @return
     */
    @PostMapping("/getCurrentOrderInfo")
    public BaseResponse<Page<ApiOrderStatusVo>> getCurrentOrderInfo(ApiOrderStatusInfoDto statusInfoDto, HttpServletRequest request){
        return apiOrderService.getCurrentOrderInfo(statusInfoDto,request);
    }

    /**
     * 生成防重令牌：保证创建订单的接口幂等性
     * @param id
     * @param response
     * @return
     */
    @GetMapping("/generateToken")
    public BaseResponse generateToken(Long id,HttpServletResponse response){
        return apiOrderService.generateToken(id,response);
    }


    /**
     * 创建订单
     * @param apiOrderDto
     * @return
     */
    @PostMapping("/generateOrderSn")
    public BaseResponse<OrderSnVo> generateOrderSn(ApiOrderDto apiOrderDto, HttpServletRequest request, HttpServletResponse response) throws ExecutionException, InterruptedException {
        return apiOrderService.generateOrderSn(apiOrderDto,request,response);
    }


    /**
     * 取消订单
     * @param apiOrderCancelDto
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/cancelOrderSn")
    public BaseResponse cancelOrderSn(ApiOrderCancelDto apiOrderCancelDto, HttpServletRequest request, HttpServletResponse response) {
        return apiOrderService.cancelOrderSn(apiOrderCancelDto,request,response);
    }
}
