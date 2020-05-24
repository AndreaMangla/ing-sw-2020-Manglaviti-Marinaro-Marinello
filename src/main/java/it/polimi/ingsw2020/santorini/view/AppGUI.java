package it.polimi.ingsw2020.santorini.view;

import it.polimi.ingsw2020.santorini.model.Cell;
import it.polimi.ingsw2020.santorini.network.client.Client;
import it.polimi.ingsw2020.santorini.utils.Message;
import it.polimi.ingsw2020.santorini.utils.PhaseType;
import it.polimi.ingsw2020.santorini.utils.messages.actions.*;
import it.polimi.ingsw2020.santorini.utils.messages.actions.AskMoveSelectionMessage;
import it.polimi.ingsw2020.santorini.utils.messages.errors.IllegalPositionMessage;
import it.polimi.ingsw2020.santorini.utils.messages.matchMessage.LoginMessage;
import it.polimi.ingsw2020.santorini.utils.messages.matchMessage.MatchSetupMessage;
import it.polimi.ingsw2020.santorini.utils.messages.matchMessage.MatchStateMessage;
import it.polimi.ingsw2020.santorini.utils.messages.matchMessage.UpdateMessage;
import it.polimi.ingsw2020.santorini.view.gui.BoardController;
import it.polimi.ingsw2020.santorini.view.gui.InfoMatchController;
import it.polimi.ingsw2020.santorini.view.gui.RegisterController;
import it.polimi.ingsw2020.santorini.view.gui.SelectionBuilderController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class AppGUI extends Application implements ViewInterface{

    private Client client;

    private Stage primaryStage;

    private RegisterController registerController;

    private BoardController boardController;

    private InfoMatchController infoMatchController;

    private SelectionBuilderController selectionBuilderController;

    private Scene registerScene;

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    /**
     * metodo in cui si chiede l'iP del server, dopodichè di fanno inserire username, data di nascita e tipo di partita (numero di giocatori nella partita)
     */
    @Override
    public void displaySetupWindow(boolean firstTime) {
        Parent root;

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/FXML/Register.fxml"));

        if(firstTime) {
            try {
                root = fxmlLoader.load();
                registerScene = new Scene(root);
            } catch (IOException e) {
                root = null;
                registerScene = new Scene(new Label("Graphical Resources not found. Fatal Error"));
            }
            registerController = fxmlLoader.getController();
            registerController.setClient(client);
            primaryStage.setTitle("Santorini");
            primaryStage.setScene(registerScene);
            primaryStage.show();
        } else {
            // si fa un alert sulla scene, disabilitando tutto tranne casella username
            //1 mostrare di nuovo RegisterController non facendo partire RegisterAction, ma un nuovo metodo che Mostri tutto uguale a Prima con
            //soltanto la casella username da mostrare.
            //2 Creare una nuova schermata che fa entrare solo l'username( ma ti costrine a creare una nuova classe)
        }

    }


    /**
     * metodo per intrattenere l'utente mentre aspettiamo altri utenti che vogliono giocare
     *
     * @param message
     */
    @Override
    public void displayLoadingWindow(String message) {
        //DA VEDERE
    }

    /**
     * metodo in cui si da il welcome alla partita, vengono assegnate le carte e i colori.
     * viene visualizzata una board semplificata per facilitare il posizionamento delle pedine
     *
     * @param matchSetupMessage
     */
    @Override
    public void displayMatchSetupWindow(MatchSetupMessage matchSetupMessage) {
        Platform.runLater(()-> {

            Stage stage = new Stage();
            Parent children;
            Scene scene;
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/FXML/InfoMatch.fxml"));

            try {
                children = fxmlLoader.load();
                scene = new Scene(children);
            } catch (IOException e) {
                children = null;
                scene = new Scene(new Label ("ERROR "));
            }
            infoMatchController = fxmlLoader.getController();
            infoMatchController.setClient(client);
            stage.setTitle("START GAME");
            stage.setScene(scene);
            stage.show();

        });

    }

    /**
     * metodo addetto alla selezione dei builder secondo l'ordine definito dal controller
     *
     * @param turnPlayerMessage
     */
    @Override
    public void displaySelectionBuilderWindow(MatchStateMessage turnPlayerMessage) {
        Stage stage = new Stage();
        Parent children;
        Scene scene;

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/FXML/board.fxml"));

        try {
            children = fxmlLoader.load();
            scene = new Scene(children);
        } catch (IOException e) {
            children = null;
            scene = new Scene(new Label ("ERROR "));
        }
        selectionBuilderController = fxmlLoader.getController();
        selectionBuilderController.setClient(client);
        stage.setTitle("SELECT THE CELL WHERE YOU WANT TO PUT YOUR BUILDER");
        stage.setScene(scene);
        stage.show();
    }


    @Override
    public void displayNewSelectionBuilderWindow(IllegalPositionMessage message) {
        Stage stage = new Stage();
        Parent children;
        Scene scene;

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/FXML/board.fxml"));

        try {
            children = fxmlLoader.load();
            scene = new Scene(children);
        } catch (IOException e) {
            children = null;
            scene = new Scene(new Label("ERROR "));
        }

        stage.setTitle("POSITION SELECTED IS OCCUPIED. INSERT NEW COORDINATES");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * metodo che aggiorna la board ogni volta che viene fatta una mossa (modificato il model)
     * parametro un messaggio con scritte le informazioni sulla board.
     *
     * @param message
     */
    @Override
    public void updateMatch(UpdateMessage message) {

    }

    /**
     * far visualizzare la board con le pedine e tutta l'interfaccia testuale e il primo giocatore che gioca
     *
     * @param message
     */
    @Override
    public void displayStartTurn(UpdateMessage message) {

    }

    @Override
    public void displaySP(UpdateMessage updateMessage, PhaseType phase) {

    }

    @Override
    public void displayMoveUpdate(UpdateMessage updateMessage) {
        Stage stage = new Stage();
        Parent children;
        Scene scene;

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/FXML/newUsername.fxml"));

        try {
            children = fxmlLoader.load();
            scene = new Scene(children);
        } catch (IOException e) {
            children = null;
            scene = new Scene(new Label ("ERROR "));
        }

        stage.setTitle("NOT VALID USERNAME");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void displayBuildUpdate(UpdateMessage updateMessage) {
        Stage stage = new Stage();
        Parent children;
        Scene scene;

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/FXML/newUsername.fxml"));

        try {
            children = fxmlLoader.load();
            scene = new Scene(children);
        } catch (IOException e) {
            children = null;
            scene = new Scene(new Label ("ERROR "));
        }

        stage.setTitle("NOT VALID USERNAME");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void displayParametersSelection(MatchStateMessage message) {

    }

    @Override
    public void displayChooseBuilder(MatchStateMessage message) {
        Stage stage = new Stage();
        Parent children;
        Scene scene;

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/FXML/newUsername.fxml"));

        try {
            children = fxmlLoader.load();
            scene = new Scene(children);
        } catch (IOException e) {
            children = null;
            scene = new Scene(new Label ("ERROR "));
        }

        stage.setTitle("NOT VALID USERNAME");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * prova
     *
     * @param updateMessage parameter
     */
    @Override
    public void displayEndTurn(UpdateMessage updateMessage) {

    }

    @Override
    public void displayWouldActivate(MatchStateMessage question) {
        Stage stage = new Stage();
        Parent children;
        Scene scene;

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/FXML/newUsername.fxml"));

        try {
            children = fxmlLoader.load();
            scene = new Scene(children);
        } catch (IOException e) {
            children = null;
            scene = new Scene(new Label ("ERROR "));
        }

        stage.setTitle("NOT VALID USERNAME");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * metodo che mostra all'utente le possibili mosse che il builder selezionato può fare
     *
     * @param message
     */
    @Override
    public void displayPossibleMoves(AskMoveSelectionMessage message) {

    }

    /**
     * metodo che mostra all'utente le possibili costruzioni che il builder mosso può fare
     *
     * @param message
     */
    @Override
    public void displayPossibleBuildings(AskBuildSelectionMessage message) {

    }

    /**
     * metodo che mostra vincitori e vinti. conclude la partita con epic sax guy
     *
     * @param winner
     */
    @Override
    public void displayEndMatch(String winner) {

    }


    /**
     * metodo che mostra all'utente possibili errori che sono capitati
     *
     * @param error
     */
    @Override
    public void displayErrorMessage(String error) {

    }

    @Override
    public void showBoard(ArrayList<Cell> listOfCells) {

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        client = new Client();
        client.setView(this);

        displaySetupWindow(true);

    }
}
