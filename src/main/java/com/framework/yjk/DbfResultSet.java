package com.framework.yjk;

import com.framework.yjk.sqlparser.CommonSelectItemVisitor;
import com.framework.yjk.sqlparser.CommonWhereExpressionVisitor;
import com.framework.yjk.util.StringConverter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import nl.knaw.dans.common.dbflib.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yujiakui
 * @version 1.0
 * Email: jkyu@haiyi-info.com
 * date: 2018/9/11 16:08
 * description：
 **/
public class DbfResultSet implements ResultSet {

    /**
     * 日志
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(DbfResultSet.class);

    /**
     * 默认列的size
     */
    private final static int DEFAULT_COLUMN_SIZE = 20;

    /**
     * Metadata for this ResultSet
     */
    private ResultSetMetaData resultSetMetaData;

    /**
     * Statement that produced this ResultSet
     */
    private DbfStatement statement;

    /**
     * 结果集类型
     */
    private int resultSetType = ResultSet.TYPE_SCROLL_SENSITIVE;

    /**
     * Helper class that performs the actual file reads
     */
    private DataReaderWriter reader;

    /**
     * Table referenced by the Statement
     */
    private String tableName;

    /**
     * sql
     */
    private String sql;

    /**
     * string 转换器
     */
    private StringConverter converter;


    /**
     * where 表达式
     */
    private Expression whereExpression;

    /**
     * 限制
     */
    private Limit limit;

    /**
     * sql语句
     */
    private net.sf.jsqlparser.statement.Statement sqlStatement;

    /** --------------------------------下面是对应的数据---------------------------------- **/
    /**
     * 查询返回列信息:第一个为value名称，第二个为类型
     */
    private List<Object[]> queryReturnColInfos;

    /**
     * 记录对应的列名和value对应的关系
     */
    private List<Map<String, Object>> queryNameValueMaps;

    /**
     * 记录对应的列名alias和value对应的关系
     */
    private List<Map<String, Object>> queryAliasNameValueMaps;

    /**
     * 查询返回值
     */
    private List<List<Object>> queryReturnValueLists;

    /**
     * 列名和别名的映射
     */
    private Map<String, String> colAliasNameMap;

    /**
     * 当前处理的记录行数
     */
    private int curRowNo = -1;

    /**
     * 是否加载的标记
     */
    private boolean loadFlag = false;


    public DbfResultSet(DbfStatement statement,
                        DataReaderWriter reader,
                        String tableName, String sql,
                        net.sf.jsqlparser.statement.Statement sqlStatement) throws SQLException {
        this.statement = statement;
        this.reader = reader;
        this.tableName = tableName;
        this.sql = sql.trim().toUpperCase();
        this.sqlStatement = sqlStatement;
        loadFlag = false;
        if (sqlStatement instanceof Select) {
            // 解析对应的sql语法
            Select selectStatement = (Select) sqlStatement;
            PlainSelect plainSelect = (PlainSelect) selectStatement.getSelectBody();
            List<SelectItem> selectItems = plainSelect.getSelectItems();
            CommonSelectItemVisitor mySelectItemVisitor = new CommonSelectItemVisitor(reader);
            queryReturnColInfos = Lists.newArrayList();
            colAliasNameMap = Maps.newHashMap();
            for (SelectItem selectItem : selectItems) {
                selectItem.accept(mySelectItemVisitor);
                LOGGER.info("select Item--->{}", selectItem);
                // 设置queryResultValue
                queryReturnColInfos.addAll(mySelectItemVisitor.getFieldCntInfos());
                colAliasNameMap.putAll(mySelectItemVisitor.getColAliasNameMap());
            }

            // where 条件
            whereExpression = plainSelect.getWhere();
            limit = plainSelect.getLimit();
        }

    }

    /**
     * 做初始化
     */
    private void doLoad() throws SQLException {

        CommonWhereExpressionVisitor whereExpressionVisitor = new CommonWhereExpressionVisitor();
        // sql语句解析
        Expression expression = whereExpression;
        whereExpressionVisitor.setExpression(expression);
        whereExpressionVisitor.setDataReaderWriter(this.reader);

        long startRowNum = 0;
        int totalRecordNum = reader.getRecordCount();
        long maxReturnRecordNum = totalRecordNum;
        if (null != limit) {
            Expression startRowNumExp = limit.getOffset();
            if (null != startRowNumExp) {
                startRowNumExp.accept(whereExpressionVisitor);
                startRowNum = (long) whereExpressionVisitor.getResult();
            }
            Expression maxReturnRecordNumExp = limit.getRowCount();
            if(null != maxReturnRecordNumExp) {
                maxReturnRecordNumExp.accept(whereExpressionVisitor);
                maxReturnRecordNum = (long) whereExpressionVisitor.getResult();
            }
        }

        if (startRowNum > totalRecordNum) {
            LOGGER.error("sql请求开始的行数{}大于表中总的记录行数{},sql={}", startRowNum, totalRecordNum, sql);
            throw new RuntimeException("sql请求开始的行数大于表中总的记录行数");
        }
        // 设置从哪一行开始
        reader.skip((int) startRowNum);
        int recordCount = 1;
        int curRecordNum = (int) startRowNum;
        // 通过读取dbf文件获得对应的记录
        while (reader.next() && recordCount <= maxReturnRecordNum) {
            Record curRecord = reader.getCurRecord();
            if (!curRecord.isMarkedDeleted()) {
                LOGGER.info("dbf 读取结果{}", reader.getRecordMap());
                whereExpressionVisitor.setValueMaps(reader.getRecordMap());
                // where 条件
                expression.accept(whereExpressionVisitor);
                Boolean filterFlag = (Boolean) whereExpressionVisitor.getResult();
                if (filterFlag) {
                    // 将记录保存到结果集合中
                    putRecordInResultSet();
                    recordCount++;
                }
            }
            if (++curRecordNum > totalRecordNum) {
                break;
            }
        }
    }

    /**
     * 将记录放入结果集合中
     */
    private void putRecordInResultSet() throws SQLException {
        Map<String, Object> fieldValueMap = reader.getRecordMap();

        if (null == queryNameValueMaps) {
            queryNameValueMaps = Lists.newArrayList();
        }
        if (null == queryAliasNameValueMaps) {
            queryAliasNameValueMaps = Lists.newArrayList();
        }
        if (null == queryReturnValueLists) {
            queryReturnValueLists = Lists.newArrayList();
        }

        Map<String, Object> queryFieldValueMap = Maps.newHashMap();
        Map<String, Object> queryAliasFieldValueMap = Maps.newHashMap();
        List<Object> fieldValues = Lists.newArrayList();

        for (Object[] queryFieldInfos : queryReturnColInfos) {
            String columnName = (String) queryFieldInfos[0];
            Object value = fieldValueMap.get(columnName);
            queryFieldValueMap.put(columnName, value);
            fieldValues.add(value);
            if (null != colAliasNameMap && colAliasNameMap.size() > 0) {
                String colAliasName = colAliasNameMap.get(columnName);
                if (null != colAliasName) {
                    queryAliasFieldValueMap.put(colAliasName, value);
                }
            }

        }

        queryNameValueMaps.add(queryFieldValueMap);
        queryReturnValueLists.add(fieldValues);
        if (null != queryAliasFieldValueMap && queryAliasFieldValueMap.size() > 0) {
            queryAliasNameValueMaps.add(queryAliasFieldValueMap);
        }
    }


    @Override
    public boolean next() {
        if (loadFlag == false) {
            try {
                doLoad();
            } catch (SQLException e) {
                throw new RuntimeException("加载异常exception", e);
            }
            loadFlag = true;
        }
        curRowNo++;
        if (null == queryReturnValueLists ||
                curRowNo >= this.queryReturnValueLists.size()) {
            return false;
        }
        return true;
    }

    @Override
    public void close() throws SQLException {
        this.reader.close();
    }

    @Override
    public boolean wasNull() throws SQLException {
        return false;
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {
        return false;
    }

    @Override
    public byte getByte(int columnIndex) throws SQLException {
        return 0;
    }

    @Override
    public short getShort(int columnIndex) throws SQLException {
        return 0;
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        return 0;
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {
        return 0;
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException {
        return 0;
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {
        return 0;
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        return null;
    }

    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {
        return new byte[0];
    }

    @Override
    public Date getDate(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Time getTime(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        return false;
    }

    @Override
    public byte getByte(String columnLabel) throws SQLException {
        return 0;
    }

    @Override
    public short getShort(String columnLabel) throws SQLException {
        return 0;
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        return 0;
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {
        return 0;
    }

    @Override
    public float getFloat(String columnLabel) throws SQLException {
        return 0;
    }

    @Override
    public double getDouble(String columnLabel) throws SQLException {
        return 0;
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        return null;
    }

    @Override
    public byte[] getBytes(String columnLabel) throws SQLException {
        return new byte[0];
    }

    @Override
    public Date getDate(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Time getTime(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public InputStream getAsciiStream(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public InputStream getUnicodeStream(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public InputStream getBinaryStream(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {

    }

    @Override
    public String getCursorName() throws SQLException {
        return null;
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        if (resultSetMetaData == null) {
            String[] readerTypeNames = reader.getColumnTypes();
            String[] readerColumnNames = reader.getColumnNames();
            int[] readerColumnSizes = reader.getColumnSizes();
            String tableAlias = reader.getTableAlias();
            int columnCount = queryReturnColInfos.size();
            String[] columnNames = new String[columnCount];
            String[] columnLabels = new String[columnCount];
            int[] columnSizes = new int[columnCount];
            String[] typeNames = new String[columnCount];

			/*
             * Create a record containing dummy values.
			 */
            HashMap<String, Object> env = new HashMap<String, Object>();
            for (int i = 0; i < readerTypeNames.length; i++) {
                Object literal = StringConverter.getLiteralForTypeName(readerTypeNames[i]);
                String columnName = readerColumnNames[i].toUpperCase();
                env.put(columnName, literal);
                if (tableAlias != null) {
                    env.put(tableAlias + "." + columnName, literal);
                }
            }
            if (converter != null) {
                env.put(StringConverter.COLUMN_NAME, converter);
            }
            env.put("@STATEMENT", statement);

            for (int i = 0; i < columnCount; i++) {
                Object[] o = queryReturnColInfos.get(i);
                columnNames[i] = (String) o[0];
                columnLabels[i] = columnNames[i];

				/*
                 * Evaluate each expression to determine what data type it returns.
				 */
                Object result = null;
                try {
                    Expression expr = ((Expression) o[1]);

                    int columnSize = DEFAULT_COLUMN_SIZE;
                    if (expr instanceof Column) {
                        String usedColumn = ((Column) expr).getColumnName();
                        for (int k = 0; k < readerColumnNames.length; k++) {
                            if (usedColumn.equalsIgnoreCase(readerColumnNames[k])) {
                                columnSize = readerColumnSizes[k];
                                break;
                            }
                        }

                        result = ((Column) expr).getColumnName();
                    }
                    columnSizes[i] = columnSize;

                } catch (NullPointerException e) {
                    /* Expression is invalid */
                }
                if (result != null) {
                    typeNames[i] = StringConverter.getTypeNameForLiteral(result);
                } else {
                    typeNames[i] = "expression";
                }
            }
            resultSetMetaData = new DbfResultSetMetaData(tableName, columnNames, columnLabels, typeNames,
                    columnSizes, this.colAliasNameMap);
        }
        return resultSetMetaData;
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        return this.queryReturnValueLists.get(curRowNo).get(columnIndex - 1);
    }

    @Override
    public Object getObject(String columnLabel) throws SQLException {
        Object obj = this.queryNameValueMaps.get(curRowNo).get(columnLabel.toUpperCase());
        if (null == obj && null != this.queryAliasNameValueMaps &&
                null != this.queryAliasNameValueMaps.get(curRowNo)) {
            obj = this.queryAliasNameValueMaps.get(curRowNo).get(columnLabel.toUpperCase());
        }
        return obj;
    }

    @Override
    public int findColumn(String columnLabel) throws SQLException {
        return 0;
    }

    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Reader getCharacterStream(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        return false;
    }

    @Override
    public boolean isAfterLast() throws SQLException {
        return false;
    }

    @Override
    public boolean isFirst() throws SQLException {
        return false;
    }

    @Override
    public boolean isLast() throws SQLException {
        return false;
    }

    @Override
    public void beforeFirst() throws SQLException {

    }

    @Override
    public void afterLast() throws SQLException {

    }

    @Override
    public boolean first() throws SQLException {
        return false;
    }

    @Override
    public boolean last() throws SQLException {
        return false;
    }

    @Override
    public int getRow() throws SQLException {
        return 0;
    }

    @Override
    public boolean absolute(int row) throws SQLException {
        return false;
    }

    @Override
    public boolean relative(int rows) throws SQLException {
        return false;
    }

    @Override
    public boolean previous() throws SQLException {
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
    public int getType() throws SQLException {
        return 0;
    }

    @Override
    public int getConcurrency() throws SQLException {
        return 0;
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        return false;
    }

    @Override
    public boolean rowInserted() throws SQLException {
        return false;
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        return false;
    }

    @Override
    public void updateNull(int columnIndex) throws SQLException {

    }

    @Override
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {

    }

    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException {

    }

    @Override
    public void updateShort(int columnIndex, short x) throws SQLException {

    }

    @Override
    public void updateInt(int columnIndex, int x) throws SQLException {

    }

    @Override
    public void updateLong(int columnIndex, long x) throws SQLException {

    }

    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException {

    }

    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException {

    }

    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {

    }

    @Override
    public void updateString(int columnIndex, String x) throws SQLException {

    }

    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {

    }

    @Override
    public void updateDate(int columnIndex, Date x) throws SQLException {

    }

    @Override
    public void updateTime(int columnIndex, Time x) throws SQLException {

    }

    @Override
    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {

    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {

    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {

    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {

    }

    @Override
    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {

    }

    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException {

    }

    @Override
    public void updateNull(String columnLabel) throws SQLException {

    }

    @Override
    public void updateBoolean(String columnLabel, boolean x) throws SQLException {

    }

    @Override
    public void updateByte(String columnLabel, byte x) throws SQLException {

    }

    @Override
    public void updateShort(String columnLabel, short x) throws SQLException {

    }

    @Override
    public void updateInt(String columnLabel, int x) throws SQLException {

    }

    @Override
    public void updateLong(String columnLabel, long x) throws SQLException {

    }

    @Override
    public void updateFloat(String columnLabel, float x) throws SQLException {

    }

    @Override
    public void updateDouble(String columnLabel, double x) throws SQLException {

    }

    @Override
    public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {

    }

    @Override
    public void updateString(String columnLabel, String x) throws SQLException {

    }

    @Override
    public void updateBytes(String columnLabel, byte[] x) throws SQLException {

    }

    @Override
    public void updateDate(String columnLabel, Date x) throws SQLException {

    }

    @Override
    public void updateTime(String columnLabel, Time x) throws SQLException {

    }

    @Override
    public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {

    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {

    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {

    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {

    }

    @Override
    public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {

    }

    @Override
    public void updateObject(String columnLabel, Object x) throws SQLException {

    }

    @Override
    public void insertRow() throws SQLException {

    }

    @Override
    public void updateRow() throws SQLException {

    }

    @Override
    public void deleteRow() throws SQLException {

    }

    @Override
    public void refreshRow() throws SQLException {

    }

    @Override
    public void cancelRowUpdates() throws SQLException {

    }

    @Override
    public void moveToInsertRow() throws SQLException {

    }

    @Override
    public void moveToCurrentRow() throws SQLException {

    }

    @Override
    public Statement getStatement() throws SQLException {
        return this.statement;
    }

    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
        return null;
    }

    @Override
    public Ref getRef(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Blob getBlob(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Clob getClob(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Array getArray(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
        return null;
    }

    @Override
    public Ref getRef(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Blob getBlob(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Clob getClob(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Array getArray(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Date getDate(String columnLabel, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Time getTime(String columnLabel, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public URL getURL(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public URL getURL(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public void updateRef(int columnIndex, Ref x) throws SQLException {

    }

    @Override
    public void updateRef(String columnLabel, Ref x) throws SQLException {

    }

    @Override
    public void updateBlob(int columnIndex, Blob x) throws SQLException {

    }

    @Override
    public void updateBlob(String columnLabel, Blob x) throws SQLException {

    }

    @Override
    public void updateClob(int columnIndex, Clob x) throws SQLException {

    }

    @Override
    public void updateClob(String columnLabel, Clob x) throws SQLException {

    }

    @Override
    public void updateArray(int columnIndex, Array x) throws SQLException {

    }

    @Override
    public void updateArray(String columnLabel, Array x) throws SQLException {

    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {

    }

    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException {

    }

    @Override
    public int getHoldability() throws SQLException {
        return 0;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }

    @Override
    public void updateNString(int columnIndex, String nString) throws SQLException {

    }

    @Override
    public void updateNString(String columnLabel, String nString) throws SQLException {

    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {

    }

    @Override
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {

    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public NClob getNClob(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {

    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {

    }

    @Override
    public String getNString(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public String getNString(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {

    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {

    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {

    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {

    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {

    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {

    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {

    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {

    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {

    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {

    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {

    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {

    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {

    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {

    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {

    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {

    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {

    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {

    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException {

    }

    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException {

    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {

    }

    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException {

    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        return null;
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    public void setStatement(DbfStatement statement) {
        this.statement = statement;
    }

    public int getResultSetType() {
        return resultSetType;
    }

    public void setResultSetType(int resultSetType) {
        this.resultSetType = resultSetType;
    }

    public DataReaderWriter getReader() {
        return reader;
    }

    public void setReader(DataReaderWriter reader) {
        this.reader = reader;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public ResultSetMetaData getResultSetMetaData() {
        return resultSetMetaData;
    }

    public void setResultSetMetaData(ResultSetMetaData resultSetMetaData) {
        this.resultSetMetaData = resultSetMetaData;
    }

    public Limit getLimit() {
        return limit;
    }

    public void setLimit(Limit limit) {
        this.limit = limit;
    }

    public List<Object[]> getQueryReturnColInfos() {
        return queryReturnColInfos;
    }

    public void setQueryReturnColInfos(List<Object[]> queryReturnColInfos) {
        this.queryReturnColInfos = queryReturnColInfos;
    }

    public List<Map<String, Object>> getQueryNameValueMaps() {
        return queryNameValueMaps;
    }

    public void setQueryNameValueMaps(List<Map<String, Object>> queryNameValueMaps) {
        this.queryNameValueMaps = queryNameValueMaps;
    }

    public List<Map<String, Object>> getQueryAliasNameValueMaps() {
        return queryAliasNameValueMaps;
    }

    public void setQueryAliasNameValueMaps(List<Map<String, Object>> queryAliasNameValueMaps) {
        this.queryAliasNameValueMaps = queryAliasNameValueMaps;
    }

    public List<List<Object>> getQueryReturnValueLists() {
        return queryReturnValueLists;
    }

    public void setQueryReturnValueLists(List<List<Object>> queryReturnValueLists) {
        this.queryReturnValueLists = queryReturnValueLists;
    }

    public Map<String, String> getColAliasNameMap() {
        return colAliasNameMap;
    }

    public void setColAliasNameMap(Map<String, String> colAliasNameMap) {
        this.colAliasNameMap = colAliasNameMap;
    }

    public int getCurRowNo() {
        return curRowNo;
    }

    public void setCurRowNo(int curRowNo) {
        this.curRowNo = curRowNo;
    }
}