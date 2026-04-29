module io.devtoys.tools {
    requires io.devtoys.api;

    provides io.devtoys.api.IGuiTool with
            io.devtoys.tools.base64.Base64Tool;
}