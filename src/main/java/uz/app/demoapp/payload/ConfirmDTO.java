package uz.app.demoapp.payload;

import lombok.Data;

@Data
public class ConfirmDTO {
    private String phoneNumber;
    private String code;
}
