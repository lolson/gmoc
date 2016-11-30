package gov.nasa.gsfc.gmsec.gmoc.model;

import gov.nasa.gsfc.gmsec.gmoc.Options;
import gov.nasa.gsfc.gmsec.gmoc.service.PassScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by leif on 10/12/16.
 * Countdown clock is configured with a WIRE pass schedule composed
 * of a 3 minute pass, repeated every 5 minutes, between the hours
 * of 8AM and 6PM
 *
 *
 * "WIRE", "2016-10-07-08:00:00", "+00:03:00", "1", "1"
 * "WIRE", "2016-10-07-08:05:00", "+00:03:00", "2", "2"
 * "WIRE", "2016-10-07-08:10:00", "+00:03:00", "3", "3"
 * ...
 * ...
 * "WIRE", "2016-10-07-17:55:00", "+00:03:00", "120", "120"
 * "WIRE", "2016-10-07-18:00:00", "+00:03:00", "121", "121"
 *
 * "WIRE", "2016-10-07-08:00:00", "+00:03:00", "1", "1"
 * "WIRE", "2016-10-07-08:05:00", "+00:03:00", "2", "2"
 * "WIRE", "2016-10-07-08:10:00", "+00:03:00", "3", "3"
 * ...
 * ...
 * "WIRE", "2016-10-07-17:55:00", "+00:03:00", "120", "120"
 * "WIRE", "2016-10-07-18:00:00", "+00:03:00", "121", "121"
 */
public class PassSchedule
{
    private static final Logger log = LoggerFactory.getLogger(PassSchedule.class);

    // TODO: add default pass time stamp increment of 5 minutes
    // Constructor should initialize to 8AM of current day
    // should have method getNextRow that increments values

    Options options;
    public static final String DEFAULT_DURATION = "00:30:00";
    public static final int DEFAULT_PASS_INTERVAL = 5;

    private DateFormat dateFormat;

    public PassSchedule(Options options) {
        this.options = options;
        dateFormat = new SimpleDateFormat(options.getDateFormatString());
        category = options.getCategory();
        passScheduleInterval = options.getPassInterval();
        passDuration = options.getDuration();

        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        int gmtOffset = tz.getOffset(
                cal.get(Calendar.ERA),
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.DAY_OF_WEEK),
                cal.get(Calendar.MILLISECOND)
                );
        // convert to hours
        gmtOffset = gmtOffset / (60*60*1000);
        log.info("Current Offset from GMT (in hrs): " + gmtOffset); // eg -5
        cal.set(Calendar.AM_PM, Calendar.AM);
        int startHour = 8 - gmtOffset;
        cal.set(Calendar.HOUR, startHour);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        setPassStartTime(cal);

        setCounterOne(1);
        setCounterTwo(1);
    }

    private String category;
    private int passScheduleInterval;
    private String passDuration;
    private Calendar passStartTime;
    private int counterOne;
    private int counterTwo;

    public void incrementPass() {
        passStartTime.add(Calendar.MINUTE, passScheduleInterval);
    }

    public String getPassDuration() {
        String pattern = "^\\+[\\d]{2}:[\\d]{2}:[\\d]{2}";
        // check starts with "+"
        if(!passDuration.startsWith("+"))
        {
            this.passDuration = "+" + passDuration;
        }
        // Validate date pattern, for instance +00:03:00
        if(!passDuration.matches(pattern))
        {
            log.error("Configured pass duration " + passDuration + " does not have correct hh:mm:ss time format.");
            log.error("Defaulting to pass duration "+DEFAULT_DURATION);
            this.passDuration = DEFAULT_DURATION;
        }
        return passDuration;
    }

    // "WIRE", "2016-10-07-08:00:00", "+00:03:00", "1", "1"
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(category); sb.append(",");
        sb.append(dateFormat.format(passStartTime.getTime())); sb.append(",");
        sb.append(getPassDuration()); sb.append(",");
        sb.append(options.getStartLabel()); sb.append(" "); sb.append(options.getCategory()); sb.append(",");
        sb.append(options.getEndLabel()); sb.append(" "); sb.append(options.getCategory());
        // Test counter mode?
//        sb.append(counterOne); sb.append(",");
//        sb.append(counterTwo);
        return sb.toString();
    }

    public void setPassStartTime(Calendar passStartTime)
    {
        this.passStartTime = passStartTime;
    }

    public void setCounterOne(int counterOne)
    {
        this.counterOne = counterOne;
    }

    public void setCounterTwo(int counterTwo)
    {
        this.counterTwo = counterTwo;
    }

}
