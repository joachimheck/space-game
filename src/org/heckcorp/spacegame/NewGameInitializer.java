package org.heckcorp.spacegame;

import com.google.common.collect.Lists;
import org.heckcorp.spacegame.Player.PlayerType;
import org.heckcorp.spacegame.map.HexMap;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class NewGameInitializer {
    public DefaultModel initialize(GameView mainPlayerView, int width, int height) {
        HexMap map = new HexMap(width, height);
        mainPlayerView.setMap(map);

        List<GameView> views = Lists.newArrayList();
        List<Player> players = createPlayers(mainPlayerView);
        for (Player player : players) {
            GameView playerView = player.getType() == PlayerType.HUMAN
                    ? mainPlayerView : new ComputerPlayerView();
            views.add(playerView);

            List<Unit> units = createUnits(player);
            for (Unit unit : units) {
                unit.setHex(map.getRandomHex());
                map.addUnit(unit, unit.getPosition());
                playerView.addUnit(unit);
                if (playerView != mainPlayerView) {
                    mainPlayerView.addUnit(unit);
                }
            }
        }

        DefaultModel model = new DefaultModel(map, players, views, players.get(0));
        model.startTurnManager();
        return model;
    }

    private List<Player> createPlayers(GameView mainPlayerView) {
        List<Player> players = new ArrayList<>();
        String[] playerNames = { "Human Player", "Computer Player" };
        PlayerType[] playerTypes = { PlayerType.HUMAN, PlayerType.COMPUTER };
        Color[] playerColors = { Constants.HUMAN_PLAYER_COLOR, Constants.COMPUTER_PLAYER_COLOR };

        // Create the two players.
        for (int i=0; i<2; i++) {
            players.add(createPlayer(playerNames[i], playerTypes[i], playerColors[i], mainPlayerView));
        }

        return players;
    }

    /**
     * @param name the name of the player to create.
     * @param type the type of player to create.
     */
    public static Player createPlayer(String name, Player.PlayerType type, Color color, GameView view) {
        @Nullable Player player = null;

        if (type == Player.PlayerType.HUMAN) {
            player = new HumanPlayer(name, color);
        } else if (type == Player.PlayerType.COMPUTER) {
            player = new ComputerPlayer(name, color);
        } else {
            assert false;
        }

        return player;
    }

    private List<Unit> createUnits(Player player) {
        return List.of(new Unit(Unit.Type.SPACESHIP, player));
    }
}
