package com.framework.yjk.test;

import com.framework.yjk.DbfJdbcDriver;
import com.framework.yjk.DbfStatement;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author yujiakui
 * @version 1.0
 * Email: jkyu@haiyi-info.com
 * date: 2018/9/11 18:32
 * description：
 **/
public class TestInsertMain {

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        DriverManager.setLogWriter(new PrintWriter(System.out, true));
        //Class.forName("com.haiyi.framework.dbf.DbfJdbcDriver");
        DbfJdbcDriver.registDriver();
        Connection connection = DriverManager.getConnection("jdbc:yjk:dbf:src/test/resources");
        DbfStatement statement = (DbfStatement) connection.createStatement();
        String query = "insert into zq_order(REC_DATE,OB_SETTLE,REC_NUM,STOCK_CODE,WITHDRAW,STATUS,S_QTY,\n" +
                "PRODUCT,B_QTY,REC_TIME,SETTLE,FIRM,ACC,OB_COMPANY,OB_ACC,ORDER_TYPE,STOCK_NAME,B_PRICE,\n" +
                "TRADE_TYPE,DIR,OB_FIRM,OB_TRADER,COMPANY,S_PRICE,ICE_QTY,TRADER,NICKED,REF_ID)\n" +
                "values('20180628', null, 20243000, '500500',null ,'U', null, '01', 1, '180013',null , \n" +
                "'88839', 'D890008091', null,null , '1', null, 100, null, 'B',null , null, '277',null, \n" +
                "0, null, 'N', '1551030189')";
        statement.execute(query);

        System.out.println("----插入---");
    }
}
