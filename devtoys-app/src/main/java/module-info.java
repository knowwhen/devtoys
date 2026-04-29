module io.devtoys.app {
    requires io.devtoys.api;
    requires io.devtoys.core;
    requires io.devtoys.tools;
    requires javafx.controls;
    requires javafx.graphics;
    requires java.logging;
    requires java.compiler;
    requires atlantafx.base;

    uses io.devtoys.api.IGuiTool;

    exports io.devtoys.app;
}