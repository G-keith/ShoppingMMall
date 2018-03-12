package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;

import java.util.List;

/**
 * @author GEMI
 * @date 2018/3/10/0010 12:59
 */
public interface ICategoryService {

    ServerResponse addCategory(String categoryName, Integer parentId);

    ServerResponse updateCategoryName(Integer categoryId,String categoryName);

    ServerResponse<List<Category>> getCategoryByParentId(Integer categoryId);

    ServerResponse DeepGetCategoryByParentId(Integer categoryId);
}
