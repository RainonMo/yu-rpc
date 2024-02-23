package com.yu.provider;

import com.yu.common.model.User;
import com.yu.common.service.UserService;

public class UserServiceImpl implements UserService {

    @Override
    public User getUser(User user) {
        System.out.println("用户名："+user.getName());
        return user;
    }
}
