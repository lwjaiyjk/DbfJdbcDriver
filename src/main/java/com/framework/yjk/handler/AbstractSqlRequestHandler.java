package com.framework.yjk.handler;

import com.framework.yjk.DataReaderWriter;
import com.framework.yjk.DbfConnection;
import com.framework.yjk.DbfStatement;
import com.framework.yjk.constants.ConnectionPropConstants;
import com.framework.yjk.dbfio.DbfDataReaderWriter;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

/**
 * @author yujiakui
 * @version 1.0
 * Email: jkyu@haiyi-info.com
 * date: 2018/9/11 16:47
 * description：抽象sql请求处理
 **/
@Slf4j
public abstract class AbstractSqlRequestHandler implements SqlRequestHandler {

    /**
     * 表名获取器
     */
    protected TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();

    @Override
    public void handle(DbfStatement dbfStatement, String sql) throws SQLException {
        Statement sqlStatement = getSqlStatementFromSql(sql);
        String tableName = getTableNameFromSql(sqlStatement);
        DbfConnection dbfConnection = (DbfConnection) dbfStatement.getConnection();
        Properties connProp = dbfConnection.getConnProp();
        DataReaderWriter dataReaderWriter = new DbfDataReaderWriter(
                dbfConnection.getFilePath() + tableName + ".dbf", null,
                (String) connProp.get(ConnectionPropConstants.CHARSET_KEY));

        // 做真正的处理
        doHandle(dbfStatement, dataReaderWriter, sqlStatement, sql, tableName);

        // 后处理
        afterHandler(dbfStatement, dataReaderWriter, sqlStatement, sql);
    }

    /**
     * 后处理
     *
     * @param dbfStatement
     * @param dataReaderWriter
     * @param sqlStatement
     * @param sql
     */
    protected void afterHandler(DbfStatement dbfStatement,
                                DataReaderWriter dataReaderWriter,
                                Statement sqlStatement, String sql) throws SQLException {
    }

    /**
     * 真正处理的地方
     *
     * @param dbfStatement
     * @param dataReaderWriter
     * @param sql
     */
    protected abstract void doHandle(DbfStatement dbfStatement,
                                     DataReaderWriter dataReaderWriter,
                                     Statement sqlStatement,
                                     String sql, String tableName) throws SQLException;

    /**
     * 从sql语句中获取对应的sql stament 对象
     *
     * @param sql
     */
    private Statement getSqlStatementFromSql(String sql) throws SQLException {
        try {
            return CCJSqlParserUtil.parse(sql);
        } catch (JSQLParserException e) {
            throw new SQLException("从sql语句中获取对应的sql statement异常: " + e);
        }
    }

    /**
     * 从sql语句中获取表名
     *
     * @return
     */
    protected String getTableNameFromSql(Statement statement) {
        List<String> tablenames = null;
        if (statement instanceof Select) {
            tablenames = tablesNamesFinder.getTableList((Select) statement);
        } else if (statement instanceof Insert) {
            tablenames = tablesNamesFinder.getTableList((Insert) statement);
        } else if (statement instanceof Delete) {
            tablenames = tablesNamesFinder.getTableList((Delete) statement);
        } else if (statement instanceof Replace) {
            tablenames = tablesNamesFinder.getTableList((Replace) statement);
        } else if (statement instanceof Update) {
            tablenames = tablesNamesFinder.getTableList((Update) statement);
        } else {
            log.error("对应的statement={}不识别", statement);
            throw new RuntimeException("对应的statement不识别" + statement);
        }

        if (null != tablenames) {
            return tablenames.get(0);
        } else {
            throw new RuntimeException("根据sql获取的表名为空" + statement);
        }
    }
}
