package com.hubspot.project.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserSessions {
    private String visitorId;
    private List<UserSession> sessions;

    public UserSessions(String visitorId){
        this.visitorId = visitorId;
        this.sessions = new ArrayList<>();
    }
}
