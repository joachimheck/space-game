package org.heckcorp.spacegame;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.heckcorp.spacegame.map.Point;
import org.heckcorp.spacegame.map.javafx.GameViewPane;
import org.heckcorp.spacegame.map.javafx.MapUtils;
import org.heckcorp.spacegame.map.javafx.ViewResources;
import org.heckcorp.spacegame.model.Model;
import org.heckcorp.spacegame.model.Player;
import org.heckcorp.spacegame.model.Unit;

import java.io.FileNotFoundException;

public class SpaceGame extends Application {

  @Override
  public void start(Stage stage) throws FileNotFoundException {
    MapUtils mapUtils = new MapUtils();
    ViewResources viewResources = new ViewResources();
    Model model = new Model();
    GameViewPane gameViewPane = GameViewPane.create(model, mapUtils);
    Controller.create(model, gameViewPane, viewResources);

    Player humanPlayer = new Player("Human Player", .25, .45, .85);
    Player computerPlayer = new Player("Computer Player", .75, .25, .25);
    model.addPlayer(humanPlayer);
    model.addPlayer(computerPlayer);
    ViewResources.Identifier spaceshipId =
        viewResources.addImageResource(ResourceLoader.getResource("resource/spaceship.png"));
    model.addUnit(new Unit(humanPlayer, spaceshipId, 5, 5), new Point(1, 1));
    model.addUnit(new Unit(computerPlayer, spaceshipId, 5, 5), new Point(5, 5));

    Scene scene = new Scene(gameViewPane);
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args) {
    launch();
  }
}
