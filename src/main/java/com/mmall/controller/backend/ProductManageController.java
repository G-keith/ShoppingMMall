package com.mmall.controller.backend;

import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.util.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @author GEMI
 * @date 2018/3/12/0012 13:39
 */
@Controller
@RequestMapping("/manage/product")
public class ProductManageController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private IProductService iProductService;

    @Autowired
    private IFileService iFileService;

    //添加或更新产品
    @RequestMapping(value = "product_save.do")
    @ResponseBody
    public ServerResponse productSave(HttpSession session, Product product) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        //判断有没有登录
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "请先登录");
        }
        //判断是不是管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //业务逻辑
            return iProductService.SaveOrUpdateProduct(product);
        } else {
            return ServerResponse.createBySuccessMessage("该用户不是管理员，无权限操作");
        }
    }

    //更改产品销售状态
    @RequestMapping(value = "set_sal_status.do")
    @ResponseBody
    public ServerResponse setSalStatus(HttpSession session, Integer productId, Integer status) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "请先登录");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //业务逻辑
            return iProductService.setSalStatus(productId, status);
        } else {
            return ServerResponse.createByErrorMessage("该用户不是管理员，无权限操作");
        }
    }

    //获取产品详情
    @RequestMapping(value = "get_product_detail.do")
    @ResponseBody
    public ServerResponse getProductDetail(HttpSession session, Integer productId) {

        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "请先登录");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //业务逻辑
            return iProductService.manageProductDetail(productId);
        } else {
            return ServerResponse.createByErrorMessage("该用户不是管理员，无权限操作");
        }
    }


    //获取商品列表(默认值，第一页，一页十条)
    @RequestMapping(value = "get_list.do")
    @ResponseBody
    public ServerResponse getList(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "请先登录");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //业务逻辑
            return iProductService.getProductList(pageNum, pageSize);
        } else {
            return ServerResponse.createByErrorMessage("该用户不是管理员，无权限操作");
        }
    }

    //根据商品名称或ID搜索商品
    @RequestMapping(value = "product_search.do")
    @ResponseBody
    public ServerResponse productSearch(HttpSession session, String productName, Integer productId, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "请先登录");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //业务逻辑
            return iProductService.searchProduct(productName, productId, pageNum, pageSize);
        } else {
            return ServerResponse.createByErrorMessage("该用户不是管理员，无权限操作");
        }
    }

    //文件上传
    @RequestMapping(value = "upload.do")
    @ResponseBody
    public ServerResponse<Map> upload(@RequestParam(value = "upload_file", required = false) MultipartFile file, HttpServletRequest request, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "请先登录");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //业务逻辑
            String path = request.getSession().getServletContext().getRealPath("upload");
            System.out.println(path + "-----------------------------------------------------------------");
            String targetFileName = iFileService.upload(file, path);
            System.out.println(targetFileName + "-----------------------------------------------------------------");
            String url = PropertiesUtil.getValue("ftp.server.http.prefix") + targetFileName;
            Map map = Maps.newHashMap();
            map.put("uri", targetFileName);
            map.put("url", url);
            return ServerResponse.createBySuccess(map);
        } else {
            return ServerResponse.createByErrorMessage("该用户不是管理员，无权限操作");
        }
    }

    //富文本图片上传
    //富文本中对于返回值格式有特殊的要求，参考simditor官方文档
    @RequestMapping(value = "img_upload.do")
    @ResponseBody
    public Map imgUpload(@RequestParam(value = "upload_file", required = false) MultipartFile file, HttpServletRequest request, HttpSession session,HttpServletResponse response) {
        Map resultMap = Maps.newHashMap();
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            resultMap.put("SUCCESS", false);
            resultMap.put("msg", "请先登录");
            return resultMap;
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //业务逻辑
            String path = request.getSession().getServletContext().getRealPath("upload");
            System.out.println(path + "-----------------------------------------------------------------");
            String targetFileName = iFileService.upload(file, path);
            System.out.println(targetFileName + "-----------------------------------------------------------------");
            if (StringUtils.isBlank(targetFileName)) {
                resultMap.put("SUCCESS", false);
                resultMap.put("msg", "上传失败");
            }
            String url = PropertiesUtil.getValue("ftp.server.http.prefix") + targetFileName;
            resultMap.put("SUCCSEE", true);
            resultMap.put("msg", "上传成功");
            resultMap.put("file_path", url);
            response.addHeader("Access-Control-Allow-Headers", "X-File-Name");
            return resultMap;
        } else {
            resultMap.put("SUCCESS", false);
            resultMap.put("msg", "改用户不是管理员，无权限操作");
            return resultMap;
        }
    }
}
