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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainView extends BorderPane {
    private final ToolRegistry registry;
    private final TreeView<Object> tree;
    private final StackPane content;
    private final Label welcome;

    public MainView(ToolRegistry registry, Runnable onToggleTheme, boolean isDarkInitially) {
        this.registry = registry;

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
        welcome = new Label("请选择一个工具");
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
                setText(tool.metadata().iconGlyph());
                setGraphic(null);
                setStyle("");
            }
        }
    }

}
