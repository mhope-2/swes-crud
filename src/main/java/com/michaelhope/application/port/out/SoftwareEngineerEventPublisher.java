package com.michaelhope.application.port.out;

import com.michaelhope.domain.entity.SoftwareEngineer;

public interface SoftwareEngineerEventPublisher {

    void publish(String eventType, SoftwareEngineer engineer, int aggregateVersion);
}
