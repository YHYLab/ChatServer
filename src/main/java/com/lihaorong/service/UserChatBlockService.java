package com.lihaorong.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import com.neocyon.chat.ChatServerHandler;
import com.neocyon.dao.BlockUserDao;

public class UserChatBlockService {
	final static Logger logger = LoggerFactory
			.getLogger(UserChatBlockService.class.getName());
	private BlockUserDao blockUserDao;

	public void setBlockUserDao(BlockUserDao blockUserDao) {
		this.blockUserDao = blockUserDao;
	}

	@Scheduled(fixedRate = 1000 * 60)
	public void blockUser() {
		List<Map<String, Object>> users = blockUserDao.getBlockUsers();
		if (users.size() != ChatServerHandler.blockUsers.size()) {
			ChatServerHandler.blockUsers = users;
		}
	}
}
