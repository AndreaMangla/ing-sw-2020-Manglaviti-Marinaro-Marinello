package it.polimi.ingsw2020.santorini.exceptions;

import it.polimi.ingsw2020.santorini.model.Match;

public class EndMatchException extends Exception {
    private Match match;

    /**
     * Constructs a new exception with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     * @param match the match that ended and has to be notified to all players currently in the match
     */
    public EndMatchException(Match match) {
        this.match = match;
    }

    public Match getMatch() {
        return match;
    }
}
