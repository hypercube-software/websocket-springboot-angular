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
