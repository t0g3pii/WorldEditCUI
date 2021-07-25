package eu.mikroskeem.worldeditcui;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Shader;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class NoOpRenderWrapper implements RenderWrapper {

    public static final Supplier<Shader> VANILLA_SHADER = GameRenderer::getPositionColorShader;

    @Override
    public String id() {
        return "vanilla";
    }

    @Override
    public boolean available() {
        return true;
    }

    @Override
    public boolean render(WorldRenderContext ctx, BiConsumer<WorldRenderContext, Supplier<Shader>> renderer) {
        renderer.accept(ctx, VANILLA_SHADER);
        return true;
    }
}
