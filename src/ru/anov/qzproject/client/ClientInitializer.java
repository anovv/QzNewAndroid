package ru.anov.qzproject.client;

import ru.anov.qzproject.interfaces.OnCommandListener;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import android.content.Context;


public class ClientInitializer extends ChannelInitializer<SocketChannel>{
	
	private OnCommandListener onCommandListener;
	private Context context;
	
	public ClientInitializer(OnCommandListener onCommandListener){
		this.onCommandListener = onCommandListener;
	}

	@Override
	protected void initChannel(SocketChannel arg0) throws Exception {
		ChannelPipeline pipeline = arg0.pipeline();
		
		pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
		pipeline.addLast("decoder", new StringDecoder());
		pipeline.addLast("encoder", new StringEncoder());
		pipeline.addLast("handler", new ClientHandler(onCommandListener));
	}			
}
