package bookmyshow.gateway.authservice.config;

import bookmyshow.gateway.authservice.handler.OtpAuthenticationFailureHandler;
import bookmyshow.gateway.authservice.provider.AuthProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${http.header.allowedHost}")
    private String allowedHost;

    @Autowired
    private AuthProvider authProvider;
//    @Autowired
//    private UidOtpAuthenticationFailureHandler uidOtpAuthenticationFailureHandler;

    protected void configure(HttpSecurity http) throws Exception{
       // http.addFilterBefore(new AuthenticationFilter(super.authenticationManager()), UsernamePasswordAuthenticationFilter.class);
        http.authorizeRequests()
                .antMatchers("/login").authenticated()
                .and()
                .formLogin(form -> form
                                           .loginPage("/login")
                                           .permitAll()
                                           .failureHandler(new OtpAuthenticationFailureHandler())

                )
                .sessionManagement()
                .invalidSessionUrl("/invalidsession");

    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowedHostnames(hostname -> hostname.matches(allowedHost));
        web.ignoring().antMatchers("/generateOTPForOAuth");
        web.ignoring().antMatchers("/oauth/check_token");
        web
                .httpFirewall(firewall);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authProvider);
    }

}
