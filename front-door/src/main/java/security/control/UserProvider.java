package security.control;

import io.quarkiverse.renarde.security.RenardeSecurity;
import io.quarkiverse.renarde.security.RenardeUser;
import io.quarkiverse.renarde.security.RenardeUserProvider;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import security.entity.User;

@ApplicationScoped
public class UserProvider implements RenardeUserProvider {

    @Inject
    RenardeSecurity security;
    
    @Override
    public RenardeUser findUser(final String tenantId, final String authId) {
        return User.findByUserName(authId);
    }
    
}
