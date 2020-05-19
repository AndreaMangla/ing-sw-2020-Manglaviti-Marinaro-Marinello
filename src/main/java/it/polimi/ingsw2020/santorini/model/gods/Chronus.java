package it.polimi.ingsw2020.santorini.model.gods;

import it.polimi.ingsw2020.santorini.controller.TurnLogic;
import it.polimi.ingsw2020.santorini.exceptions.EndMatchException;
import it.polimi.ingsw2020.santorini.model.*;
import it.polimi.ingsw2020.santorini.utils.Message;
import it.polimi.ingsw2020.santorini.utils.PhaseType;

public class Chronus extends GodCard {

    private boolean activated = false;
    private String invoker = null;
    private boolean set = false;

    public Chronus(){
        name = getClass().getSimpleName();
        maxPlayersNumber = 2;
        timingName = "Opponents' Turn";
        timing = PhaseType.STANDBY_PHASE_3;
        mandatory = true;
        needParameters = false;
    }

    public String getInvoker() {
        return invoker;
    }

    @Override
    public boolean canActivate(Match match){
        if(!activated) {
            activated = true;
            invoker = match.getCurrentPlayer().getNickname();
            return true;
        }
        return false;
    }

    @Override
    public void invokeGod(Match match, Message message, TurnLogic turnManager) throws EndMatchException {
        System.out.println("potere di " + name + " attivato");
        if(!set) {
            try {
                turnManager.setChronus(this);
                turnManager.setChronusEffect(Chronus.class.getMethod("invokeGod", Match.class, Message.class, TurnLogic.class));
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            set = true;
        }
        if(match.getNumberOfCompletedTowers() == 5){
            match.setNextPlayer();
            match.currentWins();
        }
    }

    public static String toStringEffect(GodCard card) {
        return card.getName() + "\n" + card.getTimingName() + ": If possible, at least one Worker must move up this turn.";
    }
}