package com.alerthub.sms_service.sender.implement;

import com.alerthub.sms_service.sender.SmsSender;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component 
@ConditionalOnProperty(name = "sms.sender", havingValue = "whatsapp")
public class TwilioWhatsAppSender implements SmsSender {
    
    private static final Logger log = LoggerFactory.getLogger(TwilioWhatsAppSender.class);

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.whatsapp-from}")
    private String fromWhatsApp;

    @PostConstruct
    public void init(){
        Twilio.init(accountSid,authToken);

    }

    @Override
    public void send(String to,String message){
        if(to==null||to.isBlank()){
            throw new IllegalArgumentException("Recipient phone number is required");
        }
        Message sent=Message.creator(
            new PhoneNumber("whatsapp:" + to),   // recipient, e.g. whatsapp:+972501234567
                new PhoneNumber(fromWhatsApp),        // sandbox / approved sender
                message

        ).create();
        log.info("WhatsApp message queued via Twilio. SID={}", sent.getSid());
    }

}
