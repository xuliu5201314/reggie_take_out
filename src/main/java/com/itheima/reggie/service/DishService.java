package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;

import java.util.List;


public interface DishService extends IService<Dish> {

    // 新增菜品 同时插入菜品对应的口味信息
    void savaWithFlavor(DishDto dishDto);

    DishDto getByIdWithFlavor(Long id);

    // 更新菜品信息 同时更新口味信息
    void updateWithFlavor(DishDto dishDto);

    void removeWithFlavor(List<Long> ids);

}
