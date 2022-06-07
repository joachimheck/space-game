package org.heckcorp.spacegame;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.heckcorp.spacegame.map.javafx.Counter;
import org.heckcorp.spacegame.map.javafx.GameViewPane;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class Controller {

    public Controller(JavaFxModel model, GameViewPane view) throws FileNotFoundException {
        this.model = model;
        this.view = view;
    }

    public void listenForPropertyChanges() {
        model.selectedHexPositionProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                view.unselectHex();
            } else {
                view.selectHex(newValue.get());
            }
        });
        model.unitsProperty().addListener((observable, oldValue, newValue) -> {
            Sets.difference(oldValue, newValue).forEach(u -> view.removeCounter(unitCounters.get(u)));
            Sets.difference(newValue, oldValue).forEach(u -> {
                Player.Color color = u.getOwner().getColor();
                unitCounters.put(u, new Counter(SPACESHIP_IMAGE, new Color(color.r(), color.g(), color.b(), 1.0)));
                view.addCounter(unitCounters.get(u), model.unitPositionsProperty().get().get(u));
            });
        });
        model.unitPositionsProperty().addListener((observable, oldValue, newValue) ->
                Maps.difference(oldValue, newValue).entriesDiffering().forEach((u, d) ->
                        view.moveCounter(unitCounters.get(u), d.leftValue(), d.rightValue())));
    }

    private final GameViewPane view;
    private final JavaFxModel model;
    private final Map<Unit, Counter> unitCounters = new HashMap<>();

    private final Image SPACESHIP_IMAGE = new Image(Util.getResource("resource/spaceship.png"));
}
