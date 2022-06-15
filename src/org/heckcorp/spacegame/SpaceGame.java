package org.heckcorp.spacegame;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.heckcorp.spacegame.model.*;
import org.heckcorp.spacegame.ui.GameViewPane;
import org.heckcorp.spacegame.ui.map.MapUtils;
import org.heckcorp.spacegame.ui.map.Point;
import org.heckcorp.spacegame.ui.map.ViewResources;

import java.io.FileNotFoundException;

public class SpaceGame extends Application {

  @Override
  public void start(Stage stage) throws FileNotFoundException {
    MapUtils mapUtils = new MapUtils(Constants.HEX_RADIUS);
    Model model = new Model(mapUtils);
    ViewResources viewResources = new ViewResources();
    GameViewPane gameViewPane = GameViewPane.create(model, mapUtils, viewResources);
    Controller.create(model, gameViewPane);

    Player humanPlayer = new Player("Human Player", .25, .45, .85);
    Player computerPlayer = new Player("Computer Player", .75, .25, .25);
    ViewResources.Identifier spaceshipId =
        viewResources.addImageResource(ResourceLoader.getResource("resource/spaceship.png"));
    model.addUnit(
        new Unit(humanPlayer, spaceshipId), new MapPosition(new Point(1, 1), Direction.NORTH));
    model.addUnit(
        new Unit(computerPlayer, spaceshipId), new MapPosition(new Point(5, 5), Direction.NORTH));

    Scene scene = new Scene(gameViewPane);
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args) {
    launch();
  }
}
