package io.devtoys.app;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import io.devtoys.api.ClipboardService;
import io.devtoys.api.ServiceContext;
import io.devtoys.api.SettingsStore;
import io.devtoys.core.ToolRegistry;
import io.devtoys.core.services.FileSettingsStore;
import io.devtoys.core.services.JavaFxClipboardService;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DevToysApp extends Application {

    private static final Logger LOG = Logger.getLogger(DevToysApp.class.getName());

    private static final String SETTING_DARK_MODE = "app.darkMode";

    private SettingsStore settings;

    @Override
    public void start(Stage stage) throws Exception {
        // Host service
        ClipboardService clipboard = new JavaFxClipboardService();
        settings = new FileSettingsStore();
        ServiceContext context = new ServiceContext(clipboard, settings);

        // Discover tools
        ToolRegistry registry = ToolRegistry.discover(context);
        LOG.log(Level.INFO, "加载了 {0} 个工具", registry.all().size());

        // Initial theme
        boolean dark = settings.getBoolean(SETTING_DARK_MODE, true);
        applyTheme(dark);

        // Main window
        MainView window = new MainView(registry, this::toggleTheme, dark);
        Scene scene = new Scene(window, 1100, 720);
        stage.setScene(scene);
        stage.setTitle("DevToys Java");
        stage.setMinWidth(800);
        stage.setMinHeight(500);
        stage.show();
    }

    private void applyTheme(boolean dark) {
        setUserAgentStylesheet(dark
                ? new PrimerDark().getUserAgentStylesheet()
                : new PrimerLight().getUserAgentStylesheet());
    }

    private void toggleTheme() {
        boolean nowDark = !settings.getBoolean(SETTING_DARK_MODE, true);
        settings.setBoolean(SETTING_DARK_MODE, nowDark);
        applyTheme(nowDark);
    }

    @Override
    public void stop() {
        if (settings instanceof FileSettingsStore fileSettingsStore) {
            fileSettingsStore.flush();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
