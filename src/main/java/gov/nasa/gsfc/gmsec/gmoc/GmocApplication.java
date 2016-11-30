package gov.nasa.gsfc.gmsec.gmoc;

import gov.nasa.gsfc.gmsec.gmoc.service.PassScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Run arguments
 * schedule_file_path=/home/
 * Else defaults to write to HOME
 *
 * @See view-source:http://127.0.0.1:8090/
 *
 */

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = "gov.nasa.gsfc.gmsec.gmoc")
public class GmocApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(GmocApplication.class);

	@Autowired
	private Options options;

	@Autowired
	private PassScheduleService passScheduleService;

	public static void main(String[] args)
	{
		SpringApplication app = new SpringApplication(GmocApplication.class);
//		app.setWebEnvironment(false);
		app.run(args);
	}

	@Override
	public void run(String... strings) throws Exception
	{
		log.info(options.usageText());
		options.parse(strings);
		passScheduleService.createPassScheduleFile();
		passScheduleService.getGenericPassSchedule();
	}
}
