# Real-time messaging API

APIs for a simple real-time messaging application. Developed with Spring Boot and Websocket.

## Setting up & Configurations

### Dependencies

* `JDK 21`
* `PostgreSQL` as the database
* An identity provider, preferrably `Auth0`

### Cloning the repository

```bash
git clone https://github.com/ducbrick/real-time-messaging-api.git
cd real-time-messaging-api/
```

### Configurations

#### Database

* Ensure an instance of `PostgreSQL` is running
* Configure your database using the provided schema, located at `sql/schema.sql`

#### Identity provider

This application uses `Auth0` with opaque token (specifically JWT) as its identity provider but configuring it to use another one is possible. 

Ensure you have your authorization server URL, application audience and userinfo endpoint.

#### Environment variables

Provide your database and authorization server information as environment variablas. For example:


```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/yourdatabase
export DATABASE_USERNAME=yourusername
export DATABASE_PASSWORD=yourpassword

export OAUTH2_ISSUER=https://youridp.auth0.com/
export OAUTH2_AUDIENCE=youraudience
export OAUTH2_USERINFO_ENDPOINT=/userinfo
```

### Running the application

```bash
./mvnw spring-boot:run
```

The application should now be running on port `8080`

## API Documentation

### REST APIs

This application conforms to the OpenAPI specifications. The documenation for the REST APIs can be discovered at the `/v3/api-docs` endpoint.

Additionally, Swagger UI is also available at the `/swagger-ui/index.html` endpoint.

### Websocket

#### Handshake

This application provides WebSocket support with `SockJS` fallback at the `/msg` endpoint. 

The `SockJS` client begins by sending `GET /info` to obtain basic information from the server. After that, it must decide what transport to use. If possible, WebSocket is used. If not, in most browsers, there is at least one HTTP streaming option. If not, then HTTP (long) polling is used.

Clients with WebSocket support can initiate the handshake at the `/msg/websocket` endpoint.

#### Protocol

This application uses [STOMP](https://stomp.github.io/stomp-specification-1.2.html) as the WebSocket sub-protocol.

#### API Documentation

The documentation for this application's STOMP over WebSocket API follows the AsyncAPI specifications.

The AsyncAPI file can be found at `asyncapi.yaml`.
