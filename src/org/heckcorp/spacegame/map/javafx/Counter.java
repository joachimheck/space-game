package org.heckcorp.spacegame.map.javafx;

import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.io.InputStream;

public class Counter extends Pane {
    public Counter(InputStream imageStream, Color backgroundColor) {
        Image image = new Image(imageStream);
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
