package aircom.model;

/**
 *simple but important, this basically is a packet of key data that is used to synch and pass info from class to classs
 *
 * @param row the row index, 0-based, increases downward on screen
 * @param col the column index, 0-based, increases rightward on screen
 */
public record GridC(int row, int col) {
}