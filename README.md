# Contract Testing
Sahil Mohmed

Task to create a RESTful API for managing a simple inventory system. API endpoints should allow to add items, get items, edit items, and delete items. 
Contract testing must be implemented to verify the interactions between the API and the consumer. 

## Contract Testing Approach
There are multiple tools and frameworks designed for contract testing, but for this task i have selected **Pact** 
which is open source and very well supported, it also integrates easily into the Spring boot framework. Pact is a consumer driven tool, which means the consumer will produce the contract and the provider will have to adhere to it. There are other tools like Spring Cloud Contract, but they generally focus on the provider side. 

My approach to contract testing involves having three seperate entities:
<br>
1. **Consumer** - the consumer runs tests against a **mock** provider API which has been designed to always provide the expected results. Once the tests have been completed, a contract will be generated (Pact json file).
2. **Pact Broker** - the Pact broker is a web app which allows the consumer to share contacts with the provider. It also allows the provider to publish results. 
3. **Provider** - the provider is the real API, the contracts are tested agains the real API. 

<!-- description of the approach - diagrams -->
## Brief Repo Contents
In the real world, the Consumer and Provider would be on seperate repos, however for this task i have put them both on a single repo to keep things simple. 
Contents: 
1. **consumer** - contains a Spring boot Maven project which uses a mock server to test against a RestTemplate. 
2. **inventory_api** (provider) - contains a Spring boot Maven project which hosts an API and web page (Thymeleaf). The real API is hosted using this project and the contracts are tested against it. 
3. **docker-compose.yml** - docker container to host the Pact Broker.
   
## Inventory API (Provider)
This project is where the real API is hosted using a `@RestController`:
<br>

    @RestController
    @RequestMapping("/api")
    public class AppRestController {

        @Autowired
        private InventoryRepository repository;

        /**
        * Adding a new item to the inventory
        * 
        * @param input
        * @return 201 Created status with the new item as the response
        */
        @RequestMapping(method = RequestMethod.POST, value = "/items")
        public ResponseEntity<?> addItem(@RequestBody Inventory input) {
            if (!isValid(input)) {
                return new ResponseEntity<>("Invalid input data", HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(repository.save(input), HttpStatus.CREATED);
        }


Data is stored into an in memory H2 database, the data is modelled as a Spring `@Entity` to allow for easy mapping between tables and objects:

    @Entity
    @Table(name = "Inventory")
    public class Inventory {....
    .....
    }

To allow for CRUD storage operations - i have created an interface which extends `CrudRepository`:
<br>

    public interface InventoryRepository extends CrudRepository<Inventory, String> {
        Optional<Inventory> findById(String id);
    }

To initialize some dummy data for testing purposed, i have added a `data.sql` file into resources. The following dummy data is initialized into the database:
<br>

    INSERT INTO Inventory (id, name, quantity, price) VALUES ('12db9bdc-617e-4710-b946-b0cab5482d1', 'Iphone 13', '12', '4999.99')
    INSERT INTO Inventory (id, name, quantity, price) VALUES ('1b07b137-c486-4a5e-9e5c-a33295e471cc', 'Macbook Pro', '27', '5999.99')
    INSERT INTO Inventory (id, name, quantity, price) VALUES ('a1aed83e-1c33-4f09-8334-0d7618d6f86c', 'Ipad', '19', '3499.99')
    INSERT INTO Inventory (id, name, quantity, price) VALUES ('1bc78422-c55c-4c4d-b453-124b56ed261e', 'Keyboard', '86', '799.99')

<table>
<tr>
    <th>ID</th>
    <th>Name</th>
    <th>Quantity</th>
    <th>Price</th>
</tr>
<tr>
    <td>12db9bdc-617e-4710-b946-b0cab5482d1</td>
    <td>Iphone 13</td>
    <td>12</td>
    <td>4999.99</td>
</tr>
<tr>
    <td>1b07b137-c486-4a5e-9e5c-a33295e471cc</td>
    <td>Macbook Pro</td>
    <td>27</td>
    <td>5999.99</td>
</tr>
<tr>
    <td>a1aed83e-1c33-4f09-8334-0d7618d6f86c</td>
    <td>Ipad</td>
    <td>19</td>
    <td>3499.99</td>
</tr>
<tr>
    <td>1bc78422-c55c-4c4d-b453-124b56ed261e</td>
    <td>Keyboard</td>
    <td>86</td>
    <td>799.99</td>
</tr>
</table>
<br>

I have also implemented a front end API reference using a `@Controller`, this uses `Thymeleaf` to publish the html page. 
<br>

    @Controller
    public class AppController {
        /**
        * Get api reference
        * @return api reference page
        */
        @RequestMapping("/api")
        public String getApiRef() {
            return "apiref";
        }
<!-- image of api reference  -->

The Pact verification tests use JUnit5 annotations to setup the class and to define the test methods: 
<br>
`@SpringBootTest` - to define that the class is a Spring boot test. 
<br>
`@Provider` - Pact JUnit5 annotation to define the name of the provider - only contracts that are setup for this provider will be tested.
<br>
`@PactBroker` - contains the access details for the Pact broker.
<br>
`@IgnoreMissingStateChange` - this will continue testing even if a Pact test requres a State change which is missing.
<br>
`@BeforeEach` - Runs methods before each test.
<br>
`@TestTemplate` - This is where Pact will run the tests using the contract.
<br>
`@State` - Some Pact tests require the API to be in a certain state before running the test. This annotation allows for state changes. 
<br>
<br>
I have also implemented Web security config for Bearer token authorization (see Authorization chapter below)

## Consumer Mock
This is where the consumer tests are written and tested against a mock API. 
A `@Service` client has been implemented to serve the mock server requests using a rest template:
<br>

    @Service
    public class ItemServiceClient {
    @Autowired
    private RestTemplate restTemplate;

    @Value("${serviceClients.inventory.baseUrl}")
    private String baseUrl;

    /**
    * Post an item to rest template
    * 
    * @param itemMap - Json object
    * @return response entity with Item type
    * @throws JsonProcessingException
    */
    public ResponseEntity<Item> postItem(ObjectNode itemMap) throws JsonProcessingException {
        ResponseEntity<Item> entity = restTemplate.exchange(baseUrl + "/api/items", HttpMethod.POST,
            new HttpEntity<>(itemMap, auth()), Item.class);
        return entity;
    }

The data model is a simple `@Data` Lombok DTO which allows to easily map to an object without needed to manually define setters and getters. 
<br>

    @Data
    public class Item {
    private final String id;
    private final String name;
    private final int quantity;
    private final float price;
    }

None of the data is being stored so there is no need for a repository on the consumer side. 

The Pact tests are created as a `@SpringBootTest` which extends the `PactConsumerTestExt` class. 
In order to create the test, first a `@Pact` needs to be created which builds a domain specific language using `PactDslWithProvider`. 
<br>

    @Pact(consumer = "ItemsCatalogue")
    public RequestResponsePact singleItem(PactDslWithProvider builder) {
        return builder
                .given("item with valid ID and body exists", "id",
                        VALID_ID, "name", VALID_NAME, "quantity", VALID_QUANTITY, "price", VALID_PRICE)
                .uponReceiving("get item with valid ID")
                .path("/api/items/" + VALID_ID)
                .matchHeader("Authorization", "Bearer [a-zA-Z0-9=\\+/]+", "Bearer AAABd9yHUjI=")
                .willRespondWith()
                .status(200)
                .headers(itemService.getJsonHeader())
                .body(
                        new PactDslJsonBody()
                                .stringType("id", VALID_ID)
                                .stringType("name", VALID_NAME)
                                .integerType("quantity", VALID_QUANTITY)
                                .numberType("price", VALID_PRICE))
                .toPact();
    }

Then a test is run against the Pact on the consumer side to ensure the consumer side mock server is returning the expected result.
<br>

    @Test
    @PactTestFor(pactMethod = "singleItem", pactVersion = PactSpecVersion.V3)
    void testSingleItem(MockServer mockServer) {
        itemService.setBaseUrl(mockServer.getUrl());
        Item item = itemService.getItem(VALID_ID).getBody();
        assertThat(item,
                is(equalTo(new Item(VALID_ID, VALID_NAME, VALID_QUANTITY, VALID_PRICE))));
        assertThat(
                itemService.getItem(VALID_ID).getHeaders().get("Content-Type")
                        .contains(JSON_STRING),
                is(true));
        assertThat(itemService.getItem(VALID_ID).getStatusCode().value(),
                is(equalTo(200)));
    }

## Pact broker
The Pact broker allows the consumer to pass the contract to the provider. For this project, the Pact broker will be hosted locally on port 9292. 
The Pact broker can also be used to create web hooks to automatically trigger tests. 
**The login for the Pact broker is set to admin/password
**
<!-- picture of pact broker -->
## API End points
All API end points can also be found in the API reference which is hosted on `inventory_api` Spring boot webapp on `/api`.

The API end points are: 

<table>
    <tr>
        <th><strong>Type</strong></th>
        <th><strong>Path</strong></th>
        <th><strong>Body</strong></th>
        <th><strong>Response</strong></th>
        <th><strong>HTTP Status</strong></th>
    </tr>
    <tr>
        <td>POST</td>
        <td>/api/items</td>
        <td>{"name": "abc", "quantity": 123, "price": 1.23}</td>
        <td>{
            "id": "b5df2750-14c9-4cd5-8c90-eb851f51ac74",
            "name": "abc",
            "quantity": 123,
            "price": 1.23
            }</td>
        <td>201 - Created<br>400 - Bad request</td>
    </tr>
    <tr>
        <td>GET</td>
        <td>/api/items/{id}</td>
        <td></td>
        <td>{
            "id": "b5df2750-14c9-4cd5-8c90-eb851f51ac74",
            "name": "abc",
            "quantity": 123,
            "price": 1.23
            }</td>
        <td>200 - OK <br> 404 - Not found</td>
    </tr>
    <tr>
        <td>PUT</td>
        <td>/api/items/{id}</td>
        <td>{"name": "abc", "quantity": 88, "price": 1.23}</td>
        <td>{
            "id": "b5df2750-14c9-4cd5-8c90-eb851f51ac74",
            "name": "abc",
            "quantity": 88,
            "price": 1.23
            }</td>
        <td>200 - OK<br> 400 - Bad request<br> 404 - Not found</td>
    </tr>
    <tr>
        <td>DELETE</td>
        <td>/api/items/{id}</td>
        <td></td>
        <td></td>
        <td>204 - No Content<br> 404 - Not found</td>
    </tr>
</table>

## Contract tests
I have created a total of 25 contract tests on the consumer mock, these tests are then put into a Pact and sent to the provider to test against the real API. 
<br><br>
Below is a list of the test descriptions:

<ul>
<li><p><strong>Add a item with no auth token </strong>- given item with valid ID does not exist</p></li>
<li><p><strong>Add item with empty body </strong>- given adding an item with an empty request body should be rejected</p></li>
<li><p><strong>Add item with invalid name </strong>- given adding an item with invalid name should be rejected</p></li>
<li><p><strong>Add item with invalid name key</strong> - given adding a item with an invalid name should be rejected</p></li>
<li><p><strong>Add item with invalid price </strong>- given adding a item with an invalid price should be rejected</p></li>
<li><p><strong>Add item with invalid price key </strong>- given adding a item with an invalid price should be rejected</p></li>
<li><p><strong>Add item with invalid quantity </strong>- given adding an item with invalid quantity should be rejected</p></li>
<li><p><strong>Add item with invalid quantity key </strong>- given adding a item with an invalid quantity should be rejected</p></li>
<li><p><strong>Add valid item </strong>- given item with valid ID does not exist</p></li>
<li><p><strong>Delete a item with no auth token</strong> - given item with valid ID exists</p></li>
<li><p><strong>Delete item with invalid ID </strong>- given delete an item with invalid ID</p></li>
<li><p><strong>Delete item with valid ID</strong> - given item with valid ID exists</p></li>
<li><p><strong>Edit a item with no auth token</strong> - given item with valid ID exists</p></li>
<li><p><strong>Edit item with invalid ID </strong>- given editing an item with invalid ID should be rejected</p></li>
<li><p><strong>Edit item with invalid name</strong> - given item with valid ID exists</p></li>
<li><p><strong>Edit item with invalid name key</strong> - given item with valid ID exists</p></li>
<li><p><strong>Edit item with invalid price</strong> - given item with valid ID exists</p></li>
<li><p><strong>Edit item with invalid price key</strong> - given item with valid ID exists</p></li>
<li><p><strong>Edit item with invalid quantity</strong> - given item with valid ID exists</p></li>
<li><p><strong>Edit item with invalid quantity key </strong>- given item with valid ID exists</p></li>
<li><p><strong>Edit item with valid data </strong>- given item with valid ID and body exists</p></li>
<li><p><strong>Editing an item with an empty body should be rejected</strong> - given item with valid ID exists</p></li>
<li><p><strong>Get a item with no auth token </strong>- given item with valid ID exists</p></li>
<li><p><strong>Get item with invalid ID </strong>- given item with invalid ID does not exist</p></li>
<li><p><strong>Get item with valid ID </strong>- given item with valid ID and body exists</p></li>
</ul>

## Authorization
Most real world API endpoints will need some kind of authroization, so i have implemented bearer authorization to the API endpoint and to the contract tests. The bearer token is put into the header of each request for each test under `Authorization`

<br>

On the **Consumer** side i have added a method which will get the current time into a 64byte long and then encode it into a string - this will be used as the bearer token. The method will then create a header and add the bearer token under the `Authorization` part of the header:

    /**
    * Sets a bearer authorization header value with an encoded Base64 Long (to
    * String)
    * 
    * @return header
    */
    private HttpHeaders auth() {
        HttpHeaders headers = new HttpHeaders();
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(System.currentTimeMillis());
        headers.setBearerAuth(Base64.getEncoder().encodeToString(buffer.array()));
        return headers;
    }

This header will then be added to the `HttpEntity` of each request method. 

Each test then has a `matchHeader` added to the DSL builder to verify the token by passing a static token value and a regex.

    .matchHeader("Authorization", "Bearer [a-zA-Z0-9=\\+/]+", "Bearer AAABd9yHUjI=")

On the **Provider** side i have implemented an authorization filter which overrides the `doFilterInternal` method of `OncePerRequestFilter` this method checks if the token is valid - if the token is over 1 hour long then it will become invalid. If the token is valid then it will set the context authentication to a new pre-authenticated user. 
<br>

    /**
    * Override the doFilterInternal method to set the authentication if the Bearer
    * token is valid
    */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (tokenValid(header)) {
        SecurityContextHolder.getContext().setAuthentication(new PreAuthenticatedAuthenticationToken("user", header));
        filterChain.doFilter(request, response);
        } else {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

In order to validate the token, the token will have to be decoded to get the timestamp and then check if the timestamp is less than 1 hour old. 
<br>

    /**
    * Method to check if the Bearer token is valid, the token is decoded into a
    * timestamp and then the time stamp is used to ensure the token isnt over one
    * hour old
    * 
    * @param header
    * @return
    */
    private boolean tokenValid(String header) {
    boolean hasBearerToken = StringUtils.isNotEmpty(header) && header.startsWith("Bearer ");
    if (hasBearerToken) {
        String token = header.substring("Bearer ".length());
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(Base64.getDecoder().decode(token));
        buffer.flip();
        long timestamp = buffer.getLong();
        return System.currentTimeMillis() - timestamp <= ONE_HOUR;
    } else {
        return false;
    }
    }

I have also setup websecurity filter chain for the security matcher and to call the new filter class before the filter takes place, this will ensure the bearer is valid. 
<br>

    /**
    * Filter chain for the API to ensure the Bearer token is valid
    * 
    * @param http
    * @return
    * @throws Exception
    */
    @Bean
    @Order(2)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/**")
            .csrf(AbstractHttpConfigurer::disable)
            .addFilterBefore(new BearerAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

However the contract test passes in a static bearer token which will be invalid so i have done some request injection which gets the header before the request is sent and it set the bearer to a new and valid token:
<br>

    @TestTemplate
    @ExtendWith(PactVerificationSpringProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context, HttpRequest request) throws ProtocolException {
        if (request.containsHeader("Authorization")) {
        request.setHeader("Authorization", "Bearer " + generateToken());
        }
        context.verifyInteraction();
    }

    private static String generateToken() {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(System.currentTimeMillis());
        return Base64.getEncoder().encodeToString(buffer.array());
    }

## Running the program
### Pre-requisites 
pre reqs
### 1. Launch Pact broker
launch pact broker
### 2. Run Consumer first to generate contracts
run consumer
### 3. View contract on Pact broker
view contract
### 4. Run tests on Provider
run tests
### 5. View result on Pact broker
view results
### 6. (optional) Setup Jenkins Pipeline
setup jenkins pipeline

## Observations

## Future improvments 


