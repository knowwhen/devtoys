module io.devtoys.core {
    requires java.logging;
    requires transitive io.devtoys.api;

    uses io.devtoys.api.IGuiTool;

    exports io.devtoys.core;
    exports io.devtoys.core.services;
}