package com.shoppingmmall.service;

import com.shoppingmmall.common.ServerResponse;
import com.shoppingmmall.pojo.User;

/**
 * @author GEMI
 * @date 2018/3/6/0006 13:25
 */
public interface IUserServise {

    ServerResponse<User> login(String name, String password);

    ServerResponse<String> register(User user);

    ServerResponse<String> checkValid(String str, String type);

}
