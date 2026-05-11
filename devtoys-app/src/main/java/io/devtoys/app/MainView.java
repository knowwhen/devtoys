package io.devtoys.app;

import io.devtoys.core.ToolDescriptor;
import io.devtoys.core.ToolRegistry;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The primary application shell.
 *
 * <p>Layout, from top to bottom:
 * <pre>
 *   +-------------------------------------------------------+
 *   |  [🔍 Search...]                             [🌓 theme] |  &lt;-- top bar
 *   +-------------------------------------------------------+
 *   |         |                                             |
 *   |  Tools  |             Active tool view                |
 *   | (tree)  |                                             |
 *   |         |                                             |
 *   +-------------------------------------------------------+
 * </pre>
 *
 * <p>Icons use Ikonli's {@link FontIcon#setIconLiteral(String)}, which throws
 * {@link IllegalArgumentException} for unknown codes. Every use here is wrapped
 * in a try/catch that falls back to "no icon" rather than trying another code —
 * so a bad icon code logs a warning but never crashes the app.
 */
public class MainView extends BorderPane {

    private static final Logger LOG = LoggerFactory.getLogger(MainView.class);

    private final TreeView<Object> tree;
    private final StackPane content;

    public MainView(ToolRegistry registry, Runnable onToggleTheme, boolean isDarkInitially) {

        // 顶部
        TextField search = new TextField();
        search.setPromptText("搜索");

        HBox.setHgrow(search, Priority.ALWAYS);

        Button themeBtn = new Button(isDarkInitially ? "☀ 亮" : "🌙 暗");
        themeBtn.setOnAction(event -> {
            onToggleTheme.run();

            boolean nowLight = themeBtn.getText().contains("亮");
            themeBtn.setText(nowLight ? "🌙 暗" : "☀ 亮");
        });

        HBox topBar = new HBox(8, search, themeBtn);
        topBar.setPadding(new Insets(10, 12, 10, 12));
        topBar.setAlignment(Pos.CENTER);
        topBar.getStyleClass().add("tool-bar");

        // 左侧导航
        tree = new TreeView<>();
        tree.setShowRoot(false);
        tree.setCellFactory(treeView -> new NavCell());
        rebuildTree(registry.byGroup());

        VBox left = new VBox(tree);
        VBox.setVgrow(left, Priority.ALWAYS);
        left.setPrefWidth(220);
        left.setMinWidth(180);

        // 内容
        Label welcome = new Label("请选择一个工具");
        welcome.getStyleClass().add("text-muted");
        content = new StackPane();
        content.setPadding(new Insets(0));

        SplitPane split = new SplitPane(left, content);
        split.setDividerPositions(0.22);
        SplitPane.setResizableWithParent(left, false);

        setTop(topBar);
        setCenter(split);

        // tree selection -> switch content
        tree.getSelectionModel().selectedItemProperty().addListener((obs, old, n) -> {
            if(n != null && n.getValue() instanceof ToolDescriptor descriptor) {
                showTool(descriptor);
            }
        });

        // search -> rebuild tree
        ChangeListener<String> searchListener = (o, a, q) -> {
            if(q == null || q.isBlank()) {
                rebuildTree(registry.byGroup());
            } else {
                List<ToolDescriptor> matches = registry.search(q);
                rebuildTree(groupOf(matches));
            }
        };
        search.textProperty().addListener(searchListener);

        Platform.runLater(() -> {
            if(!registry.all().isEmpty()) {
                selectFirstTool();
            }
        });
    }

    /**
     * Create a FontIcon for the given literal. Returns {@code null} (no icon)
     * if the code doesn't resolve, instead of throwing. Callers decide whether
     * to substitute text or just render nothing.
     */
    private static FontIcon safeIcon(String literal, int size) {
        if (literal == null || literal.isBlank()) return null;
        try {
            FontIcon ic = new FontIcon();
            ic.setIconLiteral(literal);
            ic.setIconSize(size);
            return ic;
        } catch (Exception e) {
            LOG.warn("Unknown icon code '{}'; rendering without icon.", literal);
            return null;
        }
    }

    private Map<String, List<ToolDescriptor>> groupOf(List<ToolDescriptor> descriptors) {
        LinkedHashMap<String, List<ToolDescriptor>> result = new LinkedHashMap<>();
        for (ToolDescriptor descriptor : descriptors) {
            result.computeIfAbsent(descriptor.group(), key -> new ArrayList<>()).add(descriptor);
        }
        return result;
    }

    private void rebuildTree(Map<String, List<ToolDescriptor>> group) {
        TreeItem<Object> root = new TreeItem<>("root");
        for (Map.Entry<String, List<ToolDescriptor>> e : group.entrySet()) {
            TreeItem<Object> groupNode = new TreeItem<>(new Group(e.getKey()));
            groupNode.setExpanded(true);
            for (ToolDescriptor tool : e.getValue()) {
                groupNode.getChildren().add(new TreeItem<>(tool));
            }
            root.getChildren().add(groupNode);
        }
        tree.setRoot(root);
    }

    private void selectFirstTool() {
        TreeItem<Object> root = tree.getRoot();
        if(root == null) return;
        for (TreeItem<Object> group : root.getChildren()) {
            if(!group.getChildren().isEmpty()) {
                tree.getSelectionModel().select(group.getChildren().getFirst());
                return;
            }
        }
    }

    private void showTool(ToolDescriptor descriptor) {
        Node view;
        try {
            view = descriptor.tool().getView();
        } catch (RuntimeException e) {
            Label err = new Label("加载工具失败：" + e.getMessage());
            err.getStyleClass().add("text-danger");
            view = err;
        }
        content.getChildren().setAll(view);
    }

    private record Group(String name) {
        @Override
        public String toString() {
            return name;
        }
    }

    private static final class NavCell extends TreeCell<Object> {
        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            if(empty || item == null) {
                setText(null);
                setGraphic(null);
                setStyle("");
                return;
            }
            if(item instanceof Group g) {
                setText(g.name());
                setGraphic(null);
                setStyle("-fx-font-weight: bold;");
            }
            if(item instanceof ToolDescriptor tool) {
                setText("  " + tool.shortTitle());
                // safeIcon returns null on unknown code → no icon, no crash
                FontIcon ic = safeIcon(tool.metadata().iconCode(), 16);
                setGraphic(ic);
                setStyle("");
            }
        }
    }

}
