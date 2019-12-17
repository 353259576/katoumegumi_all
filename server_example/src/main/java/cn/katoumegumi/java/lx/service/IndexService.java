package cn.katoumegumi.java.lx.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * @author ws
 */
public interface IndexService {
    @Path("index")
    @GET
    public String index() throws Exception;
}
