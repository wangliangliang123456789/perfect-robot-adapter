package com.robot.adapter.util;

import com.alibaba.fastjson.JSON;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 时间操作工具类
 * @date
 * @author 亮亮
 */
@SuppressWarnings("all")
public class DateUtil {
    /**
     * 时间段装换为 开始时间和结束时间
     * 时间段格式为  yyyyMMdd HH:mm:ss
     * 输出时间格式  outFormat
     * @param duration
     * @param outFormat 输出的时间格式
     * @return
     */
    public static List<String> durationToStartAndEnd(String duration,String outFormat){
        try {
            String[] split = duration.split("~");
            SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd HH:mm:ss");
            Date start = sdf.parse(split[0]);
            Date end = sdf.parse(split[1]);
            SimpleDateFormat sd=new SimpleDateFormat(outFormat);
            String startDate = sd.format(start);
            String endDate = sd.format(end);
            List<String> dateList=new ArrayList<>();
            dateList.add(startDate);
            dateList.add(endDate);
            return dateList;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

    }


    /**
     * 格式  yyyyMM
     * 获取两个月份之间的所有月份
     *
     * @param minDate 开始月份
     * @param maxDate 结束月份
     * @return
     * @throws ParseException
     */
    public static List<String> getMonthBetween(String minDate, String maxDate)  {
        ArrayList<String> result = null;
        try {
            result = new ArrayList<String>();
            //格式化为年月
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
            Calendar min = Calendar.getInstance();
            Calendar max = Calendar.getInstance();
            min.setTime(sdf.parse(minDate));
            min.set(min.get(Calendar.YEAR), min.get(Calendar.MONTH), 1);
            max.setTime(sdf.parse(maxDate));
            max.set(max.get(Calendar.YEAR), max.get(Calendar.MONTH), 2);
            Calendar curr = min;
            while (curr.before(max)) {
                result.add(sdf.format(curr.getTime()));
                curr.add(Calendar.MONTH, 1);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取 多少天  以前或 以后的 日期
     * @param data  日期(yyyy-MM-dd)
     * @param days  天数，正数表示获得传入日期以前多少天的日期，负数表示
     *              获取传入日期多少天以后的日期
     * @return
     */
    public static LocalDate getNextDay(String data,Integer days){
        LocalDate localDate1 = LocalDate.parse(data).minusDays(days);
        return localDate1;
    }

    /**
     * 字符串转时间
     * @param datetime  时间字符
     * @param formatter 格式
     * @return
     */
    public static LocalDateTime dataParse(String datetime,String formatter){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(formatter);
        LocalDateTime ldt = LocalDateTime.parse(datetime,dtf);
        return ldt;

    }

    /**
     * 时间格式转换字符串
     * @param dateTime   时间
     * @param formatter  格式
     * @return
     */
    public static String dataFormatter(LocalDateTime dateTime,String formatter){

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(formatter);
        String datetime2 = dtf.format(dateTime);
        return datetime2;
    }


    /**
     * Date 转 LocalDateTime
     * @param date 时间
     * @return
     */
    public static LocalDateTime DateToLocalDateTime(Date date) {
        Instant instant = date.toInstant();
        System.out.println(JSON.toJSONString(instant));
        ZoneId zone = ZoneId.systemDefault();
        System.out.println(JSON.toJSONString(zone));
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
        return localDateTime;
    }

    /**
     * LocalDateTime 转 Date
     * @param localDateTime
     * @return
     */
    public static Date LocalDateTimeToUdate(LocalDateTime localDateTime ) {
        localDateTime= LocalDateTime.now();
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        Date date = Date.from(instant);
        return date;
    }
    /**
     * 获取当前月份的 前后 的月份
     * @param month 以前的月份 为 负数，以后的月份 为正数
     */
    @Test
    public static String getLastMonth(int month) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, month);
        SimpleDateFormat dft = new SimpleDateFormat("yyyyMM");
        String lastMonth = dft.format(cal.getTime());
        return lastMonth;
    }


    /**
     * 获得该月最后一天
     * @param year
     * @param month
     * @return
     */
    public static String getLastDayOfMonth(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        //获取某月最大天数
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        //设置日历中月份的最大天数
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        //格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String lastDayOfMonth = sdf.format(cal.getTime())+" 23:59:59";

        return lastDayOfMonth;
    }


}
