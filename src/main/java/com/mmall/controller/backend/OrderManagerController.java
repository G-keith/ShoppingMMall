package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @author GEMI
 * @date 2018/3/28/0028 17:36
 */
@Controller
@RequestMapping("/manage/order")
public class OrderManagerController {

    @Autowired
    private IOrderService iOrderService;

    @Autowired
    private IUserService iUserService;

    @RequestMapping("get_order_detail.do")
    @ResponseBody
    public ServerResponse getOrderDetail(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorMessage("请先登录");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iOrderService.manageGetDetail(orderNo);
        } else {
            return ServerResponse.createByErrorMessage("您不是管理员，无权限");
        }
    }

    @RequestMapping("get_order_list.do")
    @ResponseBody
    public ServerResponse getOrderList(HttpSession session,@RequestParam(value = "pageNum",defaultValue = "1")int pageNum, @RequestParam(value = "pageSize",defaultValue = "10") int pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorMessage("请先登录");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iOrderService.manageGetList(pageNum,pageSize);
        } else {
            return ServerResponse.createByErrorMessage("您不是管理员，无权限");
        }
    }

    @RequestMapping("get_order_search.do")
    @ResponseBody
    public ServerResponse getOrderSearch(HttpSession session,Long orderNo,@RequestParam(value = "pageNum",defaultValue = "1")int pageNum, @RequestParam(value = "pageSize",defaultValue = "10") int pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorMessage("请先登录");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iOrderService.manageSearch(orderNo,pageNum,pageSize);
        } else {
            return ServerResponse.createByErrorMessage("您不是管理员，无权限");
        }
    }
    @RequestMapping("send_goods.do")
    @ResponseBody
    public ServerResponse sendGoods(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorMessage("请先登录");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iOrderService.sendGoods(orderNo);
        } else {
            return ServerResponse.createByErrorMessage("您不是管理员，无权限");
        }
    }

}
