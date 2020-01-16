package cn.katoumegumi.java.lx.controller;

import cn.katoumegumi.java.common.WsFileUtils;
import cn.katoumegumi.java.hibernate.HibernateDao;
import cn.katoumegumi.java.hibernate.HibernateTransactional;
import cn.katoumegumi.java.lx.jpa.UserJpaDao;
import cn.katoumegumi.java.lx.model.User;
import cn.katoumegumi.java.lx.model.UserDetails;
import cn.katoumegumi.java.lx.model.UserDetailsRemake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

/**
 * @author ws
 */
@Controller
public class Index2Conftoller {

    public static void main(String[] args) {
        String str = new BCryptPasswordEncoder().encode("nacos");
        System.out.println(str);
        File file = WsFileUtils.createFile("C:\\Users\\Administrator\\Pictures\\Camera Roll\\39.png");
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            FileChannel fileChannel = fileInputStream.getChannel();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            WritableByteChannel writableByteChannel = Channels.newChannel(byteArrayOutputStream);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
            int i = 0;
            while ((i = fileChannel.read(byteBuffer)) != -1){
                byteBuffer.flip();
                while (byteBuffer.hasRemaining()) {
                    writableByteChannel.write(byteBuffer);
                }
                byteBuffer.clear();
            }
            fileChannel.close();
            writableByteChannel.close();
            byte bytes[] = byteArrayOutputStream.toByteArray();
            byte base64bytes[] = Base64.getEncoder().encode(bytes);
            File file1 = WsFileUtils.createFile("C:\\Users\\Administrator\\Pictures\\Camera Roll\\39.txt");
            FileOutputStream fileOutputStream = new FileOutputStream(file1);
            fileChannel = fileOutputStream.getChannel();
            fileChannel.write(ByteBuffer.wrap(base64bytes));
            fileChannel.close();

        }catch (IOException e){
            e.printStackTrace();
        }


    }

    @Autowired
    private UserJpaDao userJpaDao;

    @Autowired
    private HibernateDao hibernateDao;


    @RequestMapping(value = "index2")
    @ResponseBody
    @HibernateTransactional
    public Mono<String> index(ServerHttpRequest serverHttpRequest){
        System.out.println(serverHttpRequest.getId());
        System.out.println(serverHttpRequest.getMethod().name());
        //List<User> list = userJpaDao.selectUser();
        for(int i = 0; i < 1000; i++){
            User user = new User();
            user.setName("你好"+i);
            user.setPassword("世界"+i);
            user.setCreateDate(LocalDateTime.now());
            hibernateDao.insertObject(user);
            for(int k = 0; k < 10; k++){
                UserDetails userDetails = new UserDetails();
                userDetails.setSex(k%2==0?"男":"女");
                userDetails.setNickName("你好世界"+i);
                userDetails.setUserId(user.getId());
                hibernateDao.insertObject(userDetails);
                for(int j = 0; j < 10; j++){
                    UserDetailsRemake userDetailsRemake = new UserDetailsRemake();
                    userDetailsRemake.setRemake(j+"");
                    userDetailsRemake.setUserDetailsId(userDetails.getId());
                    hibernateDao.insertObject(userDetailsRemake);
                }
            }

            //hibernateDao.insertObject(userDetails);
        }
        //Iterator<User> iterator = list.iterator();
        return Mono.just("你好世界");
    }

}
