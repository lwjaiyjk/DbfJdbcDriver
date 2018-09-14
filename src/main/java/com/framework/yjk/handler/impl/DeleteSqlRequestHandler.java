package com.framework.yjk.handler.impl;

import com.framework.yjk.DataReaderWriter;
import com.framework.yjk.DbfStatement;
import com.framework.yjk.handler.AbstractSqlRequestHandler;
import com.framework.yjk.sqlparser.CommonWhereExpressionVisitor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;

import java.sql.SQLException;

/**
 * @author yujiakui
 * @version 1.0
 * Email: jkyu@haiyi-info.com
 * date: 2018/9/12 8:37
 * description：删除对应的sql请求处理
 **/
@Slf4j
public class DeleteSqlRequestHandler extends AbstractSqlRequestHandler {

    @Override
    protected void doHandle(DbfStatement dbfStatement,
                            DataReaderWriter dataReaderWriter,
                            Statement sqlStatement,
                            String sql,String tableName) throws SQLException {
        Delete deleteSql = (Delete) sqlStatement;

        CommonWhereExpressionVisitor whereExpressionVisitor = new CommonWhereExpressionVisitor();
        // sql语句解析
        Expression expression = deleteSql.getWhere();
        whereExpressionVisitor.setExpression(expression);
        whereExpressionVisitor.setDataReaderWriter(dataReaderWriter);

        int delRecordNum = 0;
        // 通过读取dbf文件获得对应的记录
        while (dataReaderWriter.next()) {
            log.debug("dbf 读取结果{}", dataReaderWriter.getRecordMap());
            whereExpressionVisitor.setValueMaps(dataReaderWriter.getRecordMap());
            // where 条件
            expression.accept(whereExpressionVisitor);
            Boolean filterFlag = (Boolean) whereExpressionVisitor.getResult();
            if (filterFlag) {
                // 将记录保存到结果集合中
                delRecordNum++;
                // 删除操作
                dataReaderWriter.removeCurRecord();
            }

        }

        log.info("sql={}删除记录个数{}", sql, delRecordNum);
    }
}
