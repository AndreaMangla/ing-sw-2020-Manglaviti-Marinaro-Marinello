package it.polimi.ingsw2020.santorini.view.gui;

import it.polimi.ingsw2020.santorini.model.Board;
import it.polimi.ingsw2020.santorini.model.Builder;
import it.polimi.ingsw2020.santorini.utils.Direction;
import it.polimi.ingsw2020.santorini.utils.messages.matchMessage.MatchStateMessage;
import it.polimi.ingsw2020.santorini.view.AppGUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class AresController {
    private Stage stage;
    private Button[][] matrix = new Button[3][3];
    private Label[][] labelMatrix = new Label[3][3];
    public char demolitionBuilderSex;
    private MatchStateMessage matchStateMessage;

    public void setStage(Stage stage){
        this.stage = stage;
    }

    public void setMatchStateMessage(MatchStateMessage matchStateMessage) {
        this.matchStateMessage = matchStateMessage;
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
    Label p00;
    @FXML
    Label p01;
    @FXML
    Label p02;
    @FXML
    Label p10;
    @FXML
    Label p12;
    @FXML
    Label p20;
    @FXML
    Label p21;
    @FXML
    Label p22;

    /**
     * the method checks which one of the builders is the unmoved one. After that it checks and disables the buttons that represents
     * directions not allowed by rules
     */
    public void initializeAresMatrix(){
        Builder demolitionBuilder;
        AppGUI.buildButtonMatrices(matrix, b00, b01, b02, b10, b12, b20, b21, b22);
        AppGUI.buildLabelMatrices(labelMatrix, p00, p01, p02, p10, p12, p20, p21, p22);
        if(matchStateMessage.getCurrentPlayer().getPlayingBuilder().getGender() == '\u2640'){
            demolitionBuilder = matchStateMessage.getCurrentPlayer().getBuilderM();
            demolitionBuilderSex = 'M';
        } else {
            demolitionBuilder = matchStateMessage.getCurrentPlayer().getBuilderF();
            demolitionBuilderSex = 'F';
        }
        demolitionBuilder.setBoard(new Board(matchStateMessage.getBoard()));
        demolitionBuilder.setPlayer(matchStateMessage.getCurrentPlayer());

        int[][] neighboringLevelCell = Board.neighboringLevelCell(demolitionBuilder);
        for(int i=0; i<3; ++i){
            for (int j=0; j<3;++j){
                if(i!=1 || j!= 1){
                    if (neighboringLevelCell[i][j] <= 0 || neighboringLevelCell[i][j] >= 4) {
                        matrix[i][j].setStyle("-fx-background-color: #ff0000");
                        matrix[i][j].setDisable(true);
                    }
                }
            }
        }
    }

    /**
     * the method extract from the actionEvent the direction clicked by the player and add it to AresParamMessage
     * @param actionEvent is the event of the click on one of the buttons representing the directions
     */
    @FXML
    public void selectDemolition(ActionEvent actionEvent) {
        Direction direction = AppGUI.extractDirection(actionEvent, b00, b01, b02, b10, b12, b20, b21, b22);
        AppGUI.getAresParamMessage().setDemolitionBuilderSex(demolitionBuilderSex);
        AppGUI.getAresParamMessage().setTargetedBlock(direction);
        stage.setOnCloseRequest(e->stage.close());
        stage.close();
    }
}
