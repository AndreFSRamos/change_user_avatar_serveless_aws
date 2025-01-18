package pucpr.edu.avatar_generator.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import pucpr.edu.avatar_generator.AvatarGeneratorApplication;
import pucpr.edu.avatar_generator.controllers.AvatarController;

import java.util.Map;

@SuppressWarnings("ALL")
public class AwsLambdaHandler implements RequestHandler<Map<String, Object>, Object> {
    Logger logger = LogManager.getLogger(this.getClass().getName());

    private static final ApplicationContext applicationContext;

    static {
        applicationContext = SpringApplication.run(AvatarGeneratorApplication.class);
    }

    @Override
        public Object handleRequest(Map<String, Object> event, Context context) {
        logger.info("Start request handler: {}", event);

        String httpMethod = (String) event.get("httpMethod");
        Map<String, String> pathParameters = (Map<String, String>) event.get("pathParameters");
        Map<String, String> queryParameters = (Map<String, String>) event.get("queryStringParameters");

        if (httpMethod == null || pathParameters == null) return createErrorResponse("Invalid params: 'httpMethod' and 'pathParameters' is mandatory.");

        String userId = pathParameters.get("id");

        if (userId == null) return createErrorResponse("Fields 'userId' is mandatory.");

        try {
            AvatarController avatarController = applicationContext.getBean(AvatarController.class);

            switch (httpMethod) {
                case "GET":
                    if (queryParameters == null) return createErrorResponse("Invalid params: 'queryParameters is mandatory.");

                    final String email = queryParameters.get("email");
                    final String name = queryParameters.get("name");

                    if (email == null || name == null) return createErrorResponse("Fields 'email' and 'name' is mandatory.");

                    return createLambdaResponse(avatarController.getAvatar(userId, email, name));

                case "DELETE":
                    return createLambdaResponse(avatarController.deleteAvatar(userId));

                default:
                    return createErrorResponse("Method not supported: " + httpMethod);
            }
        } catch (Exception e) {
            return createErrorResponse("Internal server error: " + e.getMessage());
        }
    }

    private Map<String, Object> createLambdaResponse(ResponseEntity<String> response) {
        return Map.of(
                "statusCode", response.getStatusCodeValue(),
                "body", response.getBody()
        );
    }

    private Map<String, Object> createErrorResponse(String message) {
        return Map.of(
                "statusCode", 400,
                "body", String.format("{\"error\": \"%s\"}", message)
        );
    }
}
