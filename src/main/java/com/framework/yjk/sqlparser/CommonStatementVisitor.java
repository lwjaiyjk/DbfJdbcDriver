package com.framework.yjk.sqlparser;

import com.framework.yjk.DataReaderWriter;
import com.framework.yjk.dbfio.DbfDataReaderWriter;
import com.google.common.collect.Maps;
import lombok.Data;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.insert.Insert;
import nl.knaw.dans.common.dbflib.Value;

import java.util.List;
import java.util.Map;

/**
 * @author yujiakui
 * @version 1.0
 * Email: jkyu@haiyi-info.com
 * date: 2018/9/11 18:26
 * description：
 **/
@Data
public class CommonStatementVisitor extends StatementVisitorAdapter {

    /**
     * 插入解析映射
     */
    private Map<String, Value> insertParseResultMap;

    /**
     * 数读写
     */
    private DbfDataReaderWriter dataReaderWriter;

    public CommonStatementVisitor(DbfDataReaderWriter dataReaderWriter) {
        this.dataReaderWriter = dataReaderWriter;
    }

    @Override
    public void visit(Insert insert) {
        List<Column> columns = insert.getColumns();
        ExpressionList itemsList = (ExpressionList) insert.getItemsList();
        List<Expression> expressions = itemsList.getExpressions();
        insertParseResultMap = Maps.newHashMap();
        CommonInsertExpressionVisitor myInsertExpressionVisitor = new CommonInsertExpressionVisitor();
        Map<String, String> colNameMap = dataReaderWriter.getFieldNameMap();
        for (int i = 0; i < columns.size(); i++) {
            Column tempCol = columns.get(i);
            Expression expression = expressions.get(i);
            expression.accept(myInsertExpressionVisitor);
            insertParseResultMap.put(colNameMap.get(tempCol.getColumnName().toUpperCase()),
                    myInsertExpressionVisitor.getResult());
        }

        System.out.println(insertParseResultMap);
    }
}
