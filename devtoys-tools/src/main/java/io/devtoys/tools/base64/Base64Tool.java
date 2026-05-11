package io.devtoys.tools.base64;

import io.devtoys.api.IGuiTool;
import io.devtoys.api.PredefinedToolGroups;
import io.devtoys.api.ToolMetadata;
import io.devtoys.core.tasks.BackgroundTaskRunner.DebouncedTaskRunner;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ToolMetadata(
        name = "io.devtoys.tools.base64.Base64Tool",
        groupName = PredefinedToolGroups.ENCODERS_DECODERS,
        shortTitle = "Base64",
        longTitle = "Base64 Encoder / Decoder",
        description = "Encode and decode Base64 strings.",
        iconCode = "mdal-lock",
        searchKeywords = {"base64", "encode", "decode", "b64"},
        order = 10
)
public class Base64Tool implements IGuiTool {

    private static final Logger LOG = LoggerFactory.getLogger(Base64Tool.class);

    private Node view;
    private final DebouncedTaskRunner<Result> runner = new DebouncedTaskRunner<>();

    @Override
    public Node getView() {
        if (view == null) view = build();
        return view;
    }

    private Node build() {
        TextArea input = ToolLayout.codeArea("粘贴文本或Base64……");
        TextArea output = ToolLayout.codeArea("结果");
        output.setEditable(false);

        ChoiceBox<String> mode = new ChoiceBox<>();
        mode.getItems().addAll("编码", "解码");
        mode.getSelectionModel().selectFirst();

        Label status = new Label();
        status.getStyleClass().add("text-muted");

        Runnable compute = () -> {
            String src = input.getText() == null ? "" : input.getText();
            boolean encode = "编码".equals(mode.getValue());
            runner.run(
                    () -> doOperation(src, encode),
                    r -> {
                        output.setText(r.text);
                        status.setText(r.status);
                    },
                    err -> {
                        output.setText("");
                        status.setText("错误：" + err.getMessage());
                        LOG.debug("Base64 工具计算失败", err);
                    }
            );
        };

        // live update on input / mode change
        ChangeListener<Object> live = (obs, o, n) -> compute.run();
        input.textProperty().addListener(live);
        mode.valueProperty().addListener(live);

        Button copyBtn = new Button("复制输出");
        copyBtn.setOnAction(e -> {
            javafx.scene.input.ClipboardContent c = new javafx.scene.input.ClipboardContent();
            c.putString(output.getText());
            javafx.scene.input.Clipboard.getSystemClipboard().setContent(c);
            status.setText("输出已复制至剪贴板。");
        });

        Button clearBtn = new Button("清空");
        clearBtn.setOnAction(e -> {
            input.clear();
            output.clear();
            status.setText("");
        });

        HBox topBar = new HBox(12, new Label("模式:"), mode, copyBtn, clearBtn);
        topBar.setSpacing(8);

        VBox page = ToolLayout.page();
        page.getChildren().addAll(
                ToolLayout.header("Base64 编码 / 解码",
                        "将文本编码为 Base64 或将 Base64 解码回文本。"),
                topBar,
                ToolLayout.section("输入", input),
                ToolLayout.section("输出", output),
                status
        );
        VBox.setVgrow(input, Priority.ALWAYS);
        VBox.setVgrow(output, Priority.ALWAYS);
        return page;
    }

    private static Result doOperation(String src, boolean encode) {
        if (encode) {
            String encoded = Base64Core.encode(src);
            return new Result(encoded,
                    "Encoded " + src.length() + " chars → " + encoded.length() + " chars.");
        } else {
            String decoded = Base64Core.decode(src);
            int bytes = decoded.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
            return new Result(decoded, "Decoded " + bytes + " bytes.");
        }
    }

    private record Result(String text, String status) {
    }
}
