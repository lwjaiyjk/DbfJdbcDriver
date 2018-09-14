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
 * date: 2018/9/12 8:56
 * description：
 **/
public class TestUpdateMain {

    public static void main(String[] args) throws SQLException {
        DriverManager.setLogWriter(new PrintWriter(System.out, true));
        //Class.forName("com.haiyi.framework.dbf.DbfJdbcDriver");
        DbfJdbcDriver.registDriver();
        Connection connection = DriverManager.getConnection("jdbc:yjk:dbf:D:/temp");
        DbfStatement statement = (DbfStatement) connection.createStatement();
        String query = "update zq_order set rec_num = 1 where rec_num = 3";
        statement.execute(query);

    }
}
