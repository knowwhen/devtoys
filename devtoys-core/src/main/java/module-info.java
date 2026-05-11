module io.devtoys.core {
    requires org.slf4j;
    requires transitive io.devtoys.api;

    uses io.devtoys.api.IGuiTool;

    exports io.devtoys.core;
    exports io.devtoys.core.services;
    exports io.devtoys.core.tasks;
}