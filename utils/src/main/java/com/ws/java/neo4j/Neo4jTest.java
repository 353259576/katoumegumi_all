package com.ws.java.neo4j;

import org.neo4j.driver.v1.*;

import java.net.URI;

/**
 * @author ws
 * @date Created by Administrator on 2019/12/3 17:07
 */
public class Neo4jTest {


    public static void main(String[] args) {
        Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j","199645"));
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult statementResult  = transaction.run("create(n:Label:Test2 {name:\"test1\",type:\"t1\"})");
        transaction.commitAsync();
        transaction.success();
    }


}