package com.framework.yjk;

import nl.knaw.dans.common.dbflib.Record;

import java.sql.SQLException;
import java.util.Map;

/**
 * @author yujiakui
 * @version 1.0
 * Email: jkyu@haiyi-info.com
 * date: 2018/9/11 16:12
 * description：数据读写接口
 **/
public interface DataReaderWriter {

    /**
     * 读取下一条记录
     *
     * @return
     * @throws SQLException
     */
    boolean next() throws SQLException;

    /**
     * 获取对应的列名列表
     *
     * @return
     * @throws SQLException
     */
    String[] getColumnNames() throws SQLException;

    /**
     * 获取索引为i对应的字段
     *
     * @param i
     * @return
     * @throws SQLException
     */
    Object getField(int i) throws SQLException;

    /**
     * 关闭
     *
     * @throws SQLException
     */
    void close() throws SQLException;

    /**
     * 获取记录对应的map，其中key为列名，value为列对应的值
     *
     * @return
     * @throws SQLException
     */
    Map<String, Object> getRecordMap() throws SQLException;

    /**
     * 获取列对应的类型
     *
     * @return
     * @throws SQLException
     */
    String[] getColumnTypes() throws SQLException;

    /**
     * 获取记录每一个列对应的大小
     *
     * @return
     * @throws SQLException
     */
    int[] getColumnSizes() throws SQLException;

    /**
     * 获取表的别名
     *
     * @return
     */
    String getTableAlias();

    /**
     * 插入记录
     *
     * @param record
     */
    void insertRecord(Record record) throws SQLException;

    /**
     * 获取记录条数
     *
     * @return
     */
    int getRecordCount();

    /**
     * 跳过多少行
     *
     * @param rowNum
     */
    void skip(int rowNum);

    /**
     * 删除记录
     *
     * @param recordIndex
     */
    void removeRecordAt(int recordIndex) throws SQLException;

    /**
     * 删除当前记录
     */
    void removeCurRecord() throws SQLException;

    /**
     * 更新recordIndex位置上的记录
     *
     * @param recordIndex
     * @param record
     */
    void updateRecordAt(int recordIndex, Record record) throws SQLException;

    /**
     * 获取当前记录的索引
     *
     * @return
     */
    int getCurRecordPos();

    /**
     * 获取当前记录
     *
     * @return
     */
    Record getCurRecord();
}
