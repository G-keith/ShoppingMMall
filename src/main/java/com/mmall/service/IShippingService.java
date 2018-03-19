package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;

/**
 * @author GEMI
 * @date 2018/3/17/0017 19:25
 */
public interface IShippingService {

    ServerResponse addShipping(Integer userId, Shipping shipping);

    ServerResponse deleteShipping(Integer userId, Integer shippingId);

    ServerResponse updateShipping(Integer userId, Shipping shipping);

    ServerResponse selectShipping(Integer userId, Integer shippingId);

    ServerResponse selectAllShipping(Integer userId,Integer pageNum,Integer pageSize);

}
