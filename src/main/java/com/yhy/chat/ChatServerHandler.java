package com.yhy.chat;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.EvictingQueue;
import com.neocyon.bean.User;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class ChatServerHandler extends SimpleChannelInboundHandler<String> {
	final static Logger logger = LoggerFactory
			.getLogger(ChatServerHandler.class);

	public static List<Map<String, Object>> blockUsers = new ArrayList<Map<String, Object>>();
	public static final List<List<User>> rooms = new ArrayList<List<User>>();
	final int MAX_PEOPLE = 1000;
	final int MAX_CHAT_QUEUE = 20;
	final static int MAX_ROOM_SIZE = 1000;
	@SuppressWarnings("rawtypes")
	static final List<Queue> messagesQueues = new ArrayList<Queue>(
			MAX_ROOM_SIZE);

	public ChatServerHandler() {
		if (rooms.size() == 0) {
			for (int i = 0; i < MAX_ROOM_SIZE; i++) {
				List<User> room = new ArrayList<User>();
				rooms.add(room);
				Queue<JSONObject> messageQueue = EvictingQueue
						.create(MAX_CHAT_QUEUE);
				messagesQueues.add(messageQueue);
			}
		}
	}

	public static final ChannelGroup channels = new DefaultChannelGroup(
			GlobalEventExecutor.INSTANCE);

	@Override
	public void channelActive(final ChannelHandlerContext ctx) {
		channels.add(ctx.channel());
	}

	/*
	 * @Override public void channelActive(final ChannelHandlerContext ctx) { //
	 * Once session is secured, send a greeting and register the channel to the
	 * global channel // list so the channel received the messages from others.
	 * 
	 * ctx.pipeline().get(SslHandler.class).handshakeFuture().addListener( new
	 * GenericFutureListener<Future<Channel>>() { public void
	 * operationComplete(Future<Channel> future) throws Exception {
	 * ctx.writeAndFlush( "Welcome to " +
	 * InetAddress.getLocalHost().getHostName() + " secure chat service!\n");
	 * ctx.writeAndFlush( "Your session is protected by " +
	 * ctx.pipeline().get(SslHandler
	 * .class).engine().getSession().getCipherSuite() + " cipher suite.\n");
	 * 
	 * channels.add(ctx.channel()); } }); }
	 */

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// cause.printStackTrace();
	}

	/**
	 * 5.0 부터는 messageReceived 메서드를 오버라이딩한다. msg 는 json 형식
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) {
		// msg {id:00203192, name: "칼날의여왕", msg : " 좋은 아침입니다"}
		JSONParser parser = new JSONParser();
		int parserError = 0;
		Map<String, Object> m = null;

		if (StringUtils.isEmpty(msg)) {
			return;
		}
		try {
			// ctx.pipeline().
			m = (Map<String, Object>) parser.parse(msg);
		} catch (ParseException e) {
			logger.error("parser Error: ", e);
			parserError = 1;
			return;
		}

		String action = MapUtils.getString(m, "action");
		String playerId = MapUtils.getString(m, "player_id");

		disconnectBlockedUser(ctx, playerId);

		String nickName = MapUtils.getString(m, "name");
		String message = MapUtils.getString(m, "msg");
		String newChannel = Integer.toString(MapUtils.getIntValue(m,
				"new_channel_id", 0));
		String channelId = Integer.toString(MapUtils.getIntValue(m,
				"channel_id", 0));
		String allYn = Integer.toString(MapUtils.getIntValue(m, "all_yn", 0));

		if (StringUtils.isEmpty(playerId) || StringUtils.isEmpty(action)
				|| StringUtils.isEmpty(message)
				|| StringUtils.equals(channelId, "0")) {
			parserError = 1;
		}

		doAction(ctx, parserError, action, playerId, nickName, message,
				newChannel, channelId, allYn);
	}

	private void doAction(ChannelHandlerContext ctx, int parserError,
			String action, String playerId, String nickName, String message,
			String newChannel, String channelId, String allYn) {
		if (StringUtils.equals("initChannel", action)) {
			ininitChannel(ctx, action, playerId);
		} else if (StringUtils.equals("exchangeChannel", action)) {
			exchangeChannel(ctx, action, playerId, newChannel, channelId);
		} else if (StringUtils.equals("chat", action)) {
			chat(ctx, parserError, action, playerId, nickName, message,
					channelId);
		} else if (StringUtils.equals("itemShow", action)) {
			itemShow(action, playerId, nickName, message, channelId, allYn);
		} else if (StringUtils.equals("system", action)) {
			systemMessage(action, message);
		} else if (StringUtils.equals("health", action)) {
			checkHealth(ctx, action, channelId);
		} else if (StringUtils.equals("acount", action)) {
			acountPeople(ctx, action, channelId);
		}
	}

	private void disconnectBlockedUser(ChannelHandlerContext ctx,
			String playerId) {
		// 블록 사용자 일경우 채팅창 접속을 막는다.
		for (Map<String, Object> blockUser : blockUsers) {
			if (StringUtils.equals(playerId,
					MapUtils.getString(blockUser, "player_id"))) {
				ctx.close();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void acountPeople(ChannelHandlerContext ctx, String action,
			String channelId) {
		JSONObject obj = new JSONObject();
		obj.put("action", action);
		obj.put("channel_id", channelId);
		obj.put("msg", channels.size());
		ctx.channel().writeAndFlush(obj.toJSONString() + '\n');
	}

	@SuppressWarnings("unchecked")
	private void checkHealth(ChannelHandlerContext ctx, String action,
			String channelId) {
		JSONObject obj = new JSONObject();
		obj.put("action", action);
		obj.put("channel_id", channelId);
		obj.put("msg", 200);
		ctx.channel().writeAndFlush(obj.toJSONString() + '\n');
	}

	@SuppressWarnings("unchecked")
	private void systemMessage(String action, String message) {
		for (Channel c : channels) {
			JSONObject obj = new JSONObject();
			obj.put("action", action);
			obj.put("msg", message);
			c.writeAndFlush(obj.toJSONString() + '\n');
		}
	}

	private void itemShow(String action, String playerId, String nickName,
			String message, String channelId, String allYn) {
		if (Integer.parseInt(allYn) == 0) {
			List<User> room = (List<User>) rooms.get(Integer
					.parseInt(channelId) - 1);
			for (User user : room) {
				JSONObject obj = messageJson(action, playerId, nickName,
						message, channelId);
				user.getChannel().writeAndFlush(obj.toJSONString() + '\n');
			}
		} else {
			for (Channel c : channels) {
				JSONObject obj = messageJson(action, playerId, nickName,
						message, channelId);
				c.writeAndFlush(obj.toJSONString() + '\n');
			}
		}
	}

	@SuppressWarnings("unchecked")
	private JSONObject messageJson(String action, String playerId,
			String nickName, String message, String channelId) {
		JSONObject obj = new JSONObject();
		obj.put("action", action);
		obj.put("channel_id", channelId);
		obj.put("player_id", playerId);
		obj.put("name", nickName);
		obj.put("msg", message);
		return obj;
	}

	@SuppressWarnings("unchecked")
	private void chat(ChannelHandlerContext ctx, int parserError,
			String action, String playerId, String nickName, String message,
			String channelId) {
		List<User> room = (List<User>) rooms
				.get(Integer.parseInt(channelId) - 1);
		logger.debug("there is " + room.size() + " peple in channel "
				+ channelId);
		logger.info("action:" + action + " ,playerId: " + playerId
				+ " , name :" + nickName + ", message : " + message
				+ ", channel ID : " + channelId);
		JSONObject obj = messageJson(action, playerId, nickName, message,
				channelId);
		messagesQueues.get(Integer.parseInt(channelId) - 1).add(obj);

		for (User user : room) {
			if (parserError == 1) {
				// message = "전송 문구에 특수문자가 포함되어 있습니다.";
				message = "傳送語句中有包含含特殊文字";
				obj.put("msg", message);
				ctx.channel().writeAndFlush(obj.toJSONString() + '\n');
				break;
			} else {
				message = StringUtils.replace(message, "'", "`");
				message = StringUtils.replace(message, "\"", "`");
				obj.put("msg", message);
				if (user.getChannel() == ctx.channel()) {
					obj.put("self_yn", 1);
				} else {
					obj.put("self_yn", 0);
				}
				user.getChannel().writeAndFlush(obj.toJSONString() + '\n');
			}
		}
	}

	private void exchangeChannel(ChannelHandlerContext ctx, String action,
			String playerId, String newChannel, String channelId) {
		String message;
		if (Integer.parseInt(newChannel) < 1
				|| Integer.parseInt(newChannel) > MAX_ROOM_SIZE - 1) {
			// message = "채널 번호는 1-999 사이에서 입장\n 가능합니다.";
			message = "頻道編號1-999間可以\n 選擇進入";
			JSONObject obj = changeChannelJson(action, channelId, channelId,
					message);
			ctx.channel().writeAndFlush(obj.toJSONString() + '\n');
		} else {
			List<User> beforeRoom = (List<User>) rooms.get(Integer
					.parseInt(channelId) - 1);

			List<User> afterRoom = (List<User>) rooms.get(Integer
					.parseInt(newChannel) - 1);
			if (afterRoom.size() >= MAX_PEOPLE) {
				// message = newChannel + "번 채널이 최대인원이어서 입장 \n할 수 없습니다.";
				cannotChangeChannel(ctx, action, newChannel, channelId);
			} else {
				changeChannel(ctx, action, playerId, newChannel, channelId,
						beforeRoom, afterRoom);

				getChannelChat(ctx, playerId, newChannel);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private JSONObject changeChannelJson(String action, String channelId,
			String newChannelId, String message) {
		JSONObject obj = new JSONObject();
		obj.put("action", action);
		obj.put("channel_id", channelId);
		obj.put("new_channel_id", newChannelId);
		obj.put("msg", message);
		return obj;
	}

	@SuppressWarnings("unchecked")
	private void getChannelChat(ChannelHandlerContext ctx, String playerId,
			String newChannel) {
		Queue<JSONObject> msgQueue = EvictingQueue.create(MAX_CHAT_QUEUE);
		msgQueue.addAll(messagesQueues.get(Integer.parseInt(newChannel) - 1));
		while (msgQueue.size() != 0) {
			JSONObject chat = new JSONObject();
			chat = msgQueue.poll();
			String chat_message = (String) chat.get("msg");
			chat_message = StringUtils.replace(chat_message, "'", "`");
			chat_message = StringUtils.replace(chat_message, "\"", "`");
			chat.put("msg", chat_message);
			String cPlayerId = (String) chat.get("player_id");
			if (StringUtils.equals(playerId, cPlayerId)) {
				chat.put("self_yn", 1);
			} else {
				chat.put("self_yn", 0);
			}
			ctx.channel().writeAndFlush(chat.toJSONString() + "\n");
		}
	}

	private void changeChannel(ChannelHandlerContext ctx, String action,
			String playerId, String newChannel, String channelId,
			List<User> beforeRoom, List<User> afterRoom) {
		String message;
		for (User user : beforeRoom) {
			if (user.getChannel() == ctx.channel()) {
				beforeRoom.remove(user);
				cleanEmptyRoomsChat(beforeRoom);
				break;
			}
		}
		intoRoom(ctx, playerId, newChannel, afterRoom);
		// message = newChannel + "번 채널에 입장하셨습니다.";
		message = "已進入" + newChannel + "號頻道";
		JSONObject obj = changeChannelJson(action, channelId, newChannel,
				message);
		ctx.channel().writeAndFlush(obj.toJSONString() + '\n');
	}

	private void intoRoom(ChannelHandlerContext ctx, String playerId,
			String newChannel, List<User> afterRoom) {
		User user = new User();
		user.setUserId(playerId);
		user.setRoomNumber(Integer.parseInt(newChannel) - 1);
		user.setChannel(ctx.channel());
		afterRoom.add(user);
	}

	private void cannotChangeChannel(ChannelHandlerContext ctx, String action,
			String newChannel, String channelId) {
		String message;
		message = newChannel + "號頻道已滿 \n無法進入";
		JSONObject obj = changeChannelJson(action, channelId, channelId,
				message);
		ctx.channel().writeAndFlush(obj.toJSONString() + '\n');
	}

	@SuppressWarnings("unchecked")
	private void ininitChannel(ChannelHandlerContext ctx, String action,
			String playerId) {
		for (int i = 1; i <= MAX_ROOM_SIZE; i++) {
			List<User> room = (List<User>) rooms.get(i - 1);
			if (room.size() >= MAX_PEOPLE) {
				continue;
			}

			for (User user : room) {
				if (user.getChannel() == ctx.channel()) {
					JSONObject obj = new JSONObject();
					obj.put("action", action);
					obj.put("channel_id", Integer.toString(i));
					ctx.channel().writeAndFlush(obj.toJSONString() + "\n");
					return;
				}
			}

			intoRoom(ctx, playerId, Integer.toString(i), room);

			JSONObject obj = new JSONObject();
			obj.put("action", action);
			obj.put("channel_id", Integer.toString(i));

			ctx.channel().writeAndFlush(obj.toJSONString() + "\n");

			getChannelChat(ctx, playerId, Integer.toString(i));
			break;
		}
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		boolean isBreak = false;
		for (List<User> room : rooms) {
			if (isBreak) {
				break;
			}
			for (User user : room) {
				if (user.getChannel() == ctx.channel()) {
					room.remove(user);
					cleanEmptyRoomsChat(room);
					isBreak = true;
					break;
				}
			}
		}
	}

	private void cleanEmptyRoomsChat(List<User> room) {
		if (room.isEmpty()) {
			@SuppressWarnings({ "unchecked" })
			Queue<JSONObject> que = messagesQueues.get(rooms
					.indexOf(room));
			que.clear();
		}
	}

	public String fixByBiteSize(String encoding, String data, int maxBytes) {
		if (data == null || data.length() == 0 || maxBytes < 1) {
			return "";
		}
		Charset CS = Charset.forName(encoding);
		CharBuffer cb = CharBuffer.wrap(data);
		ByteBuffer bb = ByteBuffer.allocate(maxBytes);
		CharsetEncoder enc = CS.newEncoder();
		enc.encode(cb, bb, true);
		bb.flip();
		return CS.decode(bb).toString();
	}
}
