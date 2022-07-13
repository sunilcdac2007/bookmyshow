package bookmyshow.gateway.authservice.authfilter;

import bookmyshow.gateway.authservice.handler.OtpAuthenticationFailureHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.NullRememberMeServices;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ApplicationAuthFilter extends AbstractAuthenticationProcessingFilter {
    // Spring security form parameters
    private String uidParameter = "uid";
    private String otpParameter = "otp";
    private boolean postOnly = true;



    private RememberMeServices rememberMeServices = new NullRememberMeServices();
    private OtpAuthenticationFailureHandler uidOtpAuthenticationFailureHandler = new OtpAuthenticationFailureHandler();
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private Authentication authentication;

    protected ApplicationAuthFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {

        setAllowSessionCreation(false);
       return authentication;
    }


}
