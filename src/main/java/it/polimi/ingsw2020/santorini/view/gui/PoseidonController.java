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

import java.util.ArrayList;

public class PoseidonController {
    private Stage stage;
    private Button[][] matrix = new Button[3][3];
    private Label[][] labelMatrix = new Label[3][3];
    private MatchStateMessage matchStateMessage;
    private int number = 0;
    private ArrayList<Direction> directions = new ArrayList<>();

    public void setStage(Stage stage){
        this.stage = stage;
    }

    public void setMatchStateMessage(MatchStateMessage matchStateMessage) {
        this.matchStateMessage = matchStateMessage;
    }

    @FXML
    Button one;
    @FXML
    Button two;
    @FXML
    Button three;
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
     * this method is used to select the direction of the build
     * @param actionEvent is the event of the button clicked from which the method extracts the direction
     */
    @FXML
    public void build(ActionEvent actionEvent) {
        Button pos = (Button) actionEvent.getSource();
        if(pos.equals(b00)){
            directions.add(Direction.NORTH_WEST);
            if(p00.getText().equals("3")){
                b00.setDisable(true);
            }
            p00.setText(String.valueOf(Integer.parseInt(p00.getText()) + 1));
        }else if ( pos.equals(b01)){
            directions.add(Direction.NORTH);
            if(p01.getText().equals("3")){
                b01.setDisable(true);
            }
            p01.setText(String.valueOf(Integer.parseInt(p01.getText()) + 1));
        }else if ( pos.equals(b02)){
            directions.add(Direction.NORTH_EAST);
            if(p02.getText().equals("3")){
                b02.setDisable(true);
            }
            p02.setText(String.valueOf(Integer.parseInt(p02.getText()) + 1));
        }else if ( pos.equals(b10)){
            directions.add(Direction.WEST);
            if(p10.getText().equals("3")){
                b10.setDisable(true);
            }
            p10.setText(String.valueOf(Integer.parseInt(p10.getText()) + 1));
        }else if ( pos.equals(b12)){
            directions.add(Direction.EAST);
            if(p12.getText().equals("3")){
                b12.setDisable(true);
            }
            p12.setText(String.valueOf(Integer.parseInt(p12.getText()) + 1));
        }else if ( pos.equals(b20)){
            directions.add(Direction.SOUTH_WEST);
            if(p20.getText().equals("3")){
                b20.setDisable(true);
            }
            p20.setText(String.valueOf(Integer.parseInt(p20.getText()) + 1));
        }else if ( pos.equals(b21)){
            directions.add(Direction.SOUTH);
            if(p21.getText().equals("3")){
                b21.setDisable(true);
            }
            p21.setText(String.valueOf(Integer.parseInt(p21.getText()) + 1));
        }else if ( pos.equals(b22)){
            directions.add(Direction.SOUTH_EAST);
            if(p22.getText().equals("3")){
                b22.setDisable(true);
            }
            p22.setText(String.valueOf(Integer.parseInt(p22.getText()) + 1));
        }
        number++;
        if(number==AppGUI.getPoseidonParamMessage().getNumberOfBuild()) {
            b00.setDisable(true);
            b01.setDisable(true);
            b02.setDisable(true);
            b10.setDisable(true);
            b12.setDisable(true);
            b20.setDisable(true);
            b21.setDisable(true);
            b22.setDisable(true);
            AppGUI.getPoseidonParamMessage().setDirection(directions);
            stage.setOnCloseRequest(e->stage.close());
            stage.close();
        }
    }

    /**
     * this Method initialize the 3*3 Matrix of Poseidon power
     */
    public void initializePoseidonMatrix(){
        AppGUI.buildButtonMatrices(matrix, b00, b01, b02, b10, b12, b20, b21, b22);
        AppGUI.buildLabelMatrices(labelMatrix, p00, p01, p02, p10, p12, p20, p21, p22);

        Builder constructionBuilder = null;
        char constructionBuilderSex = 'o';
        if (matchStateMessage.getCurrentPlayer().getPlayingBuilder().getGender() == '\u2640') {
            constructionBuilder = matchStateMessage.getCurrentPlayer().getBuilderM();
            constructionBuilderSex = 'M';
        } else {
            constructionBuilder = matchStateMessage.getCurrentPlayer().getBuilderF();
            constructionBuilderSex = 'F';
        }
        AppGUI.getPoseidonParamMessage().setConstructionGender(constructionBuilderSex);
        //Salvataggio della possible buildings
        constructionBuilder.setBoard(new Board(matchStateMessage.getBoard()));
        constructionBuilder.setPlayer(matchStateMessage.getCurrentPlayer());
        int[][] possibleBuildingsP = Board.neighboringLevelCell(constructionBuilder);
        AppGUI.printMatrix(possibleBuildingsP, matrix, labelMatrix);
    }

    /**
     * this method is used to disable button if that builder can't be chosen
     */
    public void initializeButtons(){
        Builder constructionBuilder;
        char constructionBuilderSex = 'o';
        if (matchStateMessage.getCurrentPlayer().getPlayingBuilder().getGender() == '\u2640') {
            constructionBuilder = matchStateMessage.getCurrentPlayer().getBuilderM();
            constructionBuilderSex = 'M';
        } else {
            constructionBuilder = matchStateMessage.getCurrentPlayer().getBuilderF();
            constructionBuilderSex = 'F';
        }
        constructionBuilder.setBoard(new Board(matchStateMessage.getBoard()));
        constructionBuilder.setPlayer(matchStateMessage.getCurrentPlayer());
        int[][] possibleBuildingsP = Board.neighboringLevelCell(constructionBuilder);
        int count = 0;
        for(int i = 0; i < 3; ++i)
            for(int j = 0; j < 3; ++j)
                if(possibleBuildingsP[i][j] >= 0 && possibleBuildingsP[i][j] < 4) count = count + 4 - possibleBuildingsP[i][j];

        if(count==1){
            one.setStyle("-fx-border-color: #00ff00; -fx-border-width: 5px;");
            two.setDisable(true);
            two.setStyle("-fx-border-color: #ff0000; -fx-border-width: 5px;");
            three.setDisable(true);
            three.setStyle("-fx-border-color: #ff0000; -fx-border-width: 5px;");
        }else if(count==2){
            one.setStyle("-fx-border-color: #00ff00; -fx-border-width: 5px;");
            two.setStyle("-fx-border-color: #00ff00; -fx-border-width: 5px;");
            three.setDisable(true);
            three.setStyle("-fx-border-color: #ff0000; -fx-border-width: 5px;");
        }else {
            one.setStyle("-fx-border-color: #00ff00; -fx-border-width: 5px;");
            two.setStyle("-fx-border-color: #00ff00; -fx-border-width: 5px;");
            three.setStyle("-fx-border-color: #00ff00; -fx-border-width: 5px;");
        }
    }

    /**
     * this method set the number of buildings selected by the player for Poseidon power
     * @param actionEvent is the event of the mouse clicked
     */
    @FXML
    public void selectNumber(ActionEvent actionEvent) {
        Button pos = (Button) actionEvent.getSource();
        int selectedNumber = 0;
        if(pos.getId().equals("one")) selectedNumber = 1;
        else if(pos.getId().equals("two")) selectedNumber = 2;
        else selectedNumber = 3;
        one.setDisable(true);
        two.setDisable(true);
        three.setDisable(true);
        AppGUI.getPoseidonParamMessage().setNumberOfBuild(selectedNumber);
        stage.setOnCloseRequest(e->stage.close());
        stage.close();
    }
}