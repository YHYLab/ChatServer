package com.yhy.dao;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import ch.qos.logback.classic.db.names.DBNameResolver;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.db.DBAppenderBase;

public class ChatDao extends DBAppenderBase<ILoggingEvent> {

	protected String insertSQL;

    @SuppressWarnings("unused")
	private DBNameResolver dbNameResolver;
    
	@Override
	public void start() {
		insertSQL = "INSERT INTO chat_log(msg) VALUES(?)";
		super.start();
	}
	
	@Override
	protected void subAppend(ILoggingEvent event, Connection connection,
			PreparedStatement insertStatement) throws Throwable {
		bindLoggingEventWithInsertStatement(insertStatement, event);

		int updateCount = insertStatement.executeUpdate();
		if (updateCount != 1) {
			addWarn("Failed to insert loggingEvent");
		}
	}

	void bindLoggingEventWithInsertStatement(PreparedStatement stmt,
			ILoggingEvent event) throws SQLException {
		stmt.setString(1, event.getFormattedMessage());
	}

	@Override
	protected String getInsertSQL() {
		return insertSQL;
	}

	@Override
	protected Method getGeneratedKeysMethod() {
		return null;
	}

	@Override
	protected void secondarySubAppend(ILoggingEvent eventObject,
			Connection connection, long eventId) throws Throwable {
	}
	
}
