package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    /**
     * 新增套餐 同时保存套餐和菜品的关系
     * @param setmealDto
     */
    void saveWithDish(SetmealDto setmealDto);

    /**
     * 通过categoryId 查询关联的菜品
     * @param categoryId
     * @return
     */
    SetmealDto getByIdWithDish(Long categoryId);


    /**
     * 修改套餐和菜品信息
     * @param setmealDto
     */
    void updateWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐，同时删除套餐关联的菜品
     * @param ids
     */
    void removeWithDish(List<Long> ids);
}
