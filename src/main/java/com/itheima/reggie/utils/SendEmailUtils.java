package com.itheima.reggie.utils;
import org.apache.commons.mail.HtmlEmail;

public class SendEmailUtils {
    /**
     * 发送验证码
     * @param email  接收邮箱
     * @param code   验证码
     * @return  void
     */
    public static void sendAuthCodeEmail(String email,String code) {
        try {
            HtmlEmail mail = new HtmlEmail();
            /*发送邮件的服务器 126邮箱为smtp.126.com,163邮箱为163.smtp.com，QQ为smtp.qq.com*/
            mail.setHostName("smtp.qq.com");
            /*不设置发送的消息有可能是乱码*/
            mail.setCharset("UTF-8");
            /*IMAP/SMTP服务的密码 username为你开启发送验证码功能的邮箱号 password为你在qq邮箱获取到的一串字符串*/
            mail.setAuthentication("598011093@qq.com", "atrailggdhetbdhc");
            /*发送邮件的邮箱和发件人*/
            mail.setFrom("598011093@qq.com", "短信验证码");
            /*使用安全链接*/
            mail.setSSLOnConnect(true);
            /*接收的邮箱*/
            mail.addTo(email);
            /*设置邮件的主题*/
            mail.setSubject("登录验证码");
            /*设置邮件的内容*/
            mail.setMsg("尊敬的用户:你好! 登录验证码为:" + code + "(有效期为5分钟)");
            mail.send();//发送
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
