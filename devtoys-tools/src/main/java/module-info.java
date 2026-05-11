/**
 * Built-in tools module.
 *
 * <p>Each tool class is registered as an {@link IGuiTool} service via the
 * {@code provides ... with} directive. The host's {@code ToolRegistry}
 * discovers them through {@link java.util.ServiceLoader}.
 *
 * <p>A {@code META-INF/services/io.devtoys.api.IGuiTool} file listing the same
 * classes is also provided, so the tools are still discoverable when the app
 * is launched from the plain classpath.
 */
module io.devtoys.tools {
    requires io.devtoys.api;
    requires io.devtoys.core;
    requires org.slf4j;

    provides io.devtoys.api.IGuiTool with
            io.devtoys.tools.base64.Base64Tool;
}