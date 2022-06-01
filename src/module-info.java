module space.game.main {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires org.jetbrains.annotations;
    requires java.logging;
    requires com.google.common;
    requires org.checkerframework.checker.qual;
    exports org.heckcorp.spacegame;
    exports org.heckcorp.spacegame.map;
    exports org.heckcorp.spacegame.map.swing;
    exports org.heckcorp.spacegame.swing;
}
