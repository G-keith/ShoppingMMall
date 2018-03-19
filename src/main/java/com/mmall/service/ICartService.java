package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.CartVo;

/**
 * @author GEMI
 * @date 2018/3/15/0015 15:19
 */
public interface ICartService {

    ServerResponse<CartVo> addProduct(Integer count, Integer productId, Integer userId);

    ServerResponse<CartVo> updateProduct(Integer count, Integer productId, Integer userId);

    ServerResponse<CartVo> deleteProduct(String productIds, Integer userId);

    ServerResponse<CartVo> getList(Integer userId);

    ServerResponse setAllProductCheckedOrUnChecked(Integer userId,Integer checked);

    ServerResponse setProductCheckedOrUnChecked(Integer userId,Integer checked,Integer productId);

    ServerResponse getProductCount(Integer userId);
}
