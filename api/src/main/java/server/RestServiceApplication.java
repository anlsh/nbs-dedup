package server;

import Constants.LocalDatabase;

import abstraction.DbAuxConstructor;
import abstraction.NBSConnectionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.SQLException;

@SpringBootApplication
public class RestServiceApplication {

    static DbAuxConstructor database;

    public static void main(String[] args) throws SQLException {
        database = new DbAuxConstructor(NBSConnectionFactory.make(LocalDatabase.DB_SERVER, LocalDatabase.DB_PORT,
                LocalDatabase.DB_NAME, LocalDatabase.DB_USERNAME, LocalDatabase.DB_PASSWORD));
        SpringApplication.run(RestServiceApplication.class, args);
    }
}
