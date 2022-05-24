package org.heckcorp.domination;

import java.io.IOException;

/**
 * A ModelInitializer contains all the information required
 * for a GameModel to initialize its database.
 * 
 * @author Joachim Heck
 */
public interface ModelInitializer {
    /**
     * Initializes the specified model.
     * @param model
     * @param mainPlayerView
     * @throws IOException 
     * @throws ClassNotFoundException 
     * @pre model != null
     * @pre mainPlayerView != null
     */
    public void initializeModel(GameModel model, GameView mainPlayerView) throws IOException, ClassNotFoundException;
}
