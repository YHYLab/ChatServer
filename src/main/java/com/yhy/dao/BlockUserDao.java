package com.yhy.dao;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class BlockUserDao{
	final static Logger logger = LoggerFactory
			.getLogger(BlockUserDao.class.getName());
	
	private JdbcTemplate jdbcTemplate;
	
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	public List<Map<String, Object>> getBlockUsers() {
		String sql = "SELECT player_id FROM block_user";
		List<Map<String, Object>> users = jdbcTemplate.queryForList(sql);
		return users;
	}
}