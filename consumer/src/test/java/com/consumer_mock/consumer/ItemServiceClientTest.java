package com.consumer_mock.consumer;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.consumer_mock.consumer.clients.ItemServiceClient;
import com.consumer_mock.consumer.models.Item;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.lanwen.wiremock.ext.WiremockResolver;
import ru.lanwen.wiremock.ext.WiremockResolver.Wiremock;
import ru.lanwen.wiremock.ext.WiremockUriResolver;
import ru.lanwen.wiremock.ext.WiremockUriResolver.WiremockUri;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@ExtendWith({ WiremockResolver.class, WiremockUriResolver.class })
class ItemServiceClientTest {
  @Autowired
  private ItemServiceClient itemService;

  /**
   * Integration test to ensure consumer hits the correct endpoint and the response is correctly put into an object.
   * @param server
   * @param uri
   */

  @Test
  void fetchItem(@Wiremock WireMockServer server, @WiremockUri String uri) {
    itemService.setBaseUrl(uri);
    server.stubFor(
      get(urlPathEqualTo("/api/items/12db9bdc-617e-4710-b946-b0cab5482d1"))
        .willReturn(aResponse()
          .withStatus(200)
          .withBody("{\n" +
          "            \"id\": \"12db9bdc-617e-4710-b946-b0cab5482d1\",\n" +
          "            \"name\": \"Iphone 13\",\n" +
          "            \"quantity\": 12,\n" +
          "            \"price\": 4999.99\n" +
          "        }\n")
          .withHeader("Content-Type", "application/json"))
    );

    Item item = itemService.getItem("12db9bdc-617e-4710-b946-b0cab5482d1").getBody();
    assertThat(item, is(equalTo(new Item("12db9bdc-617e-4710-b946-b0cab5482d1", "Iphone 13", 12, (float) 4999.99))));
  }

}
