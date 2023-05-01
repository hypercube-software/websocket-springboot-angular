# WebSocket with SpringBoot and Angular 10
**Table of content**

[1 WebSocket with SpringBoot and Angular 10](#websocket-with-springboot-and-angular-10)  
&nbsp;&nbsp;&nbsp;&nbsp;[1.1 Project layout](#11-project-layout)  
&nbsp;&nbsp;&nbsp;&nbsp;[1.2 Websocket server in SpringBoot](#12-websocket-server-in-springboot)  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[1.2.1 WebSocket server](#121-websocket-server)  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[1.2.2 Object serialization](#122-object-serialization)  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[1.2.3 Endpoint](#123-endpoint)  
&nbsp;&nbsp;&nbsp;&nbsp;[1.3 Websocket client in Angular](#13-websocket-client-in-angular)  
[2 Build the project](#2-build-the-project)  
[3 Development mode](#3-development-mode)  

## 1.1 Project layout

The project is a simple SpringBoot microservice containing an Angular application. The maven project contains two projects in one:

- `src/main/java` contains the SpringBoot microservice
- `src/main/angular ` contains the Angular application (you typically open VSCode here)

In `src/main/angular/angular.json` we specify an output directory matching the SpringBoot convention (`static`):

```json
"outputPath": "dist/static",
```

Then in the maven `pom.xml` we add this directory as a resource. This will embed the whole directory in the final JAR.

```xml
<build>
	<resources>
		<resource>
			<directory>${project.basedir}/src/main/angular/dist</directory>
		</resource>
	</resources>
```

## 1.2 Websocket server in SpringBoot

The [official tutorial from spring](https://spring.io/guides/gs/messaging-stomp-websocket/) uses [STOMP](http://stomp.github.io/) and [SockJS](https://github.com/sockjs/sockjs-client) on top of the WebSocket API. This can be useful in some cases but that's not what we are going to do here. We will use only the WebSocket API in SpringBoot without STOMP or SockJS. This mean we expect to have a browser with WebSocket client API.

### 1.2.1 WebSocket server

We just have to inherit from class `org.springframework.web.socket.handler.TextWebSocketHandler`

```java
@Component
public class WebSocketHandler extends TextWebSocketHandler {

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		
	}
}
```

### 1.2.2 Object serialization

We use the `Jackson ObjectMapper` provided by SpringBoot runtime.

```java
@Autowired
private ObjectMapper objectMapper;
```
Our model is very simple, we have a `HelloRequest` and a `HelloResponse`.

### 1.2.3 Endpoint

We declare the WebSocket endpoint inheriting from `org.springframework.web.socket.config.annotation.WebSocketConfigurer`

```java
@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {

	@Autowired
    protected WebSocketHandler webSocketHandler;
	
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(webSocketHandler, "/websocket-server");		
	}
}
```

The endpoint will be `ws://host:port/websocket-server`

## 1.3 Websocket client in Angular

In Angular we use the browser API for Websocket in our Application component:

```typescript
import { Component } from '@angular/core';
import { HelloRequest } from './model/HelloRequest';
import { HelloResponse } from './model/HelloResponse';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.less']
})
export class AppComponent {
  title: string = 'websocket-app';
  ws: any = null;
  messages: Array<HelloResponse> = [];

  onWebSocketOpen() {
    console.log("Websocket connected");
  }

  onWebSocketMessage(evt) {
    let received_msg:HelloResponse = <HelloResponse>JSON.parse(evt.data);
    console.log("Message is received:" + received_msg.message);
    this.messages.push(received_msg);
  }

  onWebSocketclose() {
    console.log("Websocket connection is closed...");
    this.ws = null;
  }

  onConnect() {
    if ("WebSocket" in window) {
      console.log("Connecting...");
      let url = "ws://" + window.location.host + "/websocket-server";
      console.log("Connect to " + url);
      this.ws = new WebSocket(url);
      this.ws.onopen = this.onWebSocketOpen.bind(this);
      this.ws.onmessage = this.onWebSocketMessage.bind(this);
      this.ws.onclose = this.onWebSocketclose.bind(this);
    }
    else {
      alert("WebSocket NOT supported by your Browser!");
    }
  }
  onSend() {
    if (this.ws) {
      console.log("Send message");
      let msg:HelloRequest = new HelloRequest("The message");

      this.ws.send(JSON.stringify(msg));
    }
  }
}


```

# 2 Build the project

Build the angular application with npm (in `src/main/angular`):

```
npm run build
```

Then build the JAR with maven:

```
mvn clean install -Dmaven.test.skip=true
```

Then run the microservice with:

```
c:\java\jdk-11.0.2\bin\java -jar target\websocket-server-0.0.1-SNAPSHOT.jar
```

Then go to [http://localhost:8080/](http://localhost:8080/)

# 3 Development mode

During development, you have to work differently:

- You run the SpringBoot service inside Eclipse in Debug, on port 8080
- You run at the same time the Angular server on 4200

We added a proxy setting in the angular server to redirect the Websocket endpoint `ws://localhost:4200/websocket-server` to `ws:://localhost:8080/websocket-server`, So there is no [CORS](https://developer.mozilla.org/fr/docs/Web/HTTP/CORS) issues.

See `src/main/angular/proxy.conf.json`

```json
{
    "/websocket-server/*": {
        "target": "ws://localhost:8080/",
        "secure": false,
        "logLevel": "debug",
        "ws": true
    }
}
```

Note that this configuration file is used because we changed the `src/main/angular/angular.json`:

```json
       "serve": {
          "options": {
            "proxyConfig": "proxy.conf.json"
          },
  
```

Note that when you debug the microservice in eclipse (on port 8080), the folder used to deliver the angular application is `src/main/angular/dist/static`. It may be not up-to-date, so always use the angular server on port 4200.



