package io.devtoys.api;

import java.util.Objects;

public class ServiceContext {
    private final ClipboardService clipboard;
    private final SettingsStore settings;

    public ServiceContext(ClipboardService clipboard, SettingsStore settings) {
        this.clipboard = Objects.requireNonNull(clipboard, "clipboard");
        this.settings = Objects.requireNonNull(settings, "settings");
    }

    public ClipboardService clipboard() {
        return clipboard;
    }

    public SettingsStore settings() {
        return settings;
    }
}
