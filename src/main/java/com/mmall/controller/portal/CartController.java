package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @author GEMI
 * @date 2018/3/15/0015 15:15
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private ICartService iCartService;

    @RequestMapping("add_product.do")
    @ResponseBody
    public ServerResponse addProduct(HttpSession session, Integer count, Integer productId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorMessage("请先登录");
        }
        return iCartService.addProduct(user.getId(), productId, count);
    }

    @RequestMapping("update_product.do")
    @ResponseBody
    public ServerResponse updateProduct(HttpSession session, Integer count, Integer productId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorMessage("请先登录");
        }
        return iCartService.updateProduct(user.getId(), productId, count);
    }

    @RequestMapping("delete_product.do")
    @ResponseBody
    public ServerResponse deleteProduct(HttpSession session, String productIds) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorMessage("请先登录");
        }
        return iCartService.deleteProduct(productIds, user.getId());
    }

    @RequestMapping("get_product.do")
    @ResponseBody
    public ServerResponse getProduct(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorMessage("请先登录");
        }
        return iCartService.getList(user.getId());
    }

    @RequestMapping("set_all_product_checked.do")
    @ResponseBody
    public ServerResponse setAllProductChecked(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorMessage("请先登录");
        }
        return iCartService.setAllProductCheckedOrUnChecked(user.getId(), Const.Cart.CHECKED);
    }

    @RequestMapping("set_all_product_unchecked.do")
    @ResponseBody
    public ServerResponse setAllProductUnChecked(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorMessage("请先登录");
        }
        return iCartService.setAllProductCheckedOrUnChecked(user.getId(), Const.Cart.UN_CHECKED);
    }

    @RequestMapping("set_product_checked.do")
    @ResponseBody
    public ServerResponse setProductChecked(HttpSession session, Integer productId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorMessage("请先登录");
        }
        return iCartService.setProductCheckedOrUnChecked(user.getId(), Const.Cart.CHECKED, productId);
    }

    @RequestMapping("set_product_unchecked.d0")
    @ResponseBody
    public ServerResponse setProductUnChecked(HttpSession session, Integer productId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorMessage("请先登录");
        }
        return iCartService.setProductCheckedOrUnChecked(user.getId(), Const.Cart.UN_CHECKED, productId);
    }

    @RequestMapping("get_product_count.do")
    @ResponseBody
    public ServerResponse getProductCount(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorMessage("请先登录");
        }
        return iCartService.getProductCount(user.getId());
    }
}
