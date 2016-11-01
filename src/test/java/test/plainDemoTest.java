package test;

import entity.User;
import facades.UserFacade;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author lam
 */
public class plainDemoTest {
  
  public plainDemoTest() {
  }
  
  @Test
  public void dummyTest(){
      UserFacade uf = new UserFacade();
      User user = new User("Bo", "poulsen");
      uf.createUser(user);
      
      assertEquals(user, uf.getUserByName("bo"));
  }
  
}
