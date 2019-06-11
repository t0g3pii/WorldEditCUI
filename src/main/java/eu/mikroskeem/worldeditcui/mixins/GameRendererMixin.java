package eu.mikroskeem.worldeditcui.mixins;

import eu.mikroskeem.worldeditcui.FabricModWorldEditCUI;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Mark Vainomaa
 */
@Mixin(value = GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "renderCenter", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/WorldRenderer;renderEntities(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/VisibleRegion;F)V",
            shift = At.Shift.AFTER
    ))
    private void afterRenderEntities(float partialTicks, long timeSlice, CallbackInfo ci) {
        FabricModWorldEditCUI.getInstance().onPostRenderEntities(partialTicks);
    }
}