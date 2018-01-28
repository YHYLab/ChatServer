package com.lihaorong.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;


public class UserCounterDao {
	final static Logger logger = LoggerFactory
			.getLogger(UserCounterDao.class.getName());
	
	private JdbcTemplate jdbcTemplate;
	
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	public void saveUserCount(int userCount) {
		int insertResult = jdbcTemplate.update("insert into concurrent_user_number (user_number) values(?)", userCount);
		logger.debug("insert result => " + insertResult);
	}

}
