package team.quad.trivia.opentdb;

import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class OtdbConfiguration {

    @Bean
    public RestClient openTdbRestClient(RestClient.Builder builder) {
        return builder.build();
    }

    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return (restClientBuilder) -> restClientBuilder
                // Setting the requestFactory directly onto the RestClient breaks testing functionality, because
                // RestClientTest utilizes setting the request factory to a `MockClientHttpRequestFactory`.
                // By setting it through a customizer, we can safely use @RestClientTest
                // see: https://github.com/spring-projects/spring-boot/issues/38832#issuecomment-2350865571
                .requestFactory(new HttpComponentsClientHttpRequestFactory())
                .defaultHeader("accept", MediaType.APPLICATION_JSON_VALUE)
                .baseUrl("https://opentdb.com");
    }

}
