package eu.mikroskeem.worldeditcui.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mumfrey.worldeditcui.render.LineStyle;
import com.mumfrey.worldeditcui.render.RenderStyle;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL32;

import java.util.Objects;
import java.util.function.Supplier;

public class BufferBuilderRenderSink implements RenderSink {

    private final RenderType lines;
    private final RenderType lineLoop;
    private final RenderType quads;
    private final Runnable preFlush;
    private final Runnable postFlush;
    private BufferBuilder builder;
    private @Nullable BufferBuilderRenderSink.RenderType activeRenderType;
    private boolean active;
    private boolean canFlush;
    private float r = -1f, g, b, a;
    private double loopX, loopY, loopZ; // track previous vertices for lines_loop
    private double loopFirstX, loopFirstY, loopFirstZ; // track initial vertices for lines_loop
    private boolean canLoop;

    // line state
    private float lastLineWidth = -1;
    private int lastDepthFunc = -1;

    public BufferBuilderRenderSink(final TypeFactory types) {
        this(types, () -> {}, () -> {});
    }

    public BufferBuilderRenderSink(final TypeFactory types, final Runnable preFlush, final Runnable postFlush) {
        this.lines = types.lines();
        this.lineLoop = types.linesLoop();
        this.quads = types.quads();
        this.preFlush = preFlush;
        this.postFlush = postFlush;
    }

    static class LineWidth {
        private static final boolean HAS_COMPATIBILITY = (GL11.glGetInteger(GL32.GL_CONTEXT_PROFILE_MASK) & GL32.GL_CONTEXT_COMPATIBILITY_PROFILE_BIT) != 0;
        private static float lineWidth = GL11.glGetInteger(GL11.GL_LINE_WIDTH);

        static void set(final float width) {
            if (HAS_COMPATIBILITY) {
                if (lineWidth != width) {
                    GL11.glLineWidth(width);
                    lineWidth = width;
                }
            }
            RenderSystem.lineWidth(width);
        }

    }

    @Override
    public RenderSink color(float r, float g, float b, float alpha) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = alpha;
        return this;
    }

    @Override
    public boolean apply(LineStyle line, RenderStyle.RenderType type) {
        if (line.renderType.matches(type))
        {
            if (line.lineWidth != this.lastLineWidth || line.renderType.depthFunc() != this.lastDepthFunc) {
                this.flush();
                if (this.active && this.activeRenderType != null) {
                    this.canFlush = true;
                    this.builder = Tessellator.getInstance().getBuffer();
                    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                    this.builder.begin(this.activeRenderType.mode, VertexFormats.POSITION_COLOR);
                }
                LineWidth.set(this.lastLineWidth = line.lineWidth);
                RenderSystem.depthFunc(this.lastDepthFunc = line.renderType.depthFunc());
            }
            return true;
        }

        return false;
    }

    @Override
    public RenderSink vertex(double x, double y, double z) {
        if (r == -1f) {
            throw new IllegalStateException("No colour has been set!");
        }
        if (!this.active) {
            throw new IllegalStateException("Tried to draw when not active");
        }

        final BufferBuilder builder = this.builder;
        if (this.activeRenderType == this.lineLoop) {
            // duplicate last
            if (this.canLoop) {
                final Vec3f normal = this.activeRenderType.hasNormals ? this.computeNormal(this.loopX, this.loopY, this.loopZ, x, y, z) : null;
                builder.vertex(this.loopX, this.loopY, this.loopZ).color(this.r, this.g, this.b, this.a);
                if (normal != null) {
                    // we need to compute normals pointing directly towards the screen
                    builder.normal(normal.getX(), normal.getY(), normal.getZ());
                }
                builder.next();
                builder.vertex(x, y, z).color(this.r, this.g, this.b, this.a);
                if (normal != null) {
                    builder.normal(normal.getX(), normal.getY(), normal.getZ());
                }
                builder.next();
            } else {
                this.loopFirstX = x;
                this.loopFirstY = y;
                this.loopFirstZ = z;
            }
            this.loopX = x;
            this.loopY = y;
            this.loopZ = z;
            this.canLoop = true;
        } else if (this.activeRenderType == this.lines) {
            // we buffer vertices so we can compute normals here
            if (this.canLoop) {
                final Vec3f normal = this.activeRenderType.hasNormals ? this.computeNormal(this.loopX, this.loopY, this.loopZ, x, y, z) : null;
                builder.vertex(this.loopX, this.loopY, this.loopZ).color(this.r, this.g, this.b, this.a);
                if (normal != null) {
                    builder.normal(normal.getX(), normal.getY(), normal.getZ());
                }
                builder.next();
                builder.vertex(x, y, z).color(this.r, this.g, this.b, this.a);
                if (normal != null) {
                    builder.normal(normal.getX(), normal.getY(), normal.getZ());
                }
                builder.next();
                this.canLoop = false;
            } else {
                this.loopX = x;
                this.loopY = y;
                this.loopZ = z;
                this.canLoop = true;
            }
        } else {
            builder.vertex(x, y, z).color(this.r, this.g, this.b, this.a).next();
        }
        return this;
    }

    private Vec3f computeNormal(final double x0, final double y0, final double z0, final double x1, final double y1, final double z1) {
        // we need to compute normals so all drawn planes appear perpendicular to the screen
        final double dX = (x1 - x0);
        final double dY = (y1 - y0);
        final double dZ = (z1 - z0);
        final double length = Math.sqrt(dX * dX + dY * dY + dZ * dZ);
        final Vec3f normal = new Vec3f((float) (dX / length), (float) (dY / length), (float) (dZ / length));
        // normal.cross(MinecraftClient.getInstance().gameRenderer.getCamera().getRotation().method_35820() /* toXYZ */);
        normal.transform(RenderSystem.getModelViewStack().peek().getNormal());
        // return new Vec3f(1, 1, 1);
        return normal;
    }

    @Override
    public RenderSink beginLineLoop() {
        this.transitionState(this.lineLoop);
        return this;
    }

    @Override
    public RenderSink endLineLoop() {
        this.end(this.lineLoop);
        if (this.canLoop) {
            this.canLoop = false;
            final Vec3f normal = this.activeRenderType.hasNormals ? this.computeNormal(this.loopX, this.loopY, this.loopZ, this.loopFirstX, this.loopFirstY, this.loopFirstZ) : null;
            this.builder.vertex(this.loopX, this.loopY, this.loopZ).color(this.r, this.g, this.b, this.a);
            if (normal != null) {
                this.builder.normal(normal.getX(), normal.getY(), normal.getZ());
            }
            this.builder.next();

            this.builder.vertex(this.loopFirstX, this.loopFirstY, this.loopFirstZ).color(this.r, this.g, this.b, this.a);
            if (normal != null) {
                this.builder.normal(normal.getX(), normal.getY(), normal.getZ());
            }
            this.builder.next();
        }
        return this;
    }

    @Override
    public RenderSink beginLines() {
        this.transitionState(this.lines);
        return this;
    }

    @Override
    public RenderSink endLines() {
        this.end(this.lines);
        return this;
    }

    @Override
    public RenderSink beginQuads() {
        this.transitionState(this.quads);
        return this;
    }

    @Override
    public RenderSink endQuads() {
        this.end(this.quads);
        return this;
    }

    @Override
    public void flush() {
        if (!this.canFlush) {
            return;
        }
        if (this.active) {
            throw new IllegalStateException("Tried to flush while still active");
        }
        this.canFlush = false;
        this.preFlush.run();
        try {
            if (this.activeRenderType != null) {
                RenderSystem.setShader(this.activeRenderType.shader);
            }
            Tessellator.getInstance().draw();
        } finally {
            this.postFlush.run();
            this.builder = null;
            this.activeRenderType = null;
        }
    }

    private void end(final RenderType renderType) {
        if (!this.active) {
            throw new IllegalStateException("Could not exit " + renderType + ", was not active");
        }
        if (this.activeRenderType != renderType) {
            throw new IllegalStateException("Expected to end state " + renderType + " but was in " + this.activeRenderType);
        }
        this.active = false;
    }

    private void transitionState(final RenderType renderType) {
        if (this.active) {
            throw new IllegalStateException("Tried to enter new state before previous operation had been completed");
        }
        if (this.activeRenderType != null && renderType.mustFlushAfter(this.activeRenderType)) {
            this.flush();
        }
        if (this.activeRenderType == null || this.activeRenderType.mode != renderType.mode) {
            this.canFlush = true;
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            this.builder = Tessellator.getInstance().getBuffer();
            this.builder.begin(renderType.mode, renderType.format);
        }
        this.activeRenderType = renderType;
        this.active = true;
    }

    public static class RenderType {

        private final VertexFormat.DrawMode mode;
        private final VertexFormat format;
        private final boolean hasNormals;
        private final Supplier<Shader> shader;

        public RenderType(final VertexFormat.DrawMode mode, final VertexFormat format, final Supplier<Shader> shader) {
            this.mode = mode;
            this.format = format;
            this.hasNormals = format.getShaderAttributes().contains("Normal");
            this.shader = shader;
        }

        VertexFormat.DrawMode mode() {
            return this.mode;
        }

        VertexFormat format() {
            return this.format;
        }

        boolean hasNormals() {
            return this.hasNormals;
        }

        Supplier<Shader> shader() {
            return this.shader;
        }

        boolean mustFlushAfter(final RenderType previous) {
            return previous.mode != this.mode || !Objects.equals(previous.format, this.format);
        }
    }

    public interface TypeFactory {
        RenderType quads();
        RenderType lines();
        RenderType linesLoop();
    }
}
