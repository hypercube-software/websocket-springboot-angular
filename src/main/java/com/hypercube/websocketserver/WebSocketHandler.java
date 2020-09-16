package com.hypercube.websocketserver;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypercube.websocketserver.messages.HelloRequest;
import com.hypercube.websocketserver.messages.HelloResponse;

@Component
public class WebSocketHandler extends TextWebSocketHandler {
	private Logger log =  Logger.getLogger(WebSocketHandler.class.getName());
	
	@Autowired
    private ObjectMapper objectMapper;
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		log.info("afterConnectionEstablished: sessionId="+session.getId());
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		log.info("handleTextMessage: sessionId="+session.getId()+" Message="+message.getPayload());
		HelloRequest r = objectMapper.readValue(message.getPayload(),HelloRequest.class);
		sendMessage(session);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		log.info("afterConnectionClosed: sessionId="+session.getId());		
	}

	private void sendMessage(WebSocketSession session)  throws Exception {
		HelloResponse r = new HelloResponse();
		r.setMessage("The response");
		
		TextMessage message = new TextMessage(objectMapper.writeValueAsString(r));
		session.sendMessage(message);
	}	
}
