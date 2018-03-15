package com.mmall.service.impl;

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

    public ServerResponse<CartVo> addProduct(Integer count, Integer productId, Integer userId) {
        if (productId == null || count==null) {
return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCarByUserIdAndProductId(productId, userId);
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

    public ServerResponse<CartVo> getList(Integer userId) {
        CartVo cartVo = this.getCartProductVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
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
                cartProductVo.setProductTotalPrice(BigDecimalUtil.multiply(buyMaxCount, product.getPrice().doubleValue()));
            }

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
        return cartMapper.getStatusByUserId(userId) == 0;
    }
}
