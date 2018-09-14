package com.framework.yjk.dbfio;

import com.framework.yjk.DataReaderWriter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.dans.common.dbflib.*;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author yujiakui
 * @version 1.0
 * Email: jkyu@haiyi-info.com
 * date: 2018/9/11 16:17
 * description：dbf文件对应的读写操作
 **/
public class DbfDataReaderWriter implements DataReaderWriter {

    /**
     * 记录条数
     */
    private Integer recordCount;
    /**
     * 行号
     */
    private int rowNo;
    private Table dbfTable;
    private Record dbfRecord;
    private Map<String, String> dbfTypeToSQLType;
    /**
     * 表别名
     */
    private String tableAlias;
    private List<Field> dbfFields;
    /**
     * 字段名称映射，其中key为字段名称的大写，value是原始的字段名称
     */
    private Map<String, String> fieldNameMap;
    /**
     * 当前记录
     */
    private Record curRecord;

    public DbfDataReaderWriter(String path, String tableAlias, String charset) throws SQLException {
        super();
        try {
            if (StringUtils.isNotBlank(charset)) {
                dbfTable = new Table(new File(path), charset);
            } else {
                dbfTable = new Table(new File(path));
            }
            dbfTable.open();
            dbfFields = dbfTable.getFields();
            fieldNameMap = Maps.newHashMap();
            for (Field colField : dbfFields) {
                fieldNameMap.put(colField.getName().toUpperCase(), colField.getName());
            }
            rowNo = -1;
            this.tableAlias = tableAlias;
            recordCount = dbfTable.getRecordCount();
        } catch (Exception e) {
            throw new SQLException("dbf 文件读取失败: " + e);
        }
        dbfTypeToSQLType = Maps.newHashMap();
        dbfTypeToSQLType.put("CHARACTER", "String");
        dbfTypeToSQLType.put("NUMBER", "Double");
        dbfTypeToSQLType.put("LOGICAL", "Boolean");
        dbfTypeToSQLType.put("DATE", "Date");
        dbfTypeToSQLType.put("MEMO", "String");
        dbfTypeToSQLType.put("FLOAT", "Double");
    }

    /**
     * 获取记录条数
     *
     * @return
     */
    @Override
    public int getRecordCount() {
        return dbfTable.getRecordCount();
    }

    @Override
    public void skip(int rowNum) {
        this.rowNo = rowNum;
    }

    @Override
    public void removeRecordAt(int recordIndex) throws SQLException {
        try {
            dbfTable.deleteRecordAt(recordIndex);
        } catch (IOException e) {
            throw new SQLException("dbf 文件记录删除失败: " + e);
        }
    }

    @Override
    public void removeCurRecord() throws SQLException {
        removeRecordAt(this.rowNo);
    }

    @Override
    public void updateRecordAt(int recordIndex, Record record) throws SQLException {
        try {
            dbfTable.updateRecordAt(recordIndex, record);
        } catch (Exception e) {
            throw new SQLException("dbf 文件记录更新失败: " + e);
        }
    }

    @Override
    public int getCurRecordPos() {
        return this.rowNo;
    }

    @Override
    public Record getCurRecord() {
        return this.curRecord;
    }

    @Override
    public void close() throws SQLException {
        if (dbfTable != null) {
            try {
                dbfTable.close();
            } catch (Exception e) {
                throw new SQLException("dbf 文件关闭失败: " + e);
            }
        }
        dbfTable = null;
    }

    @Override
    public void insertRecord(Record record) throws SQLException {
        try {
            dbfTable.addRecord(record);
        } catch (Exception e) {
            throw new SQLException("dbf文件插入记录失败: " + e);
        }
    }

    @Override
    public String[] getColumnNames() throws SQLException {
        int columnCount = dbfFields.size();
        String[] result = new String[columnCount];
        for (int i = 0; i < columnCount; i++) {
            Field field = dbfFields.get(i);
            result[i] = field.getName();
        }
        return result;
    }

    @Override
    public Object getField(int i) throws SQLException {
        Field field = dbfFields.get(i - 1);
        String fieldName = field.getName();

        Object result = curRecord.getTypedValue(fieldName);
        if (result instanceof String) {
            result = ((String) result).trim();
        } else if (result instanceof java.util.Date) {
            result = new java.sql.Date(((java.util.Date) result).getTime());
        }
        return result;
    }

    @Override
    public boolean next() throws SQLException {
        rowNo++;

        if (rowNo >= recordCount.intValue()) {
            return false;
        }

        try {
            curRecord = dbfTable.getRecordAt(rowNo);
        } catch (Exception e) {
            throw new SQLException("dbf文件读取下一行记录失败: " + e);
        }
        return true;
    }

    @Override
    public String[] getColumnTypes() throws SQLException {
        String[] result = new String[dbfFields.size()];
        for (int i = 0; i < dbfFields.size(); i++) {
            String dbfType = "";
            Field field = dbfFields.get(i);
            try {
                dbfType = field.getType().toString();
            } catch (Exception e) {
                throw new SQLException("dbf文件获取对应的列类型失败: " + e);
            }
            result[i] = dbfTypeToSQLType.get(dbfType);
            if (result[i] == null) {
                throw new SQLException("dbfTypeNotSupported: " + dbfType);
            }
        }
        return result;
    }

    @Override
    public int[] getColumnSizes() throws SQLException {
        int[] result = new int[dbfFields.size()];
        for (int i = 0; i < dbfFields.size(); i++) {
            Field field = dbfFields.get(i);
            result[i] = field.getLength();
        }
        return result;
    }

    @Override
    public Map<String, Object> getRecordMap() throws SQLException {
        Map<String, Object> result = new HashMap<String, Object>();
        for (int i = 0; i < dbfFields.size(); i++) {
            Field field = dbfFields.get(i);
            try {
                String fieldName = field.getName();
                Object o = getField(i + 1);
                /*
                 * Convert column names to upper case because
				 * that is what query environment uses.
				 */
                fieldName = fieldName.toUpperCase();
                result.put(fieldName, o);
                if (tableAlias != null) {
                    /*
                     * Also allow field value to be accessed as S.ID  if table alias S is set.
					 */
                    result.put(tableAlias + "." + fieldName, o);
                }
            } catch (Exception e) {
                throw new SQLException("dbf文件获取对应的记录map失败: " + e);
            }
        }
        return result;
    }

    @Override
    public String getTableAlias() {
        return tableAlias;
    }

    public Map<String, String> getFieldNameMap() {
        return fieldNameMap;
    }
}
