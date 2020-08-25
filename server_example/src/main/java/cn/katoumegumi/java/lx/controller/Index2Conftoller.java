package cn.katoumegumi.java.lx.controller;

import cn.katoumegumi.java.hibernate.HibernateDao;
import cn.katoumegumi.java.hibernate.HibernateTransactional;
import cn.katoumegumi.java.http.client.model.HttpRequestBody;
import cn.katoumegumi.java.http.client.model.HttpResponseBody;
import cn.katoumegumi.java.http.client.model.HttpResponseTask;
import cn.katoumegumi.java.lx.jpa.UserJpaDao;
import cn.katoumegumi.java.lx.model.User;
import cn.katoumegumi.java.lx.model.UserDetails;
import cn.katoumegumi.java.lx.model.UserDetailsRemake;
import cn.katoumegumi.java.sql.MySearchList;
import cn.katoumegumi.java.sql.SQLModelUtils;
import cn.katoumegumi.java.sql.UpdateSqlEntity;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @author ws
 */
@Controller
public class Index2Conftoller {

    @Autowired
    private UserJpaDao userJpaDao;
    @Autowired
    private HibernateDao hibernateDao;

    public static void main(String[] args) {
        /*String str = new BCryptPasswordEncoder().encode("nacos");
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
        }*/


        /*Field field = WsFieldUtils.getFieldByName(User.class, "userDetails");
        Class c = WsFieldUtils.getClassListType(field);
        System.out.println(c);

        MySearchList mySearchList = MySearchList.newMySearchList();
        mySearchList.setMainClass(UserOrder.class);
        mySearchList.join(null, User.class, "user123", "userId", "id");
        SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
        String str = sqlModelUtils.searchListBaseSQLProcessor();
        System.out.println(str);*/


        /*HttpRequestBody httpRequestBody = HttpRequestBody.createHttpRequestBody()
                .setRequestProperty("identityType","rawGrain")
                .setRequestProperty("ClientType","webAdmin")
                .setRequestProperty("accessToken","d14ce104fa7649c78f6ba8d8e093a65a")
                .setMethod("POST")
                .setUrl("http://www.apeclassic.com/wx/xboot/upload/file")
                .addHttpRequestBodyEntry("file",new File("D:\\手机备份\\图片\\0D9037E3DCDEC74E0095915A8A69A2C2.jpg"));
        HttpResponseTask httpResponseTask = httpRequestBody.nettyBuild();
        try {
            HttpResponseBody httpResponseBody = httpResponseTask.call();
            System.out.println(httpResponseBody.getResponseBodyToString());
        } catch (TimeoutException e) {
            e.printStackTrace();
        }*/

        /*HttpRequestBody httpRequestBody = HttpRequestBody.createHttpRequestBody()
                .setMethod("GET")
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                .setUrl("https://www.bilibili.com");
        HttpResponseTask httpResponseTask = httpRequestBody.nettyBuild();
        try {
            HttpResponseBody httpResponseBody = httpResponseTask.call();
            System.out.println(httpResponseBody.getResponseBodyToString());
            System.out.println(httpResponseBody.getCharSet());
        } catch (TimeoutException e) {
            e.printStackTrace();
        }*/

        User user = new User();
        MySearchList mySearchList = MySearchList.create(User.class);
        mySearchList.set(user::getName,"你好")
                .set(user::getPassword,"哈哈")
                .set(user::getCreateDate,new Date())
                .eq(user::getId,1);
        SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
        UpdateSqlEntity updateSqlEntity = sqlModelUtils.update(mySearchList);
        System.out.println(updateSqlEntity.getUpdateSql());
        System.out.println(JSON.toJSONString(updateSqlEntity.getValueList()));




        /*System.out.println(LocalDateTime.now() instanceof LocalDateTime);

        String date = WsDateUtils.objectDateFormatString(LocalDateTime.now());
        System.out.println(date);


        Consumer<Runnable> runnableSupplier = runnable -> {
            long start = System.currentTimeMillis();
            runnable.run();
            long end = System.currentTimeMillis();
            System.out.println(end - start);
        };

        LocalDateTime localDateTime = LocalDateTime.now();
        runnableSupplier.accept(() -> {
            for (int i = 0; i < 10000000; i++) {
                boolean b = localDateTime instanceof LocalDateTime;
            }
        });

        runnableSupplier.accept(() -> {
            for (int i = 0; i < 10000000; i++) {
                boolean b = WsFieldUtils.classCompare(localDateTime.getClass(), LocalDateTime.class);
            }
        });*/

    }

    @RequestMapping(value = "hibernateTest")
    @ResponseBody
    public String hibernateTest() {
        User user = new User();
        MySearchList mySearchList = MySearchList.newMySearchList().sort("userDetails.id", "desc");
        mySearchList.or(MySearchList.newMySearchList().eq("userDetails.sex", "男").eq(user::getName, "你好"),
                MySearchList.newMySearchList().eq(user::getPassword, "世界")
        )
                .eq(user::getId, 1)
                .lte(user::getCreateDate, "2019-12-13")
                .eqp(user::getName, user::getPassword)
                .sort("id", "ASC")
                .sort("userDetails.sex", "DESC");
        List<User> users = hibernateDao.selectValueToList(mySearchList, User.class);
        return JSON.toJSONString(user);
    }


    @RequestMapping(value = "index2")
    @ResponseBody
    @HibernateTransactional
    public Mono<String> index(ServerHttpRequest serverHttpRequest) {
        System.out.println(serverHttpRequest.getId());
        System.out.println(serverHttpRequest.getMethod().name());
        //List<User> list = userJpaDao.selectUser();
        for (int i = 0; i < 1000; i++) {
            User user = new User();
            user.setName("你好" + i);
            user.setPassword("世界" + i);
            user.setCreateDate(LocalDateTime.now());
            hibernateDao.insertObject(user);
            for (int k = 0; k < 10; k++) {
                UserDetails userDetails = new UserDetails();
                userDetails.setSex(k % 2 == 0 ? "男" : "女");
                userDetails.setNickName("你好世界" + i);
                userDetails.setUserId(user.getId());
                hibernateDao.insertObject(userDetails);
                for (int j = 0; j < 10; j++) {
                    UserDetailsRemake userDetailsRemake = new UserDetailsRemake();
                    userDetailsRemake.setRemake(j + "");
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
