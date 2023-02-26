package application.boundary;

import io.quarkiverse.renarde.Controller;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import javax.ws.rs.Path;

@Blocking
public class ApplicationResource extends Controller {

    @CheckedTemplate
    static class Templates {

        public static native TemplateInstance index();
    }

    @Path("/")
    public TemplateInstance index() {
        return Templates.index();
    }

}
