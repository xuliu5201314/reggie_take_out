package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrdersService;
import com.itheima.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private UserService userService;

    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){

        ordersService.submit(orders);

        return R.success("下单成功！");
    }

    @GetMapping("/userPage")
    public R<Page> userPage(int page,int pageSize){

        // 分页构造器
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        Page<OrdersDto> dtoPage = new Page<>();

        // Orders条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Orders::getCheckoutTime);

        // 查询分页订单
        ordersService.page(pageInfo,queryWrapper);

        List<Orders> records = pageInfo.getRecords();

        // 保存orderDetail明细的集合，用户封装到Dto类
        List<OrdersDto> ordersDtoList = new ArrayList<>();

        for (Orders record : records){
            LambdaQueryWrapper<OrderDetail> orderDetailWrapper = new LambdaQueryWrapper<>();
            String orderId = record.getNumber();// 获取订单号

            // 根据订单号查询订单明细
            orderDetailWrapper.eq(OrderDetail::getOrderId,orderId);
            List<OrderDetail> orderDetailList = orderDetailService.list(orderDetailWrapper);

            OrdersDto ordersDto = new OrdersDto();
            ordersDto.setOrderDetails(orderDetailList);
            BeanUtils.copyProperties(record,ordersDto);

            ordersDtoList.add(ordersDto);
        }

        dtoPage.setRecords(ordersDtoList);

        return R.success(dtoPage);
    }

    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,Long number,String beginTime,String endTime){
        Page<Orders> ordersPage = new Page<>(page,pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>();
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

        // 添加订单号 和时间范围的查询条件
        if (number != null){
            queryWrapper.eq(Orders::getNumber,number);
        } else if (beginTime != null && endTime != null){
            queryWrapper.between(Orders::getOrderTime,beginTime,endTime);
        }

        queryWrapper.orderByDesc(Orders::getCheckoutTime);

        ordersService.page(ordersPage,queryWrapper);

        // 获取orders对象
        List<Orders> orders = ordersPage.getRecords();

        // 遍历orders对象集合，获取userid，然后根据userId获取userName

        List<OrdersDto> ordersDtos = new ArrayList<>();
        for (Orders order: orders){
            Long userId = order.getUserId();
            String name = userService.getById(userId).getName();
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(order,ordersDto);
            ordersDto.setUserName(name);
            ordersDtos.add(ordersDto);
        }

        ordersDtoPage.setRecords(ordersDtos);

        return R.success(ordersDtoPage);
    }
}
