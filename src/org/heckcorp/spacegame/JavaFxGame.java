package org.heckcorp.spacegame;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.heckcorp.spacegame.map.Point;
import org.heckcorp.spacegame.map.javafx.ControllerPane;
import org.heckcorp.spacegame.map.javafx.GameViewPane;
import org.heckcorp.spacegame.map.javafx.MapCanvas;
import org.heckcorp.spacegame.map.javafx.MapUtils;

import java.io.FileNotFoundException;

import static org.heckcorp.spacegame.Constants.*;

public class JavaFxGame extends Application {

    @Override
    public void start(Stage stage) throws FileNotFoundException {
        JavaFxModel model = new JavaFxModel();
        MapUtils mapUtils = new MapUtils();
        GameViewPane gameViewPane = new GameViewPane(mapUtils);
        ControllerPane controllerPane = new ControllerPane(model, gameViewPane, mapUtils);
        controllerPane.setOnMouseClicked(controllerPane::onMouseClicked);
        Player humanPlayer = new Player(.25, .45, .85);
        model.addPlayer(humanPlayer);
        Player computerPlayer = new Player(.75, .25, .25);
        model.addPlayer(computerPlayer);
        model.addUnit(new Unit(humanPlayer), new Point(1, 1));
        model.addUnit(new Unit(computerPlayer), new Point(5, 5));
        BorderPane mapPane = new BorderPane(new MapCanvas(mapUtils, MAP_WIDTH, MAP_HEIGHT));
        StackPane gameViewStackPane = new StackPane(mapPane, gameViewPane, controllerPane);
        gameViewStackPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(10))));
        ScrollPane mapScrollPane = new ScrollPane(gameViewStackPane);
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
