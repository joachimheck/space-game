package org.heckcorp.spacegame;

import javafx.animation.PathTransition;
import javafx.animation.PathTransition.OrientationType;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.heckcorp.spacegame.map.swing.Util;

import java.io.FileNotFoundException;

import static org.heckcorp.spacegame.Constants.UI_COMPONENT_LARGE_HEIGHT;
import static org.heckcorp.spacegame.Constants.UI_COMPONENT_LARGE_WIDTH;

public class JavaFxGame extends Application {

    @Override
    public void start(Stage stage) throws FileNotFoundException {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        Label l = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");
        ImageView imageView = new ImageView(new Image(Util.getResource("resource/spaceship.png")));
        Rotate rotate = new Rotate(90);
        imageView.getTransforms().add(rotate);

        Path path = new Path();
        path.getElements().add(new MoveTo(200, 200));
        path.getElements().add(new CubicCurveTo(400, 40, 175, 250, 500, 150));
        PathTransition pathTransition = new PathTransition(Duration.seconds(5), path, imageView);
        pathTransition.setOrientation(OrientationType.NONE);
        pathTransition.setAutoReverse(true);
        pathTransition.play();

        Scene scene = new Scene(new FlowPane(l, imageView), UI_COMPONENT_LARGE_WIDTH, UI_COMPONENT_LARGE_HEIGHT);
//        Scene scene = new Scene(new Group(imageView), UI_COMPONENT_LARGE_WIDTH, UI_COMPONENT_LARGE_HEIGHT);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
