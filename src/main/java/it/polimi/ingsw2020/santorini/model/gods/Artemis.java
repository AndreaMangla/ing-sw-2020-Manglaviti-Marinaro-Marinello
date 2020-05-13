package it.polimi.ingsw2020.santorini.model.gods;

import it.polimi.ingsw2020.santorini.controller.TurnLogic;
import it.polimi.ingsw2020.santorini.exceptions.EndMatchException;
import it.polimi.ingsw2020.santorini.exceptions.IllegalMovementException;
import it.polimi.ingsw2020.santorini.model.*;
import it.polimi.ingsw2020.santorini.utils.LevelType;
import it.polimi.ingsw2020.santorini.utils.Message;
import it.polimi.ingsw2020.santorini.utils.PhaseType;
import it.polimi.ingsw2020.santorini.utils.messages.godsParam.ArtemisParamMessage;

public class Artemis extends GodCard {

    public Artemis(){
        name = getClass().getSimpleName();
        maxPlayersNumber = 3;
        timingName = "Your Move";
        timing = PhaseType.STANDBY_PHASE_2;
        mandatory = false;
        needParameters = true;
    }

    @Override
    public boolean canActivate(Match match) {
        return match.getCurrentPlayer().getPlayingBuilder().canMove();
    }

    @Override
    public void invokeGod(Match match, Message message, TurnLogic turnManager) throws EndMatchException {
        ArtemisParamMessage param = message.deserializeArtemisParamMessage();
        try {
            match.getCurrentPlayer().getPlayingBuilder().move(param.getDirection());
        } catch(IllegalMovementException ignored){}
        catch (EndMatchException e){
            match.currentWins();
        }
        System.out.println("potere di " + name + " attivato");
    }

    public static String toStringEffect(GodCard card) {
        return card.getName() + "\n" + card.getTimingName() + ": Your Worker may move one additional time,\n" +
                "but not back to the space it started on.";
    }
}
