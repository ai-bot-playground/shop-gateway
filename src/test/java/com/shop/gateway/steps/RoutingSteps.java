package com.shop.gateway.steps;

import com.shop.gateway.StubBackends;
import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

public class RoutingSteps {

    private final HttpClient http = HttpClient.newHttpClient();

    @Value("${local.server.port}")
    private int port;

    private String path;
    private int status;
    private String body;

    @Before
    public void resetBackends() {
        StubBackends.reset();
    }

    @When("a request hits {string}")
    public void aRequestHits(String path) throws IOException, InterruptedException {
        this.path = path;
        HttpResponse<String> response = http.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        status = response.statusCode();
        body = response.body();
    }

    // `/` is alternation syntax in Cucumber expressions, hence the escape.
    @Then("it is forwarded to {string} with the \\/api prefix stripped")
    public void itIsForwardedTo(String service) {
        String stripped = path.substring("/api".length());
        assertThat(status).as("gateway response for %s", path).isEqualTo(200);
        assertThat(body).as("service that answered %s", path).isEqualTo(service);
        assertThat(StubBackends.pathsReceivedBy(service)).as("paths received by %s", service)
                .containsExactly(stripped);
        for (String other : StubBackends.SERVICES) {
            if (!other.equals(service)) {
                assertThat(StubBackends.pathsReceivedBy(other)).as("%s must not see %s", other, path).isEmpty();
            }
        }
    }
}
