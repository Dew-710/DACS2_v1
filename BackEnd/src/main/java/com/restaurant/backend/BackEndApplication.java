package com.restaurant.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.TimeZone;
import vn.payos.PayOS;
import vn.payos.model.webhooks.ConfirmWebhookResponse;


@SpringBootApplication
public class BackEndApplication {

    public static void main(String[] args) {
        // Ensure JVM uses a valid IANA timezone name (Postgres rejects "Asia/Saigon")
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SpringApplication.run(BackEndApplication.class, args);
        PayOS payOS = new PayOS(
                "f0198646-c08a-4c64-894a-a9ed5cb4aabe",
                "2b1d4afd-6b7d-44ba-820a-3f9127f19927",
                "4244234c1acfc77cfd008d0fe6273eaffe7d1aad262a031291383901198754f8"
        );
        ConfirmWebhookResponse result = payOS.webhooks().confirm("https://personalized-dow-derived-tvs.trycloudflare.com/api/payos/webhook");
        System.out.println(result);
    }

}
