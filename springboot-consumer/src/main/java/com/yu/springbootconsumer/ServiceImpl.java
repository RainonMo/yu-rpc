package com.yu.springbootconsumer;

import com.yu.common.model.User;
import com.yu.common.service.UserService;
import com.yu.rpc.springboot.starter.annotation.RpcReference;
import org.springframework.stereotype.Service;

@Service
public class ServiceImpl {

    @RpcReference
    private UserService userService;

    public void test(){
        User user = new User();
        user.setName("yupi");

        User resultUser = userService.getUser(user);
        System.out.println(resultUser.getName());
    }
}
