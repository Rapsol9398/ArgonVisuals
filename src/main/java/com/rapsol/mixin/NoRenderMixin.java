package com.rapsol.mixin;

import com.rapsol.module.modules.render.NoRender;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.RenderShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public class NoRenderMixin {

    @Inject(method = "getRenderShape", at = @At("RETURN"), cancellable = true)
    private void onGetRenderShape(CallbackInfoReturnable<RenderShape> cir) {
        if (!NoRender.isActive() || !NoRender.instance.grass.getValue()) return;
        BlockState self = (BlockState)(Object)this;
        if (self.is(Blocks.SHORT_GRASS)
                || self.is(Blocks.TALL_GRASS)
                || self.is(Blocks.BUSH)
                || self.is(Blocks.DEAD_BUSH)
                || self.is(Blocks.SHORT_DRY_GRASS)
                || self.is(Blocks.TALL_DRY_GRASS)
                || self.is(Blocks.FERN)
                || self.is(Blocks.LARGE_FERN)) {
            cir.setReturnValue(RenderShape.INVISIBLE);
        }
    }
}