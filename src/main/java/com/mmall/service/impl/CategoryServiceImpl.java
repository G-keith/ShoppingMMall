package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * @author GEMI
 * @date 2018/3/10/0010 13:00
 */
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    //打印日志
    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categorymapper;

    public ServerResponse addCategory(String categoryName, Integer parentId) {
        if (parentId == null || StringUtils.isBlank(categoryName)) {
            return ServerResponse.createByErrorMessage("添加品类参数错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);
        int rowCount = categorymapper.insert(category);
        if (rowCount > 0) {
            return ServerResponse.createBySuccessMessage("添加品类成功");
        } else {
            return ServerResponse.createByErrorMessage("添加品类失败");
        }
    }

    public ServerResponse updateCategoryName(Integer categoryId, String categoryName) {
        if (categoryId == null || StringUtils.isBlank(categoryName)) {
            return ServerResponse.createByErrorMessage("更新品类参数错误");
        }

        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        int rowCount = categorymapper.updateByPrimaryKeySelective(category);
        if (rowCount > 0) {
            return ServerResponse.createBySuccessMessage("更新品类名字成功");
        }
        return ServerResponse.createByErrorMessage("更新品类名字失败");
    }

    public ServerResponse<List<Category>> getCategoryByParentId(Integer categoryId) {
        List<Category> categoryList = categorymapper.getCategoryByParentId(categoryId);
        if (CollectionUtils.isEmpty(categoryList)) {
            logger.info("未找到当前分类的子分类");
        }
        return ServerResponse.createBySuccess(categoryList);

    }

    //通过传过来的id查询子节点id判断下面是否有下一级id
    public ServerResponse DeepGetCategoryByParentId(Integer categoryId) {

        Set<Category> categorySet = Sets.newHashSet();
        findCategory(categorySet, categoryId);
        List<Integer> categoryIdList = Lists.newArrayList();
        if (categoryId != null) {
            for (Category categoryItem : categorySet) {
                categoryIdList.add(categoryItem.getId());
            }
        }
        return ServerResponse.createBySuccess(categoryIdList);
    }


    //递归函数
    private Set<Category> findCategory(Set<Category> categorySet, Integer categoryId) {
        Category category = categorymapper.selectByPrimaryKey(categoryId);
        if (category != null) {
            categorySet.add(category);
           // System.out.println(categorySet);
        }
        //递归算法一定要有退出条件
        List<Category> categoryList = categorymapper.getCategoryByParentId(categoryId);
        for (Category categoryItem : categoryList) {
            findCategory(categorySet, categoryItem.getId());
        }
        return categorySet;
    }
}
