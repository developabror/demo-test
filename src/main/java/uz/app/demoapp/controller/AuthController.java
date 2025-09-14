package uz.app.demoapp.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.app.demoapp.entity.SmsAuthentication;
import uz.app.demoapp.entity.User;
import uz.app.demoapp.entity.enums.Role;
import uz.app.demoapp.feign.EskizFeign;
import uz.app.demoapp.payload.ConfirmDTO;
import uz.app.demoapp.payload.SignInDTO;
import uz.app.demoapp.payload.SmsSender;
import uz.app.demoapp.payload.UserDTO;
import uz.app.demoapp.repository.SmsAuthenticationRepository;
import uz.app.demoapp.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final SmsAuthenticationRepository smsAuthenticationRepository;
    private final EskizFeign eskizFeign;

    @Value("${eskiz.token}")
    String token;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody UserDTO userDTO) {
        User user = User
                .builder()
                .firstName(userDTO.getFirstName())
                .lastName(userDTO.getLastName())
                .phoneNumber(userDTO.getPhoneNumber())
                .password(userDTO.getPassword())
                .role(Role.USER)
                .enabled(false)
                .build();
        sendConfirmationSMS(user.getPhoneNumber(), generatedCode());
        userRepository.save(user);
        return ResponseEntity.ok().body(user);
    }



    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestBody SignInDTO signDTO) {
        Optional<User> optionalUser = userRepository.findByPhoneNumber(signDTO.getPhoneNumber());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }
        User user = optionalUser.get();
        if (!user.getEnabled()) {
            return ResponseEntity.status(401).body("User not enabled, please confirm sms");
        }
        return ResponseEntity.ok().body(user);
    }

    @PostMapping("/confirm")
    public  ResponseEntity<?> confirm(@RequestBody ConfirmDTO confirmDTO) {
        SmsAuthentication smsAuthentication = smsAuthenticationRepository.findByPhoneNumber(confirmDTO.getPhoneNumber()).orElseThrow();
//        if (smsAuthentication.getExpiryDate().isBefore(LocalDateTime.now())) {
//            smsAuthenticationRepository.delete(smsAuthentication);
//        }
        if (smsAuthentication.getCode().equals(confirmDTO.getCode())) {
            User user = userRepository.findByPhoneNumber(confirmDTO.getPhoneNumber()).orElseThrow();
            user.setEnabled(true);
            userRepository.save(user);
            smsAuthenticationRepository.delete(smsAuthentication);
            return ResponseEntity.ok().body("Confirmed");
        }else {
            return ResponseEntity.status(401).body("Invalid code");
        }
    }

    private void sendConfirmationSMS(String phoneNumber, String s) {
        SmsAuthentication smsAuthentication = new SmsAuthentication();
        smsAuthentication.setPhoneNumber(phoneNumber);
        smsAuthentication.setCode(s);
        smsAuthentication.setExpiryDate(LocalDateTime.now().plusMinutes(2));
        smsAuthenticationRepository.save(smsAuthentication);
        System.out.println("Sending SMS to " + phoneNumber + " with code: " + s);
        sendSms(smsAuthentication);

    }

    private void sendSms(SmsAuthentication smsAuthentication) {
        System.out.println(token + " sending SMS to " + smsAuthentication.getPhoneNumber());
        SmsSender smsSender = new SmsSender();
        smsSender.setFrom("4546");
        smsSender.setMobile_phone(smsAuthentication.getPhoneNumber());
        smsSender.setMessage("This is test from Eskiz");
//        eskizFeign.sendSms(token,smsSender);
    }

    private String generatedCode() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

}
