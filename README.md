# Real-time messaging API

APIs for a simple real-time messaging application. Developed with Spring Boot and Websocket.

## Setting up & Configurations

### Dependencies

* JDK 21
* PostgreSQL as the database
* An identity provider, preferrably Auth0

### Cloning the repository

```bash
git clone https://github.com/ducbrick/real-time-messaging-api.git
cd real-time-messaging-api/
```

### Configurations

#### Database

* Ensure an instance of PostgreSQL is running
* Configure your database using the provided schema, located at `sql/schema.sql`

#### Identity provider

This application uses Auth0 as its identity provider but configuring it to use another one is possible. 

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
