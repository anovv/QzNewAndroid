package ru.anov.qzproject.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.ArrayList;
import java.util.List;

import ru.anov.qzproject.interfaces.OnCommandListener;



import android.util.Log;


public class ClientHandler extends SimpleChannelInboundHandler<String>{
	
	private OnCommandListener onCommandListener; 
	
	public ClientHandler(OnCommandListener onCommandListener){
		this.onCommandListener = onCommandListener;
	}
	
	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
		onCommandListener.onError(3);
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext context, String message) throws Exception {
		String[] str = message.split("#");
		String command = str[0];
		String[] values = null;
		
		if(str.length > 1){
			values = str[1].split("_");
		}
		
		if(command.equals("QIDS")){
			List<String> ids = new ArrayList<String>();
			for(String id : values){
				ids.add(id);
			}
			onCommandListener.onQuestions(ids);
			return;
		}
		
		if(command.equals("NEXT")){
			onCommandListener.onNext();
			return;
		}
		
		if(command.equals("ANS")){
			String answer = values[0];
			int time = Integer.parseInt(values[1]);
			int round = Integer.parseInt(values[2]);
			onCommandListener.onAnswer(answer, time, round);
			return;
		}
		
		if(command.equals("RQIDS")){
			List<String> ids = new ArrayList<String>();
			String rid = values[values.length - 1];
			for(int i = 0; i < values.length - 1; i++){
				ids.add(values[i]);
			}
			onCommandListener.onRandom(rid, ids);
			return;
		}
		
		if(command.equals("ERR")){
			int errorCode = Integer.parseInt(values[0]);
			onCommandListener.onError(errorCode);
			return;
		}
		
		if(command.equals("FIN")){
			onCommandListener.onFinalize();
			return;
		}
	}
}
