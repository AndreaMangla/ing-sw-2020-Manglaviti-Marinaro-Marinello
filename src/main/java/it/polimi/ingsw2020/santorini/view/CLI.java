package it.polimi.ingsw2020.santorini.view;

import it.polimi.ingsw2020.santorini.exceptions.UnexpectedGodException;
import it.polimi.ingsw2020.santorini.model.*;
import it.polimi.ingsw2020.santorini.network.client.Client;
import it.polimi.ingsw2020.santorini.network.client.ServerAdapter;
import it.polimi.ingsw2020.santorini.network.client.ViewAdapter;
import it.polimi.ingsw2020.santorini.utils.*;
import it.polimi.ingsw2020.santorini.utils.messages.actions.*;
import it.polimi.ingsw2020.santorini.utils.messages.errors.*;
import it.polimi.ingsw2020.santorini.utils.messages.godsParam.*;
import it.polimi.ingsw2020.santorini.utils.messages.matchMessage.*;

import java.io.IOException;
import java.text.*;
import java.util.*;

@SuppressWarnings("deprecation")

public class CLI implements ViewInterface {

    private Client client;
    private Scanner scannerIn;
    private ArrayList<Player> listOfPlayers;

    public CLI(){
        this.scannerIn = new Scanner(System.in);
    }

    /**
     * method in which it's asked to the client to insert server's IP, and after that the username, birth date and type of match(number of players)
     * metodo in cui si chiede l'iP del server, dopodichè di fanno inserire username, data di nascita e tipo di partita (numero di giocatori nella partita)
     * @param firstTime is true if it is the first time we call the method
     * is false if the username is unavailable, and ask the client a new username
     */
    @Override
    public void displaySetupWindow(boolean firstTime) {
        if(firstTime) {
            client = new Client();
            client.setView(this);
            String ip;
            boolean wrong;
            do {
                try {
                    System.out.print("Insert server's IP address: ");
                    ip = scannerIn.nextLine();
                    client.setNetworkHandler(new ServerAdapter(client, ip));
                    client.setViewHandler(new ViewAdapter(client));
                    wrong = false;
                } catch (IOException e) {
                    System.out.println("Olympus is unreachable, try a new IP");
                    wrong = true;
                }
            } while (wrong);

            client.getNetworkHandler().start();
            client.getViewHandler().start();

            do {
                try {
                    System.out.print("Insert your username: ");
                    client.setUsername(scannerIn.nextLine());
                    wrong = false;
                } catch (InputMismatchException e) {
                    wrong = true;
                }
                if (wrong) System.out.println("the username you inserted is already taken, try a new one");
            } while (wrong);

            System.out.print("Insert your birth date (dd/mm/yyyy): ");
            String date = scannerIn.nextLine();
            DateFormat parser = new SimpleDateFormat("dd/MM/yyyy");
            client.setBirthDate(new Date(0, Calendar.JANUARY, 1));
            try {
                client.setBirthDate(parser.parse(date));
            } catch (ParseException e) {
                client.setBirthDate(new Date(0, Calendar.JANUARY, 1));
            }

            do {
                try {
                    System.out.print("Insert the number of players of the match (2 o 3): ");
                    client.setSelectedMatch(Integer.parseInt(scannerIn.nextLine()));
                    wrong = client.getSelectedMatch() != 2 && client.getSelectedMatch() != 3;
                } catch (NumberFormatException e) {
                    wrong = true;
                }
                if (wrong) System.out.println("Insert 2 o 3!");
            } while (wrong);

            Message message = new Message(client.getUsername());
            message.buildLoginMessage(new LoginMessage(client.getUsername(), client.getBirthDate(), client.getSelectedMatch()));
            client.getNetworkHandler().send(message);
        } else {
            System.out.print("Insert you new username: ");
            client.setUsername(scannerIn.nextLine());
            Message message = new Message(client.getUsername());
            message.buildLoginMessage(new LoginMessage(client.getUsername(), client.getBirthDate(), client.getSelectedMatch()));
            client.getNetworkHandler().send(message);
        }
    }

    /**
     * method that display a Loading window to the client while the server waits other clients to join
     * metodo per intrattenere l'utente mentre aspettiamo altri utenti che vogliono giocare
     */
    @Override
    public void displayLoadingWindow(String message) {
        System.out.flush();
        System.out.println(message);
    }

    /**
     * method that gives the welcome to the clients and distributes color of the builders and Gods'cards
     * metodo in cui si da il welcome alla partita, vengono assegnate le carte e i colori.
     * viene visualizzata una board semplificata per facilitare il posizionamento delle pedine
     */
    @Override
    public void displayMatchSetupWindow(MatchSetupMessage matchSetupMessage) {
        System.out.flush();
        System.out.println("Match Created!\n");
        System.out.println("The order established by the Gods is: ");
        listOfPlayers = matchSetupMessage.getPlayers();
        //scannerIn.nextLine();
        for(Player player : listOfPlayers) System.out.println(player.getColor() + player.getNickname() + Color.RESET);
        if(client.getUsername().equals(listOfPlayers.get(0).getNickname())) {
            // scelta delle divinità
            int[] selectedGods = new int[listOfPlayers.size()];
            int i = 0;
            boolean correct;
            for(Player p : listOfPlayers){
                do {
                    correct = true;
                    try {
                        System.out.println("Choose a God's card between these ones");
                        System.out.println(matchSetupMessage.getGods().toString());
                        System.out.print("Choice: ");
                        selectedGods[i] = Integer.parseInt(scannerIn.nextLine());
                        if(matchSetupMessage.getGods().isExtracted(selectedGods[i])) {
                            correct = false;
                            System.out.println("Insert one of the showed numbers!");
                        } else
                            matchSetupMessage.getGods().extract(selectedGods[i]);
                    } catch(NumberFormatException e){
                        System.out.println("Insert one of the showed numbers!");
                        correct = false;
                    }
                } while (!correct);
                ++i;
            }
            Message message = new Message(client.getUsername());
            message.buildSynchronizationMessage(SecondHeaderType.BEGIN_MATCH, new GameGodsSelectionMessage(selectedGods));
            client.getNetworkHandler().send(message);
        } else {
            System.out.println("The first player will now choose the Gods that will help you during this match");
            System.out.println("He is the closest to the Gods because he is the youngest");
            Message message = new Message(client.getUsername());
            message.buildSynchronizationMessage(SecondHeaderType.BEGIN_MATCH, null);
            client.getNetworkHandler().send(message);
        }
        System.out.println("Wait for Gods' directives");
    }

    /**
     * method that asks to the current player to choose which want he wants to his side
     * @param matchSetupMessage contains all the information needed to perform this choice
     */
    @Override
    public void displayGodSelectionWindow(MatchSetupMessage matchSetupMessage) {
        System.out.flush();
        if(client.getUsername().equals(matchSetupMessage.getPlayers().get(matchSetupMessage.getCurrentPlayerIndex()).getNickname())) {
            boolean correct;
            int selectedGod = -1;
            do {
                correct = true;
                try {
                    System.out.println("It's now your chance to choose your god!");
                    for(int i = 0; i < matchSetupMessage.getSelectedGods().size(); ++i){
                        for(GodFactotum g : GodFactotum.values()){
                            if(g.getCode() == matchSetupMessage.getSelectedGods().get(i)) {
                                try {
                                    System.out.println("Insert " + g.getCode() + ":\t" + g.getName());
                                } catch (UnexpectedGodException ignored){}
                                break;
                            }
                        }
                    }
                    System.out.print("God chosen: ");
                    selectedGod = Integer.parseInt(scannerIn.nextLine());
                    if(!matchSetupMessage.getSelectedGods().contains(selectedGod)) {
                        System.out.print("Insert one of the showed numbers!");
                        correct = false;
                    }
                } catch(NumberFormatException e){
                    System.out.print("Insert one of the showed numbers!!");
                    correct = false;
                }
            } while (!correct);
            System.out.println("");
            matchSetupMessage.getSelectedGods().remove((Integer)selectedGod);
            Message message = new Message(client.getUsername());
            message.buildInvokedGodMessage(new GodSelectionMessage(selectedGod, matchSetupMessage.getSelectedGods()));
            client.getNetworkHandler().send(message);
        } else {
            System.out.println("Wait, " + matchSetupMessage.getPlayers().get(matchSetupMessage.getCurrentPlayerIndex()).getNickname() + " is choosing his God!\n");
        }
    }

    /**
     * the method aks to the current player to choose where he wants to place the builders in the board. This method will be called only
     * in the setup phase
     * metodo addetto alla selezione dei builder secondo l'ordine definito dal controller
     * @param matchStateMessage contains all the references to the current player, the match and the board
     */
    @Override
    public void displaySelectionBuilderWindow(MatchStateMessage matchStateMessage) {
        System.out.flush();
        listOfPlayers = matchStateMessage.getPlayers();
        String currentPlayer = matchStateMessage.getCurrentPlayer().getNickname();
        if(client.getUsername().equals(currentPlayer)) {
            int[] builderM, builderF;
            builderM = new int[2];
            builderF = new int[2];
            System.out.printf("\n%s, It's your turn!You have to insert the coordinates of two cells where you want to place your builders !\n\n", currentPlayer);
            showBoard(matchStateMessage.getCells(), listOfPlayers);
            System.out.print("Let's start with the female builder\n");
            boolean wrong;

            do{
                wrong = true;
                try{
                    do{
                        System.out.print("Insert the row, it must be between 1 and 5 and also free, as you can see from the board: ");
                        builderF[0] = Integer.parseInt(scannerIn.nextLine());
                    } while(builderF[0] < 1 || builderF[0] > 5);
                    do{
                        System.out.print("Insert the column, it must be between 1 and 5 and also free, as you can see from the board: ");
                        builderF[1] = Integer.parseInt(scannerIn.nextLine());
                    } while(builderF[1] < 1 || builderF[1] > 5);
                    wrong = false;
                } catch (NumberFormatException ignored){}
                if(wrong) System.out.println("oh-oh, you have to insert free coordinates!");
            }while(wrong);

            do{
                wrong = false;
                try{
                    System.out.print("Now place the male builder\n");
                    do{
                        System.out.print("Insert the row, it must be between 1 and 5 and also free, as you can see from the board: ");
                        builderM[0] = Integer.parseInt(scannerIn.nextLine());
                    } while(builderM[0] < 1 || builderM[0] > 5);
                    do{
                        System.out.print("Insert the column, it must be between 1 and 5 and also free, as you can see from the board ");
                        builderM[1] = Integer.parseInt(scannerIn.nextLine());
                    } while(builderM[1] < 1 || builderM[1] > 5 || (builderM[1] == builderF[1] && builderM[0] == builderF[0]));
                    wrong = false;
                }catch (NumberFormatException e){
                    wrong = true;
                }
                if(wrong) System.out.println("oh-oh, You must insert the coordinate of a free cell!");
            }while(wrong);

            Message message = new Message(client.getUsername());
            message.buildSelectedBuilderPosMessage(new SelectedBuilderPositionMessage(client.getUsername(), builderF, builderM));
            System.out.println("Wait! The Gods are checking your choices...");
            client.getNetworkHandler().send(message);
        } else {
            System.out.printf("Ok, %s is choosing the positions of his builders! Wait...\n", currentPlayer);
        }
    }

    /**
     * the method will be called only if a player will insert wrong parameters in displaySelectionBuilderWindow, and will ask to the player
     * to insert them again.
     * @param message contains the username of the player that has made the mistake and also a boolean that indicates which builder has the
     * wrong coordinates
     */
    @Override
    public void displayNewSelectionBuilderWindow(IllegalPositionMessage message){
        int[] builderM = null;
        int[] builderF = null;
        boolean wrong;
        if(message.isBuilderFToChange()){
            builderF = new int[2];
            do {
                try {
                    System.out.print("Your female builder is in illegal position\n");
                    do {
                        System.out.print("Insert the row, it must be between 1 and 5 and also free, as you can see from the board:  ");
                        builderF[0] = Integer.parseInt(scannerIn.nextLine());
                    } while (builderF[0] < 1 || builderF[0] > 5);
                    do {
                        System.out.print("Insert the column, it must be between 1 and 5 and also free, as you can see from the board: ");
                        builderF[1] = Integer.parseInt(scannerIn.nextLine());
                    } while (builderF[1] < 1 || builderF[1] > 5);
                    wrong = false;
                } catch (NumberFormatException e) {
                    wrong = true;
                }
                if(wrong) System.out.println("Error, you must reinsert the coordinates!");
            } while(wrong);
        }
        if(message.isBuilderMToChange()) {
            builderM = new int[2];
            do{
                try{
                    System.out.print("Your male builder is in illegal position\n");
                    do {
                        System.out.print("Insert the row, it must be between 1 and 5 and also free, as you can see from the board:  ");
                        builderM[0] = Integer.parseInt(scannerIn.nextLine());
                    } while (builderM[0] < 1 || builderM[0] > 5);
                    do {
                        System.out.print("Insert the column, it must be between 1 and 5 and also free, as you can see from the board: ");
                        builderM[1] = Integer.parseInt(scannerIn.nextLine());
                    } while (builderM[1] < 1 || builderM[1] > 5);
                    wrong = false;
                }catch (InputMismatchException e){
                    wrong = true;
                }
                if(wrong) System.out.println("Error, you must reinsert the coordinates!");
            }while(wrong);
        }

        Message newPos = new Message(client.getUsername());
        newPos.buildSelectedBuilderPosMessage(new SelectedBuilderPositionMessage(client.getUsername(), builderF, builderM));
        System.out.println("Wait! The Gods are checking your choices...");
        client.getNetworkHandler().send(newPos);
    }

    /**
     * method that update the board every time that the model is modified, it does that by calling other methods
     * metodo che aggiorna la board ogni volta che viene fatta una mossa (modificato il model)
     * parametro un messaggio con scritte le informazioni sulla board.
     */
    @Override
    public void updateMatch(UpdateMessage updateMessage) {
        System.out.println("\n");
        try {
            Thread.sleep(25);
        } catch (InterruptedException ignored) {}
        switch(updateMessage.getPhase()){
            case START_TURN:
                System.out.println("Santorini - Start Turn");
                displayStartTurn(updateMessage);
                break;
            case STANDBY_PHASE_1:
                System.out.println("Santorini - StandBy Phase 1");
                displaySP(updateMessage, PhaseType.STANDBY_PHASE_1);
                break;
            case MOVE_PHASE:
                System.out.println("Santorini - Move Phase");
                displayMoveUpdate(updateMessage);
                break;
            case STANDBY_PHASE_2:
                System.out.println("Santorini - StandBy Phase 2");
                displaySP(updateMessage, PhaseType.STANDBY_PHASE_2);
                break;
            case BUILD_PHASE:
                System.out.println("Santorini - Build Phase");
                displayBuildUpdate(updateMessage);
                break;
            case STANDBY_PHASE_3:
                System.out.println("Santorini - StandBy Phase 3");
                displaySP(updateMessage, PhaseType.STANDBY_PHASE_3);
                break;
            case END_TURN:
                System.out.println("Santorini - End Phase");
                displayEndTurn(updateMessage);
                break;
            default:
                break;
        }
    }

    /**
     * method that shows board, builders, the textual interface and the first player to play
     * far visualizzare la board con le pedine e tutta l'interfaccia testuale e il primo giocatore che gioca
     */
    @Override
    public void displayStartTurn(UpdateMessage message) {
        System.out.flush();
        if(client.getUsername().equals(message.getCurrentPlayer().getNickname())) {
            System.out.println(message.getCurrentPlayer().getNickname() + " it's your turn!");
            Message nextPhase = new Message(client.getUsername());
            nextPhase.buildNextPhaseMessage();
            client.getNetworkHandler().send(nextPhase);
        } else {
            System.out.println("Now it's the turn of " + message.getCurrentPlayer().getNickname());
        }
        showBoard(message.getBoard(), message.getPlayers());
    }

    /**
     * the method asks to the player if he wants to activate his god's power
     * @param question is a message that contains the name of the player (that will receive the question) and the name of the god
     */
    @Override
    public void displayWouldActivate(MatchStateMessage question) {
        // richiediamo se il giocatore vuole attivare il potere divino
        if (client.getUsername().equals(question.getCurrentPlayer().getNickname())) {
            System.out.println("Do You want to ask for help to " + question.getCurrentPlayer().getDivinePower().getName() + "? Y-N");
            Message message = new Message(client.getUsername());
            boolean wrong;
            do{
                wrong = false;
                String answer = scannerIn.nextLine();
                answer = answer.toUpperCase();
                if (answer.equals("Y"))
                    message.buildActivateGodMessage(new ActivateGodMessage(true));
                else if (answer.equals("N"))
                    message.buildActivateGodMessage(new ActivateGodMessage(false));
                else
                    wrong = true;
            } while(wrong);
            client.getNetworkHandler().send(message);
        }
    }

    /**
     * the method calls a method linked to a specific God that will ask to the player to insert parameters needed to use the god
     * @param message is a message that contains the name of the player (that will receive the question) and the name of the god
     */
    @Override
    public void displayParametersSelection(MatchStateMessage message) {
        if(message.getCurrentPlayer().getNickname().equals(client.getUsername())) {
            Message selectedParam = new Message(client.getUsername());
            String god = message.getCurrentPlayer().getDivinePower().getName();
            System.out.println(god + " is here to help You!");
            switch (god) {
                case "Apollo":
                    selectedParam.buildApolloParamMessage(displayApolloParamSel(message));
                    break;
                case "Ares":
                    selectedParam.buildAresParamMessage(displayAresParamSel(message));
                    break;
                case "Artemis":
                    selectedParam.buildArtemisParamMessage(displayArtemisParamSel(message));
                    break;
                case "Atlas":
                    selectedParam.buildAtlasParamMessage(displayAtlasParamSel(message));
                    break;
                case "Demeter":
                    selectedParam.buildDemeterParamMessage(displayDemeterParamSel(message));
                    break;
                case "Hestia":
                    selectedParam.buildHestiaParamMessage(displayHestiaParamSel(message));
                    break;
                case "Minotaur":
                    selectedParam.buildMinotaurParamMessage(displayMinotaurParamSel(message));
                    break;
                case "Poseidon":
                    selectedParam.buildPoseidonParamMessage(displayPoseidonParamSel(message));
                    break;
                case "Prometheus":
                    selectedParam.buildPrometheusParamMessage(displayPrometheusParamSel(message));
                    break;
                default:
                    break;
            }
            client.getNetworkHandler().send(selectedParam);
        }
    }

    /**
     * the method prints to the players when and which god helped a player
     * @param updateMessage contains the name of the god used and the user name of the player that used it
     * @param phase is the phase in which the god helped the player
     */
    @Override
    public void displaySP(UpdateMessage updateMessage, PhaseType phase) {
        System.out.flush();
        System.out.print(updateMessage.getCurrentPlayer().getDivinePower().getName());
        if(updateMessage.getCurrentPlayer().getNickname().equals(client.getUsername())) {
            System.out.println(" has accepted you help request");
            Message nextPhase = new Message(client.getUsername());
            nextPhase.buildNextPhaseMessage();
            client.getNetworkHandler().send(nextPhase);
        }
        else
            System.out.println(" has helped " + updateMessage.getCurrentPlayer().getNickname());
        showBoard(updateMessage.getBoard(), updateMessage.getPlayers());
    }

    /**
     * the method asks to the current player which one of his builder he wants to move and build with.
     * To help the player in his choice the method also shows the board in its current state and it allows the player to select a builder only
     * if he or she can move
     * @param message contains the username of the player and
     */
    @Override
    public void displayChooseBuilder(MatchStateMessage message) {
        System.out.flush();
        if(message.getCurrentPlayer().getNickname().equals(client.getUsername())) {
            System.out.println("Which builder do You want to move? Male or Female?");
            Message chosenBuilder = new Message(client.getUsername());
            Builder builder = null;
            String choice = null;
            boolean wrong;
            message.getCurrentPlayer().getBuilderF().setBoard(new Board(message.getBoard()));
            message.getCurrentPlayer().getBuilderF().setPlayer(message.getCurrentPlayer());
            message.getCurrentPlayer().getBuilderM().setBoard(new Board(message.getBoard()));
            message.getCurrentPlayer().getBuilderM().setPlayer(message.getCurrentPlayer());
            do {
                if(message.getCurrentPlayer().getBuilderM().canMove())
                    System.out.println("Press M to select the male builder");
                if(message.getCurrentPlayer().getBuilderF().canMove())
                    System.out.println("Press F to select the female builder");
                try{
                    choice = scannerIn.nextLine();
                    choice = choice.toUpperCase();
                    if (choice.equals("M")) {
                        builder = message.getCurrentPlayer().getBuilderM();
                        builder.setBoard(new Board(message.getBoard()));
                        builder.setPlayer(message.getCurrentPlayer());
                        chosenBuilder.buildSelectedBuilderMessage(new SelectedBuilderMessage('M'));
                        wrong = !builder.canMove();
                    }
                    else if (choice.equals("F")){
                        builder = message.getCurrentPlayer().getBuilderF();
                        builder.setBoard(new Board(message.getBoard()));
                        builder.setPlayer(message.getCurrentPlayer());
                        chosenBuilder.buildSelectedBuilderMessage(new SelectedBuilderMessage('F'));
                        wrong = !builder.canMove();
                    }
                    else
                        wrong = true;
                } catch(InputMismatchException e){
                    wrong = true;
                    scannerIn.nextLine();
                }
            } while (wrong);
            client.getNetworkHandler().send(chosenBuilder);
        }
    }

    /**
     * the method asks to the player in which direction he wants to move the builder. The method shows a little matrix to represents the
     * allowed direction of movements. If the player inserts a wrong direction, he will be asked again to insert the direction
     * metodo che mostra all'utente le possibili mosse che il builder selezionato può fare
     */
    @Override
    public void displayPossibleMoves(AskMoveSelectionMessage message) {
        int[][] possibleMoves = message.getPossibleMoves();
        Direction direction = null;
        boolean wrong;
        int buttonPressed;
        String[] actions = new String[8];
        if (possibleMoves[0][0] != 0) actions[0] = ("Press 1 to move your builder to NORTH-WEST");
        if (possibleMoves[0][1] != 0) actions[1] = ("Press 2 to move your builder to NORTH");
        if (possibleMoves[0][2] != 0) actions[2] = ("Press 3 to move your builder builder to NORTH-EAST");
        if (possibleMoves[1][0] != 0) actions[3] = ("Press 4 to move your builder builder to WEST");
        if (possibleMoves[1][2] != 0) actions[4] = ("Press 5 to move your builder builder to EAST");
        if (possibleMoves[2][0] != 0) actions[5] = ("Press 6 to move your builder builder to SOUTH-WEST");
        if (possibleMoves[2][1] != 0) actions[6] = ("Press 7 to move your builder builder to SOUTH");
        if (possibleMoves[2][2] != 0) actions[7] = ("Press 8 to move your builder builder to SOUTH-EAST");
        do {
            try {
                scannerIn.reset();
                showPossibleMatrix(possibleMoves, 'm', actions);
                System.out.print("Insert the direction chosen for the movement: ");
                buttonPressed = Integer.parseInt(scannerIn.nextLine());

                if (buttonPressed == 1 && possibleMoves[0][0] != 0) {
                    direction = Direction.NORTH_WEST;
                    wrong = false;
                } else if (buttonPressed == 2 && possibleMoves[0][1] != 0) {
                    direction = Direction.NORTH;
                    wrong = false;
                } else if (buttonPressed == 3 && possibleMoves[0][2] != 0) {
                    direction = Direction.NORTH_EAST;
                    wrong = false;
                } else if (buttonPressed == 4 && possibleMoves[1][0] != 0) {
                    direction = Direction.WEST;
                    wrong = false;
                } else if (buttonPressed == 5 && possibleMoves[1][2] != 0) {
                    direction = Direction.EAST;
                    wrong = false;
                } else if (buttonPressed == 6 && possibleMoves[2][0] != 0) {
                    direction = Direction.SOUTH_WEST;
                    wrong = false;
                } else if (buttonPressed == 7 && possibleMoves[2][1] != 0) {
                    direction = Direction.SOUTH;
                    wrong = false;
                } else if (buttonPressed == 8 && possibleMoves[2][2] != 0) {
                    direction = Direction.SOUTH_EAST;
                    wrong = false;
                }else wrong = true;
            }catch(NumberFormatException e){
                wrong = true;
            }
            if(wrong) System.out.println("Invalid Direction! Insert a valid direction");
        }while(wrong);

        Message moveSelection = new Message(client.getUsername());
        moveSelection.buildSelectedMoveMessage(new SelectedMoveMessage(direction));
        client.getNetworkHandler().send(moveSelection);
        // display delle possible moves per restringere il campo d'azione -> vedi canMove
        // conversione della direzione
        // creazione del messaggio di selezione
        // invio del messaggio al server
    }

    /**
     * the method shows to the current player the board after the movement.
     * @param updateMessage contains the username of current player and also a reference to the board(used to show the board to the player).
     */
    @Override
    public void displayMoveUpdate(UpdateMessage updateMessage) {
        System.out.flush();
        showBoard(updateMessage.getBoard(), updateMessage.getPlayers());
        if(updateMessage.getCurrentPlayer().getNickname().equals(client.getUsername())) {
            Message nextPhase = new Message(client.getUsername());
            nextPhase.buildNextPhaseMessage();
            client.getNetworkHandler().send(nextPhase);
        }
    }

    /**
     * the method asks to the player in which direction he wants to build. The method shows a little matrix to represents the
     * allowed direction to construct near his playing builder. If the player inserts a wrong direction, he will be asked again to insert the direction
     * metodo che mostra all'utente le possibili costruzioni che il builder mosso può fare
     */
    @Override
    public void displayPossibleBuildings(AskBuildSelectionMessage message) {
        int[][] possibleBuildings = message.getPossibleBuildings();
        Direction direction = null;
        boolean wrong;
        int buttonPressed;
        String[] actions = new String[8];
        if (possibleBuildings[0][0] >= 0 && possibleBuildings[0][0] < 4) actions[0] = ("Press 1 to build with your builder NORTH-WEST");
        if (possibleBuildings[0][1] >= 0 && possibleBuildings[0][1] < 4) actions[1] = ("Press 2 to build with your builder NORTH");
        if (possibleBuildings[0][2] >= 0 && possibleBuildings[0][2] < 4) actions[2] = ("Press 3 to build with your builder NORTH-EAST");
        if (possibleBuildings[1][0] >= 0 && possibleBuildings[1][0] < 4) actions[3] = ("Press 4 to build with your builder WEST");
        if (possibleBuildings[1][2] >= 0 && possibleBuildings[1][2] < 4) actions[4] = ("Press 5 to build with your builder EAST");
        if (possibleBuildings[2][0] >= 0 && possibleBuildings[2][0] < 4) actions[5] = ("Press 6 to build with your builder SOUTH-WEST");
        if (possibleBuildings[2][1] >= 0 && possibleBuildings[2][1] < 4) actions[6] = ("Press 7 to build with your builder SOUTH");
        if (possibleBuildings[2][2] >= 0 && possibleBuildings[2][2] < 4) actions[7] = ("Press 8 to build with your builder SOUTH-EAST");
        do {
            showPossibleMatrix(possibleBuildings, 'b', actions);
            try {
                System.out.print("Insert the direction chosen for building: ");
                buttonPressed = Integer.parseInt(scannerIn.nextLine());

                if (buttonPressed == 1 && possibleBuildings[0][0] >= 0 && possibleBuildings[0][0] < 4) {
                    direction = Direction.NORTH_WEST;
                    wrong = false;
                } else if (buttonPressed == 2 && possibleBuildings[0][1] >= 0 && possibleBuildings[0][1] < 4) {
                    direction = Direction.NORTH;
                    wrong = false;
                } else if (buttonPressed == 3 && possibleBuildings[0][2] >= 0 && possibleBuildings[0][2] < 4) {
                    direction = Direction.NORTH_EAST;
                    wrong = false;
                } else if (buttonPressed == 4 && possibleBuildings[1][0] >= 0 && possibleBuildings[1][0] < 4) {
                    direction = Direction.WEST;
                    wrong = false;
                } else if (buttonPressed == 5 && possibleBuildings[1][2] >= 0 && possibleBuildings[1][2] < 4) {
                    direction = Direction.EAST;
                    wrong = false;
                } else if (buttonPressed == 6 && possibleBuildings[2][0] >= 0 && possibleBuildings[2][0] < 4) {
                    direction = Direction.SOUTH_WEST;
                    wrong = false;
                } else if (buttonPressed == 7 && possibleBuildings[2][1] >= 0 && possibleBuildings[2][1] < 4) {
                    direction = Direction.SOUTH;
                    wrong = false;
                } else if (buttonPressed == 8 && possibleBuildings[2][2] >= 0 && possibleBuildings[2][2] < 4) {
                    direction = Direction.SOUTH_EAST;
                    wrong = false;
                } else wrong = true;
            } catch(NumberFormatException e){
                wrong = true;
            }
            if(wrong) System.out.println("Invalid Direction! Insert a valid direction");
        }while(wrong);
        Message buildSelection = new Message(client.getUsername());
        buildSelection.buildSelectedBuildingMessage(new SelectedBuildingMessage(direction));
        client.getNetworkHandler().send(buildSelection);
    }

    /**
     * the method shows the board updated after the build phase
     * @param updateMessage is a message that contains the reference to the board, which is used to print the board itself
     */
    @Override
    public void displayBuildUpdate(UpdateMessage updateMessage) {
        System.out.flush();
        showBoard(updateMessage.getBoard(), updateMessage.getPlayers());
        if(updateMessage.getCurrentPlayer().getNickname().equals(client.getUsername())) {
            Message nextPhase = new Message(client.getUsername());
            nextPhase.buildNextPhaseMessage();
            client.getNetworkHandler().send(nextPhase);
        }
    }

    /**
     *the method prints a message to the players showing how the board has been modified during the turn and communicates the end of the turn
     * of the current player
     * @param updateMessage contains the the user name of current player and the reference to the board
     */
    @Override
    public void displayEndTurn(UpdateMessage updateMessage) {
        System.out.println("The turn of " + updateMessage.getCurrentPlayer().getNickname() + " is finished!");
        showBoard(updateMessage.getBoard(), updateMessage.getPlayers());
        Message nextPhase = new Message(client.getUsername());
        nextPhase.buildNextPhaseMessage();
        client.getNetworkHandler().send(nextPhase);
    }

    /**
     * method that shows winner. It then close the match or if the players wants to begin a new match
     * metodo che mostra vincitori e vinti. conclude la partita con epic sax guy
     * @param winner is the winner of the match
     */
    @Override
    public void displayEndMatch(String winner) {
        System.out.flush();
        System.out.println("AND THE WINNER IS... " + winner);
        boolean wrong;
        Message message = new Message(client.getUsername());
        do{
            wrong = false;
            System.out.println("Do You want to play a new match? Answer Y-N: ");
            String answer;
            boolean canGo;
            do {
                try {
                    canGo = true;
                    answer = scannerIn.next();
                    answer = answer.toUpperCase();
                    scannerIn.nextLine();
                    if (answer.equals("Y")) {
                        do{
                            try{
                                System.out.print("Insert the number of the players of the match (2 or 3): ");
                                client.setSelectedMatch(Integer.parseInt(scannerIn.nextLine()));
                                wrong = client.getSelectedMatch() != 2 && client.getSelectedMatch() != 3;
                            }catch (NumberFormatException e){
                                wrong = true;
                            }
                            if(wrong) System.out.println("Insert 2 o 3!");
                        }while(wrong);
                        message.buildNewMatchMessage(new NewMatchMessage(true, client.getSelectedMatch(), client.getBirthDate()));
                    } else if (answer.equals("N")) {
                        System.out.println("Thanks for playing with us, see you soon!");
                        message.buildNewMatchMessage(new NewMatchMessage(false, 0, null));
                        System.exit(0);
                    }
                    else
                        wrong = true;
                } catch (Exception e) {
                    canGo = false;
                }
            } while(!canGo);
        } while(wrong);
        client.getNetworkHandler().send(message);
    }

    /**
     * method that shows possible errors occurred
     * metodo che mostra all'utente possibili errori che sono capitati
     */
    @Override
    public void displayErrorMessage(String error) {
        System.out.println(error);
    }

    /**
     * the method asks to the current player to insert parameters need to use Apollo's power. These parameters are the choice of which builder
     * the player want to move (and swap with opponent's builder) and in which direction. If the builder selected cannot be moved, the method will choose for the player the other
     * builder. If the direction insert is not allowed the method wil ask to the player to insert it again. The method will also built the
     * message containing the parameters gathered
     * @param message contains references about the board and the currents player(About the match ).
     * @return the message containing the parameters gathered.
     */
    public ApolloParamMessage displayApolloParamSel(MatchStateMessage message){
        ApolloParamMessage apolloParamMessage = new ApolloParamMessage();
        //Inserire la stampa per la scelta del builder
        char yourBuilderGender = 'O';
        Builder chosen = null;
        Direction direction = null;
        System.out.println("Select the builder best suited to serve Apollo.Press M o F. Remember it must be near an opponent's builder to be worthy!");
        String choice = null;
        boolean wrong;
        do {
            try{
                choice = scannerIn.nextLine();
                choice = choice.toUpperCase();
                wrong  = false;
            } catch (InputMismatchException e){wrong = true;}

            if (choice.equals("M")) {
                chosen = message.getCurrentPlayer().getBuilderM();
                yourBuilderGender = 'M';
            } else if (choice.equals("F")){
                chosen = message.getCurrentPlayer().getBuilderF();
                yourBuilderGender = 'F';
            } else wrong = true;
            if(wrong){
                System.out.println("Wrong Letter, Insert again");
            }
        } while (wrong);

        chosen.setBoard(new Board(message.getBoard()));
        chosen.setPlayer(message.getCurrentPlayer());
        int[][] neighboringSwappingCell = Board.neighboringSwappingCell(chosen, AccessType.OCCUPIED);

        boolean allZeros = true;
        for(int i = 0; i < 3 && allZeros; ++i)
            for(int j = 0; j < 3 && allZeros; ++j)
                if(neighboringSwappingCell[i][j] != 0) allZeros = false;

        if(allZeros){
            System.out.println("The selected builder is not worthy to serve Apollo, the other builder has been selected automatically");
            if(yourBuilderGender == 'M'){
                yourBuilderGender = 'F';
                chosen = message.getCurrentPlayer().getBuilderF();
            } else {
                yourBuilderGender = 'M';
                chosen = message.getCurrentPlayer().getBuilderM();
            }
        }
        //Ricalcola neighboringSwappingCell e la display
        chosen.setBoard(new Board(message.getBoard()));
        chosen.setPlayer(message.getCurrentPlayer());
        neighboringSwappingCell = Board.neighboringSwappingCell(chosen, AccessType.OCCUPIED);
        String[] actions = new String[8];
        System.out.println("Now it's time to choose the opponent's builder, press one of the number shown to choose the direction that You prefer");
        if (neighboringSwappingCell[0][0] != 0) actions[0] = "Press 1 to go to NORTH-WEST";
        if (neighboringSwappingCell[0][1] != 0) actions[0] = "Press 2 to go to NORTH";
        if (neighboringSwappingCell[0][2] != 0) actions[0] = "Press 3 to go to NORTH-EAST";
        if (neighboringSwappingCell[1][0] != 0) actions[0] = "Press 4 to go to WEST";
        if (neighboringSwappingCell[1][2] != 0) actions[0] = "Press 5 to go to EAST";
        if (neighboringSwappingCell[2][0] != 0) actions[0] = "Press 6 to go to SOUTH-WEST";
        if (neighboringSwappingCell[2][1] != 0) actions[0] = "Press 7 to go to SOUTH";
        if (neighboringSwappingCell[2][2] != 0) actions[0] = "Press 8 to go to SOUTH-EAST";
        int pressedButton;
        do {
            showPossibleMatrix(neighboringSwappingCell, 'm', actions);
            //System.out.println("Ora è il momento di scegliere il costruttore avversario, premi il numero indicato per scegliere la direzione che preferisci");
            try{
                System.out.print("Button pressed: ");
                pressedButton = Integer.parseInt(scannerIn.nextLine());
                if (pressedButton == 1 && neighboringSwappingCell[0][0] != 0) {
                    direction = Direction.NORTH_WEST;
                    wrong = false;
                } else if (pressedButton == 2 && neighboringSwappingCell[0][1] != 0) {
                    direction = Direction.NORTH;
                    wrong = false;
                } else if (pressedButton == 3 && neighboringSwappingCell[0][2] != 0) {
                    direction = Direction.NORTH_EAST;
                    wrong = false;
                } else if (pressedButton == 4 && neighboringSwappingCell[1][0] != 0) {
                    direction = Direction.WEST;
                    wrong = false;
                } else if (pressedButton == 5 && neighboringSwappingCell[1][2] != 0) {
                    direction = Direction.EAST;
                    wrong = false;
                } else if (pressedButton == 6 && neighboringSwappingCell[2][0] != 0) {
                    direction = Direction.SOUTH_WEST;
                    wrong = false;
                } else if (pressedButton == 7 && neighboringSwappingCell[2][1] != 0) {
                    direction = Direction.SOUTH;
                    wrong = false;
                } else if (pressedButton == 8 && neighboringSwappingCell[2][2] != 0) {
                    direction = Direction.SOUTH_EAST;
                    wrong = false;
                }else wrong = true;
            } catch (NumberFormatException e){
                wrong = true;
            }
            if(wrong) System.out.println("You selected a wrong direction! Insert a correct number, remember you can't swap your builder with an opponent's builder on a building too high");
        } while(wrong);

        apolloParamMessage.setYourBuilderGender(yourBuilderGender);
        apolloParamMessage.setOpponentBuilderDirection(direction);
        return apolloParamMessage;
    }

    /**
     * the method asks to the current player to insert parameters need to use  Ares's power. the Parameter asked is direction of the cell
     * where the demolition will occur
     * If the direction insert is not allowed the method wil ask to the player to insert it again. The method will also built the
     * message containing the parameters gathered
     * @param message contains information about the match such as the current player
     * @return the message containing the parameter insert by the player
     */
    public AresParamMessage displayAresParamSel(MatchStateMessage message){
        AresParamMessage aresParamMessage = new AresParamMessage();
        Direction direction = null;
        Builder demolitionBuilder = null;
        char demolitionBuilderSex ='O' ;
        if (message.getCurrentPlayer().getPlayingBuilder().getGender() == '\u2640') {
            demolitionBuilder = message.getCurrentPlayer().getBuilderM();
            demolitionBuilderSex = 'M';
        } else {
            demolitionBuilder = message.getCurrentPlayer().getBuilderF();
            demolitionBuilderSex = 'F';
        }

        demolitionBuilder.setBoard(new Board(message.getBoard()));
        demolitionBuilder.setPlayer(message.getCurrentPlayer());
        int[][] neighboringLevelCell = Board.neighboringLevelCell(demolitionBuilder);

        //INserire display per mostrare le possibili costruzioni

        boolean wrong;
        int pressedButton;
        String[] actions = new String[8];
        if (neighboringLevelCell[0][0] > 0 && neighboringLevelCell[0][0] < 4) actions[0] = ("Press 1 to demolish a block to the NORTH-WEST");
        if (neighboringLevelCell[0][1] > 0 && neighboringLevelCell[0][1] < 4) actions[1] = ("Press 2 to demolish a block to the NORTH");
        if (neighboringLevelCell[0][2] > 0 && neighboringLevelCell[0][2] < 4) actions[2] = ("Press 3 to demolish a block to the NORTH-EAST");
        if (neighboringLevelCell[1][0] > 0 && neighboringLevelCell[1][0] < 4) actions[3] = ("Press 4 to demolish a block to the WEST");
        if (neighboringLevelCell[1][2] > 0 && neighboringLevelCell[1][2] < 4) actions[4] = ("Press 5 to demolish a block to the EAST");
        if (neighboringLevelCell[2][0] > 0 && neighboringLevelCell[2][0] < 4) actions[5] = ("Press 6 to demolish a block to the SOUTH-WEST");
        if (neighboringLevelCell[2][1] > 0 && neighboringLevelCell[2][1] < 4) actions[6] = ("Press 7 to demolish a block to the SOUTH");
        if (neighboringLevelCell[2][2] > 0 && neighboringLevelCell[2][2] < 4) actions[7] = ("Press 8 to demolish a block to the SOUTH-EAST");
        System.out.println("Now it's time to choose the block that has to be destroyed, press one of the number shown to choose the direction of the block You want to destroy");
        do {
            showPossibleMatrix(neighboringLevelCell, 'b', actions);
            //System.out.println("Ora è il momento di scegliere il blocco da demolire, premi il numero indicato per scegliere la direzione del blocco che preferisci distruggere");
            try{
                System.out.print("Button pressed: ");
                pressedButton = Integer.parseInt(scannerIn.nextLine());
                wrong = true;

                if (pressedButton == 1 && neighboringLevelCell[0][0] != 0 && neighboringLevelCell[0][0] != 4) {
                    direction = Direction.NORTH_WEST;
                    wrong = false;
                } else if (pressedButton == 2 && neighboringLevelCell[0][1] != 0 && neighboringLevelCell[0][1] != 4) {
                    direction = Direction.NORTH;
                    wrong = false;
                } else if (pressedButton == 3 && neighboringLevelCell[0][2] != 0 && neighboringLevelCell[0][2] != 4) {
                    direction = Direction.NORTH_EAST;
                    wrong = false;
                } else if (pressedButton == 4 && neighboringLevelCell[1][0] != 0 && neighboringLevelCell[1][0] != 4) {
                    direction = Direction.WEST;
                    wrong = false;
                } else if (pressedButton == 5 && neighboringLevelCell[1][2] != 0 && neighboringLevelCell[1][2] != 4) {
                    direction = Direction.EAST;
                    wrong = false;
                } else if (pressedButton == 6 && neighboringLevelCell[2][0] != 0 && neighboringLevelCell[2][0] != 4) {
                    direction = Direction.SOUTH_WEST;
                    wrong = false;
                } else if (pressedButton == 7 && neighboringLevelCell[2][1] != 0 && neighboringLevelCell[2][1] != 4) {
                    direction = Direction.SOUTH;
                    wrong = false;
                } else if (pressedButton == 8 && neighboringLevelCell[2][2] != 0 && neighboringLevelCell[2][2] != 4) {
                    direction = Direction.SOUTH_EAST;
                    wrong = false;
                }
            }catch (NumberFormatException e){
                wrong = true;
            }

            if(wrong) System.out.println("You selected a wrong direction! remember you can't demolish the ground or a dome");
        } while(wrong);

        aresParamMessage.setDemolitionBuilderSex(demolitionBuilderSex);
        aresParamMessage.setTargetedBlock(direction);
        return aresParamMessage;
    }

    /**
     * the method asks to the current player to insert parameters need to use Artemis's power. The Parameter asked is direction of the cell
     * where the player wants to moved again the builder in.
     * If the direction insert is not allowed the method wil ask to the player to insert it again. The method will also built the
     * message containing the parameters gathered
     * @param message contains information about the match such as the current player, information used for acquiring which is the playing
     * builder
     * @return the message containing the parameter acquired by the method
     */
    public ArtemisParamMessage displayArtemisParamSel(MatchStateMessage message){
        ArtemisParamMessage artemisParamMessage = new ArtemisParamMessage();
        int[][] possibleMoves = message.getCurrentPlayer().getPlayingBuilder().getPossibleMoves();
        Direction direction = null;
        boolean wrong;
        int pressedButton;
        String[] actions = new String[8];
        if (possibleMoves[0][0] > 0 && possibleMoves[0][0] < 4) actions[0] = ("Press 1 to go to NORTH-WEST");
        if (possibleMoves[0][1] > 0 && possibleMoves[0][1] < 4) actions[1] = ("Press 2 to go to NORTH");
        if (possibleMoves[0][2] > 0 && possibleMoves[0][2] < 4) actions[2] = ("Press 3 to go to NORTH-EAST");
        if (possibleMoves[1][0] > 0 && possibleMoves[1][0] < 4) actions[3] = ("Press 4 to go to WEST");
        if (possibleMoves[1][2] > 0 && possibleMoves[1][2] < 4) actions[4] = ("Press 5 to go to EAST");
        if (possibleMoves[2][0] > 0 && possibleMoves[2][0] < 4) actions[5] = ("Press 6 to go to SOUTH-WEST");
        if (possibleMoves[2][1] > 0 && possibleMoves[2][1] < 4) actions[6] = ("Press 7 to go to SOUTH");
        if (possibleMoves[2][2] > 0 && possibleMoves[2][2] < 4) actions[7] = ("Press 8 to go to SOUTH-EAST");
        System.out.println("Now it's time to choose where you want to move the builder again, press one of the number shown to choose the direction that You prefer");
        do {
            //System.out.println("Ora è il momento di scegliere dove muovere nuovamente il builder, premi il numero indicato per scegliere la direzione del movimento");
            showPossibleMatrix(possibleMoves, 'm', actions);
            try{
                System.out.print("Button pressed: ");
                pressedButton = Integer.parseInt(scannerIn.nextLine());
                wrong = true;

                if (pressedButton == 1 && possibleMoves[0][0] > 0 && possibleMoves[0][0] < 4) {
                    direction = Direction.NORTH_WEST;
                    wrong = false;
                } else if (pressedButton == 2 && possibleMoves[0][1] > 0 && possibleMoves[0][1] < 4) {
                    direction = Direction.NORTH;
                    wrong = false;
                } else if (pressedButton == 3 && possibleMoves[0][2] > 0 && possibleMoves[0][2] < 4) {
                    direction = Direction.NORTH_EAST;
                    wrong = false;
                } else if (pressedButton == 4 && possibleMoves[1][0] > 0 && possibleMoves[1][0] < 4) {
                    direction = Direction.WEST;
                    wrong = false;
                } else if (pressedButton == 5 && possibleMoves[1][2] > 0 && possibleMoves[1][2] < 4) {
                    direction = Direction.EAST;
                    wrong = false;
                } else if (pressedButton == 6 && possibleMoves[2][0] > 0 && possibleMoves[2][0] < 4) {
                    direction = Direction.SOUTH_WEST;
                    wrong = false;
                } else if (pressedButton == 7 && possibleMoves[2][1] > 0 && possibleMoves[2][1] < 4) {
                    direction = Direction.SOUTH;
                    wrong = false;
                } else if (pressedButton == 8 && possibleMoves[2][2] > 0 && possibleMoves[2][2] < 4) {
                    direction = Direction.SOUTH_EAST;
                    wrong = false;
                }
            } catch(NumberFormatException e) {
                wrong = true;
            }
            if(wrong) System.out.println("You selected a wrong direction! Insert a correct number, remember You can't go on buildings too high or on domes");
        } while(wrong);
        artemisParamMessage.setDirection(direction);
        return artemisParamMessage;
    }

    /**
     * the method asks to the current player to insert parameters need to use Atlas's power. the Parameter asked is direction of the cell
     * where the dome will be built
     * If the direction insert is not allowed the method wil ask to the player to insert it again. The method will also built the
     * message containing the parameters gathered
     * @param message contains information about the match such as the current player, information used for acquiring which is the playing
     * builder
     * @return the message containing the parameter acquired by the method
     */
    public AtlasParamMessage displayAtlasParamSel(MatchStateMessage message){
        AtlasParamMessage atlasParamMessage = new AtlasParamMessage();
        Direction direction = null;
        message.getCurrentPlayer().getPlayingBuilder().setBoard(new Board(message.getBoard()));
        message.getCurrentPlayer().getPlayingBuilder().setPlayer(message.getCurrentPlayer());
        int[][] neighboringLevelCell = Board.neighboringLevelCell(message.getCurrentPlayer().getPlayingBuilder());
        //INserire display per mostrare
        boolean wrong;
        int pressedButton;
        String[] actions = new String[8];
        if (neighboringLevelCell[0][0] != 4 && neighboringLevelCell[0][0] != -1) actions[0] = ("Press 1 to build with your builder NORTH-WEST");
        if (neighboringLevelCell[0][1] != 4 && neighboringLevelCell[0][1] != -1) actions[1] = ("Press 2 to build with your builder NORTH");
        if (neighboringLevelCell[0][2] != 4 && neighboringLevelCell[0][2] != -1) actions[2] = ("Press 3 to build with your builder NORTH-EAST");
        if (neighboringLevelCell[1][0] != 4 && neighboringLevelCell[1][0] != -1) actions[3] = ("Press 4 to build with your builder WEST");
        if (neighboringLevelCell[1][2] != 4 && neighboringLevelCell[1][2] != -1) actions[4] = ("Press 5 to build with your builder EAST");
        if (neighboringLevelCell[2][0] != 4 && neighboringLevelCell[2][0] != -1) actions[5] = ("Press 6 to build with your builder SOUTH-WEST");
        if (neighboringLevelCell[2][1] != 4 && neighboringLevelCell[2][1] != -1) actions[6] = ("Press 7 to build with your builder SOUTH");
        if (neighboringLevelCell[2][2] != 4 && neighboringLevelCell[2][2] != -1) actions[7] = ("Press 8 to build with your builder SOUTH-EAST");
        System.out.println("Now it's time to choose where your builder has to build a dome, press one of the number shown to choose the direction of the cell where You want to build");
        do {
            //System.out.println("Ora è il momento di scegliere dove far costruire al builder una cupola, premi il numero indicato per scegliere la direzione della costruzione");
            showPossibleMatrix(neighboringLevelCell, 'b', actions);
            try{
                System.out.print("Button pressed: ");
                pressedButton = Integer.parseInt(scannerIn.nextLine());
                wrong = true;

                if (pressedButton == 1 && neighboringLevelCell[0][0] != 4 && neighboringLevelCell[0][0] != -1)  {
                    direction = Direction.NORTH_WEST;
                    wrong = false;
                } else if (pressedButton == 2 && neighboringLevelCell[0][1] != 4 && neighboringLevelCell[0][1] != -1) {
                    direction = Direction.NORTH;
                    wrong = false;
                } else if (pressedButton == 3 && neighboringLevelCell[0][2] != 4 && neighboringLevelCell[0][2] != -1) {
                    direction = Direction.NORTH_EAST;
                    wrong = false;
                } else if (pressedButton == 4 && neighboringLevelCell[1][0] != 4 && neighboringLevelCell[1][0] != -1) {
                    direction = Direction.WEST;
                    wrong = false;
                } else if (pressedButton == 5 && neighboringLevelCell[1][2] != 4 && neighboringLevelCell[1][2] != -1) {
                    direction = Direction.EAST;
                    wrong = false;
                } else if (pressedButton == 6 && neighboringLevelCell[2][0] != 4 && neighboringLevelCell[2][0] != -1) {
                    direction = Direction.SOUTH_WEST;
                    wrong = false;
                } else if (pressedButton == 7 && neighboringLevelCell[2][1] != 4 && neighboringLevelCell[2][1] != -1) {
                    direction = Direction.SOUTH;
                    wrong = false;
                } else if (pressedButton == 8 && neighboringLevelCell[2][2] != 4 && neighboringLevelCell[2][2] != -1) {
                    direction = Direction.SOUTH_EAST;
                    wrong = false;
                }
            }catch(NumberFormatException e){
                wrong = true;
            }
            if(wrong) System.out.println("You selected a wrong direction! Insert a correct number, remember You can't build on the top of other domes or on the coast");
        } while(wrong);
        atlasParamMessage.setDirection(direction);
        return  atlasParamMessage;
    }

    /**
     * The method asks to the current player to insert parameters need to use Demeter's power. The Parameter asked is direction of the cell
     * where the player wants to build again the builder in.
     * If the direction insert is not allowed the method wil ask to the player to insert it again. The method will also built the
     * message containing the parameters gathered
     * @param message contains information about the match such as the current player, information used for acquiring which is the playing
     * builder
     * @return the message containing the parameter acquired by the method
     */
    public DemeterParamMessage displayDemeterParamSel(MatchStateMessage message){
        DemeterParamMessage demeterParamMessage = new DemeterParamMessage();
        Direction direction = null;
        int[][] possibleBuildings = message.getCurrentPlayer().getPlayingBuilder().getPossibleBuildings();
        //INserire display per mostrare
        boolean wrong;
        int pressedButton;
        String[] actions = new String[8];
        if (possibleBuildings[0][0] != 4 && possibleBuildings[0][0] != -1 && possibleBuildings[0][0] != -2) actions[0] = ("Press 1 to build with your builder NORTH-WEST");
        if (possibleBuildings[0][1] != 4 && possibleBuildings[0][1] != -1 && possibleBuildings[0][1] != -2) actions[1] = ("Press 2 to build with your builder NORTH");
        if (possibleBuildings[0][2] != 0 && possibleBuildings[0][2] != -1 && possibleBuildings[0][2] != -2) actions[2] = ("Press 3 to build with your builder NORTH-EAST");
        if (possibleBuildings[1][0] != 4 && possibleBuildings[1][0] != -1 && possibleBuildings[1][0] != -2) actions[3] = ("Press 4 to build with your builder WEST");
        if (possibleBuildings[1][2] != 4 && possibleBuildings[1][2] != -1 && possibleBuildings[1][2] != -2) actions[4] = ("Press 5 to build with your builder EAST");
        if (possibleBuildings[2][0] != 4 && possibleBuildings[2][0] != -1 && possibleBuildings[2][0] != -2) actions[5] = ("Press 6 to build with your builder SOUTH-WEST");
        if (possibleBuildings[2][1] != 4 && possibleBuildings[2][1] != -1 && possibleBuildings[2][1] != -2) actions[6] = ("Press 7 to build with your builder SOUTH");
        if (possibleBuildings[2][2] != 4 && possibleBuildings[2][2] != -1 && possibleBuildings[2][2] != -2) actions[7] = ("Press 8 to build with your builder SOUTH-EAST");
        System.out.println("Now it's time to choose where your builder has to build again, press one of the number shown to choose the direction of the cell where You want to build");
        do {
            //System.out.println("Ora è il momento di scegliere dove far costruire nuovamente al builder , premi il numero indicato per scegliere la direzione della costruzione");
            showPossibleMatrix(possibleBuildings, 'b', actions);
            try {
                System.out.print("Button pressed: ");
                pressedButton = Integer.parseInt(scannerIn.nextLine());
                wrong = true;

                if (pressedButton == 1 && possibleBuildings[0][0] != 4 && possibleBuildings[0][0] != -1 && possibleBuildings[0][0] != -2)  {
                    direction = Direction.NORTH_WEST;
                    wrong = false;
                } else if (pressedButton == 2 && possibleBuildings[0][1] != 4 && possibleBuildings[0][1] != -1 && possibleBuildings[0][1] != -2) {
                    direction = Direction.NORTH;
                    wrong = false;
                } else if (pressedButton == 3 && possibleBuildings[0][2] != 0 && possibleBuildings[0][2] != -1 && possibleBuildings[0][2] != -2) {
                    direction = Direction.NORTH_EAST;
                    wrong = false;
                } else if (pressedButton == 4 && possibleBuildings[1][0] != 4 && possibleBuildings[1][0] != -1 && possibleBuildings[1][0] != -2) {
                    direction = Direction.WEST;
                    wrong = false;
                } else if (pressedButton == 5 && possibleBuildings[1][2] != 4 && possibleBuildings[1][2] != -1 && possibleBuildings[1][2] != -2) {
                    direction = Direction.EAST;
                    wrong = false;
                } else if (pressedButton == 6 && possibleBuildings[2][0] != 4 && possibleBuildings[2][0] != -1 && possibleBuildings[2][0] != -2) {
                    direction = Direction.SOUTH_WEST;
                    wrong = false;
                } else if (pressedButton == 7 && possibleBuildings[2][1] != 4 && possibleBuildings[2][1] != -1 && possibleBuildings[2][1] != -2) {
                    direction = Direction.SOUTH;
                    wrong = false;
                } else if (pressedButton == 8 && possibleBuildings[2][2] != 4 && possibleBuildings[2][2] != -1 && possibleBuildings[2][2] != -2) {
                    direction = Direction.SOUTH_EAST;
                    wrong = false;
                }
            }catch (NumberFormatException e){
                wrong = true;
            }
            if(wrong) System.out.println("You selected a wrong direction! Insert a correct number, remember You can't build on the top of other domes, on the coast or on the cell where you built a block before");
        } while(wrong);
        demeterParamMessage.setDirection(direction);
        return demeterParamMessage;
    }

    /**
     * the method asks to the current player to insert parameters need to use Hestia's power. The Parameter asked is direction of the cell
     * where the player wants to moved again the builder in.
     * If the direction insert is not allowed the method wil ask to the player to insert it again. The method will also built the
     * message containing the parameters gathered
     * @param message contains information about the match such as the current player, information used for acquiring which is the playing
     * builder
     * @return the message containing the parameter acquired by the method
     */
    public HestiaParamMessage displayHestiaParamSel(MatchStateMessage message){
        HestiaParamMessage hestiaParamMessage = new HestiaParamMessage();
        Direction direction = null;
        int[] posBuilder = new int[2];
        posBuilder[0] = message.getCurrentPlayer().getPlayingBuilder().getPosX();
        posBuilder[1] = message.getCurrentPlayer().getPlayingBuilder().getPosY();

        message.getCurrentPlayer().getPlayingBuilder().setBoard(new Board(message.getBoard()));
        message.getCurrentPlayer().getPlayingBuilder().setPlayer(message.getCurrentPlayer());

        int[][] neighboringLevelCell = Board.neighboringLevelCell(message.getCurrentPlayer().getPlayingBuilder());
        if(message.getCurrentPlayer().getPlayingBuilder().getPosX() == 1 || message.getCurrentPlayer().getPlayingBuilder().getPosX() == 5){
            neighboringLevelCell[1][0] = -1;
            neighboringLevelCell[1][1] = -1;
            neighboringLevelCell[1][2] = -1;
        } else if(message.getCurrentPlayer().getPlayingBuilder().getPosX() == 2){
            neighboringLevelCell[0][0] = -1;
            neighboringLevelCell[0][1] = -1;
            neighboringLevelCell[0][2] = -1;
        } else if(message.getCurrentPlayer().getPlayingBuilder().getPosX() == 4){
            neighboringLevelCell[2][0] = -1;
            neighboringLevelCell[2][1] = -1;
            neighboringLevelCell[2][2] = -1;
        }

        if(message.getCurrentPlayer().getPlayingBuilder().getPosY() == 1 || message.getCurrentPlayer().getPlayingBuilder().getPosY() == 5){
            neighboringLevelCell[0][1] = -1;
            neighboringLevelCell[1][1] = -1;
            neighboringLevelCell[2][1] = -1;
        } else if(message.getCurrentPlayer().getPlayingBuilder().getPosY() == 2){
            neighboringLevelCell[0][0] = -1;
            neighboringLevelCell[1][0] = -1;
            neighboringLevelCell[2][0] = -1;
        } else if(message.getCurrentPlayer().getPlayingBuilder().getPosY() == 4){
            neighboringLevelCell[0][2] = -1;
            neighboringLevelCell[1][2] = -1;
            neighboringLevelCell[2][2] = -1;
        }
        //INserire display per mostrare
        boolean wrong;
        int pressedButton;
        String[] actions = new String[8];
        if (neighboringLevelCell[0][0] != -1 && neighboringLevelCell[0][0] != 4) actions[0] = ("Press 1 to build with your builder NORTH-WEST");
        if (neighboringLevelCell[0][1] != -1 && neighboringLevelCell[0][1] != 4) actions[1] = ("Press 2 to build with your builder NORTH");
        if (neighboringLevelCell[0][2] != -1 && neighboringLevelCell[0][2] != 4) actions[2] = ("Press 3 to build with your builder NORTH-EAST");
        if (neighboringLevelCell[1][0] != -1 && neighboringLevelCell[1][0] != 4) actions[3] = ("Press 4 to build with your builder WEST");
        if (neighboringLevelCell[1][2] != -1 && neighboringLevelCell[1][2] != 4) actions[4] = ("Press 5 to build with your builder EAST");
        if (neighboringLevelCell[2][0] != -1 && neighboringLevelCell[2][0] != 4) actions[5] = ("Press 6 to build with your builder SOUTH-WEST");
        if (neighboringLevelCell[2][1] != -1 && neighboringLevelCell[2][1] != 4) actions[6] = ("Press 7 to build with your builder SOUTH");
        if (neighboringLevelCell[2][2] != -1 && neighboringLevelCell[2][2] != 4) actions[7] = ("Press 8 to build with your builder SOUTH-EAST");
        System.out.println("Now it's time to choose where your builder has to build again, press one of the number shown to choose the direction of the cell where You want to build");
        do {
            //System.out.println("Ora è il momento di scegliere dove far costruire nuovamente al builder , premi il numero indicato per scegliere la direzione della costruzione, attenzione a non scegliere una cella perimetrale!");
            showPossibleMatrix(neighboringLevelCell, 'b', actions);
            try {
                System.out.print("Button pressed: ");
                pressedButton = Integer.parseInt(scannerIn.nextLine());
                wrong = true;

                if (pressedButton == 1 && neighboringLevelCell[0][0] != 4 && neighboringLevelCell[0][0] != -1 )  {
                    direction = Direction.NORTH_WEST;
                    wrong = false;

                } else if (pressedButton == 2 && neighboringLevelCell[0][1] != 4 && neighboringLevelCell[0][1] != -1) {
                    direction = Direction.NORTH;
                    wrong = false;
                } else if (pressedButton == 3 && neighboringLevelCell[0][2] != 4 && neighboringLevelCell[0][2] != -1) {
                    direction = Direction.NORTH_EAST;
                    wrong = false;
                } else if (pressedButton == 4 && neighboringLevelCell[1][0] != 4 && neighboringLevelCell[1][0] != -1 ) {
                    direction = Direction.WEST;
                    wrong = false;
                } else if (pressedButton == 5 && neighboringLevelCell[1][2] != 4 && neighboringLevelCell[1][2] != -1 ) {
                    direction = Direction.EAST;
                    wrong = false;
                } else if (pressedButton == 6 && neighboringLevelCell[2][0] != 4 && neighboringLevelCell[2][0] != -1 ) {
                    direction = Direction.SOUTH_WEST;
                    wrong = false;
                } else if (pressedButton == 7 && neighboringLevelCell[2][1] != 4 && neighboringLevelCell[2][1] != -1 ) {
                    direction = Direction.SOUTH;
                    wrong = false;
                } else if (pressedButton == 8 && neighboringLevelCell[2][2] != 4 && neighboringLevelCell[2][2] != -1 ) {
                    direction = Direction.SOUTH_EAST;
                    wrong = false;
                }
            }catch (NumberFormatException e){
                wrong = true;
            }
            if(wrong) System.out.println("You selected a wrong direction! Insert a correct number, remember You can't build on the top of other domes, on the coast or on cells near the coast");
        } while(wrong);
        hestiaParamMessage.setDirection(direction);
        return hestiaParamMessage;
    }

    /**
     * the method asks to the current player to insert parameters need to use Minotaur's power. These parameters are che choice of which builder
     * the player want to move (and push opponent's builder) and in which direction. If the builder selected cannot be moved, the method will choose for the player the other
     * builder. If the direction insert is not allowed the method wil ask to the player to insert it again. The method will also built the
     * message containing the parameters gathered
     * @param message contains references about the board and the currents player(About the match ).
     * @return the message containing the parameters gathered.
     */
    public MinotaurParamMessage displayMinotaurParamSel(MatchStateMessage message){
        MinotaurParamMessage minotaurParamMessage = new MinotaurParamMessage();
        //Inserire la stampa per la scelta del builder
        char playerBuilderGender = 'G';
        int[] posBuilder = new int[2];

        Builder chosenBuilderM = null;
        Direction direction = null;
        boolean wrong;
        String choice = null;
        do {
            try {
                System.out.println("Select the builder best suited to serve Minotaur.Press M o F. Remember it must be near an opponent's builder to be worthy!");
                choice = scannerIn.nextLine();
                choice = choice.toUpperCase();
                wrong  = false;
                if (choice.equals("M")) {
                    chosenBuilderM = message.getCurrentPlayer().getBuilderM();
                    posBuilder[0] = message.getCurrentPlayer().getBuilderM().getPosX();
                    posBuilder[1] = message.getCurrentPlayer().getBuilderM().getPosY();
                    playerBuilderGender = 'M';
                }
                else if (choice.equals("F")){
                    chosenBuilderM = message.getCurrentPlayer().getBuilderF();
                    posBuilder[0] = message.getCurrentPlayer().getBuilderF().getPosX();
                    posBuilder[1] = message.getCurrentPlayer().getBuilderF().getPosY();
                    playerBuilderGender = 'F';
                }
                else wrong = true;
            } catch (InputMismatchException e){
                scannerIn.nextLine();
                wrong = true;
            }
            if (wrong) System.out.println("Wrong Letter, Insert again");
        } while (wrong);

        chosenBuilderM.setBoard(new Board(message.getBoard()));
        chosenBuilderM.setPlayer( message.getCurrentPlayer());
        int[][] possibleSwap = Board.neighboringSwappingCell(chosenBuilderM, AccessType.OCCUPIED);
        //Modifico possibleSwap per includere l'impossibilità di spingere l'avversario perché la cella dopo non è FREE
        for(int i = 0; i < 3; ++i)
            for(int j = 0; j < 3; ++j)
                if(possibleSwap[i][j] != 0)
                    try {
                        if (message.getBoard()[posBuilder[0] + (i - 1) * 2][posBuilder[1] + (j - 1) * 2].getStatus() != AccessType.FREE)
                            possibleSwap[i][j] = 0;
                    }catch (IndexOutOfBoundsException e){
                        possibleSwap[i][j] = 0;
                    }

        boolean allZeros = true;
        for(int i = 0; i < 3 && allZeros; ++i)
            for(int j = 0; j < 3 && allZeros; ++j)
                if(possibleSwap[i][j] != 0) allZeros = false;

        if(allZeros){
            System.out.println("The selected builder is not worthy to serve Apollo, the other builder has been selected automatically");
            if(playerBuilderGender == 'M'){
                playerBuilderGender = 'F';
                chosenBuilderM = message.getCurrentPlayer().getBuilderF();
                posBuilder[0] = message.getCurrentPlayer().getBuilderF().getPosX();
                posBuilder[1] = message.getCurrentPlayer().getBuilderF().getPosY();
            } else {
                playerBuilderGender = 'M';
                chosenBuilderM = message.getCurrentPlayer().getBuilderM();
                posBuilder[0] = message.getCurrentPlayer().getBuilderM().getPosX();
                posBuilder[1] = message.getCurrentPlayer().getBuilderM().getPosY();
            }
        }
        chosenBuilderM.setBoard(new Board(message.getBoard()));
        chosenBuilderM.setPlayer( message.getCurrentPlayer());
        possibleSwap = Board.neighboringSwappingCell(chosenBuilderM, AccessType.OCCUPIED);
        //Modifico possibleSwap per includere l'impossibilità di spingere l'avversario perché la cella dopo non è FREE
        for(int i = 0; i < 3; ++i)
            for(int j = 0; j < 3; ++j)
                if(possibleSwap[i][j] != 0)
                    try {
                        if (message.getBoard()[posBuilder[0] + (i - 1) * 2][posBuilder[1] + (j - 1) * 2].getStatus() != AccessType.FREE)
                            possibleSwap[i][j] = 0;
                    }catch (IndexOutOfBoundsException e){
                        possibleSwap[i][j] = 0;
                    }
        int pressedButton;
        String[] actions = new String[8];
        System.out.println("Now it's time to choose the opponent's builder, press one of the number shown to choose the direction that You prefer");
        if (possibleSwap[0][0] != 0) actions[0] = "Press 1 to go to NORTH-WEST";
        if (possibleSwap[0][1] != 0) actions[0] = "Press 2 to go to NORTH";
        if (possibleSwap[0][2] != 0) actions[0] = "Press 3 to go to NORTH-EAST";
        if (possibleSwap[1][0] != 0) actions[0] = "Press 4 to go to WEST";
        if (possibleSwap[1][2] != 0) actions[0] = "Press 5 to go to EAST";
        if (possibleSwap[2][0] != 0) actions[0] = "Press 6 to go to SOUTH-WEST";
        if (possibleSwap[2][1] != 0) actions[0] = "Press 7 to go to SOUTH";
        if (possibleSwap[2][2] != 0) actions[0] = "Press 8 to go to SOUTH-EAST";
        do {
            //System.out.println("Ora è il momento di scegliere il builder avversario, premi il numero indicato per scegliere la direzione che preferisci");
            showPossibleMatrix(possibleSwap, 'm', actions);
            try {
                System.out.print("Button pressed: ");
                pressedButton = Integer.parseInt(scannerIn.nextLine());
                wrong = true;

                if (pressedButton == 1 && possibleSwap[0][0] != 0) {
                    direction = Direction.NORTH_WEST;
                    wrong = false;
                } else if (pressedButton == 2 && possibleSwap[0][1] != 0) {
                    direction = Direction.NORTH;
                    wrong = false;
                } else if (pressedButton == 3 && possibleSwap[0][2] != 0) {
                    direction = Direction.NORTH_EAST;
                    wrong = false;
                } else if (pressedButton == 4 && possibleSwap[1][0] != 0) {
                    direction = Direction.WEST;
                    wrong = false;
                } else if (pressedButton == 5 && possibleSwap[1][2] != 0) {
                    direction = Direction.EAST;
                    wrong = false;
                } else if (pressedButton == 6 && possibleSwap[2][0] != 0) {
                    direction = Direction.SOUTH_WEST;
                    wrong = false;
                } else if (pressedButton == 7 && possibleSwap[2][1] != 0) {
                    direction = Direction.SOUTH;
                    wrong = false;
                } else if (pressedButton == 8 && possibleSwap[2][2] != 0) {
                    direction = Direction.SOUTH_EAST;
                    wrong = false;
                }
            } catch (NumberFormatException e){
                wrong = true;
            }
            if(wrong) System.out.println("You selected a wrong direction! Insert a correct number, remember your builder can't push opponent's builder on the coast");
        } while(wrong);
        minotaurParamMessage.setOpponentBuilderDirection(direction);
        minotaurParamMessage.setPlayingBuilderSex(playerBuilderGender);
        return minotaurParamMessage;
    }

    /**
     * The method asks to the current player to insert parameters need to use Poseidon's power. The Parameters asked are direction of the cell
     * where the player wants to build again the builder in and how many times he wants to build. The method will display the max and min numbers
     * of times he can build
     * If the direction insert is not allowed the method wil ask to the player to insert it again. The method will also built the
     * message containing the parameters gathered
     * @param message contains information about the match such as the current player, information used for acquiring which is the playing
     * builder
     * @return the message containing the parameter acquired by the method
     */
    public PoseidonParamMessage displayPoseidonParamSel(MatchStateMessage message){
        PoseidonParamMessage poseidonParamMessage = new PoseidonParamMessage();
        //Ricerca del builder non mosso
        Builder constructionBuilder = null;
        char constructionBuilderSex = 'o';
        if (message.getCurrentPlayer().getPlayingBuilder().getGender() == '\u2640') {
            constructionBuilder = message.getCurrentPlayer().getBuilderM();
            constructionBuilderSex = 'M';
        } else {
            constructionBuilder = message.getCurrentPlayer().getBuilderF();
            constructionBuilderSex = 'F';
        }
        //Salvataggio della possible buildings
        constructionBuilder.setBoard(new Board(message.getBoard()));
        constructionBuilder.setPlayer(message.getCurrentPlayer());
        int[][] possibleBuildingsP = Board.neighboringLevelCell(constructionBuilder);

        int count = 0;
        for(int i = 0; i < 3; ++i)
            for(int j = 0; j < 3; ++j)
                if(possibleBuildingsP[i][j] >= 0 && possibleBuildingsP[i][j] < 4) count = count + 4 - possibleBuildingsP[i][j];

        //Salvataggio della posizione del builder
        int[] posBuilder = new int[2];
        posBuilder[0] = constructionBuilder.getPosX();
        posBuilder[1] = constructionBuilder.getPosY();
        //Mostrare la possible buildings
        //Creazione delle variabili per l'acquisizione di quante volte si vuole costruire
        if(count < 3)
            System.out.printf("Input how many times you want to build using Poseidon's power. Insert a number between 1 and %d: ", count);
        else
            System.out.println("Input how many times you want to build using Poseidon's power. Insert a number between 1 and 3: ");
        int numeroBuild = 0;
        boolean wrong = true;
        do{
            try{
                numeroBuild = Integer.parseInt(scannerIn.nextLine());
                if(numeroBuild > 0 && numeroBuild < 4) {wrong = false;}
            }catch (NumberFormatException e) {
                wrong = true;
            }
            if(wrong) System.out.println("You insert a wrong character! It has to be a number included in the range shown ");
        }while (wrong);
        //Preparazione alla ricezione delle direzioni
        ArrayList<Direction> directions = new ArrayList<>();
        int i = 0;
        int pressedButton;
        String[] actions = new String[8];
        if (possibleBuildingsP[0][0] != -1 && possibleBuildingsP[0][0] != 4) actions[0] = ("Press 1 to build with your builder NORTH-WEST");
        if (possibleBuildingsP[0][1] != -1 && possibleBuildingsP[0][1] != 4) actions[1] = ("Press 2 to build with your builder NORTH");
        if (possibleBuildingsP[0][2] != -1 && possibleBuildingsP[0][2] != 4) actions[2] = ("Press 3 to build with your builder NORTH-EAST");
        if (possibleBuildingsP[1][0] != -1 && possibleBuildingsP[1][0] != 4) actions[3] = ("Press 4 to build with your builder WEST");
        if (possibleBuildingsP[1][2] != -1 && possibleBuildingsP[1][2] != 4) actions[4] = ("Press 5 to build with your builder EAST");
        if (possibleBuildingsP[2][0] != -1 && possibleBuildingsP[2][0] != 4) actions[5] = ("Press 6 to build with your builder SOUTH-WEST");
        if (possibleBuildingsP[2][1] != -1 && possibleBuildingsP[2][1] != 4) actions[6] = ("Press 7 to build with your builder SOUTH");
        if (possibleBuildingsP[2][2] != -1 && possibleBuildingsP[2][2] != 4) actions[7] = ("Press 8 to build with your builder SOUTH-EAST");
        System.out.printf("Now insert %d directions /n", numeroBuild);
        do {
            showPossibleMatrix(possibleBuildingsP, 'b', actions);
            try {
                System.out.print("Button pressed: ");
                pressedButton = Integer.parseInt(scannerIn.nextLine());
                wrong = true;
                if (pressedButton == 1 && possibleBuildingsP[0][0] != -1 && possibleBuildingsP[0][0] != 4) {
                    directions.add(Direction.NORTH_WEST);
                    possibleBuildingsP[0][0] = possibleBuildingsP[0][0] + 1;
                    wrong = false;

                } else if (pressedButton == 2 && possibleBuildingsP[0][1] != -1 && possibleBuildingsP[0][1] != -4) {
                    directions.add( Direction.NORTH);
                    possibleBuildingsP[0][1] = possibleBuildingsP[0][1] + 1;
                    wrong = false;

                } else if (pressedButton == 3 && possibleBuildingsP[0][2] != -1 && possibleBuildingsP[0][2] != 4) {
                    directions.add( Direction.NORTH_EAST);
                    possibleBuildingsP[0][2] = possibleBuildingsP[0][2] + 1;
                    wrong = false;

                } else if (pressedButton == 4 && possibleBuildingsP[1][0] != -1 && possibleBuildingsP[1][0] != 4) {
                    directions.add( Direction.WEST);
                    possibleBuildingsP[1][0] = possibleBuildingsP[1][0] + 1;
                    wrong = false;

                } else if (pressedButton == 5 && possibleBuildingsP[1][2] != -1 && possibleBuildingsP[1][2] != 4) {
                    directions.add(Direction.EAST);
                    possibleBuildingsP[1][2] = possibleBuildingsP[1][2] +1;
                    wrong = false;

                } else if (pressedButton == 6 && possibleBuildingsP[2][0] != -1 && possibleBuildingsP[2][0] != 4) {
                    directions.add(Direction.SOUTH_WEST);
                    possibleBuildingsP[2][0] = possibleBuildingsP[2][0] +1;
                    wrong = false;

                } else if (pressedButton == 7 && possibleBuildingsP[2][1] != -1 && possibleBuildingsP[2][1] != 4) {
                    directions.add(Direction.SOUTH);
                    possibleBuildingsP[2][1] = possibleBuildingsP[2][1] + 1;
                    wrong = false;

                } else if (pressedButton == 8 && possibleBuildingsP[2][2] != -1 && possibleBuildingsP[2][2] != 4) {
                    directions.add(Direction.SOUTH_EAST);
                    possibleBuildingsP[2][2] = possibleBuildingsP[2][2] + 1;
                    wrong = false;
                }
            } catch (NumberFormatException e){
                wrong = true;
            }
            if(wrong) System.out.println("You selected a wrong direction! Remember you can't build on domes or on occupied cells");
            else { ++i;}
        } while(i < numeroBuild);
        poseidonParamMessage.setConstructionGender(constructionBuilderSex);
        poseidonParamMessage.setDirection(directions);
        poseidonParamMessage.setNumberOfBuild(numeroBuild);
        return poseidonParamMessage;
    }

    /**
     * the method asks to the current player to insert parameters need to use Prometheus's power. These parameters are the choice of which builder
     * the player want to build (the choice of the builder will be memorized to be used for the next phase of movement and building)
     * and in which direction.
     * If the builder selected cannot be moved, the method will choose for the player the other
     * builder. If the direction insert is not allowed the method wil ask to the player to insert it again. The method will also built the
     * message containing the parameters gathered
     * @param message contains references about the board and the currents player(About the match ).
     * @return the message containing the parameters gathered.
     */
    public PrometheusParamMessage displayPrometheusParamSel(MatchStateMessage message){
        PrometheusParamMessage prometheusParamMessage = new PrometheusParamMessage();
        Builder builderScelto = null;
        char builderSex = '0';
        int[] posBuilder = new int[2];
        Direction direction = null;
        //Scelta del builder
        String choice;
        boolean wrong;
        message.getCurrentPlayer().setRiseActions(false);
        message.getCurrentPlayer().setMoveActions(true);
        message.getCurrentPlayer().getBuilderF().setBoard(new Board(message.getBoard()));
        message.getCurrentPlayer().getBuilderF().setPlayer(message.getCurrentPlayer());
        message.getCurrentPlayer().getBuilderM().setBoard(new Board(message.getBoard()));
        message.getCurrentPlayer().getBuilderM().setPlayer(message.getCurrentPlayer());
        System.out.println("Select the builder best suited to serve Prometheus.");
        do {
            if(message.getCurrentPlayer().getBuilderM().canMove() && message.getCurrentPlayer().getBuilderM().canBuild())
                System.out.println("Press M to select the male builder");
            if(message.getCurrentPlayer().getBuilderF().canMove() && message.getCurrentPlayer().getBuilderF().canBuild())
                System.out.println("Press F to select the female builder");
            try{
                wrong  = false;
                choice = scannerIn.nextLine();
                choice = choice.toUpperCase();
                if (choice.equals("M")) {
                    builderScelto = message.getCurrentPlayer().getBuilderM();
                    posBuilder[0] = message.getCurrentPlayer().getBuilderM().getPosX();
                    posBuilder[1] = message.getCurrentPlayer().getBuilderM().getPosY();
                    builderSex = 'M';
                    if(!builderScelto.canMove()) wrong = true;
                } else if (choice.equals("F")){
                    builderScelto = message.getCurrentPlayer().getBuilderF();
                    posBuilder[0] = message.getCurrentPlayer().getBuilderF().getPosX();
                    posBuilder[1] = message.getCurrentPlayer().getBuilderF().getPosY();
                    builderSex = 'F';
                    if(!builderScelto.canMove()) wrong = true;
                } else wrong = true;
            }catch(InputMismatchException e){
                scannerIn.nextLine();
                wrong = true;
            }
            if (wrong)
                System.out.println("Wrong Letter, Insert again");
        } while (wrong);
        //scelta direzione di costruzione
        builderScelto.setBoard(new Board(message.getBoard()));
        builderScelto.setPlayer(message.getCurrentPlayer());
        int[][] possibleBuildingsPr = Board.neighboringLevelCell(builderScelto);
        int pressedButton;
        String[] actions = new String[8];
        if (possibleBuildingsPr[0][0] >= 0 && possibleBuildingsPr[0][0] < 4) actions[0] = ("Press 1 to build with your builder NORTH-WEST");
        if (possibleBuildingsPr[0][1] >= 0 && possibleBuildingsPr[0][1] < 4) actions[1] = ("Press 2 to build with your builder NORTH");
        if (possibleBuildingsPr[0][2] >= 0 && possibleBuildingsPr[0][2] < 4) actions[2] = ("Press 3 to build with your builder NORTH-EAST");
        if (possibleBuildingsPr[1][0] >= 0 && possibleBuildingsPr[1][0] < 4) actions[3] = ("Press 4 to build with your builder WEST");
        if (possibleBuildingsPr[1][2] >= 0 && possibleBuildingsPr[1][2] < 4) actions[4] = ("Press 5 to build with your builder EAST");
        if (possibleBuildingsPr[2][0] >= 0 && possibleBuildingsPr[2][0] < 4) actions[5] = ("Press 6 to build with your builder SOUTH-WEST");
        if (possibleBuildingsPr[2][1] >= 0 && possibleBuildingsPr[2][1] < 4) actions[6] = ("Press 7 to build with your builder SOUTH");
        if (possibleBuildingsPr[2][2] >= 0 && possibleBuildingsPr[2][2] < 4) actions[7] = ("Press 8 to build with your builder SOUTH-EAST");
        System.out.println("Now it's time to choose where your builder has to build before moving, press one of the number shown to choose the direction of the cell where You want to build");
        do {
            //System.out.println("Ora è il momento di scegliere il builder avversario, premi il numero indicato per scegliere la direzione che preferisci");
            showPossibleMatrix(possibleBuildingsPr, 'b', actions);
            try{
                System.out.print("Button pressed: ");
                pressedButton = Integer.parseInt(scannerIn.nextLine());
                wrong = true;

                if (pressedButton == 1 && possibleBuildingsPr[0][0] >= 0 && possibleBuildingsPr[0][0] < 4) {
                    if(message.getBoard()[posBuilder[0]-1][posBuilder[1]-1].getStatus() == AccessType.FREE ) {
                        direction = Direction.NORTH_WEST;
                        wrong = false;
                    }

                } else if (pressedButton == 2 && possibleBuildingsPr[0][1] >= 0 && possibleBuildingsPr[0][1] < 4) {
                    if(message.getBoard()[posBuilder[0]-1][posBuilder[1]].getStatus() == AccessType.FREE ) {
                        direction = Direction.NORTH;
                        wrong = false;
                    }

                } else if (pressedButton == 3 && possibleBuildingsPr[0][2] >= 0 && possibleBuildingsPr[0][2] < 4) {
                    if(message.getBoard()[posBuilder[0]-1][posBuilder[1]+1].getStatus() == AccessType.FREE ) {
                        direction = Direction.NORTH_EAST;
                        wrong = false;
                    }

                } else if (pressedButton == 4 && possibleBuildingsPr[1][0] >= 0 && possibleBuildingsPr[1][0] < 4) {
                    if(message.getBoard()[posBuilder[0]][posBuilder[1]-1].getStatus() == AccessType.FREE ) {
                        direction = Direction.WEST;
                        wrong = false;
                    }

                } else if (pressedButton == 5 && possibleBuildingsPr[1][2] >= 0 && possibleBuildingsPr[1][2] < 4) {
                    if(message.getBoard()[posBuilder[0]][posBuilder[1]+1].getStatus() == AccessType.FREE ) {
                        direction = Direction.EAST;
                        wrong = false;
                    }

                } else if (pressedButton == 6 && possibleBuildingsPr[2][0] >= 0 && possibleBuildingsPr[2][0] < 4) {
                    if(message.getBoard()[posBuilder[0]+1][posBuilder[1]-1].getStatus() == AccessType.FREE ) {
                        direction = Direction.SOUTH_WEST;
                        wrong = false;
                    }

                } else if (pressedButton == 7 && possibleBuildingsPr[2][1] >= 0 && possibleBuildingsPr[2][1] < 4) {
                    if(message.getBoard()[posBuilder[0]+1][posBuilder[1]].getStatus() == AccessType.FREE ) {
                        direction = Direction.SOUTH;
                        wrong = false;
                    }

                } else if (pressedButton == 8 && possibleBuildingsPr[2][2] >= 0 && possibleBuildingsPr[2][2] < 4) {
                    if(message.getBoard()[posBuilder[0]+1][posBuilder[1]+1].getStatus() == AccessType.FREE ) {
                        direction = Direction.SOUTH_EAST;
                        wrong = false;
                    }
                }
            }catch(NumberFormatException e){
                wrong = true;
            }
            if(wrong) System.out.println("You selected a wrong direction! Remember you can't build on domes or on occupied cells");
        } while(wrong);
        prometheusParamMessage.setDirection(direction);
        prometheusParamMessage.setBuilderSex(builderSex);
        return prometheusParamMessage;
    }

    /**
     * the method prints the scheme of the board, whit numbers inside the cells to indicate the height of the buildings, nothing if there isn't
     * any builder, the coloured symbol of the gender to indicate the gender of the builder and to whom it belongs. The triangle and the wave
     * represent the coast.
     * @param listOfCells is an array list of cells containing all the information of the board
     * @param players players of the match
     */
    private void showBoard(ArrayList<Cell> listOfCells, ArrayList<Player> players){
        String coast = Color.OCEAN_BLUE+" -^"+Color.MOUNTAIN_BROWN +"\u25B2 ";
        // Color.OCEAN_BLUE+"\u25DE\u25DC"+Color.MOUNTAIN_BROWN +"\u25B2 "
        coast = String.format("%1$5s", coast);
        String[][] printableBoard = new String[7][7];
        int j = 0;
        for(int i = 0; i < listOfCells.size(); ++i){
            if(listOfCells.get(i).getLevel() == LevelType.COAST) printableBoard[j][i%7] = coast;
            else if(listOfCells.get(i).getStatus() == AccessType.OCCUPIED) printableBoard[j][i%7] = " " + listOfCells.get(i).getLevel().getHeight() + " " + listOfCells.get(i).getBuilder().getColor() + listOfCells.get(i).getBuilder().getGender() + Color.RESET + " ";
            else printableBoard[j][i%7] = " " + listOfCells.get(i).getLevel().getHeight() + "   ";
            if(i % 7 == 6) ++j;
        }
        String temp;
        String[] parseEffect;
        String[] effects = new String[15];
        Color color;
        int np = 0;
        for(Player p : listOfPlayers){
            color = Color.CORNER_WHITE;
            for(Player pl : players)
                if(pl.getNickname().equals(p.getNickname())){
                    color = p.getColor();
                    break;
                }
            temp = p.getDivinePower().toStringEffect();
            effects[np] = color + p.getNickname() + "\t[" + p.getDivinePower().getName().toUpperCase() + "]";
            parseEffect = p.getDivinePower().toStringEffect().split("\n");
            effects[np + 1] = color + (parseEffect.length < 1 ? "" : parseEffect[0]);
            effects[np + 2] = color + (parseEffect.length < 2 ? "" : parseEffect[1]);
            effects[np + 3] = color + (parseEffect.length < 3 ? "" : parseEffect[2]);
            effects[np + 4] = ""+Color.RESET;
            np += 5;
        }

        if(listOfPlayers.size() == 2) {
            effects[10] = "";
            effects[11] = "";
            effects[12] = "";
            effects[13] = "";
            effects[14] = "";
        }

        System.out.printf(
                Color.RESET+"                                 NORTH                                 \n" +
                        Color.RESET+"                 0     1     2     3     4     5     6                 \n" +
                        "              "+Color.CORNER_WHITE+"█"+Color.BORDER_YELLOW+"═════╦═════╦═════╦═════╦═════╦═════╦═════"+Color.CORNER_WHITE+"█              " + effects[0] + "\n"+Color.RESET+
                        "          0   "+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║"+Color.RESET+"   0          " + effects[1] + Color.RESET +"\n"+
                        "              "+Color.BORDER_YELLOW+"╠═════╬═════╬═════╬═════╬═════╬═════╬═════╣              " + effects[2] + Color.RESET +"\n"+
                        "          1   "+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║"+Color.RESET+"   1          " + effects[3] + Color.RESET +"\n"+
                        "              "+Color.BORDER_YELLOW+"╠═════╬═════╬═════╬═════╬═════╬═════╬═════╣              " + effects[4] + Color.RESET +"\n"+
                        "          2   "+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║"+Color.RESET+"   2          " + effects[5] + Color.RESET +"\n"+
                        "              "+Color.BORDER_YELLOW+"╠═════╬═════╬═════╬═════╬═════╬═════╬═════╣              " + effects[6] + Color.RESET +"\n"+
                        "   WEST   3   "+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║"+Color.RESET+"   3   EAST   " + effects[7] + Color.RESET +"\n"+
                        "              "+Color.BORDER_YELLOW+"╠═════╬═════╬═════╬═════╬═════╬═════╬═════╣              " + effects[8] + Color.RESET +"\n"+
                        "          4   "+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║"+Color.RESET+"   4          " + effects[9] + Color.RESET +"\n"+
                        "              "+Color.BORDER_YELLOW+"╠═════╬═════╬═════╬═════╬═════╬═════╬═════╣              " + effects[10] + Color.RESET +"\n"+
                        "          5   "+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║"+Color.RESET+"   5          " + effects[11] + Color.RESET +"\n"+
                        "              "+Color.BORDER_YELLOW+"╠═════╬═════╬═════╬═════╬═════╬═════╬═════╣              " + effects[12] + Color.RESET +"\n"+
                        "          6   "+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║%s"+Color.BORDER_YELLOW+"║"+Color.RESET+"   6          " + effects[13] + Color.RESET +"\n"+
                        "              "+Color.CORNER_WHITE+"█"+Color.BORDER_YELLOW+"═════╩═════╩═════╩═════╩═════╩═════╩═════"+Color.CORNER_WHITE+"█              " + effects[14] + Color.RESET +"\n"+
                        "                 0     1     2     3     4     5     6                 " +"\n"+
                        "                                 SOUTH                                 "+"\n",
                printableBoard[0][0], printableBoard[0][1], printableBoard[0][2], printableBoard[0][3], printableBoard[0][4], printableBoard[0][5], printableBoard[0][6],
                printableBoard[1][0], printableBoard[1][1], printableBoard[1][2], printableBoard[1][3], printableBoard[1][4], printableBoard[1][5], printableBoard[1][6],
                printableBoard[2][0], printableBoard[2][1], printableBoard[2][2], printableBoard[2][3], printableBoard[2][4], printableBoard[2][5], printableBoard[2][6],
                printableBoard[3][0], printableBoard[3][1], printableBoard[3][2], printableBoard[3][3], printableBoard[3][4], printableBoard[3][5], printableBoard[3][6],
                printableBoard[4][0], printableBoard[4][1], printableBoard[4][2], printableBoard[4][3], printableBoard[4][4], printableBoard[4][5], printableBoard[4][6],
                printableBoard[5][0], printableBoard[5][1], printableBoard[5][2], printableBoard[5][3], printableBoard[5][4], printableBoard[5][5], printableBoard[5][6],
                printableBoard[6][0], printableBoard[6][1], printableBoard[6][2], printableBoard[6][3], printableBoard[6][4], printableBoard[6][5], printableBoard[6][6]
        );
    }

    /**
     * the method prints a 3*3 matrix that represents the possible choices both for building and moving the builders. The symbol 'X' is used
     * to represent a cell in which a builder cannot build or move into.
     * @param matrixToShow is the reference to a matrix 3*3 such as possible moves or possible buildings
     * @param type is a char that is used for understand if matrixToShow is used for representing possible moves or buildings
     * @param actions contains the possible commands the player can make
     */
    private void showPossibleMatrix(int[][] matrixToShow, char type, String[] actions){
        char[][] cell = new char[3][3];
        if(type == 'b') {
            for (int i = 0; i < 3; i++)
                for (int j = 0; j < 3; j++)
                    if (matrixToShow[i][j] == 0) cell[i][j] = '0';
                    else if (matrixToShow[i][j] == 1) cell[i][j] = '1';
                    else if (matrixToShow[i][j] == 2) cell[i][j] = '2';
                    else if (matrixToShow[i][j] == 3) cell[i][j] = '3';
                    else cell[i][j] = 'X';
        } else {
            int k = 1;
            for(int i = 0; i < 3; ++i)
                for (int j = 0; j < 3; ++j) {
                    if (matrixToShow[i][j] > 0 && matrixToShow[i][j] < 4) cell[i][j] = Character.forDigit(k, 10);
                    else cell[i][j] = ' ';
                    if(i != 1 || j != 1) ++k;
                }
        }
        System.out.printf(
                Color.RESET+"                     NORTH                                 \n" +
                        Color.RESET+"                 0     1     2                 "+Color.RESET+(actions[0] == null ? " " : actions[0])+"\n" +
                        "              "+Color.CORNER_WHITE+"█"+Color.BORDER_YELLOW+"═════╦═════╦═════"+Color.CORNER_WHITE+"█              "+ Color.RESET+ (actions[1] == null ? " " : actions[1]) + "\n"+Color.RESET+
                        "          0   "+Color.BORDER_YELLOW+"║  %c  "+Color.BORDER_YELLOW+"║  %c  "+Color.BORDER_YELLOW+"║  %c  "+Color.BORDER_YELLOW+"║"+Color.RESET+"   0          " + (actions[2] == null ? " " : actions[2]) + Color.RESET +"\n"+
                        "              "+Color.BORDER_YELLOW+"╠═════╬═════╬═════╣              " + Color.RESET+(actions[3] == null ? " " : actions[3]) + Color.RESET +"\n"+
                        "   WEST   1   "+Color.BORDER_YELLOW+"║  %c  "+Color.BORDER_YELLOW+"║  %c  "+Color.BORDER_YELLOW+"║  %c  "+Color.BORDER_YELLOW+"║"+Color.RESET+"   1   EAST   " + (actions[4] == null ? " " : actions[4]) + Color.RESET +"\n"+
                        "              "+Color.BORDER_YELLOW+"╠═════╬═════╬═════╣              " +Color.RESET+ (actions[5] == null ? " " : actions[5]) + Color.RESET +"\n"+
                        "          2   "+Color.BORDER_YELLOW+"║  %c  "+Color.BORDER_YELLOW+"║  %c  "+Color.BORDER_YELLOW+"║  %c  "+Color.BORDER_YELLOW+"║"+Color.RESET+"   2          " + (actions[6] == null ? " " : actions[6]) + Color.RESET +"\n"+
                        "              "+Color.CORNER_WHITE+"█"+Color.BORDER_YELLOW+"═════╩═════╩═════"+Color.CORNER_WHITE+"█              " + Color.RESET+(actions[7] == null ? " " : actions[7]) + Color.RESET +"\n"+
                        "                 0     1     2                 " +"\n"+
                        "                     SOUTH                                 "+"\n",
                cell[0][0], cell[0][1], cell[0][2],
                cell[1][0], cell[1][1], cell[1][2],
                cell[2][0], cell[2][1], cell[2][2]
        );
    }
}
