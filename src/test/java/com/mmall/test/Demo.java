package com.mmall.test;

import org.junit.Test;

import java.math.BigDecimal;

/**
 * @author GEMI
 * @date 2018/3/15/0015 16:48
 */
public class Demo {

    @Test
    public void test1() {
        System.out.println(0.01+0.05);

        BigDecimal b1=new BigDecimal("0.01");
        BigDecimal b2=new BigDecimal("0.05");
        b1.add(b2);
    }
}
