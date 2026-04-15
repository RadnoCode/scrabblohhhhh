package com.kotva.lan.test;

import java.net.Socket;

import com.kotva.lan.ClientConnection;

public class TemporaryClientTest {
    public static void main(String[] args) throws Exception {
        System.out.println("Client: connecting to localhost:9999...");
        
        Socket socket = new Socket("localhost", 9999);  // 连接 Server
        
        System.out.println("Client: connected!");
        
        ClientConnection host = new ClientConnection("host", socket);
        
        // 发一条消息给 Server
        // （先随便发一个 JoinSessionMessage，我们下次实现那个类之前先用匿名类测试）
        // host.sendMessage(new JoinSessionMessage("Alice"));
        
        Thread.sleep(3000);
        host.disconnect();
    }
}
