package org.heckcorp.spacegame;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.heckcorp.spacegame.model.Model;
import org.heckcorp.spacegame.model.Player;
import org.heckcorp.spacegame.model.Unit;
import org.heckcorp.spacegame.ui.GameViewPane;
import org.heckcorp.spacegame.ui.map.MapUtils;
import org.heckcorp.spacegame.ui.map.Point;
import org.heckcorp.spacegame.ui.map.ViewResources;

import java.io.FileNotFoundException;

public class SpaceGame extends Application {

  @Override
  public void start(Stage stage) throws FileNotFoundException {
    ViewResources viewResources = new ViewResources();
    ViewResources.Identifier hexImageId =
        viewResources.addImageResource(ResourceLoader.getResource("resource/hex-large-light.png"));
    MapUtils mapUtils = new MapUtils(viewResources, hexImageId);
    Model model = new Model();
    GameViewPane gameViewPane = GameViewPane.create(model, mapUtils);
    Controller.create(model, gameViewPane, viewResources);

    Player humanPlayer = new Player("Human Player", .25, .45, .85);
    Player computerPlayer = new Player("Computer Player", .75, .25, .25);
    ViewResources.Identifier spaceshipId =
        viewResources.addImageResource(ResourceLoader.getResource("resource/spaceship.png"));
    model.addUnit(new Unit(humanPlayer, spaceshipId), new Point(1, 1));
    model.addUnit(new Unit(computerPlayer, spaceshipId), new Point(5, 5));

    Scene scene = new Scene(gameViewPane);
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args) {
    launch();
  }
}
