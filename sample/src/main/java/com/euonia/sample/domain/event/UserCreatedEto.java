package com.euonia.sample.domain.event;

import com.euonia.bus.contract.Multicast;
import lombok.Data;

@Data
public class UserCreatedEto implements Multicast {
    private long id;
    private String username;
}
