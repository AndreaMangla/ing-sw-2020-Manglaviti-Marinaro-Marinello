package it.polimi.ingsw2020.santorini.view.gui;

import it.polimi.ingsw2020.santorini.model.Board;
import it.polimi.ingsw2020.santorini.model.gods.Hestia;
import it.polimi.ingsw2020.santorini.utils.Direction;
import it.polimi.ingsw2020.santorini.utils.messages.matchMessage.MatchStateMessage;
import it.polimi.ingsw2020.santorini.view.AppGUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class HestiaController {

    private Stage stage;
    private Button[][] matrix = new Button[3][3];
    private Label[][] labelMatrix = new Label[3][3];
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
     * his method extract the direction where to build, chosen by the player, and then set it in HestiaParamMessage.
     * @param actionEvent is the event of the button clicked
     */
    @FXML
    public void build(ActionEvent actionEvent) {
        Direction direction = AppGUI.extractDirection(actionEvent, b00, b01, b02, b10, b12, b20, b21, b22);
        AppGUI.getHestiaParamMessage().setDirection(direction);
        stage.setOnCloseRequest(e->stage.close());
        stage.close();
    }


    /**
     * this Method initialize the 3*3 Matrix of Hestia
     */
    public void initializeHestiaMatrix(){
        AppGUI.buildButtonMatrices(matrix, b00, b01, b02, b10, b12, b20, b21, b22);
        AppGUI.buildLabelMatrices(labelMatrix, p00, p01, p02, p10, p12, p20, p21, p22);
        matchStateMessage.getCurrentPlayer().getPlayingBuilder().setBoard(new Board(matchStateMessage.getBoard()));
        matchStateMessage.getCurrentPlayer().getPlayingBuilder().setPlayer(matchStateMessage.getCurrentPlayer());
        int[][] neighboringLevelCell = Hestia.prepareMatrix(matchStateMessage.getCurrentPlayer());
        AppGUI.printMatrix(neighboringLevelCell, matrix, labelMatrix);
    }
}