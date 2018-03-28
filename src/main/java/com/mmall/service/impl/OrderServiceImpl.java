package com.mmall.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.FTPUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderProductVo;
import com.mmall.vo.OrderVo;
import com.mmall.vo.ShippingVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.aspectj.weaver.ast.Or;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author GEMI
 * @date 2018/3/21/0021 14:08
 */
@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private PayInfoMapper payInfoMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ShippingMapper shippingMapper;

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    public ServerResponse pay(Long orderNo, Integer userId, String path) {
        Map<String, String> mapResult = Maps.newHashMap();
        Order order = orderMapper.selectByUserIdAndOrderNo(orderNo, userId);
        if (order == null) {
            return ServerResponse.createByErrorMessage("订单信息不存在");
        }
        mapResult.put("orderNo", String.valueOf(order.getOrderNo()));
        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("订单号：").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元
        String body = new StringBuilder().append("订单总金额为").append(totalAmount).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        List<OrderItem> orderItemList = orderItemMapper.selectByUserIdAndOrderno(orderNo, userId);
        for (OrderItem orderItem : orderItemList) {
            GoodsDetail goods1 = GoodsDetail.newInstance(orderItem.getProductId().toString(), orderItem.getProductName(),
                    BigDecimalUtil.multiply(orderItem.getCurrentUnitPrice().doubleValue(), new Double(100).doubleValue()).longValue(),
                    orderItem.getQuantity());
            goodsDetailList.add(goods1);
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getValue("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        AlipayTradeService tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                File file = new File(path);
                if (!file.exists()) {
                    file.setWritable(true);
                    file.mkdirs();
                }

                // 需要修改为运行机器上的路径
                //二维码路径
                //String qrPath = String.format(path + "/qr-%s.png", response.getOutTradeNo());
                String qrPath = String.format("F:/Photo/img" + "/qr-%s.png", response.getOutTradeNo());
                //二维码文件名
                String qrName = String.format("/qr-%s.png", response.getOutTradeNo());

                logger.info(response.getQrCode());
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);

                File targetFile = new File(path, qrName);
                try {
                    //二维码上传至ftp服务器
                    FTPUtil.uploadFile(Lists.newArrayList(targetFile));
                } catch (IOException e) {
                    logger.error("二维码上传异常");
                    e.printStackTrace();
                }
                logger.info("qrPath:" + qrPath);

                //拼接FTP服务器地址
                String qrurl = PropertiesUtil.getValue("ftp.server.http.prefix") + qrName;
                logger.info(qrurl);
                mapResult.put("qrurl", qrurl);
                return ServerResponse.createBySuccess(mapResult);
            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage("支付宝预下单失败!!!");

            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，预下单状态未知!!!");

            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }
    }

    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }


    public ServerResponse alipayCallback(Map<String, String> params) {
        Long orderNo = Long.parseLong(params.get("out_trade_no"));
        String treadNo = params.get("trade_no");
        String status = params.get("trade_status");
        Order order = orderMapper.selectByBorderNo(orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("该订单不存在");
        }
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
            return ServerResponse.createBySuccess("支付宝重复回调");
        }
        if (Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(status)) {
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            order.setStatus(Const.OrderStatusEnum.PAID.getCode());
            orderMapper.updateByPrimaryKeySelective(order);
        }
        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(treadNo);
        payInfo.setPlatformStatus(status);
        payInfoMapper.insert(payInfo);

        return ServerResponse.createBySuccess();
    }

    public ServerResponse getOrderStatus(Long orderNo, Integer userId) {
        Order order = orderMapper.selectByUserIdAndOrderNo(orderNo, userId);
        if (order == null) {
            return ServerResponse.createByErrorMessage("订单信息不存在");
        }
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    public ServerResponse createOrder(Integer shippingId, Integer userId) {

        //从购物车拿到商品信息
        List<Cart> cartList = cartMapper.selectCheckedByUserId(userId);

        //得到订单详情信息
        ServerResponse serverResponse = this.getOrderItem(userId, cartList);
        if (!serverResponse.isSuccess()) {
            return serverResponse;
        }
        List<OrderItem> orderItems = (List<OrderItem>) serverResponse.getData();
        BigDecimal totalPrice = this.getTotalPrice(orderItems);
        //生成订单
        Order order = this.assembOrder(userId, shippingId, totalPrice);
        if (order == null) {
            return ServerResponse.createByErrorMessage("创建订单失败");
        }
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderNo(order.getOrderNo());
        }

        //mybatis批量插入
        orderItemMapper.batchInsert(orderItems);

        //修改库存
        this.reduceProductStock(orderItems);
        //清空购物车
        this.clearCart(cartList);

        //返回给前端数据
        OrderVo orderVo = this.assembOrderVo(order, orderItems);
        return ServerResponse.createBySuccess(orderVo);
    }

    //组装ordervo对象数据

    private OrderVo assembOrderVo(Order order, List<OrderItem> orderItemList) {
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setShippingId(order.getShippingId());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getValue());
        orderVo.setStatus(order.getStatus());
        orderVo.setPostage(order.getPostage());
        orderVo.setStatusDesc(Const.OrderStatusEnum.codeOf(order.getStatus()).getValue());
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if (shipping != null) {
            orderVo.setReceiveName(shipping.getReceiverName());
            orderVo.setShippingVo(this.assembShippingVo(shipping));
        }
        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setImgHost(PropertiesUtil.getValue("ftp.server.http.prefix"));
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        for (OrderItem orderItem : orderItemList) {
            OrderItemVo orderItemVo = this.assembOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        orderVo.setOrderItemVoList(orderItemVoList);
        return orderVo;

    }

    private OrderItemVo assembOrderItemVo(OrderItem orderItem) {
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());
        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
        return orderItemVo;
    }

    private ShippingVo assembShippingVo(Shipping shipping) {
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        return shippingVo;
    }

    private void clearCart(List<Cart> cartList) {
        for (Cart cart : cartList) {
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }

    private void reduceProductStock(List<OrderItem> orderItemList) {
        for (OrderItem orderItem : orderItemList) {
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock() - orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }

    private Order assembOrder(Integer userId, Integer shippingId, BigDecimal totalPrice) {
        Order order = new Order();
        order.setOrderNo(this.getOrderNo());
        order.setUserId(userId);
        order.setShippingId(shippingId);
        order.setPayment(totalPrice);
        order.setPostage(0);
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        int rowCount = orderMapper.insert(order);
        if (rowCount > 0) {
            return order;
        }
        return null;
    }

    private Long getOrderNo() {
        Long orderNo = System.currentTimeMillis() + new Random().nextInt(100);
        return orderNo;
    }


    private BigDecimal getTotalPrice(List<OrderItem> orderItems) {
        BigDecimal bigDecimal = new BigDecimal("0");
        for (OrderItem orderItem : orderItems) {
            bigDecimal = BigDecimalUtil.add(bigDecimal.doubleValue(), orderItem.getTotalPrice().doubleValue());
        }
        return bigDecimal;
    }

    private ServerResponse getOrderItem(Integer userId, List<Cart> cartList) {
        List<OrderItem> orderItemList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(cartList)) {
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        //检验商品是否在售和数量比较
        for (Cart cart : cartList) {
            OrderItem orderItem = new OrderItem();
            Product product = productMapper.selectByPrimaryKey(cart.getProductId());
            if (Const.ProductStatusEnum.ON_SALE.getCode() != product.getStatus()) {
                return ServerResponse.createByErrorMessage("该产品已下架");
            }
            if (cart.getQuantity() > product.getStock()) {
                return ServerResponse.createByErrorMessage("产品库存不足");
            }
            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setTotalPrice(BigDecimalUtil.multiply(product.getPrice().doubleValue(), cart.getQuantity()));
            orderItemList.add(orderItem);
        }
        return ServerResponse.createBySuccess(orderItemList);

    }


    public ServerResponse cancleOrder(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderNo(orderNo, userId);
        if (order == null) {
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        if (order.getStatus() != Const.OrderStatusEnum.NO_PAY.getCode()) {
            return ServerResponse.createByErrorMessage("已付款，无法取消");
        }

        Order updateOrder = new Order();
        updateOrder.setUserId(userId);
        updateOrder.setStatus(Const.OrderStatusEnum.CANCELED.getCode());
        int rowCount = orderMapper.updateByPrimaryKeySelective(updateOrder);
        if (rowCount > 0) {
            return ServerResponse.createBySuccess("取消订单成功");
        }
        return ServerResponse.createByErrorMessage("取消订单失败");
    }

    public ServerResponse getOrderCartProduct(Integer userId) {
        List<Cart> cartList = cartMapper.selectCheckedByUserId(userId);
        ServerResponse serverResponse = this.getOrderItem(userId, cartList);
        if (!serverResponse.isSuccess()) {
            return serverResponse;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getData();
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        BigDecimal bigDecimal = new BigDecimal("0");
        for (OrderItem orderItem : orderItemList) {
            bigDecimal = BigDecimalUtil.add(bigDecimal.doubleValue(), orderItem.getTotalPrice().doubleValue());
            orderItemVoList.add(this.assembOrderItemVo(orderItem));
        }
        OrderProductVo orderProductVo = new OrderProductVo();
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setImgHost(PropertiesUtil.getValue("ftp.server.http.prefix"));
        orderProductVo.setProductTotalPrice(bigDecimal);
        return ServerResponse.createBySuccess(orderProductVo);
    }

    public ServerResponse getOrderDetail(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderNo(orderNo, userId);
        if (order != null) {
            List<OrderItem> orderItemList = orderItemMapper.selectByUserIdAndOrderno(orderNo, userId);
            OrderVo orderVo = this.assembOrderVo(order, orderItemList);
            return ServerResponse.createBySuccess(orderVo);

        }
        return ServerResponse.createByErrorMessage("查询失败");
    }

    public ServerResponse getOrderList(Integer userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectByUserId(userId);
        List<OrderVo> orderVoList = this.assembOrderVoList(orderList, userId);
        PageInfo result = new PageInfo(orderList);
        result.setList(orderVoList);
        return ServerResponse.createBySuccess(result);
    }

    private List<OrderVo> assembOrderVoList(List<Order> orderList, Integer userId) {
        List<OrderVo> orderVoList = Lists.newArrayList();
        for (Order order : orderList) {
            List<OrderItem> orderItemList = Lists.newArrayList();
            if (userId == null) {
                //管理员查询，不需要用户名
                orderItemList = orderItemMapper.selectByOrderNo(order.getOrderNo());
            } else {
                orderItemList = orderItemMapper.selectByUserIdAndOrderno(order.getOrderNo(), userId);
            }
            OrderVo orderVo = this.assembOrderVo(order, orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }

    public ServerResponse manageGetDetail(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order != null) {
            List<OrderItem> orderItemList = orderItemMapper.selectByUserIdAndOrderno(orderNo, order.getUserId());
            OrderVo orderVo = this.assembOrderVo(order, orderItemList);
            return ServerResponse.createBySuccess(orderVo);
        }
        return ServerResponse.createByErrorMessage("查询失败");
    }

    public ServerResponse manageGetList(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectAllOrder();
        List<OrderVo> orderVoList = this.assembOrderVoList(orderList, null);
        PageInfo result = new PageInfo(orderList);
        result.setList(orderVoList);
        return ServerResponse.createBySuccess(result);

    }

    public ServerResponse manageSearch(Long orderNo, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order != null) {
            List<OrderItem> orderItemList = orderItemMapper.selectByUserIdAndOrderno(orderNo, order.getUserId());
            OrderVo orderVo = this.assembOrderVo(order, orderItemList);
            PageInfo result = new PageInfo(Lists.newArrayList(order));
            result.setList(Lists.newArrayList(orderVo));
            return ServerResponse.createBySuccess(result);
        }
        return ServerResponse.createByErrorMessage("查询失败");
    }

    public ServerResponse sendGoods(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order != null) {
            if (order.getStatus() == Const.OrderStatusEnum.PAID.getCode()) {
                order.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
                order.setSendTime(new Date());
                orderMapper.updateByPrimaryKeySelective(order);
                return ServerResponse.createBySuccess("发货成功");
            }
        }
        return ServerResponse.createByErrorMessage("查询失败");
    }

}
