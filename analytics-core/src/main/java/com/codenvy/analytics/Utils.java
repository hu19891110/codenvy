/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */


package com.codenvy.analytics;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Context.Builder;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.Parameters.TimeUnit;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class Utils {

    private Utils() {
    }

    /**
     * Parse date represented by give string using {@link com.codenvy.analytics.metrics.Parameters#PARAM_DATE_FORMAT}
     * format. Wraps {@link
     * ParseException} into {@link java.io.IOException}.
     */
    public static Calendar parseDate(String date) throws ParseException {
        DateFormat df = new SimpleDateFormat(Parameters.PARAM_DATE_FORMAT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(df.parse(date));

        return calendar;
    }

    /** Formats date using {@link Parameters#PARAM_DATE_FORMAT}. */
    public static String formatDate(Calendar date) {
        DateFormat df = new SimpleDateFormat(Parameters.PARAM_DATE_FORMAT);
        return df.format(date.getTime());
    }

    public static boolean isDateFormat(String date) {
        return date.length() == Parameters.PARAM_DATE_FORMAT.length();
    }

    /** Initialize date interval accordingly to passed {@link Parameters#TIME_UNIT} */
    public static Context initDateInterval(Calendar toDate, Parameters.TimeUnit timeUnit, Context.Builder builder)
            throws ParseException {
        return initDateInterval(toDate, timeUnit, 0, builder);
    }

    private static void initByLifeTime(Context.Builder builder) {
        builder.putDefaultValue(Parameters.FROM_DATE);
        builder.putDefaultValue(Parameters.TO_DATE);
    }

    private static void initByWeek(Calendar toDate, int shift, Context.Builder builder) {
        toDate.add(Calendar.WEEK_OF_MONTH, shift);

        Calendar fromDate = (Calendar)toDate.clone();
        fromDate.add(Calendar.DAY_OF_MONTH,
                     fromDate.getActualMinimum(Calendar.DAY_OF_WEEK) - fromDate.get(Calendar.DAY_OF_WEEK));

        toDate.add(Calendar.DAY_OF_MONTH,
                   toDate.getActualMaximum(Calendar.DAY_OF_WEEK) - toDate.get(Calendar.DAY_OF_WEEK));

        builder.put(Parameters.FROM_DATE, fromDate);
        builder.put(Parameters.TO_DATE, toDate);
    }

    private static void initByMonth(Calendar toDate, int shift, Context.Builder builder) {
        toDate.add(Calendar.MONTH, shift);

        Calendar fromDate = (Calendar)toDate.clone();
        fromDate.set(Calendar.DAY_OF_MONTH, 1);

        toDate.set(Calendar.DAY_OF_MONTH, toDate.getActualMaximum(Calendar.DAY_OF_MONTH));

        builder.put(Parameters.FROM_DATE, fromDate);
        builder.put(Parameters.TO_DATE, toDate);
    }

    private static void initByDay(Calendar toDate, int shift, Context.Builder builder) {
        toDate.add(Calendar.DATE, shift);
        Calendar fromDate = (Calendar)toDate.clone();
        builder.put(Parameters.FROM_DATE, fromDate);
        builder.put(Parameters.TO_DATE, toDate);
    }

    /**
     * Shift date interval forward depending on {@link com.codenvy.analytics.metrics.Parameters.TimeUnit}. Should also
     * be placed in context.
     */
    public static Context nextDateInterval(Context.Builder builder) throws ParseException {
        Calendar toDate = builder.getAsDate(Parameters.TO_DATE);
        Parameters.TimeUnit timeUnit = builder.getTimeUnit();

        return initDateInterval(toDate, timeUnit, 1, builder);
    }

    /**
     * Shift date interval backward depending on {@link com.codenvy.analytics.metrics.Parameters.TimeUnit}. Should also
     * be placed in context.
     */
    public static Context prevDateInterval(Context.Builder builder) throws ParseException {
        Calendar toDate = builder.getAsDate(Parameters.TO_DATE);
        Parameters.TimeUnit timeUnit = builder.getTimeUnit();

        return initDateInterval(toDate, timeUnit, -1, builder);
    }

    public static Context initializeContext(Parameters.TimeUnit timeUnit) throws ParseException {
        Calendar cal = Calendar.getInstance();

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, timeUnit.toString());

        initDateInterval(cal, builder.getTimeUnit(), builder);

        return timeUnit == Parameters.TimeUnit.DAY ? Utils.prevDateInterval(builder) : builder.build();
    }

    /**
     * Calculate FROM_DATE and TO_DATE parameters of context:
     * FROM_DATE = (current_{date/week/month}_at_from_date + timeUnit * (timeShift - 1))
     * TO_FATE =  (current_{date/week/month}_at_from_date + timeUnit * timeShift)
     *
     * @param timeShift
     *         = starting from 0 to represent current time period.
     */
    public static Context initDateInterval(Calendar toDate, TimeUnit timeUnit, int timeShift, Builder builder) {
        switch (timeUnit) {
            case DAY:
                initByDay(toDate, timeShift, builder);
                break;
            case WEEK:
                initByWeek(toDate, timeShift, builder);
                break;
            case MONTH:
                initByMonth(toDate, timeShift, builder);
                break;
            case LIFETIME:
                initByLifeTime(builder);
                break;
        }

        return builder.build();
    }

    public static Map<String, String> fetchEncodedPairs(String data) throws UnsupportedEncodingException {
        String[] splitted = data.split(",");
        Map<String, String> result = new HashMap<>(splitted.length);

        for (String entry : splitted) {
            String[] pair = entry.split("=");

            if (pair.length == 2) {
                String key = URLDecoder.decode(pair[0], "UTF-8").toLowerCase();
                String value = URLDecoder.decode(pair[1], "UTF-8");

                result.put(key, value);
            }
        }

        return result;
    }
    
    /** @return mongodb operation (arg1 - arg2Field) */
    public static BasicDBObject getSubtractOperation(long arg1, String arg2Field) {
        BasicDBList subtractArgs = new BasicDBList();
        subtractArgs.add(arg1);
        subtractArgs.add(arg2Field);

        return new BasicDBObject("$subtract", subtractArgs);
    }

    public static BasicDBObject getAndOperation(BasicDBObject... predicates) {
        BasicDBList andArgs = new BasicDBList();

        Collections.addAll(andArgs, predicates);

        return new BasicDBObject("$and", andArgs);
    }
    
    public static BasicDBObject getOrOperation(BasicDBObject... predicates) {
        BasicDBList orArgs = new BasicDBList();

        Collections.addAll(orArgs, predicates);
        
        return new BasicDBObject("$or", predicates);
    }
}