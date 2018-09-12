package com.framework.yjk.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yujiakui
 * @version 1.0
 * Email: jkyu@haiyi-info.com
 * date: 2018/9/11 16:10
 * description：
 **/
public class StringConverter {


    /*
     * Key used when passing around a converter as part of a HashMap for a database row.
     * Use upper-case, as class ColumnName converts all keys to upper-case too.
     */
    public static final String COLUMN_NAME = "@STRINGCONVERTER";

    private String dateFormat;
    private SimpleDateFormat simpleTimeFormat;
    private String timeFormat;
    private GregorianCalendar calendar;
    private Pattern timestampPattern;
    private SimpleDateFormat timestampFormat;
    private SimpleDateFormat simpleDateFormat;

    public StringConverter(String dateformat, String timeformat, String timestampformat,
                           String timeZoneName) {
        init(dateformat, timeformat, timestampformat, timeZoneName, null);
    }

    public StringConverter(String dateformat, String timeformat, String timestampformat,
                           String timeZoneName, Locale locale) {
        init(dateformat, timeformat, timestampformat, timeZoneName, locale);
    }

    private void init(String dateformat, String timeformat, String timestampformat,
                      String timeZoneName, Locale locale) {
        dateFormat = dateformat;
        if (dateformat != null) {
            /*
             * Can date be parsed with a simple regular expression, or is the full
			 * SimpleDateFormat parsing required?
			 */
            // TODO prefer to use SimpleDateFormat for everything but existing regex not 100% compatible
            String upper = dateformat.toUpperCase();
            boolean useSimpleDateFormat = false;
            if (upper.contains("MMM")) {
				/*
				 * Dates contain named months -- we need to use a SimpleDateFormat to parse them.
				 */
                useSimpleDateFormat = true;
            } else {
                for (int i = 0; i < upper.length(); i++) {
                    char c = upper.charAt(i);
                    if (Character.isLetter(c) && c != 'D' && c != 'M' && c != 'Y') {
						/*
						 * Dates are not just a straightforward format with days, months,
						 * years -- we need to use a SimpleDateFormat to parse them.
						 */
                        useSimpleDateFormat = true;
                    }
                }
            }
            if (useSimpleDateFormat) {
				/*
				 * Use Java API for parsing dates.
				 */
                if (locale != null) {
                    DateFormatSymbols symbols = DateFormatSymbols.getInstance(locale);
                    simpleDateFormat = new SimpleDateFormat(dateformat, symbols);
                } else {
                    simpleDateFormat = new SimpleDateFormat(dateformat);
                }
            }
        }

		/*
		 * Use Java API for parsing times.
		 */
        timeFormat = timeformat;
        if (locale != null) {
            DateFormatSymbols symbols = DateFormatSymbols.getInstance(locale);
            simpleTimeFormat = new SimpleDateFormat(timeformat, symbols);
        } else {
            simpleTimeFormat = new SimpleDateFormat(timeformat);
        }

        TimeZone timeZone = TimeZone.getTimeZone(timeZoneName);
        calendar = new GregorianCalendar();
        calendar.clear();
        calendar.setTimeZone(timeZone);
        if (timestampformat != null && timestampformat.length() > 0) {
			/*
			 * Use Java API for parsing dates and times.
			 */
            if (locale != null) {
                DateFormatSymbols symbols = DateFormatSymbols.getInstance(locale);
                timestampFormat = new SimpleDateFormat(timestampformat, symbols);
            } else {
                timestampFormat = new SimpleDateFormat(timestampformat);
            }
            timestampFormat.setTimeZone(timeZone);
        } else {
			/*
			 * Parse timestamps using a fixed regular expression.
			 */
            timestampPattern = Pattern
                    .compile("([0-9][0-9][0-9][0-9])-([0-9]?[0-9])-([0-9]?[0-9])[ T]([0-9]?[0-9]):([0-9]?[0-9]):([0-9]?[0-9]).*");
        }
    }

    public String parseString(String str) {
        return str;
    }

    public Boolean parseBoolean(String str) {
        boolean retval;
        if (str != null && str.equals("1")) {
            retval = true;
        } else if (str != null && str.equals("0")) {
            retval = false;
        } else {
            retval = Boolean.valueOf(str);
        }
        return retval;
    }

    public Byte parseByte(String str) {
        try {
            Byte b;
            if (str == null || str.length() == 0) {
                b = null;
            } else {
                b = Byte.valueOf(Byte.parseByte(str));
            }
            return b;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Short parseShort(String str) {
        try {
            Short s;
            if (str == null || str.length() == 0) {
                s = null;
            } else {
                s = Short.valueOf(Short.parseShort(str));
            }
            return s;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Integer parseInt(String str) {
        try {
            Integer i;
            if (str == null || str.length() == 0) {
                i = null;
            } else {
                i = Integer.valueOf(Integer.parseInt(str));
            }
            return i;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Long parseLong(String str) {
        try {
            Long l;
            if (str == null || str.length() == 0) {
                l = null;
            } else {
                l = Long.valueOf(Long.parseLong(str));
            }
            return l;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Float parseFloat(String str) {
        try {
            Float f;
            if (str == null || str.length() == 0) {
                f = null;
            } else {
                str = str.replace(",", ".");
                f = Float.valueOf(Float.parseFloat(str));
            }
            return f;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Double parseDouble(String str) {
        try {
            Double d;
            if (str == null || str.length() == 0) {
                d = null;
            } else {
                str = str.replace(",", ".");
                d = Double.valueOf(Double.parseDouble(str));
            }
            return d;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public byte[] parseBytes(String str) {
        try {
            byte[] b;
            if (str == null) {
                b = null;
            } else {
                b = str.getBytes();
            }
            return b;
        } catch (RuntimeException e) {
            return null;
        }
    }

    public BigDecimal parseBigDecimal(String str) {
        try {
            BigDecimal bd;
            if (str == null || str.length() == 0) {
                bd = null;
            } else {
                bd = new BigDecimal(str);
            }
            return bd;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * transforms the date string into its equivalent ISO8601
     *
     * @param date
     * @param format
     * @return
     */
    private String makeISODate(String date, String format) {
        // first memorize the original order of the groups.
        format = format.toLowerCase();
        int dpos = format.indexOf('d');
        int mpos = format.indexOf('m');
        int ypos = format.indexOf('y');

        int day = 1, month = 1, year = 1;
        if (dpos > mpos) {
            day += 1;
        } else {
            month += 1;
        }
        if (dpos > ypos) {
            day += 1;
        } else {
            year += 1;
        }
        if (mpos > ypos) {
            month += 1;
        } else {
            year += 1;
        }

        // then build the regular expression
        Pattern part;
        Matcher m;

        part = Pattern.compile("d+");
        m = part.matcher(format);
        if (m.find()) {
            format = format.replace(m.group(), "([0-9]{" + (m.end() - m.start()) + ",2})");
        }

        part = Pattern.compile("m+");
        m = part.matcher(format);
        if (m.find()) {
            format = format.replace(m.group(), "([0-9]{" + (m.end() - m.start()) + ",2})");
        }

        part = Pattern.compile("y+");
        m = part.matcher(format);
        if (m.find()) {
            format = format.replace(m.group(), "([0-9]{" + (m.end() - m.start()) + ",4})");
        }

        format = format + ".*";

        Pattern pattern = Pattern.compile(format);
        m = pattern.matcher(date);
        if (m.matches()) {
            // and return the groups in ISO8601 format.
            String yearGroup = m.group(year);
            String monthGroup = m.group(month);
            if (monthGroup.length() < 2) {
                monthGroup = "0" + monthGroup;
            }
            String dayGroup = m.group(day);
            if (dayGroup.length() < 2) {
                dayGroup = "0" + dayGroup;
            }
            String retval = yearGroup + "-" + monthGroup + "-" + dayGroup;
            return retval;
        } else {
            return null;
        }
    }

    public Date parseDate(String str) {
        try {
            Date sqlResult = null;
            if (str != null && str.length() > 0) {
                if (simpleDateFormat != null) {
                    java.util.Date parsedDate = simpleDateFormat.parse(str);
                    long millis = parsedDate.getTime();
                    sqlResult = new Date(millis);
                    return sqlResult;
                }
                String isoDate = makeISODate(str, dateFormat);
                if (isoDate != null) {
                    sqlResult = Date.valueOf(isoDate);
                }
            }
            return sqlResult;
        } catch (ParseException e) {
            return null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    public Time parseTime(String str) {
        try {
            Time sqlResult = null;
            if (str != null && str.length() > 0) {
                str = str.trim();
                while (str.length() < timeFormat.length()) {
                    str = "0" + str;
                }
                java.util.Date parsedDate = simpleTimeFormat.parse(str);
                long millis = parsedDate.getTime();
                sqlResult = new Time(millis);
            }
            return sqlResult;
        } catch (ParseException e) {
            return null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    public Timestamp parseTimestamp(String str) {
        Timestamp result = null;
        try {
            if (str != null && str.length() > 0) {
                if (timestampFormat != null) {
                    java.util.Date date = timestampFormat.parse(str);
                    result = new Timestamp(date.getTime());
                } else {
                    Matcher matcher = timestampPattern.matcher(str);
                    if (matcher.matches()) {
                        int year = Integer.parseInt(matcher.group(1));
                        int month = Integer.parseInt(matcher.group(2)) - 1;
                        int date = Integer.parseInt(matcher.group(3));
                        int hours = Integer.parseInt(matcher.group(4));
                        int minutes = Integer.parseInt(matcher.group(5));
                        int seconds = Integer.parseInt(matcher.group(6));
                        calendar.set(year, month, date, hours, minutes, seconds);
                        result = new Timestamp(calendar.getTimeInMillis());
                    }
                }
            }
        } catch (RuntimeException e) {
        } catch (ParseException e) {
        }
        return result;
    }

    public InputStream parseAsciiStream(String str) {
        return (str == null) ? null : new ByteArrayInputStream(str.getBytes());
    }

    static private Map<String, Class<?>> forSQLNameMap = new HashMap<String, Class<?>>() {
        private static final long serialVersionUID = -3037117163532338893L;

        {
			/*
			 * Lookup from SQL type names to java class.
			 */
            put("string", String.class);
            put("boolean", Boolean.class);
            put("byte", Byte.class);
            put("short", Short.class);
            put("int", Integer.class);
            put("integer", Integer.class);
            put("long", Long.class);
            put("float", Float.class);
            put("double", Double.class);
            put("bigdecimal", BigDecimal.class);
            put("date", Date.class);
            put("time", Time.class);
            put("timestamp", Timestamp.class);
            put("asciistream", InputStream.class);
        }
    };

    public Object convert(String sqlTypeName, String stringRepresentation) {
		/*
		 * No need to do a conversion if desired type is also a string.
		 */
        if (sqlTypeName == null) {
            return stringRepresentation;
        } else if (sqlTypeName.equalsIgnoreCase("string")) {
            return stringRepresentation;
        } else if (sqlTypeName.equalsIgnoreCase("boolean")) {
            return parseBoolean(stringRepresentation);
        } else if (sqlTypeName.equalsIgnoreCase("byte")) {
            return parseByte(stringRepresentation);
        } else if (sqlTypeName.equalsIgnoreCase("short")) {
            return parseShort(stringRepresentation);
        } else if (sqlTypeName.equalsIgnoreCase("int") || sqlTypeName.equalsIgnoreCase("integer")) {
            return parseInt(stringRepresentation);
        } else if (sqlTypeName.equalsIgnoreCase("long")) {
            return parseLong(stringRepresentation);
        } else if (sqlTypeName.equalsIgnoreCase("float")) {
            return parseFloat(stringRepresentation);
        } else if (sqlTypeName.equalsIgnoreCase("double")) {
            return parseDouble(stringRepresentation);
        } else if (sqlTypeName.equalsIgnoreCase("bigdecimal")) {
            return parseBigDecimal(stringRepresentation);
        } else if (sqlTypeName.equalsIgnoreCase("date")) {
            return parseDate(stringRepresentation);
        } else if (sqlTypeName.equalsIgnoreCase("time")) {
            return parseTime(stringRepresentation);
        } else if (sqlTypeName.equalsIgnoreCase("timestamp")) {
            return parseTimestamp(stringRepresentation);
        } else if (sqlTypeName.equalsIgnoreCase("asciistream")) {
            return parseAsciiStream(stringRepresentation);
        } else {
            return stringRepresentation;
        }
    }

    public Class<?> forSQLName(String sqlTypeName) {
        sqlTypeName = sqlTypeName.toLowerCase();
        return forSQLNameMap.get(sqlTypeName);
    }

    /**
     * Get a value that has the type of an SQL data type.
     *
     * @param sqlTypeName name of SQL data type.
     * @return a constant value with this data type.
     */
    public static Object getLiteralForTypeName(String sqlTypeName) {
        Object retval = null;
        sqlTypeName = sqlTypeName.toLowerCase();
        if (sqlTypeName.equals("string")) {
            retval = "";
        } else if (sqlTypeName.equals("boolean")) {
            retval = Boolean.FALSE;
        } else if (sqlTypeName.equals("byte")) {
            retval = Byte.valueOf((byte) 1);
        } else if (sqlTypeName.equals("short")) {
            retval = Short.valueOf((short) 1);
        } else if (sqlTypeName.equals("int") || sqlTypeName.equals("integer")) {
            retval = Integer.valueOf(1);
        } else if (sqlTypeName.equals("long")) {
            retval = Long.valueOf(1);
        } else if (sqlTypeName.equals("float")) {
            retval = Float.valueOf(1);
        } else if (sqlTypeName.equals("double")) {
            retval = Double.valueOf(1);
        } else if (sqlTypeName.equals("bigdecimal")) {
            retval = BigDecimal.valueOf(1);
        } else if (sqlTypeName.equals("date")) {
            retval = Date.valueOf("1970-01-01");
        } else if (sqlTypeName.equals("time")) {
            retval = Time.valueOf("00:00:00");
        } else if (sqlTypeName.equals("timestamp")) {
            retval = Timestamp.valueOf("1970-01-01 00:00:00");
        } else if (sqlTypeName.equals("asciistream")) {
            retval = new ByteArrayInputStream(new byte[]{});
        }
        return retval;
    }

    /**
     * Get SQL data type of an object.
     *
     * @param literal object to get SQL data type for.
     * @return SQL data type name.
     */
    public static String getTypeNameForLiteral(Object literal) {
        String retval = null;
        if (literal instanceof String) {
            retval = "String";
        } else if (literal instanceof Boolean) {
            retval = "Boolean";
        } else if (literal instanceof Byte) {
            retval = "Byte";
        } else if (literal instanceof Short) {
            retval = "Short";
        } else if (literal instanceof Integer) {
            retval = "Int";
        } else if (literal instanceof Long) {
            retval = "Long";
        } else if (literal instanceof Float) {
            retval = "Float";
        } else if (literal instanceof Double) {
            retval = "Double";
        } else if (literal instanceof BigDecimal) {
            retval = "BigDecimal";
        } else if (literal instanceof Date) {
            retval = "Date";
        } else if (literal instanceof Time) {
            retval = "Time";
        } else if (literal instanceof Timestamp) {
            retval = "Timestamp";
        } else if (literal instanceof InputStream) {
            retval = "AsciiStream";
        }
        return retval;
    }

    public static List<Object[]> getTypeInfo() {
        Integer intZero = Integer.valueOf(0);
        Short shortZero = Short.valueOf((short) 0);
        Short shortMax = Short.valueOf(Short.MAX_VALUE);
        Short searchable = Short
                .valueOf((short) DatabaseMetaData.typeSearchable);
        Short nullable = Short.valueOf((short) DatabaseMetaData.typeNullable);

        ArrayList<Object[]> retval = new ArrayList<Object[]>();

        retval.add(new Object[]
                {"String", Integer.valueOf(Types.VARCHAR), shortMax, "'", "'", null,
                        nullable, Boolean.TRUE, searchable, Boolean.FALSE,
                        Boolean.FALSE, Boolean.FALSE, null, shortZero, shortMax,
                        intZero, intZero, intZero});

        retval.add(new Object[]
                {"Boolean", Integer.valueOf(Types.BOOLEAN), shortMax, null, null,
                        null, nullable, Boolean.TRUE, searchable, Boolean.FALSE,
                        Boolean.FALSE, Boolean.FALSE, null, shortZero, shortMax,
                        intZero, intZero, intZero});

        retval.add(new Object[]
                {"Byte", Integer.valueOf(Types.TINYINT), shortMax, null, null, null,
                        nullable, Boolean.TRUE, searchable, Boolean.FALSE,
                        Boolean.FALSE, Boolean.FALSE, null, shortZero, shortMax,
                        intZero, intZero, intZero});

        retval.add(new Object[]
                {"Short", Integer.valueOf(Types.SMALLINT), shortMax, null, null, null,
                        nullable, Boolean.TRUE, searchable, Boolean.FALSE,
                        Boolean.FALSE, Boolean.FALSE, null, shortZero, shortMax,
                        intZero, intZero, intZero});

        retval.add(new Object[]
                {"Integer", Integer.valueOf(Types.INTEGER), shortMax, null, null,
                        null, nullable, Boolean.TRUE, searchable, Boolean.FALSE,
                        Boolean.FALSE, Boolean.FALSE, null, shortZero, shortMax,
                        intZero, intZero, intZero});

        retval.add(new Object[]
                {"Long", Integer.valueOf(Types.BIGINT), shortMax, null, null, null,
                        nullable, Boolean.TRUE, searchable, Boolean.FALSE,
                        Boolean.FALSE, Boolean.FALSE, null, shortZero, shortMax,
                        intZero, intZero, intZero});

        retval.add(new Object[]
                {"Float", Integer.valueOf(Types.FLOAT), shortMax, null, null, null,
                        nullable, Boolean.TRUE, searchable, Boolean.FALSE,
                        Boolean.FALSE, Boolean.FALSE, null, shortZero, shortMax,
                        intZero, intZero, intZero});

        retval.add(new Object[]
                {"Double", Integer.valueOf(Types.DOUBLE), shortMax, null, null, null,
                        nullable, Boolean.TRUE, searchable, Boolean.FALSE,
                        Boolean.FALSE, Boolean.FALSE, null, shortZero, shortMax,
                        intZero, intZero, intZero});

        retval.add(new Object[]
                {"BigDecimal", Integer.valueOf(Types.DECIMAL), shortMax, null, null,
                        null, nullable, Boolean.TRUE, searchable, Boolean.FALSE,
                        Boolean.FALSE, Boolean.FALSE, null, shortZero, shortMax,
                        intZero, intZero, intZero});

        retval.add(new Object[]
                {"Date", Integer.valueOf(Types.DATE), shortMax, "'", "'", null,
                        nullable, Boolean.TRUE, searchable, Boolean.FALSE,
                        Boolean.FALSE, Boolean.FALSE, null, shortZero, shortMax,
                        intZero, intZero, intZero});

        retval.add(new Object[]
                {"Time", Integer.valueOf(Types.TIME), shortMax, "'", "'", null,
                        nullable, Boolean.TRUE, searchable, Boolean.FALSE,
                        Boolean.FALSE, Boolean.FALSE, null, shortZero, shortMax,
                        intZero, intZero, intZero});

        retval.add(new Object[]
                {"Timestamp", Integer.valueOf(Types.TIMESTAMP), shortMax, null, null,
                        null, nullable, Boolean.TRUE, searchable, Boolean.FALSE,
                        Boolean.FALSE, Boolean.FALSE, null, shortZero, shortMax,
                        intZero, intZero, intZero});

        retval.add(new Object[]
                {"Asciistream", Integer.valueOf(Types.CLOB), shortMax, null, null,
                        null, nullable, Boolean.TRUE, searchable, Boolean.FALSE,
                        Boolean.FALSE, Boolean.FALSE, null, shortZero, shortMax,
                        intZero, intZero, intZero});

        return retval;
    }

    public static String removeQuotes(String string) {
        return string.replace("\"", "");
    }
}
