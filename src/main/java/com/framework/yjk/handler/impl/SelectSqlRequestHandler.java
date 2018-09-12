package com.framework.yjk.handler.impl;

import com.framework.yjk.DataReaderWriter;
import com.framework.yjk.DbfResultSet;
import com.framework.yjk.DbfStatement;
import com.framework.yjk.handler.AbstractSqlRequestHandler;
import net.sf.jsqlparser.statement.Statement;

import java.sql.SQLException;

/**
 * @author yujiakui
 * @version 1.0
 * Email: jkyu@haiyi-info.com
 * date: 2018/9/11 16:44
 * description：select sql 请求处理
 **/
public class SelectSqlRequestHandler extends AbstractSqlRequestHandler {


    @Override
    protected void doHandle(DbfStatement dbfStatement,
                            DataReaderWriter dataReaderWriter,
                            Statement sqlStatement, String sql,String tableName) throws SQLException {
        DbfResultSet resultSet = new DbfResultSet(dbfStatement, dataReaderWriter,
                tableName, sql,sqlStatement);
        dbfStatement.setLastResultSet(resultSet);
    }

}
