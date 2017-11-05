Demo-Akka-HTTP
==========

The project provides demo of both [Akka-HTTP](https://doc.akka.io/docs/akka-http/current/scala/http/) client and server applications that use simple `DemoEntity` model for demonstration purposes.
JSON serialization and deserialization implemented using [spray-json](https://github.com/spray/spray-json).
Dependency injection used [Scala extensions for Google Guice](https://github.com/codingwell/scala-guice).

----------


Akka-HTTP Server application
----------
The server application provides RESTful API endpoints to work with `DemoEntity` model.
> **Endpoints:**
>
> - **POST** `v1/demo-server/entities` accepts body with `application/json` content type and valid JSON representation of `DemoEntity` to create the entity. Response is JSON of the created entity with id.
> - **GET** `v1/demo-server/entities/${id}` returns entity by id in JSON format.
> - **PUT** `v1/demo-server/entities/${id}` accepts body with `application/json` content type and valid JSON representation of `DemoEntity` to update the entity by id. Response is JSON of the updated entity.
> - **DELETE** `v1/demo-server/entities${id}` removes entity by id.

For valid requests the server returns valid responses in JSON format.
The server is not able to process incorrect request and will return an error response.
> **Errors:**
> - (**BadRequest** (code 400), error message) - for incorrect client's requests (unsupported JSON format, non-existing id, ...)
> - (**InternalServerError** (code 500), error message)  - for internal server errors.

The server configuration exists in `server.conf` file.
You can change `host` and `port` parameters in the configuration.
```js
server {
  host = "localhost"
  port = 8080
}
```

Tests of the server routes available in `DemoHttpServerTest`.
To run the server application use `DemoHttpServerApp`.

----------

Akka-HTTP Client application
----------

The client application provides http-client to work with DemoHttpServer endpoints.
The client allows to manage `DemoEntity` objects through RESTful API using `create`, `read`, `update` and `delete` methods.
The client configuration exists in `client.conf` file.
You can change `DemoApiEndpoint` parameter in the configuration.
```js
client {
  DemoApiEndpoint = "http://localhost:8080/v1/demo-server"
}
```

Tests of the client available in `DemoHttpClientTest`.
To run the client application use `DemoHttpClientApp`, output should be like:
```js
	REQUEST: HttpRequest(HttpMethod(POST),http://localhost:8080/v1/demo-server/entities,List(),HttpEntity.Strict(application/json,{"int":1,"long":1024,"string":"0xABC"}),HttpProtocol(HTTP/1.1))
	RESPONSE: HttpResponse(200 OK,List(Server: akka-http/10.0.10, Date: Sat, 04 Nov 2017 11:02:00 GMT),HttpEntity.Strict(application/json,{"id":"b4fea167-44be-4cb1-a4d2-1a6370731ad6","int":1,"long":1024,"string":"0xABC"}),HttpProtocol(HTTP/1.1))
create result: Success(DemoEntity(Some(b4fea167-44be-4cb1-a4d2-1a6370731ad6),Some(1),Some(1024),Some(0xABC),None))

	REQUEST: HttpRequest(HttpMethod(PUT),http://localhost:8080/v1/demo-server/entities/b4fea167-44be-4cb1-a4d2-1a6370731ad6,List(),HttpEntity.Strict(application/json,{"id":"b4fea167-44be-4cb1-a4d2-1a6370731ad6","int":1,"long":1024,"string":"0xABC"}),HttpProtocol(HTTP/1.1))
	RESPONSE: HttpResponse(200 OK,List(Server: akka-http/10.0.10, Date: Sat, 04 Nov 2017 11:02:02 GMT),HttpEntity.Strict(application/json,{"id":"b4fea167-44be-4cb1-a4d2-1a6370731ad6","int":1,"long":1024,"string":"0xABC"}),HttpProtocol(HTTP/1.1))
readNew result: Success(DemoEntity(Some(b4fea167-44be-4cb1-a4d2-1a6370731ad6),Some(1),Some(1024),Some(0xABC),None))

	REQUEST: HttpRequest(HttpMethod(PUT),http://localhost:8080/v1/demo-server/entities/b4fea167-44be-4cb1-a4d2-1a6370731ad6,List(),HttpEntity.Strict(application/json,{"bigDecimal":3.14,"string":"0xABC","long":1024,"id":"b4fea167-44be-4cb1-a4d2-1a6370731ad6","int":1}),HttpProtocol(HTTP/1.1))
	RESPONSE: HttpResponse(200 OK,List(Server: akka-http/10.0.10, Date: Sat, 04 Nov 2017 11:02:02 GMT),HttpEntity.Strict(application/json,{"bigDecimal":3.14,"string":"0xABC","long":1024,"id":"b4fea167-44be-4cb1-a4d2-1a6370731ad6","int":1}),HttpProtocol(HTTP/1.1))
update result: Success(DemoEntity(Some(b4fea167-44be-4cb1-a4d2-1a6370731ad6),Some(1),Some(1024),Some(0xABC),Some(3.14)))

	REQUEST: HttpRequest(HttpMethod(GET),http://localhost:8080/v1/demo-server/entities/b4fea167-44be-4cb1-a4d2-1a6370731ad6,List(),HttpEntity.Strict(none/none,ByteString()),HttpProtocol(HTTP/1.1))
	RESPONSE: HttpResponse(200 OK,List(Server: akka-http/10.0.10, Date: Sat, 04 Nov 2017 11:02:03 GMT),HttpEntity.Strict(application/json,{"bigDecimal":3.14,"string":"0xABC","long":1024,"id":"b4fea167-44be-4cb1-a4d2-1a6370731ad6","int":1}),HttpProtocol(HTTP/1.1))
readUpd result: Success(DemoEntity(Some(b4fea167-44be-4cb1-a4d2-1a6370731ad6),Some(1),Some(1024),Some(0xABC),Some(3.14)))

	REQUEST: HttpRequest(HttpMethod(DELETE),http://localhost:8080/v1/demo-server/entities/b4fea167-44be-4cb1-a4d2-1a6370731ad6,List(),HttpEntity.Strict(application/json,{"bigDecimal":3.14,"string":"0xABC","long":1024,"id":"b4fea167-44be-4cb1-a4d2-1a6370731ad6","int":1}),HttpProtocol(HTTP/1.1))
	RESPONSE: HttpResponse(200 OK,List(Server: akka-http/10.0.10, Date: Sat, 04 Nov 2017 11:02:06 GMT),HttpEntity.Strict(application/json,{"bigDecimal":3.14,"string":"0xABC","long":1024,"id":"b4fea167-44be-4cb1-a4d2-1a6370731ad6","int":1}),HttpProtocol(HTTP/1.1))
delete result: Success(DemoEntity(Some(b4fea167-44be-4cb1-a4d2-1a6370731ad6),Some(1),Some(1024),Some(0xABC),Some(3.14)))
```

----------
