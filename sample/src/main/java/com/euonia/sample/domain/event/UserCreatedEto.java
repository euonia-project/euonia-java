package com.euonia.sample.domain.event;

import lombok.Data;

@Data
public class UserCreatedEto {
    private long id;
    private String username;
}
