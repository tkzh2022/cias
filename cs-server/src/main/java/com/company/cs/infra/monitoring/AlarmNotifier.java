package com.company.cs.infra.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AlarmNotifier {
    private static final Logger log = LoggerFactory.getLogger(AlarmNotifier.class);

    public void notifyError(String code, String message, Throwable throwable) {
        log.error("HUBBLE_ALERT code={} message={}", code, message, throwable);
    }
}
