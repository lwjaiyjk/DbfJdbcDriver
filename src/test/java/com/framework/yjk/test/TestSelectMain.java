package com.framework.yjk.test;

import com.framework.yjk.DbfConnection;
import com.framework.yjk.DbfJdbcDriver;
import com.framework.yjk.DbfStatement;
import com.framework.yjk.constants.ConnectionPropConstants;
import org.junit.Test;

import java.io.PrintWriter;
import java.sql.*;
import java.util.Properties;

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
        Properties properties = new Properties();
        properties.setProperty(ConnectionPropConstants.CHARSET_KEY,"GBK");
        Connection connection = DriverManager.
                getConnection("jdbc:yjk:dbf:src/test/resources",properties);
        DbfStatement statement = (DbfStatement) connection.createStatement();
        /*String query = "SELECT rec_num as A  FROM zq_order where rec_num = 2000000 and  STOCK_CODE = '500500' LIMIT 1";*/
        String query = "SELECT *  FROM zq_feedback1";
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
        //String query = "select * from zq_feedback where  REC_NUM > 10  and  REC_NUM<10000000  and  REC_DATE='20180912'";
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

    @Test
    public void test3() throws ClassNotFoundException, SQLException{
        DriverManager.setLogWriter(new PrintWriter(System.out, true));
        //Class.forName("com.haiyi.framework.dbf.DbfJdbcDriver");
        DbfJdbcDriver.registDriver();
        Connection connection = DriverManager.getConnection("jdbc:yjk:dbf:src/test/resources");
       /* String query = "SELECT REF_ID AS referId ,B_QTY AS buyQty,B_FULL_SUM AS buyFullSum," +
                "B_PROFIT AS buyProfit,B_ORDERNO AS buyOrderNo,B_TRADENO AS buyTradeNo," +
                "B_PRICE AS buyPrice,S_QTY AS sellQty,S_TRADENO  AS sellTradeNo,S_PROFIT AS sellProfit," +
                "S_PRICE  AS sellPrice,S_ORDERNO AS sellOrderNo,S_FULL_SUM AS sellFullSum,FBACK_TYPE AS feedBackType," +
                "D_ORDERNO AS doubleQuoteOrderNo,OB_ACC,ORDER_DATE,TRADER,ORDER_TIME,OB_COMPANY,REMARK,REC_DATE," +
                "SETTLE,STATUS,OB_SETTLE,INTEREST,ACC,TRADE_TIME,NICKED,STOCK_NAME,REC_NUM,STOCK_CODE,COMPANY," +
                "REC_TIME,ICE_QTY,FIRM,DIR,WITHDRAW,PRODUCT,TRADE_DATE,OB_FIRM,OB_TRADER,TRADE_TYPE, DEAL_FLAG," +
                " RETRY_NUM, DEAL_TIME  FROM zq_feedback WHERE REC_DATE='20180919' AND  DEAL_FLAG !='Y' " +
                "AND FBACK_TYPE != 'd' AND  RETRY_NUM < 5";*/
       String query = "select * from zq_feedback";
        //String query = "select * from zq_feedback where  REC_NUM > 10  and  REC_NUM<10000000  and  REC_DATE='20180912'";
        DbfStatement statement = (DbfStatement) connection.prepareStatement(query);
        /*String query = "SELECT rec_num as A  FROM zq_order where rec_num = 2000000 and  STOCK_CODE = '500500' LIMIT 1";*/

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
