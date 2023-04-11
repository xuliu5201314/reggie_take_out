package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomerException;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.List;


@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增菜品 同时插入菜品对应的口味信息
     * @param dishDto
     */
    @Override
    @Transactional
    public void savaWithFlavor(DishDto dishDto) {
        // 保存菜品的基本信息
        this.save(dishDto);

        Long dishId = dishDto.getId(); // 菜品id

        List<DishFlavor> flavors = dishDto.getFlavors();
        /*flavors = flavors.stream().map((item) ->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());*/

        for(DishFlavor flavor: flavors){
            flavor.setDishId(dishId);
        }

        // 保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(dishDto.getFlavors());

    }

    /**
     * 根据id 查询菜品信息和对应的口味信息
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        // 查询菜品基本信息
        Dish dish = this.getById(id);

        // 查询口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> flavorList = dishFlavorService.list(queryWrapper);

        /*QueryWrapper<DishFlavor> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("dishId",id);*/

        // 二次封装
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);
        dishDto.setFlavors(flavorList);

        return dishDto;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        // 更新菜品表
        this.updateById(dishDto);

        // 构造器
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId,dishDto.getId());

        // 清理数据
        dishFlavorService.remove(lambdaQueryWrapper);

        // 更新口味表
        List<DishFlavor> flavors = dishDto.getFlavors();

        // 赋值dishId
        for (DishFlavor flavor : flavors){
            flavor.setDishId(dishDto.getId());
        }
        dishFlavorService.saveBatch(flavors);

    }


    @Override
    @Transactional
    public void removeWithFlavor(List<Long> ids) {
        // 查询菜品是否在售
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId,ids);
        queryWrapper.eq(Dish::getStatus,0);
        int count = this.count(queryWrapper);
        if (count > 0){
            throw new CustomerException("当前有在售菜品，不可删除！");
        }

        // 删除菜品
        this.removeWithFlavor(ids);

        // 删除口味
        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(DishFlavor::getDishId,ids);
        dishFlavorService.remove(wrapper);

    }
}
