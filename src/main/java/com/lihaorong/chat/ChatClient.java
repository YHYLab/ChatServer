package com.lihaorong.chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ChatClient {
	/**
	 *  테스트 방법 : ChatServer main메서드 실행 후 ChatClient 메인 메서드 실행.
	 *  ChatClient 실행 입력창에서 아래 데이터 입력.
	 *  테스트 데이터 :
	 *  00203194 번은 로컬에서 블록킹 유저 데이터로 등록.
	 *  
	 	{"action": "initChannel", "player_id":"00203192", "name": "cus_a"}
		{"action":"chat","player_id":"00203192", "name": "cus_a", "msg" : "Hello","channel_id":"1"}
		{"action":"chat","player_id":"00203192", "name": "cus_a", "msg" : "Hi","channel_id":"1"}
		{"action":"exchangeChannel","player_id":"00203192", "name": "cus_a", "new_channel_id" : "2","channel_id":"1"}
		{"action":"chat","player_id":"00203192", "name": "cus_a", "msg" : "Hi","channel_id":"2"}
		
		
		{"action": "initChannel", "player_id":"00203193", "name": "cus_b"}
		{"action":"chat","player_id":"00203193", "name": "cus_b", "msg" : "Yo","channel_id":"1"}
		{"action":"chat","player_id":"00203193", "name": "cus_b", "msg" : "KK","channel_id":"1"}
		{"action":"exchangeChannel","player_id":"00203193", "name": "cus_b", "new_channel_id" : "2","channel_id":"1"}
		{"action":"chat","player_id":"00203193", "name": "cus_b", "msg" : "KK","channel_id":"2"}
		
		{"action": "initChannel", "player_id":"00203195", "name": "cus_c"}
		{"action":"chat","player_id":"00203195", "name": "cus_c", "msg" : "Hello","channel_id":"1"}
		{"action":"chat","player_id":"00203195", "name": "cus_c", "msg" : "Hi","channel_id":"1"}
		{"action":"exchangeChannel","player_id":"00203195", "name": "cus_c", "new_channel_id" : "2","channel_id":"1"}
		{"action":"chat","player_id":"00203195", "name": "cus_c", "msg" : "Hi","channel_id":"2"}
		{"action":"exchangeChannel","player_id":"00203195", "name": "cus_c", "new_channel_id" : "1","channel_id":"2"}
		
		
		{"action": "initChannel", "player_id":"00203196", "name": "cus_c"}
		{"action":"chat","player_id":"00203196", "name": "cus_c", "msg" : "Hello","channel_id":"1"}
		{"action":"chat","player_id":"00203196", "name": "cus_c", "msg" : "Hi","channel_id":"1"}
		{"action":"exchangeChannel","player_id":"00203196", "name": "cus_c", "new_channel_id" : "2","channel_id":"1"}
		{"action":"chat","player_id":"00203196", "name": "cus_c", "msg" : "Hi","channel_id":"2"}
		{"action":"exchangeChannel","player_id":"00203196", "name": "cus_c", "new_channel_id" : "1","channel_id":"2"}
		
	 * 
	 * 
	 */
	//static final String HOST = System.getProperty("host", "uni-relate1.neocyon.com");
	
	static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8092"));

    public static void main(String[] args) throws Exception {

    	EventLoopGroup group = new NioEventLoopGroup();
        try {
    	
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .handler(new ChatClientInitializer());

            // Start the connection attempt.
            Channel ch = b.connect(HOST, PORT).sync().channel();

            // Read commands from the stdin.
            ChannelFuture lastWriteFuture = null;
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
            for (;;) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                
                // Sends the received line to the server.
                lastWriteFuture = ch.writeAndFlush(line + "\r\n");

                // If user typed the 'bye' command, wait until the server closes
                // the connection.
                if ("bye".equals(line.toLowerCase())) {
                    ch.closeFuture().sync();
                    break;
                }
            }

            // Wait until all messages are flushed before closing the channel.
            if (lastWriteFuture != null) {
                lastWriteFuture.sync();
            }
        } finally {
            // The connection is closed automatically on shutdown.
            group.shutdownGracefully();
        }
    }
}
