package com.hubspot.project.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.project.model.UserEvent;
import com.hubspot.project.model.UserSession;
import com.hubspot.project.model.UserSessions;
import com.hubspot.project.request.RequestSession;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

@Service
public class AggregationService {

    private static int MAX_TIME_BETWEEN_SESSIONS = 600000;

    public HttpResponse<String> performAggregationAndSend() throws IOException, InterruptedException {
        JSONObject visitorEvents = getJsonFromUrl("https://candidate.hubteam.com/candidateTest/v3/problem/dataset?userKey=0bc4b226906a022dfb152d693de8");
        List<UserSessions> sessions = aggregateSessionsFromJson(visitorEvents);
        return postSessions(sessions, "https://candidate.hubteam.com/candidateTest/v3/problem/result?userKey=0bc4b226906a022dfb152d693de8");
    }

    private JSONObject getJsonFromUrl(String url) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.
                newBuilder().
                uri(URI.create(url)).
                build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return new JSONObject(response.body());
    }

    private List<UserSessions> aggregateSessionsFromJson(JSONObject visitorEvents) throws JsonProcessingException {
        JSONArray events = visitorEvents.getJSONArray("events");
        HashMap<String, TreeMap<Long, UserSession>> sessionsMap = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        for(int i = 0; i < events.length(); i++){
            JSONObject event = events.getJSONObject(i);
            UserEvent userEvent = objectMapper.readValue(event.toString(), UserEvent.class);
            String url = userEvent.getUrl();
            String visitorId = userEvent.getVisitorId();
            long timestamp = userEvent.getTimestamp();
            TreeMap<Long, UserSession> currentUserSessions = sessionsMap.getOrDefault(visitorId, new TreeMap<>());
            long newMinTimestamp = -1;
            long nexMaxTimestamp = -1;
            long usedStartTime = -1;
            for(long startTime: currentUserSessions.keySet()){
                UserSession tmpSession = currentUserSessions.get(startTime);
                if(timestamp >= tmpSession.getStartTime() - MAX_TIME_BETWEEN_SESSIONS &&
                timestamp <= tmpSession.getEndTime() + MAX_TIME_BETWEEN_SESSIONS){
                    newMinTimestamp = Math.min(tmpSession.getStartTime(), timestamp);
                    nexMaxTimestamp = Math.max(tmpSession.getEndTime(), timestamp);
                    usedStartTime = startTime;
                    break;
                }
            }
            if(usedStartTime >= 0){
                UserSession sessionToUpdate = currentUserSessions.get(usedStartTime);
                List<String> urlsVisited = sessionToUpdate.getPagesVisited();
                urlsVisited.add(url);
                int newDuration = (int) (nexMaxTimestamp - newMinTimestamp);
                UserSession newSession = new UserSession(newDuration, urlsVisited, newMinTimestamp, nexMaxTimestamp);
                currentUserSessions.remove(usedStartTime);
                currentUserSessions.put(newMinTimestamp, newSession);
            }
            else{
                ArrayList<String> urlsVisited = new ArrayList<>();
                urlsVisited.add(url);
                UserSession newSession = new UserSession(0, urlsVisited, timestamp, timestamp);
                currentUserSessions.put(timestamp, newSession);
            }
            sessionsMap.put(visitorId, currentUserSessions);
        }

        List<UserSessions> allSessions = new ArrayList<>();
        for(String visitorId: sessionsMap.keySet()){
            UserSessions currentUserSessions = new UserSessions(visitorId);
            List<UserSession> sessions = currentUserSessions.getSessions();
            for(UserSession session: sessionsMap.get(visitorId).values()){
                sessions.add(session);
            }
            currentUserSessions.setSessions(sessions);
            allSessions.add(currentUserSessions);
        }

        return allSessions;
    }

    private HttpResponse<String> postSessions(List<UserSessions> sessions, String url) throws IOException, InterruptedException {
        String requestBody = translateSessionDataForPost(sessions);
        System.out.println(requestBody);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.
                newBuilder().
                uri(URI.create(url)).
                POST(HttpRequest.BodyPublishers.ofString(requestBody)).
                build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String translateSessionDataForPost(List<UserSessions> sessions) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        HashMap<String, HashMap<String, List<RequestSession>>> requestMap = new HashMap<>();
        HashMap<String, List<RequestSession>> sessionsMap = new HashMap<>();
        for(UserSessions session: sessions){
            String visitorId = session.getVisitorId();
            List<UserSession> userSessions = session.getSessions();
            List<RequestSession> tmpSessions = new ArrayList<>();
            for(UserSession userSession: userSessions){
                tmpSessions.add(new RequestSession(
                        userSession.getDuration(),
                        userSession.getPagesVisited(),
                        userSession.getStartTime()
                ));
            }
            sessionsMap.put(visitorId, tmpSessions);
        }
        requestMap.put("sessionsByUser", sessionsMap);
        String requestBody = objectMapper.writeValueAsString(requestMap);
        return requestBody;
    }
}
