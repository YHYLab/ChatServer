package com.yhy.chat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;

import io.netty.bootstrap.ServerBootstrap;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class ChatServer {
	final static Logger logger = LoggerFactory
			.getLogger(ChatServer.class.getName());
	
    public static void main(String[] args) throws Exception {

    	logger.debug("chatserver start!");
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:applicationContext.xml");
		//ApplicationContext context = new AnnotationConfigApplicationContext("SpringConfig.class");
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
        	Properties prop = new Properties();
        	Resource resource = context.getResource("classpath:config.properties");
        	
        	InputStream is = resource.getInputStream();
    		if (is != null) {
    			prop.load(new InputStreamReader(is, "UTF-8"));
    		}
    		int PORT = Integer.parseInt(prop.getProperty("port"));
    		
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new ChatServerInitializer());

            b.bind(PORT).sync().channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
