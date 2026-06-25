package com.foodorderingapp.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class StompClient {
    private static final String TAG = "StompClient";
    
    private final String url;
    private final Map<String, String> connectHeaders;
    private WebSocket webSocket;
    private final Map<String, Subscription> subscriptions = new HashMap<>();
    private ConnectionListener connectionListener;
    private boolean isConnected = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    public interface ConnectionListener {
        void onConnected();
        void onDisconnected();
        void onError(Throwable t);
    }
    
    public interface MessageListener {
        void onMessage(String payload);
    }
    
    private static class Subscription {
        String topic;
        MessageListener listener;
        
        Subscription(String topic, MessageListener listener) {
            this.topic = topic;
            this.listener = listener;
        }
    }
    
    public StompClient(String url, Map<String, String> connectHeaders) {
        this.url = url;
        this.connectHeaders = connectHeaders != null ? connectHeaders : new HashMap<>();
    }
    
    public void setConnectionListener(ConnectionListener listener) {
        this.connectionListener = listener;
    }
    
    public synchronized void connect() {
        if (isConnected || webSocket != null) return;
        
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
                
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d(TAG, "WebSocket Opened, sending STOMP CONNECT");
                sendConnectFrame();
            }
            
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "Received message: " + text);
                parseFrame(text);
            }
            
            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "WebSocket Closed: " + reason);
                handleDisconnect();
            }
            
            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e(TAG, "WebSocket Failure", t);
                handleError(t);
            }
        });
    }
    
    public synchronized void disconnect() {
        if (webSocket != null) {
            sendDisconnectFrame();
            webSocket.close(1000, "Disconnecting");
            webSocket = null;
        }
        handleDisconnect();
    }
    
    public synchronized void subscribe(String topic, MessageListener listener) {
        String id = "sub-" + UUID.randomUUID().toString().substring(0, 8);
        subscriptions.put(id, new Subscription(topic, listener));
        if (isConnected) {
            sendSubscribeFrame(id, topic);
        }
    }
    
    private void sendConnectFrame() {
        StringBuilder frame = new StringBuilder();
        frame.append("CONNECT\n");
        frame.append("accept-version:1.1,1.2\n");
        frame.append("heart-beat:10000,10000\n");
        for (Map.Entry<String, String> entry : connectHeaders.entrySet()) {
            frame.append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
        }
        frame.append("\n");
        frame.append("\u0000");
        send(frame.toString());
    }
    
    private void sendDisconnectFrame() {
        String frame = "DISCONNECT\n\n\u0000";
        send(frame);
    }
    
    private void sendSubscribeFrame(String id, String topic) {
        StringBuilder frame = new StringBuilder();
        frame.append("SUBSCRIBE\n");
        frame.append("id:").append(id).append("\n");
        frame.append("destination:").append(topic).append("\n");
        frame.append("ack:auto\n");
        frame.append("\n");
        frame.append("\u0000");
        send(frame.toString());
    }
    
    private void send(String frame) {
        if (webSocket != null) {
            webSocket.send(frame);
        }
    }
    
    private void handleDisconnect() {
        if (isConnected) {
            isConnected = false;
            mainHandler.post(() -> {
                if (connectionListener != null) connectionListener.onDisconnected();
            });
        }
    }
    
    private void handleError(Throwable t) {
        isConnected = false;
        webSocket = null;
        mainHandler.post(() -> {
            if (connectionListener != null) connectionListener.onError(t);
        });
    }
    
    private void parseFrame(String text) {
        if (text == null || text.trim().isEmpty()) return;
        
        String[] parts = text.split("\n\n", 2);
        if (parts.length < 1) return;
        
        String headerPart = parts[0];
        String bodyPart = parts.length > 1 ? parts[1] : "";
        if (bodyPart.endsWith("\u0000")) {
            bodyPart = bodyPart.substring(0, bodyPart.length() - 1);
        }
        
        String[] headerLines = headerPart.split("\n");
        if (headerLines.length == 0) return;
        
        String command = headerLines[0].trim();
        Map<String, String> headers = new HashMap<>();
        for (int i = 1; i < headerLines.length; i++) {
            String line = headerLines[i];
            int colonIdx = line.indexOf(':');
            if (colonIdx > 0) {
                headers.put(line.substring(0, colonIdx).trim(), line.substring(colonIdx + 1).trim());
            }
        }
        
        if ("CONNECTED".equals(command)) {
            Log.i(TAG, "STOMP Connected!");
            isConnected = true;
            mainHandler.post(() -> {
                if (connectionListener != null) connectionListener.onConnected();
            });
            for (Map.Entry<String, Subscription> entry : subscriptions.entrySet()) {
                sendSubscribeFrame(entry.getKey(), entry.getValue().topic);
            }
        } else if ("MESSAGE".equals(command)) {
            String subscriptionId = headers.get("subscription");
            Subscription sub = subscriptions.get(subscriptionId);
            if (sub != null) {
                final String finalBody = bodyPart;
                mainHandler.post(() -> sub.listener.onMessage(finalBody));
            }
        } else if ("ERROR".equals(command)) {
            Log.e(TAG, "STOMP Error: " + bodyPart);
        }
    }
}
