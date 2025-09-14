package uz.app.demoapp.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uz.app.demoapp.payload.SmsSender;

@FeignClient(name = "eskizFeign", url = "https://notify.eskiz.uz")
public interface EskizFeign {
    @PostMapping("/api/message/sms/send")
     ResponseEntity<?> sendSms(@RequestHeader("AUTHORIZATION") String auth, @RequestBody SmsSender smsSender);
}
