package com.hyoungki.study.dao;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

@Configuration
public class DaoFactory {
	
	@Bean
	public UserDao userDao() {
		UserDao		userDao		= new UserDao();
		userDao.setDataSource(dataSource());
//		userDao.setConnectionMaker(connectionMaker());
				
		return userDao;
	}
	
//	@Bean
//	public AccountDao accountDao() {
//		return new AccountDao(connectionMaker());
//	}
//	
//	@Bean
//	public ConnectionMaker connectionMaker() {
//		return new DConnectionMaker();
//	}
	
	@Bean
	public DataSource dataSource() {
		SimpleDriverDataSource	dataSource	= new SimpleDriverDataSource();
		
		dataSource.setDriverClass(oracle.jdbc.driver.OracleDriver.class);
		dataSource.setUrl("jdbc:oracle:thin:@localhost:1521:xe");
		dataSource.setUsername("curix");
		dataSource.setPassword("1234");
		
		return dataSource;
	}
}
