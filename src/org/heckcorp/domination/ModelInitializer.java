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
     * @pre model != null
     * @pre mainPlayerView != null
     */
    void initializeModel(GameModel model, GameView mainPlayerView) throws IOException, ClassNotFoundException;
}
