package simpleChat;

//Ref: https://waylau.com/netty-chat/
//     https://github.com/waylau/netty-4-user-guide
//	   https://github.com/waylau/netty-4-user-guide-demos/blob/master/netty4-demos/src/main/java/com/waylau/netty/demo/simplechat/SimpleChatServerHandler.java
//	   http://www.jianshu.com/p/216881b0573d
//	   https://github.com/levyc/NettyDemo/tree/master/src/chat

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class ChatServerHandler extends SimpleChannelInboundHandler<String> 
{
	/**
	 * A thread-safe Set  Using ChannelGroup, you can categorize Channels into a meaningful group.
	 * A closed Channel is automatically removed from the collection,
	 */
	public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception
	{
		Channel incoming = ctx.channel();
		
		// Broadcast a message to multiple Channels
		channels.writeAndFlush("[SERVER] - " + incoming.remoteAddress() + " is comming.\n");
		channels.add(ctx.channel());
	}
	
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception
	{
		Channel incoming = ctx.channel();
		
		// Broadcast a message to multiple Channels
		channels.writeAndFlush("[SERVER] - " + incoming.remoteAddress() + " left.\n");
		// A closed Channel is automatically removed from ChannelGroup,
		// so there is no need to do "channels.remove(ctx.channel());"
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception
	{
		Channel incoming = ctx.channel();
		for(Channel channel : channels) 
		{
			if(channel != incoming)
			{
				channel.writeAndFlush("[" + incoming.remoteAddress() + "] " + s + "\n");
			}
			else
			{
				channel.writeAndFlush("[you] " + s + "\n");
			}
		}
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception
	{
		Channel incoming = ctx.channel();
		System.out.println("ChatClient: " + incoming.remoteAddress() + " online");
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception
	{
		Channel incoming = ctx.channel();
		System.out.println("ChatClient: " + incoming.remoteAddress() + " offline");
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
	{
		Channel incoming = ctx.channel();
		System.out.println("ChatClient: " + incoming.remoteAddress() + " abnormal" );
		//Close connection when exception occurs
		cause.printStackTrace();
		ctx.close();
	}
}
