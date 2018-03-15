package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.CartVo;

/**
 * @author GEMI
 * @date 2018/3/15/0015 15:19
 */
public interface ICartService {

    ServerResponse<CartVo> addProduct(Integer count, Integer productId, Integer userId);
}
