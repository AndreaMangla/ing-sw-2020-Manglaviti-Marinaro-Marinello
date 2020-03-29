package it.polimi.ingsw2020.santorini.model;

/**
 * this enum represents the status of the cells:
 * Occupied if there is a builder in it
 * Free in the opposite case
 * Forbidden in the case the cell represents the limit of the board
 * Dome if it is a dome built in the cell
 */
public enum AccessType {
    Occupied, Free, Forbidden, Dome;
}
