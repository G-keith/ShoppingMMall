package com.mmall.dao;

import com.mmall.pojo.Shipping;
import com.mmall.pojo.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShippingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Shipping record);

    int insertSelective(Shipping record);

    Shipping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Shipping record);

    int updateByPrimaryKey(Shipping record);

    int deleteByUserIdAndShipping(@Param("userId") Integer userId,@Param("shippingId") Integer shippingId);

    int updateByUserIdAndShippingId(Shipping record);

    Shipping selectByUserIdAndShippingId(@Param("userId") Integer userId,@Param("shippingId") Integer shippingId);

    List<Shipping> selectByUserId(Integer userId);
}