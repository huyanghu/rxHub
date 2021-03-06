package io.apptik.rxhub;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import rx.Observer;
import rx.subjects.PublishSubject;

import static io.apptik.rxhub.RxHubTest.Helper.getDummyConsumer;
import static org.assertj.core.api.Assertions.assertThat;

public class RxHubTest {

    Helper helper;

    public RxHubTest(Helper helper) {
        this.helper = helper;
    }

    @Given("^Provider\"([^\"]*)\"$")
    public void provider(String provider) throws Throwable {
        helper.providers.put(provider, PublishSubject.create());
    }

    @Given("^Consumer\"([^\"]*)\"$")
    public void consumer(String consumer) throws Throwable {
        helper.consumers.put(consumer, getDummyConsumer());
    }

    @When("^Hub\"([^\"]*)\" subscribes to Provider\"([^\"]*)\" with tag \"([^\"]*)\"$")
    @Given("^Hub\"([^\"]*)\" is subscribed to Provider\"([^\"]*)\" with tag \"([^\"]*)\"$")
    public void hub_is_subscribed_to_Provider_with_tag(String hub, String provider,
                                                       String tag) throws Throwable {
        helper.hubs.get(hub).addProvider(tag, helper.providers.get(provider));
    }

    @When("^Consumer\"([^\"]*)\" subscribes to Hub\"([^\"]*)\" with tag \"([^\"]*)\"$")
    @Given("^Consumer\"([^\"]*)\" is subscribed to Hub\"([^\"]*)\" with tag \"([^\"]*)\"$")
    public void consumer_subscribes_to_Hub_with_tag(String consumer, String hub, String tag)
            throws Throwable {
        try {
            helper.hubs.get(hub).getNode(tag).subscribe(helper.consumers.get(consumer));
        } catch (Exception ex) {
            helper.error = ex;
        }
    }
    @When("^Consumer\"([^\"]*)\" subscribes to Hub\"([^\"]*)\" with tag \"([^\"]*)\" and filter\"" +
            "([^\"]*)\"$")
    @Given("^Consumer\"([^\"]*)\" is subscribed to Hub\"([^\"]*)\" with tag \"([^\"]*)\" and filter\"([^\"]*)\"$")
    public void consumer_is_subscribed_to_Hub_with_tag_and_filter(
            String consumer, String hub, String tag, String filter) throws Throwable {
        try {
            helper.hubs.get(hub).getNodeFiltered(tag, Class.forName(filter))
                    .subscribe(helper.consumers.get(consumer));
        } catch (Exception ex) {
            helper.error = ex;
        }
    }

    @When("^Provider\"([^\"]*)\" emits Event\"([^\"]*)\"$")
    public void provider_emits_Event(String provider, String event) throws Throwable {
        helper.providers.get(provider).onNext(event);
    }

    @Then("^Consumer\"([^\"]*)\" should receive Event\"([^\"]*)\"$")
    public void consumer_should_receive_Event(String consumer, String event) throws Throwable {
        assertThat(helper.consumers.get(consumer).events).contains(event);
    }

    @Then("^Consumer\"([^\"]*)\" should not receive Event\"([^\"]*)\"$")
    public void consumer_should_not_receive_Event(String consumer, String event) throws Throwable {
        assertThat(helper.consumers.get(consumer).events).doesNotContain(event);
    }


    @Given("^Hub\"([^\"]*)\" with NodeType (.*)$")
    public void hub_with_NodeType_(String hub, final String nodeType) throws Throwable {
        helper.hubs.put(hub, new AbstractRxHub() {
            @Override
            public NodeType getNodeType(Object tag) {
                return NodeType.valueOf(nodeType);
            }

            @Override
            public boolean isNodeThreadsafe(Object tag) {
                return true;
            }
        });

    }

    @When("^Event\"([^\"]*)\" with tag \"([^\"]*)\" is emitted on Hub\"([^\"]*)\"$")
    public void event_with_tag_is_emitted_on_Hub(String event, String tag, String hub) throws
            Throwable {
        try {
            helper.hubs.get(hub).emit(tag, event);
        } catch (Exception ex) {
            helper.error = ex;
        }
    }

    @When("^Provider\"([^\"]*)\" with tag \"([^\"]*)\" is removed from Hub\"([^\"]*)\"$")
    public void provider_with_tag_is_removed_from_Hub(String provider, String tag, String hub)
            throws
            Throwable {
        helper.hubs.get(hub).removeProvider(tag, helper.providers.get(provider));
    }

    @When("^providers are cleared from Hub\"([^\"]*)\"$")
    public void providers_are_cleared_from_Hub(String hub) throws Throwable {
        helper.hubs.get(hub).clearProviders();
    }

    @Then("^there should be Error \"([^\"]*)\"$")
    public void there_should_be_Error(String error) throws Throwable {
        assertThat(helper.error).isNotNull();
        assertThat(helper.error.getClass().getName()).isEqualTo(error);
    }

    @Then("^there should be ErrorMessage \"([^\"]*)\"$")
    public void there_should_be_ErrorMessage(String errMsg) throws Throwable {
        assertThat(helper.error).isNotNull();
        assertThat(helper.error.getMessage()).isEqualTo(errMsg);
    }


    public static class Helper {
        public Map<String, RxHub> hubs = new HashMap<>();
        //use subjects  so we can easily emit events when needed
        public Map<String, PublishSubject> providers = new HashMap<>();
        public Map<String, DummyConsumer> consumers = new HashMap<>();
        public Throwable error;

        public static DummyConsumer getDummyConsumer() {
            return new DummyConsumer();
        }
    }

    public static class DummyConsumer implements Observer<Object> {
        ArrayList<String> events = new ArrayList<>();

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(Object o) {
            events.add(o.toString());
        }
    }


}
