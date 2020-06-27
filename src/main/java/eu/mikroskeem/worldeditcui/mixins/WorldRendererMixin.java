package eu.mikroskeem.worldeditcui.mixins;

import com.google.gson.internal.$Gson$Preconditions;
import eu.mikroskeem.worldeditcui.FabricModWorldEditCUI;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Mark Vainomaa
 */
@Mixin(value = WorldRenderer.class)
public abstract class WorldRendererMixin {

    // Fabulous graphics // TODO doesn't really work

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderPhase$Target;endDrawing()V"),
            slice = @Slice( from = @At(value = "INVOKE:FIRST", target = "Lnet/minecraft/client/render/WorldRenderer;renderWorldBorder(Lnet/minecraft/client/render/Camera;)V")))
    private void afterRenderEntitiesFabulous(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline,
                                            Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager,
                                            Matrix4f matrix4f, CallbackInfo ci) {
        FabricModWorldEditCUI.getInstance().onPostRenderEntities(tickDelta);
    }

    // Standard graphics //

    @Inject(method = "render", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/WorldRenderer;renderWorldBorder(Lnet/minecraft/client/render/Camera;)V",
            shift = At.Shift.BEFORE,
            ordinal = 1
    ))
    private void afterRenderEntitiesRegular(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline,
                                     Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager,
                                     Matrix4f matrix4f, CallbackInfo ci) {
        FabricModWorldEditCUI.getInstance().onPostRenderEntities(tickDelta);
    }
}
