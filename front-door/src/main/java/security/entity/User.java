package security.entity;

import static io.quarkus.hibernate.orm.panache.PanacheEntityBase.find;

import io.quarkiverse.renarde.security.RenardeUser;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
        name = "\"user\"",
        uniqueConstraints = {
            @UniqueConstraint(name = "pk_user", columnNames = { "id" }),
            @UniqueConstraint(name = "udx_user_username", columnNames = { "username" }),
            @UniqueConstraint(name = "udx_user_confirmationcode", columnNames = { "confirmationcode" })
        }
)
public class User extends PanacheEntityBase implements RenardeUser {
    
    @Id
    @SequenceGenerator(name = "seq_user", sequenceName = "seq_user")
    @GeneratedValue(generator = "seq_user", strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    public Long id;

    @Column(nullable = false)
    public String email;

    @Column(name = "username", unique = true, nullable = false)
    public String userName;

    @Column(name = "password", nullable = false)
    public String password;

    @Column(name = "firstname")
    public String firstName;

    @Column(name = "lastname")
    public String lastName;

    public boolean admin;

    @Column(name = "confirmationcode", unique = true, nullable = false)
    public String confirmationCode;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    public UserStatus status;
    
    public static User findByUserName(final String username) {
        return find(
                "LOWER(userName) = ?1", 
                username.toLowerCase()
        ).firstResult();
    }
    
    public static User findRegisteredByUserName(final String username) {
        return find(
                "LOWER(userName) = ?1 AND status = ?2",
                username.toLowerCase(),
                UserStatus.REGISTERED
        ).firstResult();
    }
    
    public static User findUnconfirmedByEmail(final String email) {
        return find(
                "LOWER(email) = ?1 AND status = ?2", 
                email.toLowerCase(), 
                UserStatus.CONFIRMATION_REQUIRED
        ).firstResult();
    }
    
    public static User findForContirmation(final String confirmationCode) {
        return find(
                "confirmationCode = ?1 AND status = ?2", 
                confirmationCode, 
                UserStatus.CONFIRMATION_REQUIRED
        ).firstResult();
    }

    @Override
    public Set<String> getRoles() {
        final Set<String> roles = new HashSet<>();
        
        if (admin) {
            roles.add("admin");
        }
        
        return roles;
    }

    @Override
    public String getUserId() {
        return userName;
    }

    @Override
    public boolean isRegistered() {
        return status == UserStatus.REGISTERED;
    }

    
}
