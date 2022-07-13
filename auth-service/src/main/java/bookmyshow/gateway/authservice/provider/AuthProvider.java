package bookmyshow.gateway.authservice.provider;

import bookmyshow.gateway.authservice.authtoken.UidOtpAuthenticationToken;
import bookmyshow.gateway.authservice.constant.Constants;
import bookmyshow.gateway.authservice.entity.User;
import bookmyshow.gateway.authservice.model.AuthwrapperOTPRequest;
import bookmyshow.gateway.authservice.model.AuthwrapperOTPResponse;
import bookmyshow.gateway.authservice.repository.UserRepository;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.session.data.redis.RedisSessionRepository;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AuthProvider implements AuthenticationProvider {

    private static final Logger log = LogManager
                                              .getLogger(AuthProvider.class);
    private static final String RESULT_SUCCESS_TOKEN = "Y";
    private static final String RESULT_FAILURE_TOKEN = "N";
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisSessionRepository redisSessionRepository;

    @Value("${webclient.connTimeout}")
    private int connTimeout;
    @Value("${webclient.readTimeout}")
    private int readTimeout;

    private HttpClient httpClient = HttpClient.
                                                      create()
                                            .tcpConfiguration(tcpClient -> {
                                                tcpClient = tcpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connTimeout);
                                                tcpClient = tcpClient.doOnConnected(conn -> conn
                                                                                                    .addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS)));
                                                return tcpClient;
                                            })
                                            .wiretap(true);
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        ServletRequestAttributes attr = (ServletRequestAttributes)
                                                RequestContextHolder.currentRequestAttributes();
        HttpSession session= attr.getRequest().getSession(true);
        String uid = (String) session.getAttribute(Constants.UID_TOKEN);
        String otp = authentication.getCredentials().toString();
        boolean isAuthenticationSuccessful;

        String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
        DefaultSavedRequest defaultSavedRequest = (DefaultSavedRequest) redisSessionRepository.getSessionRedisOperations().opsForHash().get("spring:session:sessions:"+sessionId, "sessionAttr:SPRING_SECURITY_SAVED_REQUEST");
        String[] clientIdValues = defaultSavedRequest.getParameterMap().get("client_id");
        String oauthClient = clientIdValues[0];
        ArrayList<User> userList = (ArrayList<User>) this.userRepository.findActiveUserList(Long.parseLong(uid), oauthClient);
        if(userList.size()==0){
            throw new InternalAuthenticationServiceException("user not found");
        }

        try {
            isAuthenticationSuccessful = getAuthenticationResponse(uid,otp);
        } catch (Exception e){
            log.error(e.getMessage(),e);
            throw new InternalAuthenticationServiceException(e.getMessage());
        }
        if(isAuthenticationSuccessful){
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            for(User user : userList){
                authorities.add(new SimpleGrantedAuthority(user.getUserType().getUserTypeCode()));
            }
            return new UidOtpAuthenticationToken(uid,otp,authorities);
        }
        throw new BadCredentialsException("Incorrect credentials");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UidOtpAuthenticationToken.class);
    }


    private boolean getAuthenticationResponse(String uid,String otp) throws Exception {

        ServletRequestAttributes attr = (ServletRequestAttributes)
                                                 RequestContextHolder.currentRequestAttributes();
        HttpSession session= attr.getRequest().getSession(true);
        String otptxnId = (String) session.getAttribute(Constants.OTPTXNID_TOKEN);
        log.debug("otptxnId while validating OTP: " + otptxnId);
        AuthwrapperOTPRequest authwrapperOTPRequest = new AuthwrapperOTPRequest();
        authwrapperOTPRequest.setUid(uid);
        authwrapperOTPRequest.setTxnID(otptxnId);
        authwrapperOTPRequest.setTotp(otp);
        JAXBContext jaxbContext = JAXBContext.newInstance(AuthwrapperOTPRequest.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        StringWriter stringWriter = new StringWriter();
        jaxbMarshaller.marshal(authwrapperOTPRequest,stringWriter);
        String request = stringWriter.toString();
        WebClient client = WebClient.builder()
                                   .clientConnector(new ReactorClientHttpConnector(httpClient))
                                   .baseUrl("localhost:4000") // calling otp server to validate request
                                   .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE).build();
        AuthwrapperOTPResponse authwrapperOTPResponse;
        try {
            String response = client.method(HttpMethod.POST).uri("/validateOpt")
                                      .header("Accept","application/xml")
                                      .body(Mono.just(request),String.class).retrieve()
                                      .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new Exception("not verified")))
                                      .onStatus(HttpStatus::is5xxServerError,clientResponse -> Mono.error(new Exception("not verified")))
                                      .bodyToMono(String.class).block();

            JAXBContext jaxbContext1 = JAXBContext.newInstance(AuthwrapperOTPResponse.class);
            Unmarshaller unmarshaller = jaxbContext1.createUnmarshaller();
            authwrapperOTPResponse = (AuthwrapperOTPResponse) unmarshaller.unmarshal(new StringReader(response));
        }catch (Exception e){
            log.error("Error while getting OTP response",e);
            throw new Exception("Error while getting OTP response");
        }

        if(authwrapperOTPResponse.getResult().equals(RESULT_SUCCESS_TOKEN)){
            log.info("Authentication successful");
            return true;
        }else if(authwrapperOTPResponse.getResult().equals(RESULT_FAILURE_TOKEN)){
            log.info("OTP Auth failed with error code : " + authwrapperOTPResponse.getResponseCode());
            throw new Exception(authwrapperOTPResponse.getMessage());
        }
        log.info("OTP Response result did not match either Y or N");
        throw new Exception("OTP Response result did not match either Y or N");
    }
}
