package gov.nasa.gsfc.gmsec.gmoc.service;

import gov.nasa.gsfc.gmsec.gmoc.Options;
import gov.nasa.gsfc.gmsec.gmoc.model.PassSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Countdown clock is configured with a WIRE pass schedule composed
 * of a 3 minute pass, repeated every 5 minutes, between the hours
 * of 8AM and 6PM
 * The last two fields are set to be sequential numeric values to aid
 * in the troubleshooting CC ingest of the schedule file.  Typically they would instead be set to:
 * WIRE AOS, WIRE LOS
 *
 * Create a pass schedule to be digested by GMSEC Scheduler Component.
 * The file format is a CSV file with five fields:
 * 1) Pass schedule category: WIRE
 * 2) Pass start time datestamp: YYYY-MM-DD-hh:mm:ss
 * 3) Pass duration: +hh:mm:ss
 * 4) Counter 1: integer with row index starting at 1
 * 5) Counter 2: integer with row index starting at 1
 * 4) Start Label
 * 5) End Label
 *
 * "SDO", "2016-10-07-08:00:00", "+00:03:00", "AOS SDO", "LOS SDO"
 * "WIRE", "2016-10-07-08:00:00", "+00:03:00", "1", "1"
 * "WIRE", "2016-10-07-08:05:00", "+00:03:00", "2", "2"
 * "WIRE", "2016-10-07-08:10:00", "+00:03:00", "3", "3"
 * ...
 * "WIRE", "2016-10-07-17:55:00", "+00:03:00", "120", "120"
 * "WIRE", "2016-10-07-18:00:00", "+00:03:00", "121", "121"
 *
 */
@Service
public class PassScheduleService
{
    private static final Logger log = LoggerFactory.getLogger(PassScheduleService.class);

    @Autowired
    Options options;

    private static String newline = "\r\n";
    private List<String> passSchedLines;
    private StringBuilder passes;

    private static final String GENERIC_FILE_NAME = "GENERICPassSchedule";
    public static final String DATESTAMP_FORMAT = "yyyy-MM-dd";
    private String fileName;
    private boolean initialized = false;

    public void init() {
        DateFormat dateFormat = new SimpleDateFormat(DATESTAMP_FORMAT);
        StringBuilder sb = new StringBuilder();
        sb.append(GENERIC_FILE_NAME);
        sb.append("-");
        sb.append(dateFormat.format(new Date()));
        sb.append(".csv");
        fileName = sb.toString();

        passSchedLines = new ArrayList<>();
        PassSchedule pass = new PassSchedule(options);
        passes = new StringBuilder();
        for(int p=1; p<= 121; p++)
        {
            if(p>1) pass.incrementPass();
            pass.setCounterOne(p);
            pass.setCounterTwo(p);
            passes.append(pass.toString());
            passes.append(newline);
            passSchedLines.add(pass.toString());
        }
        initialized = true;
    }

    //view-source:http://127.0.0.1:8080/
    public String getGenericPassSchedule()
    {
        if(!initialized) init();
        return passes.toString();
    }

    public void reportCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        log.info("Creating a new pass schedule file now {}", dateFormat.format(new Date()));
    }

    //@Scheduled(fixedRate = 5000) // every 5 seconds
    @Scheduled(cron="0 7 * * * * ") // daily at 7 am
    public File createPassScheduleFile()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
        log.info("Creating a new pass schedule file now at {}", dateFormat.format(new Date()));

        if(!initialized) init();
        File path = new File(options.getScheduleFilePath());
        if(!path.exists())
        {
            log.error("Configured pass schedule file path does not exist.");
            return null;
        }
        if(!path.isDirectory())
        {
            log.error("Configured pass schedule file path is not a directory.");
            return null;
        }
        Path file = Paths.get(path+File.separator+fileName);
        try
        {
            Files.write(file, passSchedLines, Charset.forName("UTF-8"));
        } catch (IOException e)
        {
            log.error(e.getMessage());
        }
        return file.toFile();
    }

}
