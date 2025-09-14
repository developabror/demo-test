package uz.app.demoapp.payload;

import lombok.Data;

@Data
public class SmsSender {
    private String mobile_phone;
    private String message;
    private String from;
}
