package com.kotva.lan.test;

import java.net.ServerSocket;
import java.net.Socket;

import com.kotva.lan.ClientConnection;

public class TemporaryServerTest {
    public static void main(String[] args) throws Exception {
        // 提示：服务端已经启动，正在本地 9999 端口等待客户端连接。
        System.out.println("Server: waiting for connection on port 9999...");
        
        // 创建服务端套接字，监听 9999 端口。
        ServerSocket serverSocket = new ServerSocket(9999);
        // 阻塞等待客户端接入；在有客户端连接之前，程序会停在这里。
        Socket socket = serverSocket.accept();  // 阻塞等待客户端
        
        // 走到这里说明已经有客户端成功连接到服务端。
        System.out.println("Server: client connected!");
        
        // 把底层 Socket 包装成项目里的 ClientConnection，便于统一管理收发与断开逻辑。
        ClientConnection client = new ClientConnection("player-b", socket);
        
        // 开始异步监听客户端发来的消息。
        client.startListening(
            // 每收到一条消息，就打印它的消息类型。
            msg -> System.out.println("Server received: " + msg.getType()),
            // 当客户端断开或连接关闭时，打印断开提示。
            () -> System.out.println("Server: client disconnected")
        );
        
        // 主线程暂停 5 秒，给客户端留一点发送消息的时间。
        Thread.sleep(5000);
        // 5 秒后由服务端主动断开当前客户端连接。
        client.disconnect();
        // 关闭服务端监听套接字，释放端口资源。
        serverSocket.close();
    }
}
