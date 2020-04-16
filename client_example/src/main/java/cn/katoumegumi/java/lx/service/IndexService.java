package cn.katoumegumi.java.lx.service;

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
