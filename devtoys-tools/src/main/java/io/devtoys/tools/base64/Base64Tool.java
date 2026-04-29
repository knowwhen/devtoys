package io.devtoys.tools.base64;

import io.devtoys.api.IGuiTool;
import io.devtoys.api.PredefinedToolGroups;
import io.devtoys.api.ToolMetadata;
import io.devtoys.tools.support.ToolLayout;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@ToolMetadata(
        name = "io.devtoys.tools.base64.Base64Tool",
        groupName = PredefinedToolGroups.ENCODERS_DECODERS,
        shortTitle = "Base64",
        longTitle = "Base64 Encoder / Decoder",
        description = "Encode and decode Base64 strings.",
        iconGlyph = "\uD83D\uDD10",
        searchKeywords = {"base64", "encode", "decode", "b64"},
        order = 10
)
public class Base64Tool implements IGuiTool {

    private Node view;

    @Override
    public Node getView() {
        if (view == null) view = build();
        return view;
    }

    private Node build() {
        TextArea input = ToolLayout.codeArea("Paste text or Base64 here…");
        TextArea output = ToolLayout.codeArea("Result appears here");
        output.setEditable(false);

        ChoiceBox<String> mode = new ChoiceBox<>();
        mode.getItems().addAll("Encode", "Decode");
        mode.getSelectionModel().selectFirst();

        Label status = new Label();
        status.getStyleClass().add("text-muted");

        Runnable run = () -> {
            String src = input.getText() == null ? "" : input.getText();
            try {
                if ("Encode".equals(mode.getValue())) {
                    output.setText(Base64.getEncoder().encodeToString(src.getBytes(StandardCharsets.UTF_8)));
                    status.setText("Encoded " + src.length() + " chars.");
                } else {
                    byte[] decoded = Base64.getDecoder().decode(src.trim());
                    output.setText(new String(decoded, StandardCharsets.UTF_8));
                    status.setText("Decoded " + decoded.length + " bytes.");
                }
            } catch (IllegalArgumentException ex) {
                output.setText("");
                status.setText("Invalid Base64 input: " + ex.getMessage());
            }
        };

        // live update on input / mode change
        ChangeListener<Object> live = (obs, o, n) -> run.run();
        input.textProperty().addListener(live);
        mode.valueProperty().addListener(live);

        Button copyBtn = new Button("Copy output");
        copyBtn.setOnAction(e -> {
            javafx.scene.input.ClipboardContent c = new javafx.scene.input.ClipboardContent();
            c.putString(output.getText());
            javafx.scene.input.Clipboard.getSystemClipboard().setContent(c);
            status.setText("Output copied to clipboard.");
        });

        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> { input.clear(); output.clear(); status.setText(""); });

        HBox topBar = new HBox(12, new Label("Mode:"), mode, copyBtn, clearBtn);
        topBar.setSpacing(8);

        VBox page = ToolLayout.page();
        page.getChildren().addAll(
                ToolLayout.header("Base64 Encoder / Decoder",
                        "Encode text to Base64 or decode Base64 back to text."),
                topBar,
                ToolLayout.section("Input", input),
                ToolLayout.section("Output", output),
                status
        );
        VBox.setVgrow(input, Priority.ALWAYS);
        VBox.setVgrow(output, Priority.ALWAYS);
        return page;
    }
}
