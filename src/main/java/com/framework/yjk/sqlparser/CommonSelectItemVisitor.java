package com.framework.yjk.sqlparser;

import com.framework.yjk.DataReaderWriter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author yujiakui
 * @version 1.0
 * Email: jkyu@haiyi-info.com
 * date: 2018/9/11 17:50
 * description：通用select item 访问器
 **/
@Data
public class CommonSelectItemVisitor extends SelectItemVisitorAdapter {

    /**
     * 日志
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(CommonSelectItemVisitor.class);

    /**
     * 字段信息长度
     */
    private final static int FIELD_INFO_LENGTH = 5;

    /**
     * 字段相关信息
     */
    private List<Object[]> fieldCntInfos = Lists.newArrayList();

    /**
     * 列名别名映射
     */
    private Map<String, String> colAliasNameMap = Maps.newHashMap();

    /**
     * 数据读取器
     */
    private DataReaderWriter dataReaderWriter;

    public CommonSelectItemVisitor(DataReaderWriter dataReaderWriter) {
        this.dataReaderWriter = dataReaderWriter;
    }

    @Override
    public void visit(AllColumns columns) {
        LOGGER.info("MySelectItemVisitor visit AllColumns={}", columns);
        fieldCntInfos.clear();
        try {
            for (String colName : dataReaderWriter.getColumnNames()) {
                Object[] fieldCntInfo = new Object[5];
                fieldCntInfo[0] = colName.toUpperCase();
                fieldCntInfo[1] = new Column(colName.toUpperCase());
                fieldCntInfos.add(fieldCntInfo);
            }
        } catch (SQLException e) {
            LOGGER.error("MySelectItemVisitor visit AllColumns SQLException={}", e);
            throw new RuntimeException("MySelectItemVisitor visit AllColumns", e);
        }
    }

    @Override
    public void visit(AllTableColumns columns) {
        LOGGER.info("MySelectItemVisitor visit AllTableColumns={}", columns);
    }

    @Override
    public void visit(SelectExpressionItem item) {
        LOGGER.info("MySelectItemVisitor visit SelectExpressionItem={}", item);
        fieldCntInfos.clear();
        colAliasNameMap.clear();
        Object[] fieldCntInfo = new Object[FIELD_INFO_LENGTH];
        Expression expression = item.getExpression();
        if (expression instanceof Column) {
            fieldCntInfo[0] = ((Column) expression).getColumnName().toUpperCase();
        }
        if (null != item.getAlias()) {
            colAliasNameMap.put((String) fieldCntInfo[0], item.getAlias().getName());
        }
        fieldCntInfo[1] = expression;
        fieldCntInfos.add(fieldCntInfo);
    }
}
