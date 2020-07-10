package eu.mikroskeem.worldeditcui;

import grondag.frex.api.event.WorldRenderEvent;

/**
 * The active render hook being used for this CUI instance
 */
public enum RenderMode {
    /**
     * Standard hook into WorldRenderer
     */
    STANDARD,
    /**
     * Always render on top. Not currently implemented
     */
    ALWAYS_ON_TOP,

    /**
     * Use the frex {@link WorldRenderEvent#AFTER_WORLD_RENDER} hook to render.
     *
     * <p>This is more compatible with rendering mods, but produces worse results on <em>Fabulous</em> graphics mode.</p>
     */
    FREX_POST_RENDER
}
