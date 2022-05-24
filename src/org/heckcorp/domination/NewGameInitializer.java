package org.heckcorp.domination;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.heckcorp.domination.Player.PlayerType;
import org.heckcorp.domination.desktop.ComputerPlayer;
import org.heckcorp.domination.desktop.HumanPlayer;
import org.heckcorp.domination.desktop.NeutralPlayer;

public class NewGameInitializer implements ModelInitializer {
    
    private int width;
    private int height;

    public NewGameInitializer(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    private List<Player> createPlayers(GameModel model, GameView mainPlayerView) {
        List<Player> players = new ArrayList<Player>();
        Dimension mapSize = new Dimension(width, height);
        String[] playerNames = { "Human Player", "Computer Player", "Neutral Player" };
        PlayerType[] playerTypes =
            { PlayerType.HUMAN, PlayerType.COMPUTER, PlayerType.NEUTRAL };
        Color[] playerColors = { Constants.HUMAN_PLAYER_COLOR,
                                 Constants.COMPUTER_PLAYER_COLOR,
                                 Constants.NEUTRAL_PLAYER_COLOR };

        // Create the three players.
        for (int i=0; i<3; i++) {
            players.add(createPlayer(playerNames[i], playerTypes[i],
                                     playerColors[i], mapSize, model,
                                     mainPlayerView));
        }
        
        return players;
    }
    
    /**
     * @param name the name of the player to create.
     * @param type the type of player to create.
     * @param mapsize the size of the map the player will play in.
     */
    public static Player createPlayer(String name, Player.PlayerType type,
                                      Color color, Dimension mapSize,
                                      GameModel model, GameView view)
    {
        Player player = null;
        
        ShadowMap shadowMap = new ShadowMap(mapSize.width, mapSize.height);
    
        if (type == Player.PlayerType.HUMAN) {
            player = new HumanPlayer(name, color, shadowMap, view);
        } else if (type == Player.PlayerType.COMPUTER) {
            player = new ComputerPlayer(name, color, shadowMap, model,
                                        new ComputerPlayerView());
        } else if (type == Player.PlayerType.NEUTRAL) {
            player = new NeutralPlayer(name, color, shadowMap);
        } else {
            assert false;
        }
    
        return player;
    }

    public List<City> createCities(Player player) {
        List<City> cities = new ArrayList<City>();
        
        int cityCount = Constants.PLAYER_CITIES;
        if (player instanceof NeutralPlayer) {
            cityCount = Constants.NEUTRAL_CITIES;
        }

        for (int c=0; c<cityCount; c++) {
            City city = new City(player);
//            player.addGamePiece(city);
            cities.add(city);
        }
        
        return cities;
    }
    
    private List<Unit> createUnits(Player player) {
        Map<Unit.Type, Integer> unitCounts = new HashMap<Unit.Type, Integer>();
        unitCounts.put(Unit.Type.SOLDIER, 2);
        unitCounts.put(Unit.Type.TANK, 1);
        unitCounts.put(Unit.Type.BOMBER, 1);
        
        List<Unit> units = new ArrayList<Unit>();
        
        if (!(player instanceof NeutralPlayer)) {
            for (Unit.Type type : Unit.Type.values()) {
                for (int i=0; i<unitCounts.get(type); i++) {
                    units.add(new Unit(type, player));
                }
            }
        }
        
        return units;
    }
    
    public void initializeModel(GameModel model, GameView mainPlayerView) {
        try {
            HexMap map = new HexMap(width, height);
            model.setMap(map);

            List<Player> players = createPlayers(model, mainPlayerView);
            for (Player player : players) {
                model.addPlayer(player);

                List<City> cities = createCities(player);
                for (City city : cities) {
                    Hex hex = map.getRandomHex(City.getHexFilter());
                    model.addGamePiece(city, hex.getPosition());

                    List<Unit> units = createUnits(player);
                    for (Unit unit : units) {
                        model.addGamePiece(unit, hex.getPosition());
                    }
                }
            }
            
            model.startTurnManager();
        } catch (IllegalArgumentException e) {
            // TODO: something!
            // We couldn't find a random hex to put a city or a unit into -
            // we need to start over with a new map.
        }
    }
}
