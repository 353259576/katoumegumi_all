package cn.katoumegumi.java.lx.service;

import com.alibaba.cloud.dubbo.annotation.DubboTransported;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * @author ws
 */
//@FeignClient(value = "lxSpring")
//@DubboTransported(protocol = "rest")
@Path("")
public interface IndexService {

    //@RequestMapping(value = "index")
    @GET
    @Path("index")
    public String index();
}
