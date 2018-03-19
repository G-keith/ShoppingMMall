package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author GEMI
 * @date 2018/3/17/0017 19:26
 */
@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    public ServerResponse addShipping(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);
        int rowCount = shippingMapper.insert(shipping);
        if (rowCount > 0) {
            //添加成功立马拿到id，在mabatis里面配置
            Map shippingMap = Maps.newHashMap();
            shippingMap.put("shippingId", shipping.getId());
            return ServerResponse.createBySuccess("添加成功", shippingMap);
        }
        return ServerResponse.createByErrorMessage("添加失败");
    }

    //删除时，避免横向越权问题，删除时判断用户
    public ServerResponse deleteShipping(Integer userId, Integer shippingId) {
        int rowCount = shippingMapper.deleteByUserIdAndShipping(userId, shippingId);
        if (rowCount > 0) {
            return ServerResponse.createBySuccessMessage("删除地址成功");

        }
        return ServerResponse.createByErrorMessage("删除地址失败");
    }

    public ServerResponse updateShipping(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);
        int rowCount = shippingMapper.updateByUserIdAndShippingId(shipping);
        if (rowCount > 0) {
            return ServerResponse.createBySuccessMessage("更新地址成功");
        }
        return ServerResponse.createByErrorMessage("更新地址失败");
    }

    public ServerResponse selectShipping(Integer userId, Integer shippingId) {
        Shipping shippingItem = shippingMapper.selectByUserIdAndShippingId(userId, shippingId);
        if(shippingItem==null){
            return ServerResponse.createByErrorMessage("查询地址失败");
        }
        return ServerResponse.createBySuccess(shippingItem);

    }

    public ServerResponse selectAllShipping(Integer userId,Integer pageNum,Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> shippingList=shippingMapper.selectByUserId(userId);
        if(shippingList!=null){
            PageInfo pageInfo=new PageInfo(shippingList);
            return ServerResponse.createBySuccess(pageInfo);
        }
        return ServerResponse.createByErrorMessage("查询地址失败");
    }
}
