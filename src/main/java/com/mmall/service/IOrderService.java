package com.mmall.service;

import com.mmall.common.ServerResponse;

import java.util.Map;

/**
 * @author GEMI
 * @date 2018/3/21/0021 14:08
 */
public interface IOrderService {

    ServerResponse pay(Long orderNo, Integer userId, String path);

    ServerResponse alipayCallback(Map<String, String> params);

    ServerResponse getOrderStatus(Long orderNo, Integer userId);

    ServerResponse createOrder(Integer shippingId,Integer userId);

    ServerResponse cancleOrder(Integer userId,Long orderNo);

    ServerResponse getOrderCartProduct(Integer userId);

    ServerResponse getOrderDetail(Integer userId,Long orderNo);

    ServerResponse getOrderList(Integer userId, int pageNum, int pageSize);

    ServerResponse manageGetDetail(Long orderNo);

    ServerResponse manageGetList(int pageNum, int pageSize);

    ServerResponse manageSearch(Long orderNo,int pageNum, int pageSize);

    ServerResponse sendGoods(Long orderNo);
}
