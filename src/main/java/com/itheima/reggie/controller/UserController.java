package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;

import com.itheima.reggie.utils.ValidateCodeUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        // 获取手机号
        String phone = user.getPhone();

        if (StringUtils.isNotEmpty(phone)){
            // 生成随机4位验证码
            String code = ValidateCodeUtils.generateValidateCode(6).toString();
            log.info("code={}",code);
            // 调用阿里云短信服务
            //SMSUtils.sendMessage("阿里云短信测试","SMS_154950909",phone,code);

            // 需要将生成的验证码保存到session
            //session.setAttribute(phone,code);

            // 将生成的代码缓存到redis中，有效期5分钟
            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);


            return R.success("手机短信验证码发送成功");
        }

        return R.error("短信发送失败！");
    }

    /**
     * 移动端登录验证
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){

        // 获取手机号和验证码
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();

        // 从session保存的获取验证码
        //Object codeInSession = session.getAttribute(phone);

        // 从redis中获取验证码
        Object codeInRedis = redisTemplate.opsForValue().get(phone);

        // 进行验证码比对
        if (codeInRedis != null && codeInRedis.equals(code)){
            // 一致 说明可以登录成功；

            // 判断当前手机号对应的用户是否新用户，如果新用户就自动完成注册
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);

            User user = userService.getOne(queryWrapper);
            if (user == null){
                // 判断当前手机号对应的用户是否新用户，如果新用户就自动完成注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());

            // 如果用户登录成功，删除redis中缓存的验证码
            redisTemplate.delete(phone);

            return R.success(user);
        }

        return R.error("登录失败！");
    }

}
