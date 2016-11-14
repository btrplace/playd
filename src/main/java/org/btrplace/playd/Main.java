package org.btrplace.playd;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * @author Fabien Hermenier
 */
public class Main {

    public static final String MONGOHQ_URL = "MONGOHQ_URL";
    public static DB mongoDB;

    public static void main(String[] args) {
        try {
            //mongodb://<user>:<password>@lennon.mongohq.com:10083/app30683431
            if (System.getenv(MONGOHQ_URL) == null) {
                System.err.println("No DB Url available from environment variable '" + MONGOHQ_URL + "'");
                System.exit(1);
            }
            MongoClientURI uri = new MongoClientURI(System.getenv(MONGOHQ_URL));
            MongoClient client = new MongoClient(uri);
            mongoDB = client.getDB(uri.getDatabase());

            String webappDirLocation = "src/main/webapp/";

            String webPort = System.getenv("PORT");
            if (webPort == null || webPort.isEmpty()) {
                webPort = "8080";
            }

            Server server = new Server(Integer.valueOf(webPort));
            WebAppContext root = new WebAppContext();

            root.setContextPath("/");
            root.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
            root.setResourceBase(webappDirLocation);

            // Parent loader priority is a class loader setting that Jetty accepts.
            // By default Jetty will behave like most web containers in that it will
            // allow your application to replace non-server libraries that are part of the
            // container. Setting parent loader priority to true changes this behavior.
            // Read more here: http://wiki.eclipse.org/Jetty/Reference/Jetty_Classloading
            root.setParentLoaderPriority(true);

            server.setHandler(root);

            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
