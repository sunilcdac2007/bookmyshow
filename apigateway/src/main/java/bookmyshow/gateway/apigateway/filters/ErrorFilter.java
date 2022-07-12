package bookmyshow.gateway.apigateway.filters;

import bookmyshow.gateway.apigateway.model.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

import java.util.Date;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.ERROR_TYPE;

public class ErrorFilter extends ZuulFilter {
    private static final String THROWABLE_KEY = "throwable";
    @Override
    public String filterType() {
        return ERROR_TYPE;
    }

    @Override
    public int filterOrder() {
        return -1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {

        final RequestContext context = RequestContext.getCurrentContext();
        final Object throwable = context.get(THROWABLE_KEY);

        if (throwable instanceof ZuulException) {
            // remove error code to prevent further error handling in follow up filters
            context.remove(THROWABLE_KEY);

            ErrorResponse errorResponse = new ErrorResponse();

            if(context.get("unauthorized_noBearer")!=null){
                errorResponse.setTimestamp(new Date().toString());
                errorResponse.setStatus(401);
                errorResponse.setMessage((String)context.get("unauthorized_noBearer"));
                errorResponse.setError("Unauthorized");
                context.setResponseStatusCode(401);
            }else if(context.get("ouath_error_message")!=null){
                errorResponse.setTimestamp(new Date().toString());
                errorResponse.setStatus(500);
                errorResponse.setError("Internal Server Error");
                errorResponse.setMessage((String)context.get("ouath_error_message"));
                context.setResponseStatusCode(500);
            }else {
                errorResponse.setTimestamp(new Date().toString());
                errorResponse.setStatus(500);
                errorResponse.setError("Internal Server Error");
                errorResponse.setMessage("Unable to connect to backend service : " + context.getRequest().getRequestURI());
                context.setResponseStatusCode(500);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            String responseString;
            try {
                responseString = objectMapper.writeValueAsString(errorResponse);
                context.getResponse().setContentType("application/json");
                context.setResponseBody(responseString);
            } catch (JsonProcessingException e) {
                context.getResponse().setContentType("plain/text");
                context.setResponseBody("Internal Server Error");
            }
        }
        return context;
    }
}
