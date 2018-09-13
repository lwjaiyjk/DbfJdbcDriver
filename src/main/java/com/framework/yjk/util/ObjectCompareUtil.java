package com.framework.yjk.util;

import org.apache.commons.collections.comparators.ComparableComparator;

import java.util.Comparator;

/**
 * @author yujiakui
 * @version 1.0
 * Email: jkyu@haiyi-info.com
 * date: 2018/9/13 16:34
 * description：对象比较工具
 **/
public class ObjectCompareUtil {

    /**
     * 左右值进行比较
     * <p>
     * 返回0表示相等，1表示大于，-1表示小于
     *
     * @param leftValue
     * @param rightValue
     * @return
     */
    public static int compare(Object leftValue, Object rightValue) {

        if (null == leftValue && rightValue == null) {
            return 0;
        } else if (null != leftValue && null == rightValue) {
            return 1;
        } else if (null == leftValue && null != rightValue) {
            return -1;
        } else {
            // 左右值都不是空
            Comparator mycmp = ComparableComparator.getInstance();
            if (leftValue instanceof Integer){
                long tempLeftValue = (int)leftValue;
                leftValue = tempLeftValue;
            }
            if(rightValue instanceof Integer){
                long tempRightValue = (int)rightValue;
                rightValue = tempRightValue;
            }
            int compareValue = mycmp.compare(leftValue, rightValue);
            if (compareValue > 0) {
                return 1;
            } else if (compareValue == 0) {
                return 0;
            } else {
                return -1;
            }
        }
    }
}
