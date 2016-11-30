package gov.nasa.gsfc.gmsec.gmoc;

import gov.nasa.gsfc.gmsec.gmoc.service.PassScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * View generated schedule from web browser
 *      view-source:http://127.0.0.1:8090/
 *
 * Created by leif on 10/12/16.
 */
@RestController
public class DefaultRestController
{
    @Autowired
    private PassScheduleService passScheduleService;

    @RequestMapping("/")
    public String index() {
        return passScheduleService.getGenericPassSchedule();
    }
}
