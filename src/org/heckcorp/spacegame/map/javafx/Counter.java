package org.heckcorp.spacegame.map.javafx;

import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class Counter extends Pane {
    public Counter(Image image, double r, double g, double b) {
        Color backgroundColor = new Color(r, g, b, 1.0);
        setBackground(new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));
        getChildren().add(new ImageView(image));
        setMinWidth(image.getWidth());
        setMinHeight(image.getHeight());
        setMaxWidth(image.getWidth());
        setMaxHeight(image.getHeight());
        setWidth(image.getWidth());
        setHeight(image.getHeight());
    }
}
