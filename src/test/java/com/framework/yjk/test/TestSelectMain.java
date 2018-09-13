package com.framework.yjk.test;

import com.framework.yjk.DbfJdbcDriver;
import com.framework.yjk.DbfStatement;
import org.junit.Test;

import java.io.PrintWriter;
import java.sql.*;

/**
 * @author yujiakui
 * @version 1.0
 * Email: jkyu@haiyi-info.com
 * date: 2018/9/11 18:32
 * description：
 **/
public class TestSelectMain {

    @Test
    public void test1() throws ClassNotFoundException, SQLException {
        DriverManager.setLogWriter(new PrintWriter(System.out, true));
        //Class.forName("com.haiyi.framework.dbf.DbfJdbcDriver");
        DbfJdbcDriver.registDriver();
        Connection connection = DriverManager.getConnection("jdbc:yjk:dbf:src/test/resources");
        DbfStatement statement = (DbfStatement) connection.createStatement();
        /*String query = "SELECT rec_num as A  FROM zq_order where rec_num = 2000000 and  STOCK_CODE = '500500' LIMIT 1";*/
        String query = "SELECT *  FROM zq_order where rec_num = 3 ";
        statement.execute(query);
        ResultSet resultSet = statement.getResultSet();
        System.out.println(resultSet);
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int colNum = resultSetMetaData.getColumnCount();

        int recordNum = 0;
        while (resultSet.next()) {
            for (int i = 1; i <= colNum; i++) {
                System.out.println("列名" + resultSetMetaData.getColumnName(i) + " : value " + resultSet.getObject(i));
            }
            System.out.println("-----------");
            recordNum++;
        }

        System.out.println("----记录条数---" + recordNum);
    }

    @Test
    public void test2() throws ClassNotFoundException, SQLException {
        DriverManager.setLogWriter(new PrintWriter(System.out, true));
        //Class.forName("com.haiyi.framework.dbf.DbfJdbcDriver");
        DbfJdbcDriver.registDriver();
        Connection connection = DriverManager.getConnection("jdbc:yjk:dbf:src/test/resources");
        String query = "SELECT *  FROM zq_order where rec_num > ?  and rec_num < 1000 and rec_date > ? limit 2";
        DbfStatement statement = (DbfStatement) connection.prepareStatement(query);
        /*String query = "SELECT rec_num as A  FROM zq_order where rec_num = 2000000 and  STOCK_CODE = '500500' LIMIT 1";*/
        statement.setInt(1, 6);
        statement.setString(2,"20180627");
        statement.executeQuery();
        ResultSet resultSet = statement.getResultSet();
        System.out.println(resultSet);
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int colNum = resultSetMetaData.getColumnCount();

        int recordNum = 0;
        while (resultSet.next()) {
            for (int i = 1; i <= colNum; i++) {
                System.out.println("列名" + resultSetMetaData.getColumnName(i) + " : value " + resultSet.getObject(i));
            }
            System.out.println("-----------");
            recordNum++;
        }

        System.out.println("----记录条数---" + recordNum);
    }
}
