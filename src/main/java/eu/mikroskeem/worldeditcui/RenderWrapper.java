package eu.mikroskeem.worldeditcui;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.Shader;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * A wrapper around the WECUI pipeline, to allow manipulating the state used by various shader mods.
 */
public interface RenderWrapper {
    String id();
    boolean available();

    /**
     * Perform a wrapped render.
     *
     * @param ctx render context
     * @param renderer callback of wrapped rendering
     * @return {@code false} iff the render failed and a fallback method should be attempted
     */
    boolean render(final WorldRenderContext ctx, final BiConsumer<WorldRenderContext, Supplier<Shader>> renderer);

}
