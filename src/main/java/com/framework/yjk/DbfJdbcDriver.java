package com.framework.yjk;

import java.io.File;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @author yujiakui
 * @version 1.0
 * Email: jkyu@haiyi-info.com
 * date: 2018/9/11 16:00
 * description：Dbf 驱动
 **/
public class DbfJdbcDriver implements Driver {

    /**
     * 识别的驱动类型
     */
    public final static String URL_PREFIX = "jdbc:yjk:dbf:";

    /**
     * 注册驱动
     */
    public static void registDriver() {
        try {
            DriverManager.registerDriver(new DbfJdbcDriver());
        } catch (SQLException e) {
            throw new RuntimeException("driver register initFailed: " + e.getMessage());
        }
    }


    /**
     * 连接
     *
     * @param url
     * @param info
     * @return
     * @throws SQLException
     */
    public Connection connect(String url, Properties info) throws SQLException {
        writeLog("DbfJdbcDriver:connect() - url=" + url);
        url = url.trim();
        // check for correct url
        if (!url.toLowerCase().startsWith(URL_PREFIX)) {
            return null;
        }

        // get filepath from url
        String filePath = url.substring(URL_PREFIX.length());
        writeLog("DbfJdbcDriver:connect() - filePath=" + filePath);
        if (!filePath.endsWith(File.separator)) {
            filePath += File.separator;
        }
        // check if filepath is a correct path.
        File checkPath = new File(filePath);
        if (!checkPath.exists()) {
            throw new SQLException("dirNotFound: " + filePath);
        }
        if (!checkPath.isDirectory()) {
            throw new SQLException("dirNotFound: " + filePath);
        }

        DbfConnection dbfConnection = new DbfConnection(filePath, info);

        return dbfConnection;
    }

    public boolean acceptsURL(String url) throws SQLException {
        writeLog("DbfJdbcDriver:accept() - url=" + url);
        return url.trim().toLowerCase().startsWith(URL_PREFIX);
    }

    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return new DriverPropertyInfo[0];
    }

    public int getMajorVersion() {
        return 1;
    }

    public int getMinorVersion() {
        return 0;
    }

    public boolean jdbcCompliant() {
        return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("methodNotSupported: Driver.getParentLogger()");
    }

    public static void writeLog(String message) {
        PrintWriter logWriter = DriverManager.getLogWriter();
        if (logWriter != null) {
            logWriter.println(DbfJdbcDriver.class.getPackage().getName() + ": " + message);
        }
    }
}
