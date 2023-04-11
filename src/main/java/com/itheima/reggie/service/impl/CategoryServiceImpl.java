package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomerException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService{

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id删除分类，删除之前需要分类
     * @param id
     */
    @Override
    public void remove(Long id) {

        // 查询当前分类是否关联了菜品，如果已经关联了，抛出一个业务异常
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getCategoryId,id);
        int rows = dishService.count(wrapper);
        if (rows > 0){
            // 抛出异常
            throw new CustomerException("当前分类下关联了菜品！不能删除！");
        }

        // 查询当前分类是否关联了套餐，如果已经关联了，抛出一个业务异常
        LambdaQueryWrapper<Setmeal> wrapperSetmeal = new LambdaQueryWrapper<>();
        wrapperSetmeal.eq(Setmeal::getCategoryId,id);
        int count = setmealService.count(wrapperSetmeal);
        if (count > 0 ){
            // 已经关联套餐，抛出一个异常
            throw new CustomerException("当前套餐下关联了套餐！不能删除！");
        }

        // 正常删除分类
        super.removeById(id);

    }

}
