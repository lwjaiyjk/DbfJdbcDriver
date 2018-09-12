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
 * date: 2018/9/12 8:35
 * description：
 **/
public class TestDeleteMain {

    public static void main(String[] args) throws SQLException {
        DriverManager.setLogWriter(new PrintWriter(System.out, true));
        //Class.forName("com.haiyi.framework.dbf.DbfJdbcDriver");
        DbfJdbcDriver.registDriver();
        Connection connection = DriverManager.getConnection("jdbc:yjk:dbf:src/test/resources");
        DbfStatement statement = (DbfStatement) connection.createStatement();
        String query = "delete from zq_order where rec_num = 2";
        statement.execute(query);

    }
}
