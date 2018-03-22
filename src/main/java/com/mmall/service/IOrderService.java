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
}
