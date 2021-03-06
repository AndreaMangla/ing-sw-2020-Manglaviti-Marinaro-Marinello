package it.polimi.ingsw2020.santorini.model.gods;

import it.polimi.ingsw2020.santorini.controller.TurnLogic;
import it.polimi.ingsw2020.santorini.exceptions.IllegalConstructionException;
import it.polimi.ingsw2020.santorini.model.*;
import it.polimi.ingsw2020.santorini.utils.Message;
import it.polimi.ingsw2020.santorini.utils.PhaseType;
import it.polimi.ingsw2020.santorini.utils.messages.godsParam.HestiaParamMessage;

public class Hestia extends GodCard {

    public Hestia(){
        name = getClass().getSimpleName();
        maxPlayersNumber = 3;
        timing = PhaseType.STANDBY_PHASE_3;
        mandatory = false;
        needParameters = true;
    }

    @Override
    public boolean canActivate(Match match) {
        int[][] neighboringLevelCell = prepareMatrix(match.getCurrentPlayer());
        for(int i = 0; i < 3; ++i)
            for(int j = 0; j < 3; ++j)
                if (neighboringLevelCell[i][j] >= 0 && neighboringLevelCell[i][j] < 4) return true;
        return false;
    }

    public static int[][] prepareMatrix(Player currentPlayer) {
        int[][] neighboringLevelCell = Board.neighboringLevelCell(currentPlayer.getPlayingBuilder());
        if(currentPlayer.getPlayingBuilder().getPosX() == 1 || currentPlayer.getPlayingBuilder().getPosX() == 5){
            neighboringLevelCell[1][0] = -1;
            neighboringLevelCell[1][1] = -1;
            neighboringLevelCell[1][2] = -1;
        } else if(currentPlayer.getPlayingBuilder().getPosX() == 2){
            neighboringLevelCell[0][0] = -1;
            neighboringLevelCell[0][1] = -1;
            neighboringLevelCell[0][2] = -1;
        } else if(currentPlayer.getPlayingBuilder().getPosX() == 4){
            neighboringLevelCell[2][0] = -1;
            neighboringLevelCell[2][1] = -1;
            neighboringLevelCell[2][2] = -1;
        }
        if(currentPlayer.getPlayingBuilder().getPosY() == 1 || currentPlayer.getPlayingBuilder().getPosY() == 5){
            neighboringLevelCell[0][1] = -1;
            neighboringLevelCell[1][1] = -1;
            neighboringLevelCell[2][1] = -1;
        } else if(currentPlayer.getPlayingBuilder().getPosY() == 2){
            neighboringLevelCell[0][0] = -1;
            neighboringLevelCell[1][0] = -1;
            neighboringLevelCell[2][0] = -1;
        } else if(currentPlayer.getPlayingBuilder().getPosY() == 4){
            neighboringLevelCell[0][2] = -1;
            neighboringLevelCell[1][2] = -1;
            neighboringLevelCell[2][2] = -1;
        }
        return neighboringLevelCell;
    }

    @Override
    public void invokeGod(Match match, Message message, TurnLogic turnManager) {
        HestiaParamMessage param = message.deserializeHestiaParamMessage();
        try {
            match.getCurrentPlayer().getPlayingBuilder().build(param.getDirection(), match);
        } catch (IllegalConstructionException e) {
            e.printStackTrace();
        }
        System.out.println("potere di " + name + " attivato");
    }

    public static String toStringEffect(GodCard card) {
        return "Your Build" + ": Your Worker may build one additional time.\n" +
                "The additional build cannot be on a perimeter space.";
    }
}
