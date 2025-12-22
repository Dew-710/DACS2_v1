package com.restaurant.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.TimeZone;

@SpringBootApplication
public class BackEndApplication {

    public static void main(String[] args) {
        // Ensure JVM uses a valid IANA timezone name (Postgres rejects "Asia/Saigon")
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SpringApplication.run(BackEndApplication.class, args);
    }

}
