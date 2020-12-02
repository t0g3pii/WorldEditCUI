package eu.mikroskeem.worldeditcui.mixins;

import net.minecraft.client.render.RenderPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderPhase.class)
public interface RenderPhaseAccess {
    @Accessor("TRANSLUCENT_TARGET")
    static RenderPhase.Target getTranslucentTarget() {
        throw new AssertionError();
    }
}
