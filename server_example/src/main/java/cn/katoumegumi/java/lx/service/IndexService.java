package cn.katoumegumi.java.lx.service;

import cn.katoumegumi.java.hibernate.JpaDao;
import cn.katoumegumi.java.lx.model.User;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * @author ws
 */
public interface IndexService{
    @Path("index")
    @GET
    public String index() throws Exception;
}
