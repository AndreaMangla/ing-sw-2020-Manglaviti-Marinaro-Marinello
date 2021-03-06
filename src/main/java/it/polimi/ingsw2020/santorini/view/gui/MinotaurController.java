package it.polimi.ingsw2020.santorini.view.gui;

import it.polimi.ingsw2020.santorini.model.Board;
import it.polimi.ingsw2020.santorini.model.Builder;
import it.polimi.ingsw2020.santorini.utils.AccessType;
import it.polimi.ingsw2020.santorini.utils.Direction;
import it.polimi.ingsw2020.santorini.utils.messages.matchMessage.MatchStateMessage;
import it.polimi.ingsw2020.santorini.view.AppGUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class MinotaurController {
    private Stage stage;
    private Button[][] matrix = new Button[3][3];
    private Builder chosen = null;
    private MatchStateMessage matchStateMessage;

    public void setStage(Stage stage){
        this.stage = stage;
    }

    public void setMatchStateMessage(MatchStateMessage matchStateMessage) {
        this.matchStateMessage = matchStateMessage;
    }

    public Builder getChosen() {
        return chosen;
    }

    @FXML
    Label text;
    @FXML
    Label text2;
    @FXML
    Button b00;
    @FXML
    Button b01;
    @FXML
    Button b02;
    @FXML
    Button b10;
    @FXML
    Button b12;
    @FXML
    Button b20;
    @FXML
    Button b21;
    @FXML
    Button b22;
    @FXML
    Button F;
    @FXML
    Button M;

    /**
     * this method extracts the direction of the push chosen by the player
     * @param actionEvent is the event of the button clicked from which the method extracts the direction
     */
    @FXML
    public void push(ActionEvent actionEvent) {
        Direction direction = AppGUI.extractDirection(actionEvent, b00, b01, b02, b10, b12, b20, b21, b22);
        AppGUI.getMinotaurParamMessage().setOpponentBuilderDirection(direction);
        stage.setOnCloseRequest(e->stage.close());
        stage.close();
    }

    /**
     * this method initialize 3*3 matrix of Minotaur
     * @param chosen is the builder chosen to push the opponent's builder
     * @param MinotaurMatrix is the matrix of integers that represents if there is an opponent's builder that can be pushed by the
     * chosen builder
     */
    public void initializeMinotaurMatrix(Builder chosen, int[][] MinotaurMatrix){
        AppGUI.buildButtonMatrices(matrix, b00, b01, b02, b10, b12, b20, b21, b22);
        for(int i = 0; i < 3; ++i)
            for(int j = 0; j < 3; ++j)
                if(MinotaurMatrix[i][j] != 0)
                    try {
                        if (matchStateMessage.getBoard()[chosen.getPosX() + (i - 1) * 2][chosen.getPosY() + (j - 1) * 2].getStatus() != AccessType.FREE)
                            MinotaurMatrix[i][j] = 0;
                    }catch (IndexOutOfBoundsException e){
                        MinotaurMatrix[i][j] = 0;
                    }
        for(int i=0; i<3; ++i){
            for( int j=0 ; j < 3; ++j){
                if(i!=1 || j!= 1){
                    if(MinotaurMatrix[i][j]==0){
                        matrix[i][j].setDisable(true);
                        matrix[i][j].setStyle("-fx-background-color: #ff0000");
                    }
                }
            }
        }
    }

    /**
     * this method is used to let the player choose the builder that he wants to use
     * @param actionEvent is the event of the mouse clicked from which the method extracts the builder chosen
     */
    @FXML
    public void selectGender(ActionEvent actionEvent) {
        Button pos = (Button) actionEvent.getSource();
        char yourBuilderGender;
        if(pos.getId().equals("F")){
            chosen = matchStateMessage.getCurrentPlayer().getBuilderF();
            yourBuilderGender = 'F';
        }else{
            chosen = matchStateMessage.getCurrentPlayer().getBuilderM();
            yourBuilderGender = 'M';
        }
        chosen.setBoard(new Board(matchStateMessage.getBoard()));
        chosen.setPlayer(matchStateMessage.getCurrentPlayer());
        AppGUI.getMinotaurParamMessage().setPlayingBuilderSex(yourBuilderGender);
        stage.setOnCloseRequest(e->stage.close());
        stage.close();
    }

    /**
     * this method is used to disable button if that builder can't be chosen
     */
    public void initializeButtons() {
        int[] posBuilder = new int[2];
        Direction direction = null;
        chosen = matchStateMessage.getCurrentPlayer().getBuilderM();
        posBuilder[0] = matchStateMessage.getCurrentPlayer().getBuilderM().getPosX();
        posBuilder[1] = matchStateMessage.getCurrentPlayer().getBuilderM().getPosY();
        chosen.setBoard(new Board(matchStateMessage.getBoard()));
        chosen.setPlayer(matchStateMessage.getCurrentPlayer());
        int[][] possibleSwap = Board.neighboringSwappingCell(chosen, AccessType.OCCUPIED);
        for(int i = 0; i < 3; ++i)
            for(int j = 0; j < 3; ++j)
                if(possibleSwap[i][j] != 0)
                    try {
                        if (matchStateMessage.getBoard()[posBuilder[0] + (i - 1) * 2][posBuilder[1] + (j - 1) * 2].getStatus() != AccessType.FREE)
                            possibleSwap[i][j] = 0;
                    }catch (IndexOutOfBoundsException e){
                        possibleSwap[i][j] = 0;
                    }

        boolean allZeros = true;
        for(int i = 0; i < 3 && allZeros; ++i)
            for(int j = 0; j < 3 && allZeros; ++j)
                if(possibleSwap[i][j] != 0) allZeros = false;

        if(allZeros){
            M.setDisable(true);
            M.setStyle("-fx-border-color: #ff0000; -fx-border-width: 5px;");
        }else
            M.setStyle("-fx-border-color: #00ff00; -fx-border-width: 5px;");

        chosen = matchStateMessage.getCurrentPlayer().getBuilderF();
        posBuilder[0] = matchStateMessage.getCurrentPlayer().getBuilderF().getPosX();
        posBuilder[1] = matchStateMessage.getCurrentPlayer().getBuilderF().getPosY();
        chosen.setBoard(new Board(matchStateMessage.getBoard()));
        chosen.setPlayer(matchStateMessage.getCurrentPlayer());
        possibleSwap = Board.neighboringSwappingCell(chosen, AccessType.OCCUPIED);
        for(int i = 0; i < 3; ++i)
            for(int j = 0; j < 3; ++j)
                if(possibleSwap[i][j] != 0)
                    try {
                        if (matchStateMessage.getBoard()[posBuilder[0] + (i - 1) * 2][posBuilder[1] + (j - 1) * 2].getStatus() != AccessType.FREE)
                            possibleSwap[i][j] = 0;
                    }catch (IndexOutOfBoundsException e){
                        possibleSwap[i][j] = 0;
                    }
        allZeros = true;
        for(int i = 0; i < 3 && allZeros; ++i)
            for(int j = 0; j < 3 && allZeros; ++j)
                if(possibleSwap[i][j] != 0) allZeros = false;
        if(allZeros){
            F.setDisable(true);
            F.setStyle("-fx-border-color: #ff0000; -fx-border-width: 5px;");
        }else
            F.setStyle("-fx-border-color: #00ff00; -fx-border-width: 5px;");
    }
}
