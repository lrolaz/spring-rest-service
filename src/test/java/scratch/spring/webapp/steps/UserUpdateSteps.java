package scratch.spring.webapp.steps;

import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Map;

import static javax.ws.rs.client.Entity.json;
import static scratch.spring.webapp.steps.UserFields.*;

@ContextConfiguration(classes = CucumberScratchConfiguration.class)
public class UserUpdateSteps {

    @Autowired
    private PropertyObject user;

    @Autowired
    private WebTarget client;

    @Autowired
    private Responses responses;

    @When("^I update the user$")
    public void I_update_the_user() {

        final Response createResponse = responses.created().latest();

        @SuppressWarnings("unchecked")
        final Map<String, Object> body = createResponse.readEntity(Map.class);

        final String id = body.get(ID).toString();

        final Response response = client.path(id).request(MediaType.APPLICATION_JSON_TYPE).put(json(user.toMap()));
        response.bufferEntity();

        responses.add(response);
    }
}
