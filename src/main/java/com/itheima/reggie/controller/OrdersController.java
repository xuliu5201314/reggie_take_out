package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrdersService;
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
}
