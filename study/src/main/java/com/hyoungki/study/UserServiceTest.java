package com.hyoungki.study;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

import com.hyoungki.study.dao.UserDao;
import com.hyoungki.study.domain.Level;
import com.hyoungki.study.domain.User;
import com.hyoungki.study.service.UserLevelUpgradePolicy;
import com.hyoungki.study.service.UserService;

import static com.hyoungki.study.service.UserService.MIN_LOGCOUNT_FOR_SILVER;
import static com.hyoungki.study.service.UserService.MIN_RECOMMEND_FOR_GOLD;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/test-applicationContext.xml")
public class UserServiceTest {
	
	@Autowired
	UserService userService;
	
	@Autowired
	UserDao	userDao;
	
	@Autowired
	UserLevelUpgradePolicy userLevelUpgradePolicy;

	@Autowired
	PlatformTransactionManager transactionManager;
	
	List<User>	users;
    
	static class TestUserService extends UserService {
		private String id;
		
		private TestUserService(String id) {
			this.id	= id;
		}
		
		protected void upgradeLevel(User user) {
			if (user.getId().equals(this.id)) throw new TestUserServiceException();
			super.upgradeLevel(user);
		}
	}
	
	static class TestUserServiceException extends RuntimeException {
	}	
	
	@Before
	public void setUp() {
		users	= Arrays.asList(
				new User("beck", "백도르", "1234", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER - 1, 0, "beck@naver.com"), 
				new User("kimbo", "김보", "1234", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER, 0, "kimbo@naver.com"), 
				new User("lhk", "횽긔", "1234", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD - 1, "lhk@naver.com"),	
				new User("lion", "라이언", "1234", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD, "lion@naver.com"),
				new User("mung", "뭉이", "1234", Level.GOLD, 100, Integer.MAX_VALUE, "mung@naver.com")
		);
	}
	
    @Test
    public void upgradeAllOrNothing() throws Exception {
    	UserService testUserService	= new TestUserService(users.get(3).getId());
    	testUserService.setUserDao(this.userDao);
    	testUserService.setUserLevelUpgradePolicy(userLevelUpgradePolicy);
    	testUserService.setTransactionManager(transactionManager);
    	
    	userDao.deleteAll();
    	for(User user : users) userDao.add(user);

    	try {
    		testUserService.upgradeLevels();
    		fail("TestUserServiceException expected");
    	}
    	catch(TestUserServiceException e) {
    	}
    	
    	checkLevelUpgraded(users.get(1), false);
    }	
	
	@Test
	public void add() {
		userDao.deleteAll();
		
		User	userWithLevel		= users.get(4);
		User	userWithoutLevel	= users.get(0);
		userWithoutLevel.setLevel(null);
		
		userService.add(userWithLevel);
		userService.add(userWithoutLevel);
		
		User	userWithLevelRead		= userDao.get(userWithLevel.getId());
		User	userWithoutLevelRead	= userDao.get(userWithoutLevel.getId());
		
		assertThat(userWithLevelRead.getLevel(), is(userWithLevel.getLevel()));
		assertThat(userWithoutLevelRead.getLevel(), is(Level.BASIC));
	}
	
	@Test
	public void upgradeLevels() throws Exception {
		userDao.deleteAll();
		
		for(User user : users) userDao.add(user);
		
		userService.upgradeLevels();
		
		checkLevelUpgraded(users.get(0), false);
		checkLevelUpgraded(users.get(1), true);
		checkLevelUpgraded(users.get(2), false);
		checkLevelUpgraded(users.get(3), true);
		checkLevelUpgraded(users.get(4), false);
	}
	
	private void checkLevelUpgraded(User user, boolean upgraded) {
		User	userUpdate	= userDao.get(user.getId());
		if (upgraded) {
			assertThat(userUpdate.getLevel(), is(user.getLevel().nextLevel()));
		}
		else {
			assertThat(userUpdate.getLevel(), is(user.getLevel()));
		}
	}
	
	private void checkLevel(User user, Level expectedLevel) {
		User	userUpdate	= userDao.get(user.getId());
		assertThat(userUpdate.getLevel(), is(expectedLevel));
	}
	
//	@Test
//	public void bean() {
//		assertThat(this.userService, is(notNullValue()));
//	}
	
}
