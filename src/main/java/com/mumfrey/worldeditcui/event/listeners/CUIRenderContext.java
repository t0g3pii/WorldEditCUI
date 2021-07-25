package com.mumfrey.worldeditcui.event.listeners;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mumfrey.worldeditcui.util.Vector3;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * State related to CUI rendering.
 */
public final class CUIRenderContext {
    private Vector3 cameraPos;
    private MatrixStack matrices = RenderSystem.getModelViewStack();
    private float dt;
    private Supplier<Shader> shader;

    public Vector3 cameraPos() {
        return this.cameraPos;
    }

    public MatrixStack matrices() {
        return RenderSystem.getModelViewStack();
    }

    public void applyMatrices() {
        RenderSystem.applyModelViewMatrix();
    }

    public float dt() {
        return this.dt;
    }

    public Supplier<Shader> shader() {
        return this.shader;
    }

    public void withCameraAt(final Vector3 pos, final Consumer<CUIRenderContext> action) {
        final Vector3 oldPos = this.cameraPos;
        this.cameraPos = pos;
        try {
            action.accept(this);
        } finally {
            this.cameraPos = oldPos;
        }
    }

    void init(final Vector3 cameraPos, final MatrixStack matrices, final float dt, final Supplier<Shader> shader) {
        this.cameraPos = cameraPos;
        this.matrices = matrices;
        this.dt = dt;
        this.shader = shader;
    }

    /**
     * Empty state. To be called at the end of a frame.
     */
    void reset() {
        this.cameraPos = null;
        this.matrices = null;
    }
}
