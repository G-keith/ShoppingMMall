package com.shoppingmmall.service.impl;

import com.shoppingmmall.common.Const;
import com.shoppingmmall.common.ServerResponse;
import com.shoppingmmall.dao.UserMapper;
import com.shoppingmmall.pojo.User;
import com.shoppingmmall.service.IUserServise;
import com.shoppingmmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author GEMI
 * @date 2018/3/6/0006 13:27
 */
@Service("iUserServise")
public class UserServiceImpl implements IUserServise {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0) {
            return ServerResponse.createErrorMessage("用户名不存在");
        }
        //密码登录MD5
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username, md5Password);
        if (user == null) {
            return ServerResponse.createErrorMessage("密码错误");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功", user);
    }

    public ServerResponse<String> register(User user) {
        ServerResponse vaildResponse=this.checkValid(user.getUsername(),Const.USERNAME);
        if(!vaildResponse.isSuccess()){
            return vaildResponse;
        }
        vaildResponse=this.checkValid(user.getEmail(),Const.EMAIL);
        if(!vaildResponse.isSuccess()){
            return vaildResponse;
        }

        user.setRole(Const.Role.ROLE_CUSTOMER);

        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if (resultCount == 0) {
            return ServerResponse.createErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");

    }

    public ServerResponse<String> checkValid(String str, String type) {
        if (org.apache.commons.lang3.StringUtils.isNotBlank(type)) {
            //开始校验
            if (Const.USERNAME.equals(type)) {

                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0) {
                    return ServerResponse.createErrorMessage("用户名已存在");
                }
            }
            if (Const.EMAIL.equals(type)) {
                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0) {
                    return ServerResponse.createErrorMessage("邮箱已存在");
                }
            }

        } else {
            return ServerResponse.createErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }
}