package it.polimi.ingsw2020.santorini.model;

import it.polimi.ingsw2020.santorini.model.gods.*;
import it.polimi.ingsw2020.santorini.utils.PhaseType;

public class GodCard {
    /**
     * getter of class parameters, setter not needed since thy are considered final
     */
    protected String name;
    protected int maxPlayersNumber;
    protected String timingName;
    protected PhaseType timing;
    protected boolean mandatory;

    public GodCard(){}

    public String getName() {
        return name;
    }

    public int getMaxPlayersNumber() {
        return maxPlayersNumber;
    }

    public String getTimingName() {
        return timingName;
    }

    public PhaseType getTiming() {
        return timing;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    /**
     * the function that express the power of the god. Each God will have its power
     * @param field is the board of the game you are playing
     * @param invoker is the player that activates the power
     */
    public void invokeGod(Board field, Player invoker) {

    }

    /**
     * the function that explains the power of god
     * @return string format of god's name and its power. Each God will have its own
     */
    public String toStringEffect(){
        switch(this.getName()){
            case "Apollo":
                return Apollo.toStringEffect(this);
            case "Ares":
                return Ares.toStringEffect(this);
            case "Artemis":
                return Artemis.toStringEffect(this);
            case "Athena":
                return Athena.toStringEffect(this);
            case "Atlas":
                return Atlas.toStringEffect(this);
            case "Demeter":
                return Demeter.toStringEffect(this);
            case "Hephaestus":
                return Hephaestus.toStringEffect(this);
            case "Hestia":
                return Hestia.toStringEffect(this);
            case "Minotaur":
                return Minotaur.toStringEffect(this);
            case "Pan":
                return Pan.toStringEffect(this);
            case "Persephone":
                return Persephone.toStringEffect(this);
            case "Poseidon":
                return Poseidon.toStringEffect(this);
            case "Prometheus":
                return Prometheus.toStringEffect(this);
            case "Zeus":
                return Zeus.toStringEffect(this);
            default:
                return "no selected god";
        }
    }
}