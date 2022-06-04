package org.heckcorp.spacegame.map.javafx;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.heckcorp.spacegame.Direction;
import org.heckcorp.spacegame.GameView;
import org.heckcorp.spacegame.Unit;
import org.heckcorp.spacegame.UnitStatus;
import org.heckcorp.spacegame.map.Hex;
import org.heckcorp.spacegame.map.HexMap;
import org.heckcorp.spacegame.map.swing.Util;
import org.heckcorp.spacegame.map.swing.ViewMonitor;

import java.awt.*;
import java.io.FileNotFoundException;
import java.util.logging.Logger;

public class GameViewPane extends Pane implements GameView {
    @Override
    public void message(String message) {

    }

    @Override
    public void addUnit(Unit unit) {
        try {
            Image spaceshipImage = new Image(Util.getResource("resource/spaceship.png"));
            Counter counter = new Counter(spaceshipImage);
            getChildren().add(counter);
            Point2D pixelPos = mapUtils.getHexCenter(new MapUtils.Point(unit.getPosition().x, unit.getPosition().y));
            setCounterLocation(counter, pixelPos, spaceshipImage.getWidth(), spaceshipImage.getHeight());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
//        Rotate rotate = new Rotate(90);
//        imageView.getTransforms().add(rotate);



    }

    public void setCounterLocation(Counter counter, Point2D location, double width, double height) {
        counter.relocate(location.getX() - (width / 2.0), location.getY() - (height / 2.0));
    }

    @Override
    public void attack(Unit attacker, Unit target) {

    }

    @Override
    public void move(Unit unit, Direction direction) {
//        Path path = new Path();
//        path.getElements().add(new MoveTo(200, 200));
//        path.getElements().add(new CubicCurveTo(400, 40, 175, 250, 500, 150));
//        PathTransition pathTransition = new PathTransition(Duration.seconds(5), path, imageView);
//        pathTransition.setOrientation(PathTransition.OrientationType.NONE);
//        pathTransition.setAutoReverse(true);
//        pathTransition.play();

    }

    @Override
    public void selectHex(Hex hex) {

    }

    @Override
    public void setMonitor(ViewMonitor monitor) {

    }

    @Override
    public void setCurrentPlayer(String playerName) {

    }

    @Override
    public void setMap(HexMap map) {

    }

    @Override
    public void initialize() {

    }

    @Override
    public void setStatus(Unit unit, UnitStatus status) {

    }

    @Override
    public void setWinningPlayer(String playerName, Color playerColor) {

    }

    public GameViewPane(MapUtils mapUtils) {
        this.mapUtils = mapUtils;
    }

    private final MapUtils mapUtils;

    public void onMouseClicked(MouseEvent mouseEvent) {
        Point2D position = new Point2D(mouseEvent.getX(), mouseEvent.getY());
        MapUtils.Point hexCoordinates = mapUtils.getHexCoordinates(position);
        Logger logger = Logger.getLogger(GameViewPane.class.getName());
        logger.info("Mouse Clicked at " + position.getX() + ", " + position.getY()
                + ": " + hexCoordinates.x() + ", " + hexCoordinates.y());
    }
}
