package application.boundary;

import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.apache.http.client.utils.URIBuilder;

@Path("/upload")
public class FormResource {
    
    private static final Logger LOG = Logger.getLogger(FormResource.class.getName());
    
    @POST
    public Response upload(final String[] file) throws URISyntaxException {
        LOG.log(Level.WARNING, "File size: {0}", file.length);
        LOG.log(Level.WARNING, "File: \n{0}", file);
        
        return Response.seeOther(new URIBuilder().setHost("localhost").build()).build();
    }
}
