package net.tslat.tes.mixin.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.tslat.tes.core.hud.TESHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/IProfiler;pop()V"))
	private void pickTES(float partialTick, CallbackInfo callback) {
		TESHud.pickNewEntity(partialTick);
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/IngameGui;render(Lcom/mojang/blaze3d/matrix/MatrixStack;F)V", shift = At.Shift.AFTER))
	private void renderTESHud(float partialTick, long nanoTime, boolean renderLevel, CallbackInfo callback) {
		TESHud.renderForHud(new MatrixStack(), Minecraft.getInstance(), partialTick);
	}
}
