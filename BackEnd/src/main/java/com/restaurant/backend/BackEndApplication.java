package com.restaurant.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.TimeZone;
import vn.payos.PayOS;
import vn.payos.model.webhooks.ConfirmWebhookResponse;


@SpringBootApplication
public class BackEndApplication {

    public static void main(String[] args) {

        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SpringApplication.run(BackEndApplication.class, args);
        
        
        try {
            PayOS payOS = new PayOS(
                    "2dae7da2-c098-42d8-a46a-0bf93b078f17",
                    "24cfa45c-177e-4360-84a7-3e432442b1c4",
                    "3100e7a11ab456e10c6f711dc0cf063f0814f0ab159662e4191d7a3d30a66d4b"
            );
            ConfirmWebhookResponse result = payOS.webhooks().confirm("https://api.dewjunior.id.vn/api/payos/webhook");
            System.out.println("PayOS webhook confirmed: " + result);
        } catch (Exception e) {
            System.err.println("Warning: PayOS webhook confirmation failed (non-critical): " + e.getMessage());
        }
    }

}
