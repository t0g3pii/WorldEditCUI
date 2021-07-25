package eu.mikroskeem.worldeditcui;

import net.coderbot.iris.Iris;
import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.layer.GbufferPrograms;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.CoreWorldRenderingPipeline;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.Shader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class IrisRenderWrapper implements RenderWrapper {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public String id() {
        return "iris";
    }

    @Override
    public boolean available() {
        return FabricLoader.getInstance().isModLoaded("iris");
    }

    @Override
    public boolean render(WorldRenderContext ctx, BiConsumer<WorldRenderContext, Supplier<Shader>> renderer) {
        try {
            final WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipeline();
            GbufferPrograms.push(GbufferProgram.BASIC);
            try {
                if (pipeline instanceof CoreWorldRenderingPipeline core) {
                    renderer.accept(ctx, core::getLeash);
                } else {
                    renderer.accept(ctx, NoOpRenderWrapper.VANILLA_SHADER);
                }
            } finally {
                GbufferPrograms.pop(GbufferProgram.BASIC);
            }

            return true;
        } catch (final Throwable ex) {
            LOGGER.error("Failed to render WECUI using Iris wrapper", ex);
            return false;
        }

    }
}
