package com.framework.yjk.sqlparser;

import com.framework.yjk.DataReaderWriter;
import com.google.common.collect.Lists;
import lombok.Data;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author yujiakui
 * @version 1.0
 * Email: jkyu@haiyi-info.com
 * date: 2018/9/11 17:58
 * description：
 **/
@Data
public class CommonWhereExpressionVisitor extends ExpressionVisitorAdapter {

    /**
     * 日志
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(CommonWhereExpressionVisitor.class);

    private Object result;
    private DataReaderWriter dataReaderWriter;
    private Expression expression;
    private Map<String, Object> valueMaps;

    /**
     * 无参数构造函数
     */
    public CommonWhereExpressionVisitor() {
    }

    /**
     * Creates a new MyExpressionVisitor object.
     *
     * @param dataReaderWriter DOCUMENT ME!
     */
    public CommonWhereExpressionVisitor(DataReaderWriter dataReaderWriter, Expression expression) {
        this.dataReaderWriter = dataReaderWriter;
        this.expression = expression;
        try {
            valueMaps = dataReaderWriter.getRecordMap();
        } catch (SQLException e) {
            LOGGER.error("MyWhereExpressionVisitor SQLException={}", e);
            throw new RuntimeException("MyWhereExpressionVisitor SQLException", e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param nullValue DOCUMENT ME!
     */
    public void visit(NullValue nullValue) {
        this.result = null;
    }


    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {

    }

    /**
     * DOCUMENT ME!
     *
     * @param doubleValue DOCUMENT ME!
     */
    public void visit(DoubleValue doubleValue) {
        result = doubleValue.getValue();
    }

    /**
     * DOCUMENT ME!
     *
     * @param longValue DOCUMENT ME!
     */
    public void visit(LongValue longValue) {
        this.result = longValue.getValue();
    }


    /**
     * DOCUMENT ME!
     *
     * @param dateValue DOCUMENT ME!
     */
    public void visit(DateValue dateValue) {
        result = dateValue.getValue();
    }

    /**
     * DOCUMENT ME!
     *
     * @param timeValue DOCUMENT ME!
     */
    public void visit(TimeValue timeValue) {
        result = timeValue.getValue();
    }

    /**
     * DOCUMENT ME!
     *
     * @param timestampValue DOCUMENT ME!
     */
    public void visit(TimestampValue timestampValue) {
        result = timestampValue.getValue();
    }

    /**
     * DOCUMENT ME!
     *
     * @param parenthesis DOCUMENT ME!
     */
    public void visit(Parenthesis parenthesis) {
        parenthesis.getExpression().accept(this);
    }


    /**
     * DOCUMENT ME!
     *
     * @param andExpression DOCUMENT ME!
     */
    public void visit(AndExpression andExpression) {
        Expression left = andExpression.getLeftExpression();
        left.accept(this);

        Object leftValue = result;

        Expression right = andExpression.getRightExpression();
        right.accept(this);

        Object rightValue = result;

        result = ((Boolean) leftValue && (Boolean) rightValue) ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * DOCUMENT ME!
     *
     * @param orExpression DOCUMENT ME!
     */
    public void visit(OrExpression orExpression) {
        Expression left = orExpression.getLeftExpression();
        left.accept(this);

        Object leftValue = result;

        Expression right = orExpression.getRightExpression();
        right.accept(this);

        Object rightValue = result;

        result = (Boolean.valueOf(leftValue.toString()) || Boolean.valueOf(rightValue.toString())) ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * DOCUMENT ME!
     *
     * @param between DOCUMENT ME!
     */
    public void visit(Between between) {
        Expression left = between.getLeftExpression();
        left.accept(this);

        Object leftValue = result;

        Expression start = between.getBetweenExpressionStart();
        start.accept(this);

        Object startValue = result;

        Expression end = between.getBetweenExpressionStart();
        end.accept(this);

        Object endValue = result;

        if (!between.isNot()) {

            boolean ge = Integer.valueOf(leftValue.toString()) >= Integer.valueOf(startValue.toString());
            boolean le = Integer.valueOf(leftValue.toString()) <= Integer.valueOf(endValue.toString());

            result = Boolean.valueOf(ge && le);

        } else {

            boolean ge = Integer.valueOf(leftValue.toString()) > Integer.valueOf(startValue.toString());
            boolean le = Integer.valueOf(leftValue.toString()) < Integer.valueOf(endValue.toString());

            result = Boolean.valueOf(ge || le);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param equalsTo DOCUMENT ME!
     */
    public void visit(EqualsTo equalsTo) {
        Expression left = equalsTo.getLeftExpression();
        left.accept(this);

        Object leftValue = result;

        Expression right = equalsTo.getRightExpression();
        right.accept(this);

        Object rightValue = result;

        if (null != leftValue && null != rightValue) {
            leftValue = leftValue.toString();
            rightValue = rightValue.toString();
        }
        if (null == leftValue && null == rightValue) {
            result = true;
        } else if ((null == leftValue && null != rightValue) ||
                (null != leftValue && null == rightValue)) {
            result = false;
        } else {
            result = (leftValue.equals(rightValue)) ? Boolean.TRUE : Boolean.FALSE;
        }

    }

    /**
     * DOCUMENT ME!
     *
     * @param greaterThan DOCUMENT ME!
     */
    public void visit(GreaterThan greaterThan) {
        Expression left = greaterThan.getLeftExpression();
        left.accept(this);

        Object leftValue = result;

        Expression right = greaterThan.getRightExpression();
        right.accept(this);

        Object rightValue = result;

        compareObjValue(leftValue, rightValue, Lists.newArrayList(1));
    }

    /**
     * 值比较
     *
     * @param leftValue
     * @param rightValue
     * @return
     */
    private void compareObjValue(Object leftValue, Object rightValue, List<Integer> trueVaues) {

        if (null == leftValue && null == rightValue) {
            result = Boolean.TRUE;
        } else if ((null != leftValue && null == rightValue) ||
                (null == leftValue && null != rightValue)) {
            result = Boolean.FALSE;
        } else {
            String leftValueStr = leftValue.toString();
            String rightValueStr = rightValue.toString();
            result = trueVaues.contains(leftValueStr.compareTo(rightValueStr));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param greaterThanEquals DOCUMENT ME!
     */
    public void visit(GreaterThanEquals greaterThanEquals) {
        Expression left = greaterThanEquals.getLeftExpression();
        left.accept(this);

        Object leftValue = result;

        Expression right = greaterThanEquals.getRightExpression();
        right.accept(this);

        Object rightValue = result;

        compareObjValue(leftValue, rightValue, Lists.newArrayList(1, 0));


    }

    /**
     * DOCUMENT ME!
     *
     * @param isNullExpression DOCUMENT ME!
     */
    public void visit(IsNullExpression isNullExpression) {
        Expression left = isNullExpression.getLeftExpression();
        left.accept(this);

        boolean isnull = result == null;

        if (isNullExpression.isNot()) {
            result = !isnull;
        } else {
            result = isnull;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param minorThan DOCUMENT ME!
     */
    public void visit(MinorThan minorThan) {
        Expression left = minorThan.getLeftExpression();
        left.accept(this);

        Object leftValue = result;

        Expression right = minorThan.getRightExpression();
        right.accept(this);

        Object rightValue = result;

        compareObjValue(leftValue, rightValue, Lists.newArrayList(-1));

    }

    /**
     * DOCUMENT ME!
     *
     * @param minorThanEquals DOCUMENT ME!
     */
    public void visit(MinorThanEquals minorThanEquals) {
        Expression left = minorThanEquals.getLeftExpression();
        left.accept(this);

        Object leftValue = result;

        Expression right = minorThanEquals.getRightExpression();
        right.accept(this);

        Object rightValue = result;

        compareObjValue(leftValue, rightValue, Lists.newArrayList(-1, 0));

    }

    /**
     * DOCUMENT ME!
     *
     * @param notEqualsTo DOCUMENT ME!
     */
    public void visit(NotEqualsTo notEqualsTo) {
        Expression left = notEqualsTo.getLeftExpression();
        left.accept(this);

        Object leftValue = result;

        Expression right = notEqualsTo.getRightExpression();
        right.accept(this);

        Object rightValue = result;

        compareObjValue(leftValue, rightValue, Lists.newArrayList(-1, 1));
    }

    /**
     * DOCUMENT ME!
     *
     * @param tableColumn DOCUMENT ME!
     */
    public void visit(Column tableColumn) {
        result = this.valueMaps.get(tableColumn.getColumnName().toUpperCase());
    }


    @Override
    public void visit(net.sf.jsqlparser.expression.StringValue stringValue) {
        result = stringValue.getValue();
    }
}
