package com.shoppingmmall.controller.portal;

import com.shoppingmmall.common.Const;
import com.shoppingmmall.common.ServerResponse;
import com.shoppingmmall.pojo.User;
import com.shoppingmmall.service.IUserServise;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @author GEMI
 * @date 2018/3/6/0006 13:17
 */
@Controller
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private IUserServise iUserService;

    /**
     * 用户登录
     *
     * @param username
     * @param password
     * @param httpSession
     * @return
     */
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody//序列化成json
    public ServerResponse<User> login(String username, String password, HttpSession httpSession) {
        //service-->mybatis-->dao
        ServerResponse<User> respone = iUserService.login(username, password);
        if (respone.isSuccess()) {
            httpSession.setAttribute(Const.CURRENT_USER, respone.getData());
            ;
        }
        return respone;
    }

    //登出
    @RequestMapping(value = "logout.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session) {
        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createBySuccess();
    }

    //注册
    @RequestMapping(value = "register.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> register(User user) {
        return iUserService.register(user);
    }

    //实时检查
    @RequestMapping(value = "check_valid.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> checkValid(String str, String type) {
        return iUserService.checkValid(str, type);
    }

    //获取用户登录信息
    @RequestMapping(value = "get_user_info.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user!=null){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createErrorMessage("用户未登录，无法获取用户信息");
    }
}
