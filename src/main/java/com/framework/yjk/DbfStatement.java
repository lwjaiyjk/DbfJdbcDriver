package com.framework.yjk;

import com.framework.yjk.handler.SqlRequestHandler;
import com.framework.yjk.handler.impl.DeleteSqlRequestHandler;
import com.framework.yjk.handler.impl.InsertSqlRequestHandler;
import com.framework.yjk.handler.impl.SelectSqlRequestHandler;
import com.framework.yjk.handler.impl.UpdateSqlRequestHandler;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.List;

/**
 * @author yujiakui
 * @version 1.0
 * Email: jkyu@haiyi-info.com
 * date: 2018/9/11 16:06
 * description：
 **/
public class DbfStatement implements PreparedStatement {

    /**
     * 日志
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(DbfStatement.class);

    /**
     * sql语句对应的占位符号
     */
    private final static char SQL_PLACEHOLDER = '?';

    /**
     * 连接
     */
    private DbfConnection connection;

    /**
     * 上一个结果集合
     */
    protected ResultSet lastResultSet = null;

    /**
     * 结果集类型
     */
    private int resultSetType;

    /**
     * 占位符号替换之后的sql
     */
    private String sql;

    /**
     * sql 语句
     */
    private String originSql;

    /**
     * 占位符号分割的sql列表
     */
    private List<String> placeholderSepSqls;

    /**
     * 数组
     */
    private String[] placeholderSepSqlArrays;

    /**
     * 占位符所在的占位符号分割的sql列表中的索引
     */
    private List<Integer> placeholderPos;

    /**
     * 构造函数
     *
     * @param connection
     * @param resultSetType
     */
    public DbfStatement(DbfConnection connection, int resultSetType) {
        this.connection = connection;
        this.resultSetType = resultSetType;
    }

    /**
     * 构造函数
     *
     * @param connection
     * @param resultSetType
     */
    public DbfStatement(DbfConnection connection, int resultSetType, String originSql) {
        this.connection = connection;
        this.resultSetType = resultSetType;
        this.originSql = originSql;

        placeholderSepSqls = Lists.newArrayList();
        placeholderPos = Lists.newArrayList();
        String sepStr = "";
        // 对原始sql进行占位符分割
        for (int index = 0; index < originSql.length(); index++) {
            char sqlChar = originSql.charAt(index);
            if (SQL_PLACEHOLDER == sqlChar) {
                if (StringUtils.isNotBlank(sepStr)) {
                    placeholderSepSqls.add(sepStr);
                }
                placeholderSepSqls.add(String.valueOf(sqlChar));
                placeholderPos.add(placeholderSepSqls.size() - 1);
                sepStr = "";
            } else {
                sepStr += sqlChar;
            }
        }

        if (StringUtils.isNotBlank(sepStr)) {
            placeholderSepSqls.add(sepStr);
        }
        placeholderSepSqlArrays = placeholderSepSqls.toArray(new String[0]);
        this.sql = originSql;
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        // 解析sql
        //net.sf.jsqlparser.statement.Statement sqlParserStatement = SqlParserUtil.parse(sql);

        return null;
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        return 0;
    }

    @Override
    public void close() throws SQLException {

    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return 0;
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {

    }

    @Override
    public int getMaxRows() throws SQLException {
        return 0;
    }

    @Override
    public void setMaxRows(int max) throws SQLException {

    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {

    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return 0;
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {

    }

    @Override
    public void cancel() throws SQLException {

    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {

    }

    @Override
    public void setCursorName(String name) throws SQLException {

    }

    @Override
    public boolean execute(String sql) throws SQLException {

        DbfJdbcDriver.writeLog("执行sql=" + sql);
        String sqlUpperCase = sql.trim().toUpperCase();
        SqlRequestHandler sqlRequestHandler = null;
        if (sqlUpperCase.startsWith("SELECT")) {
            // 查询语句
            sqlRequestHandler = new SelectSqlRequestHandler();
        } else if (sqlUpperCase.startsWith("INSERT")) {
            sqlRequestHandler = new InsertSqlRequestHandler();
        } else if (sqlUpperCase.startsWith("DELETE")) {
            sqlRequestHandler = new DeleteSqlRequestHandler();
        } else if (sqlUpperCase.startsWith("UPDATE")) {
            sqlRequestHandler = new UpdateSqlRequestHandler();
        }

        if (null != sqlRequestHandler) {
            sqlRequestHandler.handle(this, sql);
        } else {
            throw new RuntimeException("对应的sql语句类型暂时不支持" + sql);
        }
        return true;
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return lastResultSet;
    }

    public void setLastResultSet(ResultSet lastResultSet) {
        this.lastResultSet = lastResultSet;
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return 0;
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return false;
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {

    }

    @Override
    public int getFetchDirection() throws SQLException {
        return 0;
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {

    }

    @Override
    public int getFetchSize() throws SQLException {
        return 0;
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return 0;
    }

    @Override
    public int getResultSetType() throws SQLException {
        return 0;
    }

    @Override
    public void addBatch(String sql) throws SQLException {

    }

    @Override
    public void clearBatch() throws SQLException {

    }

    @Override
    public int[] executeBatch() throws SQLException {
        return new int[0];
    }

    @Override
    public Connection getConnection() throws SQLException {
        return this.connection;
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return false;
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return null;
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return 0;
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return 0;
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return 0;
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return false;
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return false;
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return false;
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return 0;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {

    }

    @Override
    public boolean isPoolable() throws SQLException {
        return false;
    }

    @Override
    public void closeOnCompletion() throws SQLException {

    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    public void setConnection(DbfConnection connection) {
        this.connection = connection;
    }

    public void setResultSetType(int resultSetType) {
        this.resultSetType = resultSetType;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        LOGGER.info("查询对应的sql语句{}",sql);
        this.execute(this.sql);
        return this.lastResultSet;
    }

    @Override
    public int executeUpdate() throws SQLException {
        return 0;
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        setPlaceHolderParam(parameterIndex, String.valueOf(x));
    }

    /**
     * 设置占位符参数
     *
     * @param pos
     * @param value
     */
    private void setPlaceHolderParam(int pos, String value) {
        int placeHolderPos = this.placeholderPos.get(pos-1);
        this.placeholderSepSqlArrays[placeHolderPos] = value;
        this.sql = StringUtils.join(this.placeholderSepSqlArrays);
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        setPlaceHolderParam(parameterIndex, String.valueOf(x));
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        setPlaceHolderParam(parameterIndex, String.valueOf(x));
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        setPlaceHolderParam(parameterIndex, String.valueOf(x));
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        setPlaceHolderParam(parameterIndex, String.valueOf(x));
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        setPlaceHolderParam(parameterIndex, String.valueOf(x));
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        setPlaceHolderParam(parameterIndex, String.valueOf(x));
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        setPlaceHolderParam(parameterIndex, x.toString());
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        String paramValue = "'" + x + "'";
        setPlaceHolderParam(parameterIndex, paramValue);
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        setPlaceHolderParam(parameterIndex, String.valueOf(x));
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        setPlaceHolderParam(parameterIndex, x.toString());
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        setPlaceHolderParam(parameterIndex, x.toString());
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        setPlaceHolderParam(parameterIndex, x.toString());
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {

    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {

    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {

    }

    @Override
    public void clearParameters() throws SQLException {

    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {

    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        setPlaceHolderParam(parameterIndex, x.toString());
    }

    @Override
    public boolean execute() throws SQLException {
        return false;
    }

    @Override
    public void addBatch() throws SQLException {

    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {

    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {

    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {

    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {

    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {

    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return null;
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {

    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {

    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {

    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {

    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {

    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return null;
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {

    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {

    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {

    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {

    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {

    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {

    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {

    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {

    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {

    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {

    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {

    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {

    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {

    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {

    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {

    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {

    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {

    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {

    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {

    }
}
