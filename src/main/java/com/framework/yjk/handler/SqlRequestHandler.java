package com.framework.yjk.handler;

import com.framework.yjk.DbfStatement;
import net.sf.jsqlparser.statement.Statement;

import java.sql.SQLException;

/**
 * @author yujiakui
 * @version 1.0
 * Email: jkyu@haiyi-info.com
 * date: 2018/9/11 16:41
 * description：sql请求处理
 **/
public interface SqlRequestHandler {

    /**
     * sql请求处理
     *
     * @param dbfStatement
     * @param sql
     */
    void handle(DbfStatement dbfStatement, String sql) throws SQLException;
}
