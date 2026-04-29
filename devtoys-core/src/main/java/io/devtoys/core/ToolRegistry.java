package io.devtoys.core;

import io.devtoys.api.IGuiTool;
import io.devtoys.api.ServiceContext;
import io.devtoys.api.ToolMetadata;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Discovers and holds all available tools.
 *
 * <p>Uses the standard {@link ServiceLoader} mechanism: every module that
 * wants to contribute tools declares
 * {@code provides io.devtoys.api.IGuiTool with ...} in its {@code module-info.java}.
 * This replaces DevToys' MEF {@code [Export(typeof(IGuiTool))]} attributes.
 *
 * <p>During discovery the registry invokes {@link IGuiTool#initialize(ServiceContext)}
 * on each loaded tool, giving it access to host services.
 *
 * <p>The registry is sorted once at construction by (group, order, title) and
 * treated as immutable afterward.
 */
public final class ToolRegistry {

    private static final Logger LOG = Logger.getLogger(ToolRegistry.class.getName());

    private final List<ToolDescriptor> tools;

    public ToolRegistry(List<ToolDescriptor> tools) {
        this.tools = List.copyOf(tools);
    }

    /**
     * Discovers every {@link IGuiTool} on the module path and initializes it
     * with the supplied {@link ServiceContext}.
     *
     * <p>Implementations without a {@link ToolMetadata} annotation are skipped
     * with a warning — they cannot be displayed since there is no title or
     * group information. Tools whose {@code initialize} throws are logged and
     * dropped so that one bad plugin cannot take down the whole app.
     */
    public static ToolRegistry discover(ServiceContext context) {
        List<ToolDescriptor> found = new ArrayList<>();

        for (IGuiTool tool : ServiceLoader.load(IGuiTool.class)) {
            ToolMetadata meta = tool.getClass().getAnnotation(ToolMetadata.class);
            if (meta == null) {
                LOG.log(Level.WARNING,
                        "Tool {0} is registered as a service but missing @ToolMetadata; skipping.",
                        tool.getClass().getName());
                continue;
            }
            try {
                tool.initialize(context);
            } catch (RuntimeException e) {
                LOG.log(Level.WARNING,
                        "Tool " + tool.getClass().getName() + " failed to initialize; skipping.",
                        e);
                continue;
            }
            found.add(new ToolDescriptor(tool, meta, tool.getClass()));
        }

        LOG.log(Level.INFO, "Discovered {0} tool(s).", found.size());
        return new ToolRegistry(found);
    }

    public List<ToolDescriptor> all() {
        return tools;
    }

    public Map<String, List<ToolDescriptor>> byGroup() {
        Map<String, List<ToolDescriptor>> result = new LinkedHashMap<>();
        for (ToolDescriptor tool : tools) {
            result.computeIfAbsent(tool.group(), key -> new ArrayList<>())
                    .add(tool);
        }
        result.replaceAll((group, tools) -> Collections.unmodifiableList(tools));
        return Collections.unmodifiableMap(result);
    }

    public List<ToolDescriptor> search(String query) {
        if (query == null || query.isBlank()) {
            return tools;
        }
        return tools.stream()
                .filter(tool -> tool.matches(query))
                .toList();
    }
}
