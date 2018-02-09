package com.yhy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.neocyon.chat.ChatServerHandler;
import com.neocyon.dao.UserCounterDao;

@Service
@Async
public class UserCounterService {
	final static Logger logger = LoggerFactory.getLogger(UserCounterService.class
			.getName());
	
	private UserCounterDao userCounterDao;
	
	public void setUserCounterDao(UserCounterDao userCounterDao) {
		this.userCounterDao = userCounterDao;
	}

	@Scheduled(fixedRate=1000 * 60)
	public void saveCount() {
		logger.info("count => " + ChatServerHandler.channels.size() );
		userCounterDao.saveUserCount(ChatServerHandler.channels.size());
	}
}
