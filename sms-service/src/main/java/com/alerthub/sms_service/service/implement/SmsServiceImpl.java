package com.alerthub.sms_service.service.implement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alerthub.sms_service.dto.NotificationMessage;
import com.alerthub.sms_service.exception.SmsSendException;
import com.alerthub.sms_service.kafka.LogProducer;
import com.alerthub.sms_service.sender.SmsSender;
import com.alerthub.sms_service.service.SmsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsServiceImpl.class);

    private final SmsSender smsSender;
    private final LogProducer logProducer;

    @Override
    public void process(NotificationMessage message) {
        try {
            smsSender.send(message.getTo(), message.getMessage());

            String ok = "SMS/WhatsApp sent successfully to " + message.getTo();
            log.info(ok);
            logProducer.send("INFO", ok);              // success log -> logger MS

        } catch (Exception ex) {
            String fail = "Failed to send to " + message.getTo() + ": " + ex.getMessage();
            log.error(fail, ex);
            logProducer.send("ERROR", fail);           // failure log -> logger MS
            throw new SmsSendException(fail, ex);
        }
    }
}