package ru.anov.qzproject.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.Socket;

import ru.anov.qzproject.interfaces.OnCommandListener;
import ru.anov.qzproject.utils.APIHandler;




public class Client {

	private int port;
	private String host;
	private Channel channel;
	private EventLoopGroup group;
	private Bootstrap bootstrap;
	private OnCommandListener onCommandListener;
	private String id;
	private String rid;
	private String themeId;
	
	public Client(int port, String host, OnCommandListener onCommandListener, String id, String rid, String themeId){
		this.port = port;
		this.host = host;
		this.onCommandListener = onCommandListener;
		this.id = id;
		this.rid = rid;
		this.themeId = themeId;
	}
	
	public boolean init(){
		try{
			group = new NioEventLoopGroup();
			bootstrap = new Bootstrap()
				.group(group)
				.channel(NioSocketChannel.class)
				.handler(new ClientInitializer(onCommandListener));
			
			channel = bootstrap.connect(host, port).sync().channel();
			return true;
		}catch(Exception e){
			APIHandler.error = e.toString();
			return false;
		}
	}
	
	public void sendRequest(boolean isRandom, String qIds){
		if(this == null){
			return;
		}
		if(isRandom){
			channel.writeAndFlush("RAND#" + id + "_" + themeId + "_" + qIds + "\n");
		}else{
			channel.writeAndFlush("REQUEST#" + id + "_" + rid + "_" + themeId + "_" + qIds + "\n");
		}
	}
	
	public void setRId(String rid){
		this.rid = rid;
	}
	
	public void next(){
		if(channel == null){
			return;
		}
		channel.writeAndFlush("NEXT#" + id + "_" + rid + "\n");
	}
	
	public void answer(String answer, int time, int round){
		if(channel == null){
			return;
		}
		channel.writeAndFlush("ANS#" + id + "_" + rid + "_" + answer + "_" + time + "_" + round + "\n");
	}
	
	public void finalize(){
		if(channel == null){
			return;
		}
		channel.writeAndFlush("FINALIZE#" + id + "_" + rid + "\n");
	}
	
	public void error(){
		if(channel == null){
			return;
		}
		if(rid != null){
			channel.writeAndFlush("ERR#" + id + "_" + rid + "\n");
		}
	}
	
	public void close(){
		if(channel == null || group == null){
			return;
		}
		channel.close();
		group.shutdownGracefully();
		channel = null;
		group = null;
	}
	
	
}
