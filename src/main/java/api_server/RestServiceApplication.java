package api_server;

import abstraction.Constants;
import abstraction.NBS_DB;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.SQLException;

@SpringBootApplication
public class RestServiceApplication {

    static NBS_DB database;

    public static void main(String[] args) throws SQLException {
        database = new NBS_DB(Constants.DB_SERVER, Constants.DB_PORT, Constants.DB_NAME,
                Constants.DB_USERNAME, Constants.DB_PASSWORD);
        SpringApplication.run(RestServiceApplication.class, args);
    }
}
