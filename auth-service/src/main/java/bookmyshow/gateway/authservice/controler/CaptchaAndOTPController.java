package bookmyshow.gateway.authservice.controler;

import bookmyshow.gateway.authservice.constant.Constants;
import bookmyshow.gateway.authservice.entity.User;
import bookmyshow.gateway.authservice.model.*;
import bookmyshow.gateway.authservice.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.session.data.redis.RedisSessionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
public class CaptchaAndOTPController {

    private static final Logger log = LogManager
                                              .getLogger(CaptchaAndOTPController.class);

    private UserRepository userRepository;
    private HttpClient httpClient;
    private RedisSessionRepository redisSessionRepository;

    @Value("${webclient.connTimeout}")
    private int connTimeout;

    @Value("${webclient.readTimeout}")
    private int readTimeout;


    public CaptchaAndOTPController(UserRepository userRepository, RedisSessionRepository redisSessionRepository) {
        this.userRepository = userRepository;
        this.redisSessionRepository = redisSessionRepository;
        httpClient = HttpClient.
                                       create()
                             .tcpConfiguration(tcpClient -> {
                                 tcpClient = tcpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connTimeout);
                                 tcpClient = tcpClient.doOnConnected(conn -> conn
                                                                                     .addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS)));
                                 return tcpClient;
                             })
                             .wiretap(true);
    }

    @PostMapping(value = "/generateOTPForOAuth")
    public ResponseEntity<PortalGenerateOTPResponse> validateCaptchaAndGenerateOTP(@RequestBody PortalGenerateOTPRequest portalGenerateOTPRequest, HttpSession httpSession){

        PortalGenerateOTPResponse portalGenerateOTPResponse = new PortalGenerateOTPResponse();
        String generatedTxnId = UUID.randomUUID().toString();

        portalGenerateOTPResponse.setTxnId(generatedTxnId);

        String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
        DefaultSavedRequest defaultSavedRequest = (DefaultSavedRequest) redisSessionRepository.getSessionRedisOperations().opsForHash().get("spring:session:sessions:"+sessionId, "sessionAttr:SPRING_SECURITY_SAVED_REQUEST");
        if(defaultSavedRequest == null){
            portalGenerateOTPResponse.setSessionActive(false);
            return new ResponseEntity<>(portalGenerateOTPResponse, HttpStatus.OK);
        }
        portalGenerateOTPResponse.setSessionActive(true);
        String[] clientIdValues = defaultSavedRequest.getParameterMap().get("client_id");
        String oauthClient = clientIdValues[0];
        log.debug("Client Id : " + clientIdValues[0]);

        String captcha = portalGenerateOTPRequest.getCaptcha();
        String captchaTxnId = portalGenerateOTPRequest.getCaptchaTxnId();
        String uid = portalGenerateOTPRequest.getUid();
        log.debug("Portal Captcha Request : ");
        log.debug("captcha : " + captcha);
        log.debug("captchaTxnId : " + captchaTxnId);
        log.debug("uid : " + uid);
        if(captcha==null){
            log.info("Error : Captcha is null");
            portalGenerateOTPResponse.setStatus(false);
            portalGenerateOTPResponse.setMessage("Invalid Captcha");
            return new ResponseEntity<>(portalGenerateOTPResponse, HttpStatus.OK);
        }
        if(captchaTxnId==null){
            log.info("Error: CaptchatxnId is null");
            portalGenerateOTPResponse.setStatus(false);
            portalGenerateOTPResponse.setMessage("Inavlid Captcha TxnId");
            return new ResponseEntity<>(portalGenerateOTPResponse, HttpStatus.OK);
        }
        // Captcha Validate Request Start
        CaptchaValidateRequest captchaValidateRequest = new CaptchaValidateRequest();
        captchaValidateRequest.setCaptcha(captcha);
        captchaValidateRequest.setCaptchaTxnId(captchaTxnId);
        String jsonRequest;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            jsonRequest = objectMapper.writeValueAsString(captchaValidateRequest);
            log.debug("Captcha Validate request : " + jsonRequest);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(),e);
            portalGenerateOTPResponse.setStatus(false);
            portalGenerateOTPResponse.setMessage("json processing exception");
            return new ResponseEntity<>(portalGenerateOTPResponse, HttpStatus.OK);
        }
        WebClient client = WebClient.builder()
                                   .clientConnector(new ReactorClientHttpConnector(httpClient))
                                   .baseUrl("localhost:4000") // capth base url
                                   .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();
        String response;
        try {
            response = client.method(HttpMethod.POST).uri("/validateCaptha")
                               .body(Mono.just(jsonRequest),String.class).retrieve()
                               .onStatus(HttpStatus::is4xxClientError,clientResponse -> Mono.error(new Exception("Captcha validation 4xx error")))
                               .onStatus(HttpStatus::is5xxServerError,clientResponse -> Mono.error(new Exception("Captcha validation 5xx error")))
                               .bodyToMono(String.class).block();
        }catch (Exception e){
            log.error(e.getMessage(),e);
            portalGenerateOTPResponse.setStatus(false);
            portalGenerateOTPResponse.setMessage("getting some exception");
            return new ResponseEntity<>(portalGenerateOTPResponse, HttpStatus.OK);
        }
        CaptchaValidateResponse captchaValidateResponse;
        try {
            captchaValidateResponse = objectMapper.readValue(response,CaptchaValidateResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Error while validating captcha", e);
            portalGenerateOTPResponse.setStatus(false);
            portalGenerateOTPResponse.setMessage("Error while validating captcha");
            return new ResponseEntity<>(portalGenerateOTPResponse, HttpStatus.OK);
        }
        portalGenerateOTPResponse.setStatus(true);
        log.debug("Captcha response :");
        log.debug("Status Code : " + captchaValidateResponse.getStatusCode());
        log.debug("Response status : " + captchaValidateResponse.getResponseStatus());
        log.debug("isValidCaptcha : " + captchaValidateResponse.isValidCaptcha());
        if(!captchaValidateResponse.isValidCaptcha()){
            log.info("Error : Invalid captcha response from server");
            portalGenerateOTPResponse.setStatus(false);
            portalGenerateOTPResponse.setMessage(captchaValidateResponse.getResponseStatus());
            return new ResponseEntity<>(portalGenerateOTPResponse, HttpStatus.OK);
        }else {
            log.info("Captcha Validation Successful");
        }
        // Captcha Validate Request End

        // Check user status before OTP generation
        ArrayList<User> userList = (ArrayList<User>) this.userRepository.findActiveUserList(Long.parseLong(uid), oauthClient);
        if(userList.size()==0){
            log.info("Error : No user found with given uid");
            portalGenerateOTPResponse.setStatus(false);
            portalGenerateOTPResponse.setMessage("Error : No user found with given uid");
            return new ResponseEntity<>(portalGenerateOTPResponse, HttpStatus.OK);
        }
        httpSession.setAttribute(Constants.UID_TOKEN,uid);
        String otpTxnId = UUID.randomUUID().toString();
        log.debug("otpTxnId while generating OTP : " + otpTxnId);
        httpSession.setAttribute(Constants.OTPTXNID_TOKEN,otpTxnId);
        AuthwrapperGenerateOTPRequest authwrapperGenerateOTPRequest = new AuthwrapperGenerateOTPRequest();
        authwrapperGenerateOTPRequest.setTxnID(otpTxnId);
        authwrapperGenerateOTPRequest.setUid(uid);
        StringWriter stringWriter;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(AuthwrapperGenerateOTPRequest.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            stringWriter = new StringWriter();
            jaxbMarshaller.marshal(authwrapperGenerateOTPRequest,stringWriter);
        } catch (JAXBException e) {
            log.error("Error while creating otp request",e);
            portalGenerateOTPResponse.setStatus(false);
            portalGenerateOTPResponse.setMessage("Error while creating otp request");
            return new ResponseEntity<>(portalGenerateOTPResponse, HttpStatus.OK);
        }
        String otpRequest = stringWriter.toString();
        WebClient client1 = WebClient.builder()
                                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                                    .baseUrl("localhost:4000")
                                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE).build();
        String response1;
        try {
            response1 = client1.method(HttpMethod.POST).uri("/generateOtp")
                                .header("Accept","application/xml")
                                .body(Mono.just(otpRequest),String.class).retrieve()
                                .onStatus(HttpStatus::is4xxClientError,clientResponse -> Mono.error(new Exception("OTP Generation 4xx error")))
                                .onStatus(HttpStatus::is5xxServerError,clientResponse -> Mono.error(new Exception("OTP Generation 5xx error")))
                                .bodyToMono(String.class).block();
        }catch (Exception e){
            log.error(e.getMessage(),e);
            portalGenerateOTPResponse.setStatus(false);
            portalGenerateOTPResponse.setMessage("error in otp generation ");
            return new ResponseEntity<>(portalGenerateOTPResponse, HttpStatus.OK);
        }
        AuthwrapperGenerateOTPResponse authwrapperGenerateOTPResponse;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(AuthwrapperGenerateOTPResponse.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            authwrapperGenerateOTPResponse = (AuthwrapperGenerateOTPResponse) unmarshaller.unmarshal(new StringReader(response1));
        } catch (Exception e) {
            log.error("Error while requesting for OTP", e);
            portalGenerateOTPResponse.setStatus(false);
            portalGenerateOTPResponse.setMessage("Error while requesting for OTP");
            return new ResponseEntity<>(portalGenerateOTPResponse, HttpStatus.OK);
        }
        log.debug("Response from validate captcha service : ");
        log.debug(response1);
        if(authwrapperGenerateOTPResponse.getResponseCode().equals("200")){
            log.info("Success : OTP Generation Successful");
            portalGenerateOTPResponse.setStatus(true);
            portalGenerateOTPResponse.setMessage("success");
            return new ResponseEntity<>(portalGenerateOTPResponse, HttpStatus.OK);
        }else {
            log.info("Error : OTP Generation Failed");
            portalGenerateOTPResponse.setStatus(false);
            portalGenerateOTPResponse.setMessage(authwrapperGenerateOTPResponse.getMessage());
            return new ResponseEntity<>(portalGenerateOTPResponse, HttpStatus.OK);
        }

    }

    @GetMapping(value = "/generateCaptcha")
    public ResponseEntity<CaptchaResponseToFrontent> generateCaptcha(){

        CaptchaResponseToFrontent captchaResponseToFrontend = new  CaptchaResponseToFrontent();

        String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
        DefaultSavedRequest defaultSavedRequest = (DefaultSavedRequest) redisSessionRepository.getSessionRedisOperations().opsForHash().get("spring:session:sessions:"+sessionId, "sessionAttr:SPRING_SECURITY_SAVED_REQUEST");
        if(defaultSavedRequest == null){
            captchaResponseToFrontend.setSessionActive(false);
            return new ResponseEntity<>(captchaResponseToFrontend, HttpStatus.OK);
        }
        captchaResponseToFrontend.setSessionActive(true);
        CaptchaRequest captchaRequest = new CaptchaRequest();
        captchaRequest.setCaptchaType("test");
        captchaRequest.setCaptchaLength("6");
        captchaRequest.setLangCode("en_IN");
        captchaRequest.setTimestamp(LocalDateTime.now().toString());
        captchaRequest.setServicePoint("/servicepoing");
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString;
        try {
            jsonString = objectMapper.writeValueAsString(captchaRequest);
            log.debug("Captcha Request created : " + jsonString);
        } catch (JsonProcessingException e) {
            log.error("Error while generating captcha",e);
            captchaResponseToFrontend.setMessage("Error while generating captcha");
            return new ResponseEntity<>(captchaResponseToFrontend,HttpStatus.INTERNAL_SERVER_ERROR);
        }
        WebClient client = WebClient.builder().baseUrl("localhost:4000")
                                   .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();
        String response;
        try {
            response = client.method(HttpMethod.POST).uri("/captcha")
                               .body(Mono.just(jsonString),String.class).retrieve()
                               .onStatus(HttpStatus::is4xxClientError, clientResponse ->Mono.error(new Exception("Bad Request")))
                               .onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new Exception("Internal Server Error")))
                               .bodyToMono(String.class).block();
        }catch (Exception e){
            log.error("Error in getCaptcha call",e);
            captchaResponseToFrontend.setMessage("error in getcaptcha call");
            return new ResponseEntity<>(captchaResponseToFrontend,HttpStatus.INTERNAL_SERVER_ERROR);
        }
        log.debug(response);
        captchaResponseToFrontend.setMessage(response);
        return new ResponseEntity<>(captchaResponseToFrontend,HttpStatus.OK);
    }

}
