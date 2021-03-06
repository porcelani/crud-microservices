package com.matera.crudmicroservices.api.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matera.crudmicroservices.config.CrudmicroservicesGroupKeys;
import com.matera.crudmicroservices.core.entities.Person;
import com.netflix.client.http.HttpRequest;
import com.netflix.client.http.HttpRequest.Verb;
import com.netflix.client.http.HttpResponse;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.niws.client.http.RestClient;

import java.net.URI;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

/**
 * Hystrix command to create a Person
 *
 * @author egzefer
 */
public class PersonCreateCommand extends HystrixCommand<Person> {

    private static final HystrixCommand.Setter SETTER = Setter.withGroupKey(CrudmicroservicesGroupKeys.MIDDLE)
        .andCommandKey(HystrixCommandKey.Factory.asKey(PersonCreateCommand.class.getSimpleName()));

    public static final String DEFAULT_URL = "/crudmicroservicesmiddle/person";
    public static final String URL = "crudmicroservices.person.create.url";

    private final RestClient restClient;
    private final ObjectMapper mapper;
    private final Person person;

    public PersonCreateCommand(final RestClient restClient, final ObjectMapper mapper, final Person person) {

        super(SETTER);
        this.restClient = restClient;
        this.mapper = mapper;
        this.person = person;
    }

    @Override
    protected Person run() throws Exception {

        String personCreateURL = DynamicPropertyFactory.getInstance().getStringProperty(URL, DEFAULT_URL).get();

        URI URI = UriBuilder.fromPath(personCreateURL).build();

        HttpRequest request =
            HttpRequest.newBuilder().verb(Verb.POST).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON).uri(URI).entity(person).build();

        try (HttpResponse response = restClient.executeWithLoadBalancer(request)) {
            return mapper.readValue(response.getInputStream(), Person.class);
        }
    }

}
