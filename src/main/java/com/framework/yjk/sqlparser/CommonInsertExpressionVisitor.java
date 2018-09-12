package com.framework.yjk.sqlparser;

import net.sf.jsqlparser.expression.*;
import nl.knaw.dans.common.dbflib.NumberValue;
import nl.knaw.dans.common.dbflib.Value;

/**
 * @author yujiakui
 * @version 1.0
 * Email: jkyu@haiyi-info.com
 * date: 2018/9/11 18:27
 * description：
 **/
public class CommonInsertExpressionVisitor extends ExpressionVisitorAdapter {

    /**
     * 结果
     */
    private Value result;


    public Value getResult() {
        return result;
    }

    public void setResult(Value result) {
        this.result = result;
    }


    @Override
    public void visit(NullValue value) {
        result = null;
    }


    @Override
    public void visit(SignedExpression expr) {
        expr.getExpression().accept(this);
    }



    @Override
    public void visit(DoubleValue value) {
        result =new NumberValue(value.getValue());
    }

    @Override
    public void visit(LongValue value) {
        result =new NumberValue(value.getValue());
    }

    @Override
    public void visit(DateValue value) {
        result =new nl.knaw.dans.common.dbflib.DateValue(value.getValue());
    }

    @Override
    public void visit(TimeValue value) {
        result = new nl.knaw.dans.common.dbflib.DateValue(value.getValue());
    }

    @Override
    public void visit(TimestampValue value) {
        result = new nl.knaw.dans.common.dbflib.DateValue(value.getValue());
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        parenthesis.getExpression().accept(this);
    }

    @Override
    public void visit(StringValue value) {
        result = new nl.knaw.dans.common.dbflib.StringValue(value.getValue());
    }
}
