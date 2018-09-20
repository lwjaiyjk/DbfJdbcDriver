package com.framework.yjk.handler.impl;

import com.framework.yjk.DataReaderWriter;
import com.framework.yjk.DbfStatement;
import com.framework.yjk.dbfio.DbfDataReaderWriter;
import com.framework.yjk.handler.AbstractSqlRequestHandler;
import com.framework.yjk.sqlparser.CommonInsertExpressionVisitor;
import com.framework.yjk.sqlparser.CommonWhereExpressionVisitor;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.update.Update;
import nl.knaw.dans.common.dbflib.Record;
import nl.knaw.dans.common.dbflib.Value;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author yujiakui
 * @version 1.0
 * Email: jkyu@haiyi-info.com
 * date: 2018/9/12 8:58
 * description：更新sql 请求处理
 **/
@Slf4j
public class UpdateSqlRequestHandler extends AbstractSqlRequestHandler {

    @Override
    protected void doHandle(DbfStatement dbfStatement,
                            DataReaderWriter dataReaderWriter,
                            Statement sqlStatement, String sql, String tableName) throws SQLException {

        Update updateSql = (Update) sqlStatement;
        CommonWhereExpressionVisitor whereExpressionVisitor = new CommonWhereExpressionVisitor();

        // sql语句解析
        Expression expression = updateSql.getWhere();
        if (null != expression) {
            whereExpressionVisitor.setExpression(expression);
            whereExpressionVisitor.setDataReaderWriter(dataReaderWriter);
        }

        // 将需要更新的列和值放入map中
        DbfDataReaderWriter dbfDataReaderWriter = (DbfDataReaderWriter)dataReaderWriter;
        Map<String, Value> updateColInfoMap = getUpdateSqlColInfo(updateSql,dbfDataReaderWriter.getFieldNameMap());

        int delRecordNum = 0;
        // 通过读取dbf文件获得对应的记录
        while (dataReaderWriter.next()) {
            log.debug("dbf 读取结果{}", dataReaderWriter.getRecordMap());
            Boolean filterFlag = true;
            if (null != expression) {
                whereExpressionVisitor.setValueMaps(dataReaderWriter.getRecordMap());
                // where 条件
                expression.accept(whereExpressionVisitor);
                filterFlag = (Boolean) whereExpressionVisitor.getResult();
            }
            if (filterFlag) {
                // 将记录保存到结果集合中
                delRecordNum++;
                // 更新操作
                updateRecordColInfo(dataReaderWriter, updateColInfoMap);
            }

        }

        log.info("sql={}更新记录个数{}", sql, delRecordNum);
    }

    /**
     * 更新指定的列
     *
     * @param dataReaderWriter
     * @param updateColInfoMap
     */
    private void updateRecordColInfo(DataReaderWriter dataReaderWriter,
                                     Map<String, Value> updateColInfoMap) throws SQLException {
        Record curRecord = dataReaderWriter.getCurRecord();
        Map<String, Value> recordColInfoMap = null;
        try {
            Field field = Record.class.getDeclaredField("valueMap");
            field.setAccessible(true);
            recordColInfoMap = (Map<String, Value>) field.get(curRecord);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        recordColInfoMap.putAll(updateColInfoMap);
        Record record = new Record(recordColInfoMap);
        dataReaderWriter.updateRecordAt(dataReaderWriter.getCurRecordPos(), record);
    }

    /**
     * 获取更新sql 列信息
     *
     * @param updateSql
     * @return
     */
    private Map<String, Value> getUpdateSqlColInfo(Update updateSql,Map<String,String> fieldNameMap) {
        Map<String, Value> updateColValue = Maps.newHashMap();
        List<Column> updateCols = updateSql.getColumns();
        List<Expression> updateExps = updateSql.getExpressions();
        CommonInsertExpressionVisitor commonExpVisitor = new CommonInsertExpressionVisitor();
        for (int i = 0; i < updateCols.size(); i++) {
            Column col = updateCols.get(i);
            Expression exp = updateExps.get(i);
            exp.accept(commonExpVisitor);
            updateColValue.put(fieldNameMap.get(col.getColumnName().toUpperCase())
                    , commonExpVisitor.getResult());
        }
        return updateColValue;
    }
}
