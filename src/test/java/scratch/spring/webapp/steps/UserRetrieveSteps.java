package scratch.spring.webapp.steps;

import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Deque;
import java.util.Map;

import static scratch.spring.webapp.steps.UserFields.ID;

@ContextConfiguration(classes = CucumberScratchConfiguration.class)
public class UserRetrieveSteps {

    @Autowired
    private WebTarget client;

    @Autowired
    private Holder<Deque<Response>> responses;

    @When("^I request an existing user$")
    public void I_request_an_existing_user() {

        final Response createResponse = responses.get().peek();

        @SuppressWarnings("unchecked")
        final Map<String, Object> body = createResponse.readEntity(Map.class);

        I_request_a_user_with_an_ID_of(body.get(ID).toString());
    }

    @When("^I request all existing user$")
    public void I_request_all_existing_user() {

        I_request_a_user_with_an_ID_of("");
    }

    @When("^I request a user with an ID of \"([^\"]*)\"$")
    public void I_request_a_user_with_an_ID_of(String id) {

        final Response response = client.path(id).request(MediaType.APPLICATION_JSON_TYPE).get();
        response.bufferEntity();

        responses.get().push(response);
    }
}