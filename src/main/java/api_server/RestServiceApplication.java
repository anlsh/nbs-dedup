package api_server;

import abstraction.Constants;
import abstraction.AuxLogic;
import abstraction.NBSConnection;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.SQLException;

@SpringBootApplication
public class RestServiceApplication {

    static AuxLogic database;

    public static void main(String[] args) throws SQLException {
        database = new AuxLogic(NBSConnection.getNBSConnection(Constants.DB_SERVER, Constants.DB_PORT,
                Constants.DB_NAME, Constants.DB_USERNAME, Constants.DB_PASSWORD));
        SpringApplication.run(RestServiceApplication.class, args);
    }
}
