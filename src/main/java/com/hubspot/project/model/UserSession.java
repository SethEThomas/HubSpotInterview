package com.hubspot.project.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserSession implements Comparable<UserSession>{
    private int duration;
    private List<String> pagesVisited;
    private long startTime;
    @JsonIgnore
    private long endTime;

    @Override
    public int compareTo(UserSession otherSession){
        return Long.compare(getEndTime(), otherSession.getEndTime());
    }
}
