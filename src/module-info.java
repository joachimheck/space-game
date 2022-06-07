module space.game.main {
    requires org.checkerframework.checker.qual;
    requires com.google.common;
    requires java.desktop;
    requires java.logging;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jetbrains.annotations;
    exports org.heckcorp.spacegame;
    exports org.heckcorp.spacegame.map;
    exports org.heckcorp.spacegame.map.javafx;
}
