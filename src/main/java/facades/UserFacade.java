package facades;

import security.IUserFacade;
import entity.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import security.IUser;
import security.PasswordStorage;

public class UserFacade implements IUserFacade {

    /*When implementing your own database for this seed, you should NOT touch any of the classes in the security folder
    Make sure your new facade implements IUserFacade and keeps the name UserFacade, and that your Entity User class implements 
    IUser interface, then security should work "out of the box" with users and roles stored in your database */
    private final EntityManagerFactory emf = Persistence.createEntityManagerFactory("0PUSeed");
    private EntityManager em;
    private String validatePassword;

    public UserFacade() {
        Persistence.generateSchema("0PUSeed", null);
        //Test Users
        User user = new User("user", "test");
        user.addRole("User");
        createUser(user);
        User admin = new User("admin", "test");
        admin.addRole("Admin");
        createUser(admin);

        User both = new User("user_admin", "test");
        both.addRole("User");
        both.addRole("Admin");
        createUser(both);
    }

    public void addEntityManager(EntityManagerFactory emf) {
        em = emf.createEntityManager();
    }

    public User createUser(User user) {
        User u = user;

        addEntityManager(emf);
        try {
            em.getTransaction().begin();
            try {
                validatePassword = PasswordStorage.createHash(user.getPassword());
            } catch (PasswordStorage.CannotPerformOperationException ex) {
                Logger.getLogger(UserFacade.class.getName()).log(Level.SEVERE, null, ex);
            }
            em.persist(user);

            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return u;
    }

    public User getUserByName(String name) {
        addEntityManager(emf);
        User u = null;
        try {
            em.getTransaction().begin();
            u = em.createQuery("SELECT e FROM User e WHERE e.userName = :userName", User.class).setParameter("userName", name).getResultList().get(0);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return u;
    }

    @Override
    public IUser getUserByUserId(String id) {
        addEntityManager(emf);
        User u = null;
        try {
            em.getTransaction().begin();
            u = em.find(User.class, id);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return u;
    }

    /*
  Return the Roles if users could be authenticated, otherwise null
     */
    @Override
    public List<String> authenticateUser(String userName, String password) {
        IUser user = getUserByName(userName);
        try {
            if (password.equals(PasswordStorage.verifyPassword(password, validatePassword))) {
                return user != null && user.getPassword().equals(password) ? user.getRolesAsStrings() : null;
            }
        } catch (PasswordStorage.CannotPerformOperationException ex) {
            Logger.getLogger(UserFacade.class.getName()).log(Level.SEVERE, null, ex);
        } catch (PasswordStorage.InvalidHashException ex) {
            Logger.getLogger(UserFacade.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
