package security.boundary;

import application.boundary.ApplicationResource;
import application.control.Emails;
import io.quarkiverse.renarde.router.Router;
import io.quarkiverse.renarde.security.ControllerWithUser;
import io.quarkiverse.renarde.security.RenardeSecurity;
import io.quarkiverse.renarde.util.StringUtils;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import java.util.UUID;
import javax.inject.Inject;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.hibernate.validator.constraints.Length;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestQuery;
import security.entity.User;
import security.entity.UserStatus;

@Path("/authentication")
@Blocking
public class AuthenticationResource extends ControllerWithUser<User> {

    @Inject
    RenardeSecurity security;

    @CheckedTemplate
    static class Templates {

        public static native TemplateInstance login();

        public static native TemplateInstance register(String email);

        public static native TemplateInstance confirm(User newUser);

        public static native TemplateInstance logoutFirst();
    }

    public TemplateInstance login() {
        return Templates.login();
    }

    @POST
    public Response manualLogin(
            @RestForm
            @NotBlank
            final String userName,
            @RestForm
            @NotBlank
            final String password
    ) {
        if (validationFailed()) {
            login();
        }

        final User user = User.findRegisteredByUserName(userName);
        if (null == user || !BcryptUtil.matches(password, user.password)) {
            validation.addError("userName", "Invalid username / password");
            prepareForErrorRedirect();
            login();
        }

        final NewCookie cookie = security.makeUserCookie(user);
        return Response
                .seeOther(Router.getURI(ApplicationResource::index))
                .cookie(cookie)
                .build();
    }

    @POST
    public TemplateInstance register(
            @RestForm
            @NotBlank
            @Email
            final String email
    ) {
        if (validationFailed()) {
            login();
        }

        User newUser = User.findUnconfirmedByEmail(email);
        if (null == newUser) {
            newUser = new User();
            newUser.email = email;
            newUser.userName = email;
            newUser.password = UUID.randomUUID().toString();
            newUser.status = UserStatus.CONFIRMATION_REQUIRED;
            newUser.confirmationCode = UUID.randomUUID().toString();
            newUser.persist();
        }

        Emails.confirm(newUser);
        return Templates.register(email);
    }

    public TemplateInstance confirm(
            @RestQuery 
            final String confirmationCode
    ) {
        checkLogoutFirst();
        User newUser = checkConfirmationCode(confirmationCode);
        return Templates.confirm(newUser);
    }

    public TemplateInstance logoutFirst() {
        return Templates.logoutFirst();
    }

    @POST
    public Response complete(
            @RestQuery 
            final String confirmationCode,
            
            @RestForm 
            @NotBlank 
            final String userName,

            @RestForm 
            @Length(min = 8) 
            final String password,
            
            @RestForm 
            @Length(min = 8) 
            final String password2,
            
            @RestForm 
            @NotBlank 
            final String firstName,
            
            @RestForm 
            @NotBlank 
            final String lastName
    ) {
        checkLogoutFirst();
        final User user = checkConfirmationCode(confirmationCode);

        if (validationFailed()) {
            confirm(confirmationCode);
        }

        validation.required("password", password);
        validation.required("password2", password2);
        validation.equals("password", password, password2);

        if (User.findRegisteredByUserName(userName) != null) {
            validation.addError("userName", "User name already taken");
        }
        if (validationFailed()) {
            confirm(confirmationCode);
        }

        user.userName = userName;
        user.password = BcryptUtil.bcryptHash(password);
        user.firstName = firstName;
        user.lastName = lastName;
        user.status = UserStatus.REGISTERED;

        final ResponseBuilder responseBuilder = Response.seeOther(Router.getURI(ApplicationResource::index));
        final NewCookie cookie = security.makeUserCookie(user);
        responseBuilder.cookie(cookie);
        return responseBuilder.build();
    }

    private void checkLogoutFirst() {
        if (getUser() != null) {
            logoutFirst();
        }
    }

    private User checkConfirmationCode(String confirmationCode) {
        if (StringUtils.isEmpty(confirmationCode)) {
            flash("message", "Missing confirmation code");
            flash("messageType", "error");
            redirect(ApplicationResource.class).index();
        }
        
        User user = User.findForContirmation(confirmationCode);
        if (user == null) {
            flash("message", "Invalid confirmation code");
            flash("messageType", "error");
            redirect(ApplicationResource.class).index();
        }
        
        return user;
    }

}
