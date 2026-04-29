package io.devtoys.tools.support;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ToolLayout {
    private ToolLayout() {}

    /** Main vertical page: 24px padding, 16px gaps. */
    public static VBox page() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(24));
        return box;
    }

    /** Creates a title-and-description header. */
    public static VBox header(String title, String description) {
        Label t = new Label(title);
        t.getStyleClass().addAll("title-3");

        Label d = new Label(description == null ? "" : description);
        d.getStyleClass().add("text-muted");
        d.setWrapText(true);

        VBox v = new VBox(4, t, d);
        return v;
    }

    /** Section with a small heading above an arbitrary content node. */
    public static VBox section(String heading, Region content) {
        Label h = new Label(heading);
        h.getStyleClass().add("text-caption");
        VBox v = new VBox(6, h, content);
        VBox.setVgrow(content, Priority.SOMETIMES);
        return v;
    }

    /** Horizontal button row aligned left. */
    public static HBox buttonRow(javafx.scene.Node... nodes) {
        HBox row = new HBox(8, nodes);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /** Standard monospace TextArea used for all input / output fields. */
    public static TextArea codeArea(String promptText) {
        TextArea ta = new TextArea();
        ta.setPromptText(promptText);
        ta.setWrapText(false);
        ta.getStyleClass().add("code-area");
        ta.setStyle("-fx-font-family: 'JetBrains Mono', 'Consolas', 'Menlo', monospace;");
        return ta;
    }
}
