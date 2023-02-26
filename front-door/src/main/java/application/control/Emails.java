package application.control;

import io.quarkus.mailer.MailTemplate.MailTemplateInstance;
import io.quarkus.qute.CheckedTemplate;
import security.entity.User;

public class Emails {

    private static final String FROM = "Front Door <frontdoor@example.com>";
    private static final String SUBJECT_PREFIX = "[Front Door] ";

    @CheckedTemplate
    static class Templates {

        public static native MailTemplateInstance confirm(User user);
    }

    public static void confirm(final User user) {
        Templates.confirm(user)
                .subject(SUBJECT_PREFIX + "Please confirm your email address")
                .to(user.email)
                .from(FROM)
                .send().await().indefinitely();
    }
}
