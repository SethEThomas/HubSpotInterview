package com.hubspot.project.controller;

import com.hubspot.project.service.AggregationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.net.http.HttpResponse;

@Controller
@RequestMapping("/run")
public class RunController {

    @Autowired
    private AggregationService aggregationService;

    @GetMapping("/hubspot-interview")
    public ResponseEntity<String> performAggregation() throws IOException, InterruptedException {
        HttpResponse<String> response = aggregationService.performAggregationAndSend();
        return new ResponseEntity<>(response.body(), HttpStatusCode.valueOf(response.statusCode()));
    }
}
