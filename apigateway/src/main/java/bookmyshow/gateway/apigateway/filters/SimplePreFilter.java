package bookmyshow.gateway.apigateway.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;

public class SimplePreFilter extends ZuulFilter {

    @Value("${oauthCheckTokenUrl}")
    private String oauthCheckTokenUrl;

    @Value("${permitallendpoints}")
    private String[] noValidationUrls;

    //@Value("${permitallendpointsWithAuth}")
    //private String[] validationUrls;

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        boolean status = true;
        for (String noValidationUrlPattern : noValidationUrls){
            String definedMethod = noValidationUrlPattern.split(":")[0];
            String definedUrlPattern = noValidationUrlPattern.split(":")[1];
            if (validate(request.getRequestURI(),definedUrlPattern) ){
                if(request.getMethod().equals(definedMethod)){
                    // when url pattern & method is matched skip filter
                    status = false;
                }
            }
        }
        // When no url pattern & method are matched
        return status;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        String bearerString = request.getHeader("Authorization");
        if(bearerString == null){
            ctx.set("unauthorized_noBearer", "No Authorization Header present in header");
            ctx.setSendZuulResponse(false);
            throw new ZuulException("", 200, "");
        }
        String accessToken = bearerString.split(" ")[1];

        ResponseEntity<String> responseEntity = null;

        responseEntity = hitCheckTokenEndpoint(accessToken, ctx);

        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            return ctx;
        } else {
            ctx.setSendZuulResponse(false);
            ctx.setResponseBody(responseEntity.getBody());
            ctx.setResponseStatusCode(responseEntity.getStatusCode().value());
        }
        return ctx;
    }

    private ResponseEntity hitCheckTokenEndpoint(String token, RequestContext ctx) throws ZuulException {

        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(oauthCheckTokenUrl)
                .queryParam("token", token);
        ResponseEntity<String> response = null;
        try {

            response = restTemplate
                    .exchange(builder.toUriString(), HttpMethod.POST, null, String.class);
        }catch (HttpClientErrorException | HttpServerErrorException e){
            return ResponseEntity.status(e.getRawStatusCode()).headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsString());
        }catch (Exception e){
            ctx.set("ouath_error_message", "Unable to connect to check token endpoint");
            throw new ZuulException("", 200, "");
        }

        return response;
    }

    private boolean validate(String uri, String str){
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        return antPathMatcher.match(str, uri);
    }
}

