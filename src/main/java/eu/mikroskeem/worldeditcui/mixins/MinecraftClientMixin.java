package eu.mikroskeem.worldeditcui.mixins;

import eu.mikroskeem.worldeditcui.FabricModWorldEditCUI;
import eu.mikroskeem.worldeditcui.interfaces.IMinecraftClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Mark Vainomaa
 */
@Mixin(value = MinecraftClient.class)
public abstract class MinecraftClientMixin implements IMinecraftClient {
    @Shadow @Final private RenderTickCounter renderTickCounter;

    @Inject(method = "init", at = @At(value = "TAIL"))
    private void onInitDone(CallbackInfo ci) {
        FabricModWorldEditCUI.getInstance().onGameInitDone((MinecraftClient) (Object) this);
    }

    @Override
    public RenderTickCounter getRenderTickCounter() {
        return this.renderTickCounter;
    }
}