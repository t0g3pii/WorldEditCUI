package eu.mikroskeem.worldeditcui.render;

import net.coderbot.iris.Iris;
import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.layer.GbufferPrograms;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.CoreWorldRenderingPipeline;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

public final class IrisPipelineProvider implements PipelineProvider {
    static class IrisTypeFactory implements BufferBuilderRenderSink.TypeFactory {
        public static final IrisTypeFactory INSTANCE = new IrisTypeFactory();

        private static final BufferBuilderRenderSink.RenderType QUADS = new BufferBuilderRenderSink.RenderType(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR, IrisPipelineProvider::lineShader);
        private static final BufferBuilderRenderSink.RenderType LINES = new BufferBuilderRenderSink.RenderType(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR, IrisPipelineProvider::lineShader);
        private static final BufferBuilderRenderSink.RenderType LINES_LOOP = new BufferBuilderRenderSink.RenderType(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR, IrisPipelineProvider::lineShader);

        private IrisTypeFactory() {
        }

        @Override
        public BufferBuilderRenderSink.RenderType quads() {
            return QUADS;
        }

        @Override
        public BufferBuilderRenderSink.RenderType lines() {
            return LINES;
        }

        @Override
        public BufferBuilderRenderSink.RenderType linesLoop() {
            return LINES_LOOP;
        }
    }

    @Override
    public String id() {
        return "iris";
    }

    @Override
    public boolean available() {
        return FabricLoader.getInstance().isModLoaded("iris");
    }

    @Override
    public RenderSink provide() {
        return new BufferBuilderRenderSink(
                IrisTypeFactory.INSTANCE,
                () -> GbufferPrograms.push(GbufferProgram.BASIC),
                () -> GbufferPrograms.pop(GbufferProgram.BASIC)
        );
    }

    private static Shader lineShader() {
        final WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipeline();
        if (pipeline instanceof CoreWorldRenderingPipeline core) {
            return core.getLeash();
        } else {
            return GameRenderer.getPositionColorShader();
        }
    }
}
