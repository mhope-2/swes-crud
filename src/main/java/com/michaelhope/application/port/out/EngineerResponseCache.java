package com.michaelhope.application.port.out;

import com.michaelhope.api.response.SoftwareEngineerResponse;

public interface EngineerResponseCache {

    void putAfterCommit(Integer id, SoftwareEngineerResponse response);

    void evictAfterCommit(Integer id);
}
