package eu.mikroskeem.worldeditcui.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author Mark Vainomaa
 */
@Mixin(value = MinecraftClient.class)
public interface MinecraftClientAccess {
    @Accessor
    RenderTickCounter getRenderTickCounter();
}
