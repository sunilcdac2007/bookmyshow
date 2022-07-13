package bookmyshow.gateway.authservice.controler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SessionController {
    private static final Logger log = LogManager
                                              .getLogger(SessionController.class);
    @GetMapping("/invalidsession")
    public String login(){
        log.info("Request received for /invalidsession");
        return "invalidsession";
    }
}
