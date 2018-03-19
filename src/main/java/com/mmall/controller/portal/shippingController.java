package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;
import com.mmall.pojo.User;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @author GEMI
 * @date 2018/3/17/0017 19:19
 */
@Controller
@RequestMapping("/shipping")
public class shippingController {


    @Autowired
    private IShippingService iShippingService;

    @RequestMapping("add_shipping.do")
    @ResponseBody
    public ServerResponse addShipping(HttpSession session, Shipping shipping) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            ServerResponse.createByErrorMessage("请先登录");
        }
        return iShippingService.addShipping(user.getId(), shipping);
    }

    @RequestMapping("delete_shipping.do")
    @ResponseBody
    public ServerResponse deleteShipping(HttpSession session, Integer shippingId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            ServerResponse.createByErrorMessage("请先登录");
        }
        return iShippingService.deleteShipping(user.getId(), shippingId);
    }

    @RequestMapping("update_shipping.do")
    @ResponseBody
    public ServerResponse updateShipping(HttpSession session, Shipping shipping) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            ServerResponse.createByErrorMessage("请先登录");
        }
        return iShippingService.updateShipping(user.getId(), shipping);
    }

    @RequestMapping("select_shipping.do")
    @ResponseBody
    public ServerResponse selectShipping(HttpSession session, Integer shippingId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            ServerResponse.createByErrorMessage("请先登录");
        }
        return iShippingService.selectShipping(user.getId(), shippingId);
    }

    @RequestMapping("select_all_shipping.do")
    @ResponseBody
    public ServerResponse selectAllShipping(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum, @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            ServerResponse.createByErrorMessage("请先登录");
        }
        return iShippingService.selectAllShipping(user.getId(),pageNum,pageSize);
    }

}
