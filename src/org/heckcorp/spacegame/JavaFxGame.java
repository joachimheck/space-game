package org.heckcorp.spacegame;

import javafx.animation.PathTransition;
import javafx.animation.PathTransition.OrientationType;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.heckcorp.spacegame.map.HexMap;
import org.heckcorp.spacegame.map.javafx.MapCanvas;
import org.heckcorp.spacegame.map.swing.Util;

import java.io.FileNotFoundException;

import static org.heckcorp.spacegame.Constants.*;

public class JavaFxGame extends Application {

    @Override
    public void start(Stage stage) throws FileNotFoundException {
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

//        GameModel model = new NewGameInitializer().initialize(view, Constants.MAP_WIDTH, Constants.MAP_HEIGHT);

        BorderPane mapPane = new BorderPane(new MapCanvas(new HexMap(MAP_WIDTH, MAP_HEIGHT)));
        mapPane.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
        mapPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(10))));
        ScrollPane mapScrollPane = new ScrollPane(mapPane);
        mapScrollPane.setPrefSize(UI_COMPONENT_LARGE_WIDTH, UI_COMPONENT_LARGE_HEIGHT);
        Rectangle hexDescriptionPane = new Rectangle(UI_COMPONENT_SMALL_WIDTH, UI_COMPONENT_LARGE_HEIGHT);
        hexDescriptionPane.setFill(Color.gray(.75));
        ScrollPane textScrollPane = new ScrollPane(new Text("Text pane!"));
        textScrollPane.setPrefSize(UI_COMPONENT_LARGE_WIDTH, UI_COMPONENT_SMALL_HEIGHT);
        Canvas miniMapPane = new Canvas(UI_COMPONENT_SMALL_WIDTH, UI_COMPONENT_SMALL_HEIGHT);
        MenuBar menuBar = new MenuBar(new Menu("File"), new Menu("Game"), new Menu("Unit"));

        GridPane gridPane = new GridPane();
        GridPane.setConstraints(mapScrollPane, 0, 1);
        GridPane.setConstraints(hexDescriptionPane, 1, 1);
        GridPane.setConstraints(textScrollPane, 0, 2);
        GridPane.setConstraints(miniMapPane, 1, 2);
        gridPane.getChildren().addAll(mapScrollPane, hexDescriptionPane, textScrollPane);

        VBox vBox = new VBox(menuBar, gridPane);

        Scene scene = new Scene(vBox);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
