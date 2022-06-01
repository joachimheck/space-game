package org.heckcorp.spacegame;

import org.heckcorp.spacegame.Player.PlayerType;
import org.heckcorp.spacegame.map.Hex;
import org.heckcorp.spacegame.map.HexMap;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NewGameInitializer {
    public NewGameInitializer() {
    }
    /**
     * @param name the name of the player to create.
     * @param type the type of player to create.
     */
    public static Player createPlayer(String name, Player.PlayerType type,
                                      Color color, GameModel model, GameView view)
    {
        @Nullable Player player = null;

        if (type == Player.PlayerType.HUMAN) {
            player = new HumanPlayer(name, color, view);
        } else if (type == Player.PlayerType.COMPUTER) {
            player = new ComputerPlayer(name, color, model,
                    new ComputerPlayerView());
        } else if (type == Player.PlayerType.NEUTRAL) {
            player = new NeutralPlayer(name, color);
        } else {
            assert false;
        }

        return player;
    }

    public DefaultModel initialize(GameView mainPlayerView, int width, int height) {
        HexMap map = new HexMap(width, height);
        DefaultModel model = new DefaultModel(map);

        List<Player> players = createPlayers(model, mainPlayerView);
        for (Player player : players) {
            model.addPlayer(player);

            List<Unit> units = createUnits(player);
            Hex hex = map.getRandomHex();
            for (Unit unit : units) {
                model.addUnit(unit, hex.getPosition());
            }
        }

        model.startTurnManager();
        return model;
    }

    private List<Player> createPlayers(GameModel model, GameView mainPlayerView) {
        List<Player> players = new ArrayList<>();
        String[] playerNames = { "Human Player", "Computer Player", "Neutral Player" };
        PlayerType[] playerTypes =
                { PlayerType.HUMAN, PlayerType.COMPUTER, PlayerType.NEUTRAL };
        Color[] playerColors = { Constants.HUMAN_PLAYER_COLOR,
                Constants.COMPUTER_PLAYER_COLOR,
                Constants.NEUTRAL_PLAYER_COLOR };

        // Create the three players.
        for (int i=0; i<3; i++) {
            players.add(createPlayer(playerNames[i], playerTypes[i],
                    playerColors[i], model,
                    mainPlayerView));
        }

        return players;
    }

    private List<Unit> createUnits(Player player) {
        if (player instanceof NeutralPlayer) {
            return Collections.emptyList();
        }
        return List.of(new Unit(Unit.Type.SPACESHIP, player));
    }
}
