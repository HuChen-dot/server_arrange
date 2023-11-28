package com.server.util;


import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @Author: hu.chen
 * @DateTime: 2022/7/26 12:08 PM
 * 日期操作
 */
@Slf4j
public class DateUtil {
    private static final long DAYTIME_MILLISECONDS = 86400000L;
    public static final String SDF_YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    public static final String SDF_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String SDF_YYYY_MM_DD = "yyyy-MM-dd";
    public static final String SDF_HH_MM = "HH:mm";
    public static final String SDF_YYYY = "yyyy";
    public static final String SDF_YYYY_DOT_M_DOT_D = "yyyy.M.d";
    public static final String SDF_YYYY_DOT_MM = "yyyy.MM";
    public static final String SDF_YYYY_MM_DD_AT_HH_MM_SS = "yyyy-MM-dd@HH:mm:ss";

    /**
     * 字符串转换成日期
     * @param date 字符串
     * @param pattern 
     * @return
     * @throws ParseException
     */
    public static Date string2Date(String date, String pattern) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Date d = sdf.parse(date);
        return d;
    }

    /**
     * 字符串转换成日期
     * @param date 以yyyy-MM-dd HH:mm:ss的pattern进行转换
     * @return
     * @throws ParseException
     */
    public static Date string2Date(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat(SDF_YYYY_MM_DD_HH_MM_SS);
        try {
            return sdf.parse(date);
        } catch (ParseException e) {
            log.error("Error parsing date",e);
        }
        return null;
    }

    /**
     * 将日期转换为字符串
     * @param date
     * @param pattern 转化的格式，如yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String date2String(Date date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        String s = sdf.format(date);
        return s;
    }

    /**
     * 将日期转换为字符串
     * @param date 默认转化为yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String date2String(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(SDF_YYYY_MM_DD_HH_MM_SS);
        return sdf.format(date);
    }

    /**
     * 将传入的日期向前（或向后）滚动|amount|年
     * @param date
     * @param amount
     * @return
     */
    public static Date rollByYear(Date date, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, amount);
        return calendar.getTime();
    }

    /**
     * 将传入的日期向前（或向后）滚动|amount|月
     * @param date
     * @param amount
     * @return
     */
    public static Date rollByMonth(Date date, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, amount);
        return calendar.getTime();
    }

    /**
     * 得到几天前/后的时间
     * @param date
     * @param amount
     * @return
     * @Description:
     */
    public static Date rollByDays(Date date, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, amount);
        return calendar.getTime();
    }

    /**
     * 得到几小时前/后的时间
     * @param date
     * @param amount
     * @return
     * @Description:
     */
    public static Date rollByHour(Date date, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, amount);
        return calendar.getTime();
    }

    /**
     * 获取 amount 分钟的前后时间
     * @param date
     * @param amount
     * @return
     */
    public static Date rollByMinutes(Date date, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, amount);
        return calendar.getTime();
    }
    
    /**
	 * 获取 amount 秒钟的前后时间
	 * 
	 * @param date
	 * @param amount
	 * @return
	 */
	public static Date rollBySeconds(Date date, int amount) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.SECOND, amount);
		return calendar.getTime();
	}


    /**
     * 判断 date 时间是否在 date1之前
     * @param date
     * @param date1
     * @return
     */
    public static boolean isBefore(Date date, Date date1) {
        return date.before(date1);
    }
    
    /**
	 * 获取当天零点时间
	 * 
	 * @return
	 */
	public static Date getCurrentZeroTime() {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

    /**
     * 得到日期字符串
     * @param date
     * @return
     * @Description:
     */
    public static String getDateFromTime(Date date) {
        SimpleDateFormat sdfDate = new SimpleDateFormat(SDF_YYYY_MM_DD);
        return sdfDate.format(date);
    }

    /**
     * 格式化日期 
     * 当天的时间 ： HH:mm发布
     * 3天内的时间 ： n天前发布
     * 大于三天 ： yyyy-MM-dd
     * @param date 日期对象
     * @param format 三天后的日期格式， 为null 默认yyyy-MM-dd
     * @return
     * @throws ParseException
     * @Description:
     */
    public static String formatTime(Date date, String format) throws ParseException {
        if (date == null) {
            return "";
        }
        format = StringUtils.isEmpty(format) ? "yyyy-MM-dd" : format;
        int result = differDate(date, new Date());
        result = result - 1;
        if (result < 1) {
            return getDate(date, "HH:mm发布");
        }
        if (result >= 1 && result < 2) {
            return getDate(date, "1天前发布");
        }
        else if (result >= 2 && result < 3) {
            return getDate(date, "2天前发布");
        }
        else if (result >= 3 && result < 4) {
            return getDate(date, "3天前发布");
        }
        else {
            return getDate(date, "yyyy-MM-dd");
        }
    }

    /**
     *  格式化时间 列表页显示
     * @param stime
     * @param format
     * @return
     * @throws ParseException
     */
    public static String formatTime(String stime, String format) throws ParseException {
        if (stime == null) {
            return "";
        }
        Date ctime = string2Date(stime);
        return formatTime(ctime, format);
    }
    
    /**
     * 计算两个日期的相差天数
     * 注：只计算日期的差值，不精确到小时；结果可以有负值
     * @param fromDate
     * @param endDate
     * @return
     * @throws ParseException
     */
    public static int differDate(Date fromDate, Date endDate) throws ParseException {
        return differDate(date2String(fromDate, SDF_YYYY_MM_DD),
            date2String(endDate, SDF_YYYY_MM_DD));
    }

    /**
     * 计算两个日期的相差天数 （2014-10-01 到 2014-10-07  为 7天）
     * 注：只计算日期的差值，不精确到小时；结果可以有负值
     * @param fromDate 形如yyyy-MM-dd
     * @param endDate 形如yyyy-MM-dd
     * @return
     * @throws ParseException
     */
    public static int differDate(String fromDate, String endDate) throws ParseException {
        Date fDate = string2Date(fromDate, SDF_YYYY_MM_DD);
        Date eDate = string2Date(endDate, SDF_YYYY_MM_DD);
        int cha = calculateDiffDay(fDate, eDate);
        return cha >= 0 ? (cha + 1) : (cha - 1);
    }
    /**
     * 计算与当前时间相差天数
     */
    public static int calcDays(String date, String format) {
        if (date == null) {
            return 5;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        //开始结束相差天数
        try {
            return new Long((System.currentTimeMillis() - formatter.parse(date).getTime())
                    / (1000 * 24 * 60 * 60)).intValue();
        } catch (ParseException e) {
            e.printStackTrace();
            return 5;
        }
    }


    /**
     * 获取两个时间相差多少秒
     * @param begin
     * @param end
     * @return
     */
    public static long getSecond(Date begin,Date end) {
        return (end.getTime() - begin.getTime()) / 1000;
    }

    /**
     * 获取两个时间相差多少毫秒
     * @param begin
     * @param end
     * @return
     */
    public static long getMillisecond(Date begin,Date end) {
        return (end.getTime() - begin.getTime());
    }
    
    private static int calculateDiffDay(Date fDate, Date eDate){
        long cha = eDate.getTime() - fDate.getTime();
        return (int) (cha / DAYTIME_MILLISECONDS);
    	/*Calendar aCalendar = Calendar.getInstance();
		aCalendar.setTime(fDate);
		int day1 = aCalendar.get(Calendar.DAY_OF_YEAR);
		aCalendar.setTime(eDate);
		int day2 = aCalendar.get(Calendar.DAY_OF_YEAR);
		int day = day2 - day1;
		return day < 0 ? 0 - day : day + 1;*/
    }
    

    public static String getDate(Date date, String formatStr) {
        SimpleDateFormat format = new SimpleDateFormat(formatStr);
        return date != null ? format.format(date) : "";
    }

    /**
    * 将日期格式的字符串转换为长整型
    * 
    * @param date
    * @param format
    * @return
    */
    public static long convert2long(String date, String format) {
        try {
            if (date != null || !"".equals(date)) {
                if (format == null || "".equals(format)) {
                    format = "yyyy-MM-dd HH:mm:ss";
                }
                SimpleDateFormat sf = new SimpleDateFormat(format);
                return sf.parse(date).getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0L;
    }



    /**
     * 格式化页面时间显示
     * @param deliverTime
     * @param fmtStr
     * @return
     */
    public static String formatShowTime(String deliverTime, String fmtStr) {
        SimpleDateFormat sFmt = new SimpleDateFormat(SDF_YYYY_MM_DD_HH_MM_SS);
        Date date = null;
		try {
			date = sFmt.parse(deliverTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
        return formatShowTime(date, fmtStr);
            
    }

    /**
    * 格式化页面时间显示
    * @param deliverTime
    * @param fmtStr
    * @return
    */
    public static String formatShowTime(Date deliverTime, String fmtStr) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat(fmtStr);
            return fmt.format(deliverTime);
        } catch (Exception e) {
            return "";
        }

    }

    /**
     * 得到今夕是何年 
     * @return 形如 yyyy
     */
    public static String getYear() {
        return date2String(new Date(), SDF_YYYY);
    }

    /**
     * 得到今夕是何年 
     * @return 形如 yyyy
     */
    public static int getYear(Date date) {
        return Integer.parseInt(date2String(date, SDF_YYYY));
    }

    
}
