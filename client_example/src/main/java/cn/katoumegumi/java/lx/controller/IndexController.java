package cn.katoumegumi.java.lx.controller;

import cn.katoumegumi.java.lx.service.IndexService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/*import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;*/

@RestController
@RefreshScope
public class IndexController {
    @Autowired
    private RestTemplate restTemplate;

    @Reference(protocol = "rest", version = "1.0.0", check = false)
    //@Autowired
    private IndexService indexFeign;

    @Value("${jasypt.encryptor.password:0}")
    private String value;

    /*@RequestMapping(value = "index")
    //@GlobalTransactional(name = "index")
    public Mono<String> index(@AuthenticationPrincipal Authentication authenticationServerHttpRequest request){
        List<String> strings = new ArrayList<>();
        return Mono.zip(ReactiveSecurityContextHolder.getContext(),request.getBody().collectList()).filter(objects -> {
          return objects.getT1().getAuthentication() != null;
        }).map(objects -> {
            ByteBuf byteBuf = Unpooled.buffer();
            Authentication authentication = objects.getT1().getAuthentication();
            List<DataBuffer> dataBuffers = objects.getT2();
            for (DataBuffer dataBuffer : dataBuffers) {
                byteBuf.writeBytes(dataBuffer.asByteBuffer());
                DataBufferUtils.release(dataBuffer);
            }
            List list = new ArrayList();
            list.add(authentication);
            list.add(byteBuf);
            return list;
        }).map(list -> {
            Authentication authentication = (Authentication) list.get(0);
            ByteBuf byteBuf = (ByteBuf) list.get(1);
            byte[] bytes = ByteBufUtil.getBytes(byteBuf);
            try {
                File file = WsFileUtils.createFile("C:\\Users\\Administrator\\Pictures\\1.jpg");

                FileOutputStream fileInputStream = new FileOutputStream(file);
                FileChannel fileChannel = fileInputStream.getChannel();
                ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                fileChannel.write(byteBuffer);
                fileChannel.close();
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //System.out.println(JSON.toJSONString(authentication.get().getPrincipal()));
            System.out.println(value);
            //ResponseEntity<String> strE = restTemplate.getForEntity("http://lxSpring/index",String.class);
            String str = indexFeign.index();
            return str;
        });
    }*/


    @RequestMapping(value = "index2")
    @ResponseBody
    public String index2() {
        return indexFeign.index();
    }
}
