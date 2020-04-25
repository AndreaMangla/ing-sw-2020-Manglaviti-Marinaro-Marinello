package it.polimi.ingsw2020.santorini.utils;

import com.google.gson.Gson;
import it.polimi.ingsw2020.santorini.utils.messages.*;

import java.io.Serializable;

public class Message implements Serializable {

    private static final long serialVersionUID = 5140286385535003407L;

    private String username;
    private FirstHeaderType firstLevelHeader;
    private SecondHeaderType secondLevelHeader;
    private String serializedPayload;

    public Message(String username){
        this.username = username;
    }

    public void buildLoginMessage(LoginMessage payload){
        Gson gson = new Gson();
        this.firstLevelHeader = FirstHeaderType.SETUP;
        this.secondLevelHeader = SecondHeaderType.LOGIN;
        this.serializedPayload = gson.toJson(payload);
    }

    public LoginMessage deserializeLoginMessage(String payload){
        Gson gson = new Gson();
        return gson.fromJson(payload, LoginMessage.class);
    }

    public void buildUsernameErrorMessage(UsernameErrorMessage payload){
        Gson gson = new Gson();
        this.firstLevelHeader = FirstHeaderType.ERROR;
        this.secondLevelHeader = SecondHeaderType.USERNAME_ERROR;
        this.serializedPayload = gson.toJson(payload);
    }

    public UsernameErrorMessage deserializeUsernameErrorMessage(String payload){
        Gson gson = new Gson();
        return gson.fromJson(payload, UsernameErrorMessage.class);
    }

    public void buildCorrectLoginMessage(CorrectLoginMessage payload){
        Gson gson = new Gson();
        this.firstLevelHeader = FirstHeaderType.LOADING;
        this.secondLevelHeader = SecondHeaderType.LOGIN;
        this.serializedPayload = gson.toJson(payload);
    }

    public CorrectLoginMessage deserializeCorrectLoginMessage(String payload){
        Gson gson = new Gson();
        return gson.fromJson(payload, CorrectLoginMessage.class);
    }

    public void buildMatchSetupMessage(MatchSetupMessage payload){
        Gson gson = new Gson();
        this.firstLevelHeader = FirstHeaderType.SETUP;
        this.secondLevelHeader = SecondHeaderType.MATCH;
        this.serializedPayload = gson.toJson(payload);
    }

    public MatchSetupMessage deserializeMatchSetupMessage(String payload){
        Gson gson = new Gson();
        return gson.fromJson(payload, MatchSetupMessage.class);
    }

    public void buildAskSelectionOrderMessage(){
        this.firstLevelHeader = FirstHeaderType.ASK;
        this.secondLevelHeader = SecondHeaderType.SELECTION_ORDER;
    }

    public void deserializeAskSelectionOrderMessage() {

    }

    public String getUsername(){ return username; }

    public FirstHeaderType getFirstLevelHeader() {
        return firstLevelHeader;
    }

    public SecondHeaderType getSecondLevelHeader() { return secondLevelHeader; }

    public String getSerializedPayload() {
        return serializedPayload;
    }

    @Override
    public String toString(){
        return firstLevelHeader.toString() + secondLevelHeader.toString();
    }
}
