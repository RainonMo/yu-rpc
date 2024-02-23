package com.yu.consumer;

import com.yu.common.model.User;
import com.yu.common.service.UserService;
import com.yu.rpc.proxy.ServiceProxyFactory;

public class Consumer {
    public static void main(String[] args) {
        UserService userService =   ServiceProxyFactory.getProxy(UserService.class);

        User user = new User();
        user.setName("yubi");

        User newUser = userService.getUser(user);
        if(newUser != null){
            System.out.println(newUser.getName());
        }else {
            System.out.println("user == null");
        }


    }
}
