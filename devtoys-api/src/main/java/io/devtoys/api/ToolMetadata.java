package io.devtoys.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declarative metadata for a tool.
 *
 * <p>This single annotation is the Java equivalent of the stack of C# attributes
 * DevToys uses ({@code [Export]}, {@code [Name]}, {@code [ToolDisplayInformation]},
 * {@code [Order]}, {@code [NotSearchable]}, {@code [NotFavorable]}, etc.).
 * Splitting them into multiple annotations is possible but Java's annotation
 * ergonomics make a single flat annotation more practical in the common case.
 *
 * <p>Retained at runtime so that the {@code ToolRegistry} can read it reflectively
 * during {@link java.util.ServiceLoader} discovery.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ToolMetadata {

    /**
     * Stable, unique identifier for the tool. By convention this matches the
     * fully-qualified class name for built-in tools, but plugins can use any
     * unique string.
     */
    String name();

    /**
     * Group this tool belongs to in the navigation pane. Use constants from
     * {@link PredefinedToolGroups} for built-in groups, or any string for a
     * custom group.
     */
    String groupName();

    /** Short label shown in the navigation list (e.g. "Base64"). */
    String shortTitle();

    /** Longer title for the tool page header. Defaults to {@link #shortTitle()} if empty. */
    String longTitle() default "";

    /** Short description shown in tooltips and the tool page. */
    String description() default "";

    /**
     * Unicode glyph used as the tool's icon. Typically from a Fluent / Material
     * icon font that the app loads. For the skeleton we use plain Unicode
     * symbols; a later iteration can switch to Ikonli for real icon fonts.
     */
    String iconGlyph() default "\u2699"; // gear

    /** Search keywords. Used by the global search feature. */
    String[] searchKeywords() default {};

    /**
     * Display order within the group. Lower values appear first. Ties are
     * broken by {@link #shortTitle()}.
     */
    int order() default 0;

    /** When true, the tool is excluded from search results. */
    boolean notSearchable() default false;

    /** When true, the tool cannot be added to favorites. */
    boolean notFavorable() default false;
}