package com.ws.java.lx.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * @author ws
 * @date Created by Administrator on 2019/11/20 16:26
 */
public interface IndexService {
    @Path("index")
    @GET
    public String index() throws Exception;
}
