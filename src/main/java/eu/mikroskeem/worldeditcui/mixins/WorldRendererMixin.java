package eu.mikroskeem.worldeditcui.mixins;

import eu.mikroskeem.worldeditcui.FabricModWorldEditCUI;
import eu.mikroskeem.worldeditcui.RenderMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

/**
 * @author Mark Vainomaa
 */
@Mixin(value = WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow @Nullable private Framebuffer translucentFramebuffer;

    @Inject(method = "render", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/gl/ShaderEffect;render(F)V"
    ), slice = @Slice(from = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/render/WorldRenderer;renderWorldBorder(Lnet/minecraft/client/render/Camera;)V")))
    private void afterRenderEntitiesFabulous(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline,
                                            Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager,
                                            Matrix4f matrix4f, CallbackInfo ci) {
        // We are within a fabulous graphics check block
        if (this.translucentFramebuffer != null && FabricModWorldEditCUI.getRenderMode() != RenderMode.FREX_POST_RENDER) {
            this.translucentFramebuffer.beginWrite(false); // Same logic as RenderPhase.TRANSLUCENT_TARGET, but that's protected
            try {
                FabricModWorldEditCUI.getInstance().onPostRenderEntities(tickDelta);
            } finally {
                MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
            }
        }
    }

    @Inject(method = "render", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/WorldRenderer;renderChunkDebugInfo(Lnet/minecraft/client/render/Camera;)V",
            shift = At.Shift.BEFORE
    ))
    private void beforeChunkDebugInfo(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline,
                                     Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager,
                                     Matrix4f matrix4f, CallbackInfo ci) {
        if (this.translucentFramebuffer == null && FabricModWorldEditCUI.getRenderMode() != RenderMode.FREX_POST_RENDER) {
            FabricModWorldEditCUI.getInstance().onPostRenderEntities(tickDelta);
        }
    }
}
