package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServlet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 套餐管理
 */
@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishService dishService;

    /**
     * 套餐分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @RequestMapping("/page")
    public R<Page> page(int page,int pageSize,String name){

        // 分页查询构造器
        Page<Setmeal> setmealPage = new Page<>(page,pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();

        // 条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        // 根据名称模糊查询
        queryWrapper.like(name != null,Setmeal::getName,name);
        // 排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        // 分页查询
        setmealService.page(setmealPage,queryWrapper);

        BeanUtils.copyProperties(setmealPage,setmealDtoPage,"records");

        List<Setmeal> records = setmealPage.getRecords();

        // 创建集合储存 SetmealDto
        List<SetmealDto> setmealDtos = new ArrayList<>();

        for (Setmeal record:records){
            SetmealDto setmealDto = new SetmealDto();
            Long categoryId = record.getCategoryId();

            // 根据categoryId 查询套餐对象
            Category category = categoryService.getById(categoryId);

            setmealDto.setCategoryName(category.getName());

            BeanUtils.copyProperties(record,setmealDto);

            setmealDtos.add(setmealDto);
        }

        setmealDtoPage.setRecords(setmealDtos);

        return R.success(setmealDtoPage);
    }

    /**
     * 保存套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> sava(@RequestBody SetmealDto setmealDto){

        setmealService.saveWithDish(setmealDto);

        return R.success("新增成功！");
    }

    @GetMapping("/{setmealId}")
    public R<SetmealDto> get(@PathVariable Long setmealId){

        SetmealDto setmealDto = setmealService.getByIdWithDish(setmealId);

        return R.success(setmealDto);
    }

    @PutMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> update(@RequestBody SetmealDto setmealDto){

        setmealService.updateWithDish(setmealDto);

        return R.success("更新成功！");
    }

    /**
     * 批量停售或者启售
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{state}")
    public R<String> status(@PathVariable("state") int status,Long[] ids){

        LambdaUpdateWrapper<Setmeal> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(Setmeal::getStatus,status);
        wrapper.in(Setmeal::getId,ids);
        setmealService.update(wrapper);

        return R.success("更新成功！");
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids){

        setmealService.removeWithDish(ids);
        return R.success("删除成功！");
    }

    @GetMapping("/list")
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId+'_' + #setmeal.status")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(setmeal.getCategoryId() !=null, Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(queryWrapper);

        return R.success(list);
    }

    @GetMapping("/dish/{setmealId}")
    public R<List<SetmealDish>> dish(@PathVariable("setmealId") Long setmealId){
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealId);

        List<SetmealDish> dishes = setmealDishService.list(queryWrapper);
        return R.success(dishes);
    }
}
