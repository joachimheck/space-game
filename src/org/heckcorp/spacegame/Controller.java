package org.heckcorp.spacegame;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.heckcorp.spacegame.map.javafx.Counter;
import org.heckcorp.spacegame.map.javafx.GameViewPane;
import org.heckcorp.spacegame.map.javafx.ViewResources;

import java.util.HashMap;
import java.util.Map;

public class Controller {

    public Controller(Model model, GameViewPane view, ViewResources viewResources) {
        this.model = model;
        this.view = view;
        this.viewResources = viewResources;
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
                unitCounters.put(u, new Counter(viewResources, u.getImageId(), color.r(), color.g(), color.b()));
                view.addCounter(unitCounters.get(u), model.unitPositionsProperty().get().get(u));
            });
        });
        model.unitPositionsProperty().addListener((observable, oldValue, newValue) ->
                Maps.difference(oldValue, newValue).entriesDiffering().forEach((u, d) ->
                        view.moveCounter(unitCounters.get(u), d.leftValue(), d.rightValue())));
    }

    private final Model model;
    private final Map<Unit, Counter> unitCounters = new HashMap<>();
    private final GameViewPane view;
    private final ViewResources viewResources;
}
