package com.hubspot.project.model;

import lombok.Data;

@Data
public class UserEvent {
    private String url;
    private String visitorId;
    private long timestamp;
}
