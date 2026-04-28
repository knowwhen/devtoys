module devtoys.app {
    requires devtoys.api;
    requires devtoys.core;
    requires javafx.controls;
    requires javafx.graphics;
    requires java.logging;

    uses io.devtoys.api.IGuiTool;

    exports io.devtoys.app;
}