package eu.mikroskeem.worldeditcui.mixins;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.FixedColorVertexConsumer;
import net.minecraft.client.render.VertexFormatElement;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BufferBuilder.class)
public abstract class BufferBuilderMixin extends FixedColorVertexConsumer {
    @Shadow @Nullable private VertexFormatElement currentElement;

    // workaround for buffer builder not properly resetting its fixed color state after finishing a pass
    // technically a mojang bug I guess
    @Redirect(method = "end", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/BufferBuilder;currentElement:Lnet/minecraft/client/render/VertexFormatElement;", opcode = Opcodes.PUTFIELD))
    private void wecui$clearColorFixed(final BufferBuilder self, final VertexFormatElement element) {
        this.currentElement = null;
        this.colorFixed = false;
    }
}
