package io.devtoys.api;

import javafx.scene.Node;

public interface IGuiTool {
    /**
     * Called once, right after construction, giving the tool access to host services.
     * Default is a no-op.
     */
    default void initialize(ServiceContext context) {
        // no-op
    }

    /**
     * Returns the root JavaFX {@link Node} for this tool's view.
     *
     * <p>Implementations may cache and return the same instance across calls, or
     * build it lazily the first time. The host mounts it into the content area
     * when the user selects this tool.
     */
    Node getView();

    /**
     * Invoked when smart-detection routes clipboard or file data to this tool.
     * The default implementation is a no-op.
     */
    default void onDataReceived(String dataTypeName, Object parsedData) {
        // no-op
    }
}
