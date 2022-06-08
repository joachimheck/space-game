package org.heckcorp.spacegame;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.heckcorp.spacegame.map.Point;
import org.heckcorp.spacegame.map.javafx.Counter;
import org.heckcorp.spacegame.map.javafx.GameViewPane;
import org.heckcorp.spacegame.map.javafx.ViewResources;
import org.heckcorp.spacegame.model.Model;
import org.heckcorp.spacegame.model.Player;
import org.heckcorp.spacegame.model.Unit;

import java.util.HashMap;
import java.util.Map;

public class Controller {

    public void listenForPropertyChanges() {
        model.selectedHexPositionProperty().addListener(new ChangeListener<>() {
            // TODO: Figure out how to push @Nullable into this lambda.
            @Override
            public void changed(ObservableValue<? extends Point> observable, Point oldValue, @Nullable Point newValue) {
                if (newValue == null) {
                    view.unselectHex();
                } else {
                    view.unselectHex();
                    view.selectHex(newValue);
                }
            }
        });
        model.selectedUnit().addListener((observable, oldValue, newValue) -> view.selectUnit(newValue));
        model.unitsProperty().addListener((observable, oldValue, newValue) -> {
            Sets.difference(oldValue, newValue).forEach(u -> view.removeCounter(unitCounters.get(u)));
            Sets.difference(newValue, oldValue).forEach(u -> {
                Player.Color color = u.getOwner().getColor();
                unitCounters.put(u, Counter.build(viewResources, u.getImageId(), color.r(), color.g(), color.b()));
                @Nullable Point unitPosition = model.unitPositionsProperty().get().get(u);
                if (unitPosition != null) {
                    view.addCounter(unitCounters.get(u), unitPosition);
                }
            });
        });
        model.unitPositionsProperty().addListener((observable, oldValue, newValue) ->
                Maps.difference(oldValue, newValue).entriesDiffering().forEach((u, d) -> {
                    if (unitCounters.containsKey(u)) {
                        view.moveCounter(unitCounters.get(u), d.leftValue(), d.rightValue());
                    }
                }));
    }

    private Controller(Model model, GameViewPane view, ViewResources viewResources) {
        this.model = model;
        this.view = view;
        this.viewResources = viewResources;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static Controller create(Model model, GameViewPane view, ViewResources viewResources) {
        Controller controller = new Controller(model, view, viewResources);
        controller.listenForPropertyChanges();
        return controller;
    }

    private final Model model;
    private final Map<Unit, Counter> unitCounters = new HashMap<>();
    private final GameViewPane view;
    private final ViewResources viewResources;
}
