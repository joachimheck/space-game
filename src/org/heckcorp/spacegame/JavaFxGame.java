package org.heckcorp.spacegame;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
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
import org.heckcorp.spacegame.map.Hex;
import org.heckcorp.spacegame.map.HexMap;
import org.heckcorp.spacegame.map.MouseButton;
import org.heckcorp.spacegame.map.Point;
import org.heckcorp.spacegame.map.ViewMonitor;
import org.heckcorp.spacegame.map.javafx.GameViewPane;
import org.heckcorp.spacegame.map.javafx.MapCanvas;
import org.heckcorp.spacegame.map.javafx.MapUtils;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;

import static org.heckcorp.spacegame.Constants.*;

public class JavaFxGame extends Application implements ViewMonitor {

    @Override
    public void start(Stage stage) throws FileNotFoundException {
//        GameModel model = new NewGameInitializer().initialize(view, Constants.MAP_WIDTH, Constants.MAP_HEIGHT);

        MapUtils mapUtils = new MapUtils();
        BorderPane mapPane = new BorderPane(new MapCanvas(new HexMap(MAP_WIDTH, MAP_HEIGHT), mapUtils));
        mapPane.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
        gameViewPane = new GameViewPane(mapUtils, this);
        Unit spaceship = new Unit(
                Unit.Type.SPACESHIP,
                new HumanPlayer("player1", java.awt.Color.BLUE),
                new Hex(1, 1));
        gameViewPane.addUnit(spaceship);
        gameViewPane.setOnMouseClicked(gameViewPane::onMouseClicked);
        StackPane gameViewStackPane = new StackPane(mapPane, gameViewPane);
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

    @Override
    public void hexClicked(Point hexPos, MouseButton button) {
        assert gameViewPane != null;
        gameViewPane.unselectHex();
        if (button == MouseButton.PRIMARY) {
            gameViewPane.selectHex(hexPos);
        }
    }

    public static void main(String[] args) {
        launch();
    }

    // TODO: make final or better yet, move this and the ViewMonitor implementation into a separate class.
    @Nullable
    private GameViewPane gameViewPane;
}
