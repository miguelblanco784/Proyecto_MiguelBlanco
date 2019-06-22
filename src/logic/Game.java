package logic;

import java.util.ArrayList;
import java.util.Random;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import application.Controller;

public class Game {

    private Deck deck;
    private DeadDeck deadDeck;
    private Player player;
    private ArrayList<AI> ais;
    private int gameCount;
    private int challengeCounter;
    private int currentPlayer;
    private Card lastCard;
    private Color wishColor;
    private boolean challenge;
    private Direction direction;
    private Controller controller;
    private boolean lastPlayerDraw;
    private boolean skipped;
    private int counter;
    private boolean running;
    private boolean showingInfo;
    private int aiSpeed;

    public Game(Controller controller, int numberOfAIs, int aiSpeed) {
        this.controller = controller;
        this.aiSpeed = aiSpeed;
        deck = new Deck();
        deadDeck = new DeadDeck();
        player = new Player("Jugador", this);
        ais = new ArrayList<AI>();

        if (numberOfAIs == 1) {
            ais.add(new AI("Computadora 1", 1, this));
        } else if (numberOfAIs == 2) {
            ais.add(new AI("Computadora 1", 1, this));
            ais.add(new AI("Computadora 2", 2, this));
        } else if (numberOfAIs == 3) {
            ais.add(new AI("Computadora 1", 3, this));
            ais.add(new AI("Computadora 2", 1, this));
            ais.add(new AI("Computadora 3", 2, this));
        }

        gameCount = 0;
        challengeCounter = 0;
    }

    public void newGame(int numberOfStartingCards) {
        deck = new Deck();
        deck.shuffle();
        deadDeck = new DeadDeck();
        gameCount++;
        challengeCounter = 0;
        lastCard = null;
        wishColor = null;
        challenge = false;
        direction = Direction.RIGHT;
        controller.setImageViewDirection(Direction.RIGHT);
        lastPlayerDraw = false;
        skipped = false;
        showingInfo = false;

        player.initialize();

        player.drawCards(deck.drawCards(numberOfStartingCards, deadDeck));

        for (AI currentAI : ais) {
            currentAI.initialize();
            currentAI.drawCards(deck.drawCards(numberOfStartingCards, deadDeck));
        }

        deadDeck.add(deck.drawCard(deadDeck));
        lastCard = deadDeck.getCards().get(deadDeck.getCards().size() - 1);

        controller.setLastCard(lastCard);
        if (lastCard.getType().equals(CardType.WILD)) {
            wishColor = Color.ALL;
            controller.chosenWishColor = wishColor;
            controller.showCircleWishColor(wishColor);
        } else if (lastCard.getType().equals(CardType.DRAW_FOUR)) {
            wishColor = Color.ALL;
            controller.chosenWishColor = wishColor;
            controller.showCircleWishColor(wishColor);
            challenge = true;
            challengeCounter = 4;
        } else if (lastCard.getType().equals(CardType.DRAW_TWO)) {
            challenge = true;
            challengeCounter = 2;
        }
    }

    public int getGameCount() {
        return gameCount;
    }

    public void start() {
        running = true;
        Random random = new Random();
        currentPlayer = random.nextInt(ais.size() + 1) + 1;

        counter = 0;

        run();
    }

    private void run() {
        if (running) {
            if (player.getDeckSize() == 0) {
                end(player.getName());
                return;
            }

            for (AI winningAI : ais) {
                if (winningAI.getDeckSize() == 0) {
                    end(winningAI.getName());
                    return;
                }
            }

            System.out.println("ROUND: " + counter / 4 + 1);

            if (lastCard.getType().equals(CardType.REVERSE) && !lastPlayerDraw) {
                if (direction.equals(Direction.RIGHT)) {
                    direction = Direction.LEFT;
                    controller.setImageViewDirection(Direction.LEFT);

                } else {
                    direction = Direction.RIGHT;
                    controller.setImageViewDirection(Direction.RIGHT);
                }
            }

            determineNextPlayer();

            System.out.println("Player " + currentPlayer + "'s turn");

            if (skipped || !lastCard.getType().equals(CardType.SKIP)) {
                if (currentPlayer == 1) {
                    controller.setLabelCurrentPlayer(player.getName() + " esta en Turno");

                    ArrayList<Card> validDeck = player.getValidCards(lastCard, wishColor, challenge);
                    controller.setValidPlayerDeck(player.getDeck(), validDeck);

                    controller.playerMustChallenge = false;
                    if (challenge && validDeck.size() > 0) {
                        controller.playerMustChallenge = true;
                    }

                    player.turn(lastCard, wishColor, challenge);
                } else {
                    if (running) {
                        AI currentAI = ais.get(currentPlayer - 2);

                        controller.setLabelCurrentPlayer(currentAI.getName() + " esta en Turno");

                        controller.setAIDeck(currentAI);

                        try {
                            switch (aiSpeed) {
                                case 1:
                                    Thread.sleep(500);
                                    break;
                                case 2:
                                    Thread.sleep(250);
                                    break;
                                case 3:
                                    Thread.sleep(50);
                                    break;
                                case 4:
                                    Thread.sleep(0);
                                    break;
                                default:
                                    break;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        currentAI.turn(lastCard, wishColor, challenge);
                    }
                }
            } else {
                if (!skipped) {
                    System.out.println("SKIPPED player " + currentPlayer);
                    skipped = true;
                    run();
                }
            }
            counter++;
        }
    }

    private void determineNextPlayer() {
        if (direction.equals(Direction.RIGHT)) {
            if (currentPlayer == ais.size() + 1) {
                currentPlayer = 1;
            } else {
                currentPlayer++;
            }
        } else {
            if (currentPlayer == 1) {
                currentPlayer = ais.size() + 1;
            } else {
                currentPlayer--;
            }
        }
    }

    private void end(String name) {
        controller.clearAllDecks(ais);
        controller.clearAll();
        System.err.println("Player " + name + " wins!");

        running = false;

        if (currentPlayer == 1) {
            player.win();

            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Felicidades!!!!");
            alert.setHeaderText("");
            alert.setContentText("GANASTE!!!!");
            alert.initOwner(controller.stage);
            Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
            dialogStage.getIcons().add(controller.icon);
            alert.show();

            controller.showNeutralUI();
        } else {
            player.resetWinsInARow();

            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("PERDISTE!");
            alert.setHeaderText("");
            alert.setContentText(name + " GANO!");
            alert.initOwner(controller.stage);
            Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
            dialogStage.getIcons().add(controller.icon);
            alert.show();

            controller.showNeutralUI();
        }
    }

    public Deck getDeck() {
        return deck;
    }

    public DeadDeck getDeadDeck() {
        return deadDeck;
    }

    public int getChallengeCounter() {
        return challengeCounter;
    }

    public Player getPlayer() {
        return player;
    }

    public ArrayList<AI> getAIs() {
        return ais;
    }

    public boolean isRunning() {
        return running;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public Controller getController() {
        return controller;
    }

    public boolean isShowingInfo() {
        return showingInfo;
    }

    public void setShowingInfo(boolean showingInfo) {
        this.showingInfo = showingInfo;
    }

    public void draw() {
        challenge = false;
        challengeCounter = 0;
        lastPlayerDraw = true;
        controller.hideLabelChallengeCounter();

        run();
    }

    public void playCard(Card card, Color wishColor) {
        deadDeck.add(card);
        lastCard = card;
        this.wishColor = wishColor;

        if (card.getType().equals(CardType.DRAW_TWO)) {
            challenge = true;
            challengeCounter += 2;
            controller.showLabelChallengeCounter("El perdedor tira   " + challengeCounter + " Tarjetas");
        } else if (card.getType().equals(CardType.DRAW_FOUR)) {
            challenge = true;
            challengeCounter += 4;
            controller.showLabelChallengeCounter("El perdedor tira   " + challengeCounter + " Tarjetas");
        } else {
            challenge = false;
            challengeCounter = 0;
            controller.hideLabelChallengeCounter();
        }

        lastPlayerDraw = false;
        skipped = false;
        controller.setLastCard(lastCard);

        System.out.println("new lastCard: " + lastCard);
        System.out.println("new wishColor: " + this.wishColor);
        System.out.println("new challenge: " + challenge);
        System.out.println("new challengeCounter: " + challengeCounter);

        run();
    }

    public void stop() {
        running = false;
        System.out.println("STOPPED");
    }
}
