package com.ws.java.common;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.mail.*;

import javax.activation.MimeType;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.sql.DataSource;
import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Date;
import java.util.Properties;

public class EmailUtils {




    public static void main(String[] args) {
        String className = EmailUtils.class.getResource("/").getPath();
        URL url = EmailUtils.class.getResource(className);
        System.out.println(className);
        System.out.println(url.getPath());
        //sendVertXMail();
    }


    public static void sendMail(){
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.auth","true");
        properties.setProperty("mail.transport.protocol","smtp");
        properties.setProperty("mail.smtp.host", "smtp.qq.com");
        Session session = Session.getDefaultInstance(properties);
        Message message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress("1048058229@qq.com","哆啦A梦","UTF-8"));
            message.setRecipient(Message.RecipientType.TO,new InternetAddress("1048058229@qq.com","哆啦A梦","UTF-8"));
            message.setSubject("标题");
            message.setContent("正文","text/html;charset=UTF-8");
            message.setDescription("这是一封测试邮件");
            message.setSentDate(new Date());
            Transport transport = session.getTransport();
            transport.connect("1048058229@qq.com","ztbvcniucvqlbdfj");
            transport.sendMessage(message,message.getAllRecipients());
            transport.close();
        }catch (MessagingException| IOException e){
            e.printStackTrace();
        }
    }


    public static void sendVertXMail(){
        MailConfig mailConfig = new MailConfig();
        mailConfig.setHostname("smtp.qq.com");
        mailConfig.setPort(465);
        mailConfig.setUsername("1048058229@qq.com");
        mailConfig.setPassword("ztbvcniucvqlbdfj");
        mailConfig.setStarttls(StartTLSOptions.REQUIRED);
        mailConfig.setSsl(true);
        mailConfig.setLogin(LoginOption.REQUIRED);
        mailConfig.setTrustAll(true);
        VertxOptions vertxOptions = new VertxOptions();
        Vertx vertx = Vertx.vertx(vertxOptions);
        MailClient mailClient = MailClient.createShared(vertx,mailConfig);
        MailMessage mailMessage = new MailMessage();
        mailMessage.addHeader(HttpHeaderNames.CONTENT_TYPE.toString(),HttpHeaderValues.TEXT_PLAIN.toString());
        mailMessage.setFrom("1048058229@qq.com(哆啦A梦)");
        mailMessage.setTo("1048058229@qq.com");
        mailMessage.setCc("1048058229@qq.com");
        mailMessage.setSubject("测试");
        mailMessage.setText("这是一封测试文件");
        mailMessage.setHtml("这是正文");

        try {
            MailAttachment mailAttachment = new MailAttachment();
            mailAttachment.setContentType("image/jpeg");
            FileInputStream fileInputStream = new FileInputStream("D:/image/19.jpg");
            FileChannel fileChannel = fileInputStream.getChannel();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            WritableByteChannel writableByteChannel = Channels.newChannel(byteArrayOutputStream);
            fileChannel.transferTo(0,fileChannel.size(),writableByteChannel);
            byte bytes[] = byteArrayOutputStream.toByteArray();
            writableByteChannel.close();
            fileChannel.close();
            mailAttachment.setData(Buffer.buffer(bytes));
            mailMessage.setAttachment(mailAttachment);
        }catch (IOException e){
            e.printStackTrace();
        }
        mailClient.sendMail(mailMessage, new Handler<AsyncResult<MailResult>>() {
            @Override
            public void handle(AsyncResult<MailResult> event) {
                System.out.println(event.failed());
                if(!event.failed()){
                    MailResult mailResult = event.result();
                    System.out.println(mailResult.toString());
                }else {
                    event.cause().printStackTrace();
                    System.out.println(event.cause().getMessage());
                }
            }
        });
        mailClient.close();


    }




}
