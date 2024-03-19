package com.yu.springbootconsumer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ServiceImplTest {
    @Resource
    private ServiceImplTest serviceImplTest;

    @Test
    void test(){
        serviceImplTest.test();
    }

}