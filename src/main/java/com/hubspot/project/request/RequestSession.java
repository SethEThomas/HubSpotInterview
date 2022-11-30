package com.hubspot.project.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;


@Data
@AllArgsConstructor
public class RequestSession {
    private int duration;
    private List<String> pages;
    private long startTime;
}
