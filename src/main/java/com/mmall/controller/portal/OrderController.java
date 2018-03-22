package com.mmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

/**
 * @author GEMI
 * @date 2018/3/21/0021 13:25
 */
@Controller
@RequestMapping("/order")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private IOrderService iOrderService;

    @RequestMapping("pay.do")
    @ResponseBody
    public ServerResponse pay(HttpSession session, HttpServletRequest request, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            ServerResponse.createByErrorMessage("请先登录");
        }
        String path = request.getSession().getServletContext().getRealPath("upload");
        return iOrderService.pay(orderNo, user.getId(), path);
    }

    //回调接口
    @RequestMapping("alipay_callback.do")
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request) {
        Map<String, String> map = Maps.newHashMap();

        Map requestParameterMap = request.getParameterMap();
        //迭代器
        Iterator iterator = requestParameterMap.keySet().iterator();
        while (iterator.hasNext()) {
            String name = (String) iterator.next();
            String[] values = (String[]) requestParameterMap.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            map.put(name, valueStr);
        }
        logger.info("支付宝回调：sing{},trade_status{},参数{}", map.get("sing"), map.get("trade_status"), map.toString());
        //重点，验证回调是不是支付宝发的，并且还要避免重复通知
        //map.remove("sign_type");
        try {
            boolean alipayRSACheckV2 = AlipaySignature.rsaCheckV1(map, Configs.getAlipayPublicKey(), "utf-8", Configs.getSignType());
            if (!alipayRSACheckV2) {
                return ServerResponse.createByErrorMessage("非法请求，已移交司法机关");
            }
        } catch (AlipayApiException e) {
            logger.error("支付宝回调异常", e);
        }

        ServerResponse serverResponse = iOrderService.alipayCallback(map);
        if (serverResponse.isSuccess()) {
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        }
        return Const.AlipayCallback.RESPONSE_FAILED;
    }

    //查询订单是否支付
    @RequestMapping("get_order_status.do")
    @ResponseBody
    public ServerResponse getOrderStatus(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            ServerResponse.createByErrorMessage("请先登录");
        }
        ServerResponse serverResponse= iOrderService.getOrderStatus(orderNo, user.getId());
        if(serverResponse.isSuccess()){
            return ServerResponse.createBySuccess(true);
        }
        return ServerResponse.createBySuccess(false);
    }
}
