package com.mmall.dao;

import com.mmall.pojo.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);

    Cart selectCarByUserIdAndProductId(@Param("productId") Integer productId,@Param("UserId") Integer userId);

    List<Cart> selectCartByUserId(Integer userId);

    int getStatusByUserId(Integer userId);

    int deleteByProductIdAndUserId(@Param("UserId") Integer userId,@Param("productIdList")List<String> productIdList);

    int updateAllCheckedByUserId(@Param("UserId") Integer userId,@Param("checked")Integer checked);

    int updateCheckedByUserId(@Param("UserId") Integer userId,@Param("checked")Integer checked,@Param("productId")Integer productId);

    int selectProductCount(Integer userId);

    List<Cart> selectCheckedByUserId(Integer userId);
}