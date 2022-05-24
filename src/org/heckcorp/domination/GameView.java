package org.heckcorp.domination;



public interface GameView {
    /**
     * Displays a message in the view.
     * @pre message != null
     */
    public void message(String message);
    
    /**
     * Sets this player as the one watching this view.
     * @pre mainPlayer != null
     * @pre this method must not have been called already.
     */
    public void setMainPlayer(Player mainPlayer);
    
    /**
     * Makes the unit available for use in the UI.
     * @pre piece != null
     * @pre piece.getPosition() != null
     */
    public void addGamePiece(GamePiece piece);
    
    /**
     * Initiates an attack.
     * @param attacker
     * @param target
     * @pre attacker != null
     * @pre target != null
     * @pre attacker has been added to this view, and not destroyed
     * @pre attacker has been added to this view, and not destroyed
     * @pre attacker.canAttack(target.getHex())
     */
    public void attack(Unit attacker, Unit target);
    
    /**
     * Moves the unit.
     * @param unit
     * @param direction
     * 
     * @pre unit != null
     * @pre direction != null
     * @pre unit has been added to this view, and not destroyed
     */
    public void move(Unit unit, Direction direction);
    
    /**
     * Selects the specified hex.  If a unit or another hex is
     * currently selected, it is first unselected.
     * @param hex
     * @pre hex != null
     */
    public void selectHex(Hex hex);
    
    /**
     * Sets this view's monitor.  The view will update the
     * controller whenever the viewport size or position changes.
     * @param monitor
     * @pre monitor != null
     */
    public void setMonitor(ViewMonitor monitor);
    
    /**
     * Sets this view's current player to the specified player.
     * @param player
     * @pre player != null
     */
    public void setCurrentPlayer(Player player);
    
    /**
     * Sets the map to be displayed in this view.
     * @param map
     * @pre map != null
     */
    public void setMap(HexMap map);

    /**
     * Sets the shadow map to be displayed in this view.
     * @param shadowMap
     * @pre shadowMap != null
     * @pre setMap() must have already been called.
     * @pre shadowMap().getSize() == size of this view's current map.
     */
    public void setShadowMap(ShadowMap shadowMap);

    /**
     * Sets a status attribute of the unit.
     * @param unit
     * @param status
     * @pre unit != null
     * @pre unit has been added to this view, and not destroyed.
     */
    public void setStatus(Unit unit, Status status);

    /**
     * Announces that the specified player is the winner.
     * @param player
     * @pre player != null
     */
    public void setWinningPlayer(Player player);
}
