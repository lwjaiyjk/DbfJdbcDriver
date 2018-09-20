package com.framework.yjk.test;

import com.framework.yjk.DbfJdbcDriver;
import com.framework.yjk.DbfStatement;

import java.io.PrintWriter;
import java.sql.*;

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
        Connection connection = DriverManager.getConnection("jdbc:yjk:dbf:src/test/resources");
        DbfStatement statement = (DbfStatement) connection.createStatement();
        String query = "update zq_feedback set retry_num = 0";
        statement.execute(query);

        query = "select * from zq_feedback";
        statement.execute(query);
        ResultSet resultSet = statement.getResultSet();
        System.out.println(resultSet);

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
