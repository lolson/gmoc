package gov.nasa.gsfc.gmsec.gmoc;

import gov.nasa.gsfc.gmsec.gmoc.model.PassSchedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by leif on 10/12/16.
 *
 * Some command-line arguments would be:
 * Category: Examples: WIRE, SDO
 * StartLabel: Example: “AOS SDO”
 * EndLabel: Example: “LOS SDO"
 * DateFormat: Julian vs month/day
 * PassDuration: expressed in hh:mm:ss
 * PassInterval: expressed as minutes
 *
 */
@Component
public class Options
{
    private static final Logger log = LoggerFactory.getLogger(Options.class);
    private static final String SCHEDULE_FILE_PATH = "schedule.file.path";
    private static final String SCHEDULE_DATE_FORMAT = "schedule.date.format";
    private static final String SCHEDULE_PASS_DURATION = "schedule.pass.duration";
    private static final String SCHEDULE_PASS_INTERVAL = "schedule.pass.interval";
    private static final String SCHEDULE_CATEGORY = "schedule.category";
    private static final String SCHEDULE_START_LABEL = "schedule.start.label";
    private static final String SCHEDULE_END_LABEL= "schedule.end.label";

    private String scheduleFilePath;
    private String duration = PassSchedule.DEFAULT_DURATION;
    private int passInterval = PassSchedule.DEFAULT_PASS_INTERVAL;
    private String category = Category.WIRE.type;
    private String startLabel = StartLabel.AOS.type;
    private String endLabel = EndLabel.LOS.type;
    private String dateFormat = DateFormat.DAY_IN_YEAR.toString();

    @Value("${schedule.file.path}")
    private String schedule_file_path;

    @Value("${schedule.date.format}")
    private String schedule_date_format;

    @Value("${schedule.pass.duration}")
    private String schedule_pass_duration;

    @Value("${schedule.pass.interval}")
    private String schedule_pass_interval;

    @Value("${schedule.category}")
    private String schedule_category;

    @Value("${schedule.start.label}")
    private String schedule_start_label;

    @Value("${schedule.end.label}")
    private String schedule_end_label;

    @Autowired
    private Environment env;

    @PostConstruct
    public void init() {
        String OS = env.getProperty("os.name");
        if(OS.startsWith("Windows")) {
            setScheduleFilePath(env.getProperty("USERPROFILE"));
        } else {
            setScheduleFilePath(env.getProperty("HOME"));
        }

        // Setup with externalized application properties
        log.info("Configured application properties: ");
        log.info(SCHEDULE_FILE_PATH + " = "+ schedule_file_path);
        log.info(SCHEDULE_DATE_FORMAT + " = "+ schedule_date_format);
        log.info(SCHEDULE_PASS_DURATION + " = "+ schedule_pass_duration);
        log.info(SCHEDULE_PASS_INTERVAL + " = "+ schedule_pass_interval);
        log.info(SCHEDULE_CATEGORY + " = "+ schedule_category);
        log.info(SCHEDULE_START_LABEL + " = "+ schedule_start_label);
        log.info(SCHEDULE_END_LABEL + " = "+ schedule_end_label);

        // Use application properties as default values
        this.setScheduleFilePath(schedule_file_path);
        this.setDateFormat(schedule_date_format);
        this.setDuration(schedule_pass_duration);
        this.setPassInterval(schedule_pass_interval);
        this.setCategory(schedule_category);
        this.setStartLabel(schedule_start_label);
        this.setEndLabel(schedule_end_label);

        // Override with any configured environmental properties
        String envFilePath = env.getProperty("schedule_file_path");
        this.setScheduleFilePath(envFilePath != null && !envFilePath.isEmpty() ? envFilePath : schedule_file_path);

        String envDateFormat = env.getProperty("schedule_date_format");
        this.setDateFormat(envFilePath != null && !envDateFormat.isEmpty() ? envFilePath : schedule_date_format);

        String envDuration = env.getProperty("schedule_pass_duration");
        this.setDuration(envDuration != null && !envDuration.isEmpty() ? envDuration : schedule_pass_duration);

        String envInterval = env.getProperty("schedule_pass_interval");
        this.setPassInterval(envInterval != null && !envInterval.isEmpty() ? envInterval : schedule_pass_interval);

        String envCategory = env.getProperty("schedule_category");
        this.setCategory(envCategory != null && !envCategory.isEmpty() ? envCategory : schedule_category);

        String envStartLabel = env.getProperty("schedule_start_label");
        this.setStartLabel(envStartLabel != null && !envStartLabel.isEmpty() ? envStartLabel : schedule_start_label);

        String envEndLabel = env.getProperty("schedule_end_label");
        this.setEndLabel(envEndLabel != null && !envEndLabel.isEmpty() ? envEndLabel : schedule_end_label);

    }

    public String usageText() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\nUsage: java -jar gmoc-demo-x.x.x.jar [key=value]...\n"
                        + "\nNon-required available keys/values options are:"
                        + "\n\t"+SCHEDULE_FILE_PATH+"=<String>       default: " + getScheduleFilePath()
                        + "\n\t"+SCHEDULE_DATE_FORMAT+"=<String>     default: " + getDateFormat() + ". Choices: " + enumList("DATEFORMAT")
                        + "\n\t"+SCHEDULE_PASS_DURATION+"=<String>   default: " + getDuration() + ". Expressed in hh:mm:ss"
                        + "\n\t"+SCHEDULE_PASS_INTERVAL+"=<int>      default: " + getPassInterval() + ". Expressed as minutes"
                        + "\n\t"+SCHEDULE_CATEGORY+"=<String>        default: " + getCategory() + ". Choices: " + enumList("CATEGORY")
                        + "\n\t"+SCHEDULE_START_LABEL+"=<String>     default: " + getStartLabel() + ". Choices: " + enumList("STARTLABEL")
                        + "\n\t"+SCHEDULE_END_LABEL+"=<String>       default: " + getEndLabel() + ". Choices: " + enumList("ENDLABEL")
                        + "\n"
                 );
        return sb.toString();
    }

    public boolean usage(String message)
    {
        log.warn(message);
        System.out.println(usageText());
        log.warn(usageText());
        return false;
    }

    public boolean parse(String[] args)
    {
        for (String arg : args)
        {
            int p = arg.indexOf('=');
            if (p == -1)
                return usage("invalid argument " + arg);

            String key = arg.substring(0, p);
            String value = arg.substring(p + 1);
            if (key.equalsIgnoreCase("schedule.file.path"))
                this.setScheduleFilePath(value);
            if(key.equalsIgnoreCase("schedule.category"))
                this.setCategory(value);
            if(key.equalsIgnoreCase("startLabel"))
                this.setStartLabel(value);
            if(key.equalsIgnoreCase("endLabel"))
                this.setEndLabel(value);
            if(key.equalsIgnoreCase("dateFormat"))
                this.setDateFormat(value);
            if (key.equalsIgnoreCase("passDuration"))
                this.setDuration(value);
            if (key.equalsIgnoreCase("passInterval"))
                this.setPassInterval(value);
        }
        return true;
    }

    public String getScheduleFilePath()
    {
        return scheduleFilePath;
    }

    public void setScheduleFilePath(String scheduleFilePath)
    {
        this.scheduleFilePath = scheduleFilePath;
    }

    public String getDuration()
    {
        return duration;
    }

    public void setDuration(String duration)
    {
        this.duration = duration;
    }

    public int getPassInterval()
    {
        return passInterval;
    }

    public void setPassInterval(String passInterval)
    {
        if(passInterval != null && !passInterval.isEmpty())
        {
            this.passInterval = Integer.parseInt(passInterval);
        }
    }

    private String enumList(String enumName)
    {
        String type = enumName.toUpperCase();
        StringBuilder sb = new StringBuilder();
        switch(type) {
            case "CATEGORY":
                for (Category cat : Category.values())
                {
                    sb.append(cat.type);
                    sb.append(",");
                }
                break;
            case "STARTLABEL":
                for (StartLabel sl : StartLabel.values())
                {
                    sb.append(sl.type);
                    sb.append(",");
                }
                break;
            case "ENDLABEL":
                for (EndLabel el : EndLabel.values())
                {
                    sb.append(el.type);
                    sb.append(",");
                }
                break;
            case "DATEFORMAT":
                for (DateFormat df : DateFormat.values())
                {
                    sb.append(df.toString());
                    sb.append(",");
                }
                break;
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        return sb.toString();
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public String getStartLabel()
    {
        return startLabel;
    }

    public void setStartLabel(String startLabel)
    {
        this.startLabel = startLabel;
    }

    public String getEndLabel()
    {
        return endLabel;
    }

    public void setEndLabel(String endLabel)
    {
        this.endLabel = endLabel;
    }

    public String getDateFormat()
    {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat)
    {
        this.dateFormat = dateFormat;
    }

    public enum Category
    {
        WIRE("WIRE"),
        SDO("SDO");
        private String type;
        Category(String type) { this.type = type; }
    }


    public enum StartLabel
    {
        AOS("AOS"),
        SDO("SDO");
        private String type;
        StartLabel(String type) { this.type = type; }
    }

    public enum EndLabel
    {
        LOS("LOS"),
        SDO("SDO");
        private String type;
        EndLabel(String type) { this.type = type; }
    }

//    public static final String MONTH_IN_YEAR = "yyyy-MM-dd-HH:mm:ss";
//    public static final String DAY_IN_YEAR = "yyyy-DDD-HH:mm:ss"; //Day in year format
    public enum DateFormat
    {
        MONTH_IN_YEAR("yyyy-MM-dd-HH:mm:ss"),
        DAY_IN_YEAR("yyyy-DDD-HH:mm:ss");

        private static final Map<String, String> lookup = new HashMap();
        static {
            for (DateFormat d : DateFormat.values()) {
                lookup.put(d.name(), d.value());
            }
        }

        private String value;
        DateFormat(String value) { this.value = value; }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return this.name();
        }

        public static String find(String name) {
            return lookup.get(name);
        }
    }

    public String getDateFormatString() {
        return DateFormat.find(getDateFormat());
    }
}
