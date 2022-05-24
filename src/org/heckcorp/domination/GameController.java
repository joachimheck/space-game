package org.heckcorp.domination;


public interface GameController {
    /**
     * Starts the game thread, which visits one user after
     * another and processes their actions.
     */
    public void startGame();

    /**
     * Sets the model this controller controls.
     * @param model
     * @pre model != null
     */
    public void setModel(GameModel model);
    
    /**
     * Notifies this controller that the current player
     * has changed.
     * @param player
     * @pre player != null
     */
    public void setCurrentPlayer(Player player);
}
