package com.mmall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author GEMI
 * @date 2018/3/15/0015 15:20
 */
@Service("iCartService")
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    //根据前端的要求，返回相应的数据格式，泛型未自己定义的一个类
    public ServerResponse<CartVo> addProduct(Integer count, Integer productId, Integer userId) {
        if (productId == null || count == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCarByUserIdAndProductId(productId, userId);
        //cart==null，说明购物车中没有该类物品，直接添加即可；否则在购物车中加上相应的数量
        if (cart == null) {
            Cart cartItem = new Cart();
            cartItem.setUserId(userId);
            cartItem.setProductId(productId);
            cartItem.setQuantity(count);
            cartItem.setChecked(Const.Cart.CHECKED);
            cartMapper.insert(cartItem);
        } else {
            count = cart.getQuantity() + count;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        return getList(userId);
    }

    @Override
    public ServerResponse<CartVo> updateProduct(Integer count, Integer productId, Integer userId) {
        if (productId == null || count == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCarByUserIdAndProductId(productId, userId);
        if (cart != null) {
            cart.setQuantity(count);
        }
        cartMapper.updateByPrimaryKey(cart);
        return this.getList(userId);
    }

    public ServerResponse<CartVo> getList(Integer userId) {
        CartVo cartVo = this.getCartProductVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    public ServerResponse<CartVo> deleteProduct(String productIds, Integer userId) {
        //利用Guauab将string转成list集合
        List<String> productIdList = Splitter.on(",").splitToList(productIds);//将productIds根据“，”分割成集合
        if (CollectionUtils.isEmpty(productIdList)) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.deleteByProductIdAndUserId(userId, productIdList);
        return getList(userId);
    }


    public ServerResponse setAllProductCheckedOrUnChecked(Integer userId, Integer checked) {
        cartMapper.updateAllCheckedByUserId(userId, checked);
        return getList(userId);
    }

    public ServerResponse setProductCheckedOrUnChecked(Integer userId, Integer checked, Integer productId) {
        cartMapper.updateCheckedByUserId(userId, checked, productId);
        return getList(userId);
    }

    public ServerResponse getProductCount(Integer userId) {
        if (userId == null) {
            return ServerResponse.createBySuccess("0");
        }
        cartMapper.selectProductCount(userId);
        return getList(userId);
    }


    private CartVo getCartProductVoLimit(Integer userId) {
        CartVo cartVo = new CartVo();

        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        List<CartProductVo> cartProductVoList = Lists.newArrayList();

        BigDecimal cartTotalPrice = new BigDecimal("0");

        for (Cart cartItem : cartList) {
            CartProductVo cartProductVo = new CartProductVo();
            cartProductVo.setId(cartItem.getId());
            cartProductVo.setUserId(cartItem.getUserId());
            cartProductVo.setProductId(cartItem.getProductId());
            cartProductVo.setProductChecked(cartItem.getChecked());

            Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
            if (product != null) {
                cartProductVo.setProductName(product.getName());
                cartProductVo.setProductMainImage(product.getMainImage());
                cartProductVo.setProductSubtitle(product.getSubtitle());
                cartProductVo.setProductPrice(product.getPrice());
                cartProductVo.setProductStatus(product.getStatus());
                cartProductVo.setProductStock(product.getStock());

                //判断购物车和库存大小
                int buyMaxCount = 0;
                if (product.getStock() >= cartItem.getQuantity()) {
                    buyMaxCount = cartItem.getQuantity();
                    cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                } else {
                    buyMaxCount = product.getStock();
                    cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                    Cart cart = new Cart();
                    cart.setId(cartItem.getId());
                    cart.setQuantity(buyMaxCount);
                    cartMapper.updateByPrimaryKeySelective(cart);
                }
                cartProductVo.setQuantity(buyMaxCount);

                //同类商品总价，封装BigDecimal类，避免运算小数时出现的问题
                cartProductVo.setProductTotalPrice(BigDecimalUtil.multiply(buyMaxCount, product.getPrice().doubleValue()));
            }

            //计算购物车总价时，判断是否商品被勾选状态
            if (cartItem.getChecked() == Const.Cart.CHECKED) {
                cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(), cartProductVo.getProductTotalPrice().doubleValue());
            }
            cartProductVoList.add(cartProductVo);
        }

        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setAllChecked(this.getStatus(userId));
        cartVo.setImgHost(PropertiesUtil.getValue("ftp.server.http.prefix"));
        return cartVo;
    }

    private boolean getStatus(Integer userId) {
        if (userId == null) {
            return false;
        }
        return cartMapper.getStatusByUserId(userId) == 0;
    }
}
