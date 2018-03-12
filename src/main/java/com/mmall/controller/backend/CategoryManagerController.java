package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @author GEMI
 * @date 2018/3/10/0010 12:30
 */
@Controller
@RequestMapping("/manage/category")
public class CategoryManagerController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;

    //添加品类
    @RequestMapping(value = "add_category.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse addCategory(HttpSession session, String categoryName, @RequestParam(value = "parentId", defaultValue = "0") Integer parentId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createBySuccessMessage("用户未登录");
        }
        //校验一下是否为管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
        //处理分类逻辑
            return iCategoryService.addCategory(categoryName, parentId);
        } else {
            return ServerResponse.createByErrorMessage("该用户不是管理员，无权限操作");
        }
    }

    //更新品类名字
    @RequestMapping(value = "update_category_name.do" , method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse updateCategoryName(HttpSession session,Integer categoryId,String categoryName){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createBySuccessMessage("用户未登录");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //更新名称
            return iCategoryService.updateCategoryName(categoryId,categoryName);
        } else {
            return ServerResponse.createByErrorMessage("该用户不是管理员，无权限操作");
        }
    }

    //获取当前分类的子分类(不递归)
    @RequestMapping(value = "get_category.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse getCategory(HttpSession session,@RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createBySuccessMessage("用户未登录");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iCategoryService.getCategoryByParentId(categoryId);
        } else {
            return ServerResponse.createByErrorMessage("该用户不是管理员，无权限操作");
        }
    }

    //获取当前分类的子分类(递归)
    @RequestMapping("get_deep_category.do")
    @ResponseBody
    public ServerResponse getDeepCategory(HttpSession session,@RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createBySuccessMessage("用户未登录");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //获取当前子节点id和递归子节点id
            return iCategoryService.DeepGetCategoryByParentId(categoryId);
        } else {
            return ServerResponse.createByErrorMessage("该用户不是管理员，无权限操作");
        }
    }

}
