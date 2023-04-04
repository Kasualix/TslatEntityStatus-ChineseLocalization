package net.tslat.tes.api.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.tslat.tes.api.TESAPI;

/**
 * Various helper methods for client-side functions
 */
public final class TESClientUtil {
	public static final ResourceLocation CREATIVE_INVENTORY_TEXTURE = new ResourceLocation("textures/gui/container/creative_inventory/tab_inventory.png");

	/**
	 * Draw some text on screen at a given position, offset for the text's height and width
	 */
	public static void renderCenteredText(String text, MatrixStack poseStack, FontRenderer fontRenderer, float x, float y, int colour) {
		renderCenteredText(new StringTextComponent(text), poseStack, fontRenderer, x, y, colour);
	}

	/**
	 * Draw some text on screen at a given position, offset for the text's height and width
	 */
	public static void renderCenteredText(ITextComponent text, MatrixStack poseStack, FontRenderer fontRenderer, float x, float y, int colour) {
		drawText(poseStack, text, x - fontRenderer.width(text) / 2f, y + 4f, colour);
	}

	/**
	 * Translate the given {@link MatrixStack} to face the game camera
	 */
	public static void positionFacingCamera(MatrixStack poseStack) {
		poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
	}

	/**
	 * Prep the shader and bind a texture for the given ResourceLocation
	 */
	public static void prepRenderForTexture(ResourceLocation texture) {
		Minecraft.getInstance().textureManager.bind(texture);
	}

	/**
	 * Render a TES-style health bar at a given position for a given width
	 * @param poseStack The MatrixStack used for rendering
	 * @param x The x-position of the bar (use 0 and translate the {@link MatrixStack} for in-world rendering
	 * @param y The y-position of the bar (use 0 and translate the {@link MatrixStack} for in-world rendering
	 * @param width The width of the bar. This method reconstructs the bar accurately using the two ends and a stretched center to make an indefinitely-applicable width
	 * @param v The v coordinate of the bar texture (from 'textures/gui/bars.png')
	 * @param percentComplete Percentage progress of the bar (use 1 for background or overlay pieces)
	 * @param withBarOverlay Render the bar segments overlay
	 * @param opacity The overall opacity of the bar
	 */
	public static void constructBarRender(MatrixStack poseStack, int x, int y, int width, int v, float percentComplete, boolean withBarOverlay, float opacity) {
		int percentPixels = Math.round(percentComplete * width);
		int midBarWidth = width - 10;

		drawSimpleTexture(poseStack, x, y, Math.min(5, percentPixels), 5, 0, v, 256);

		if (percentPixels > 5) {
			if (midBarWidth > 0)
				drawSimpleTexture(poseStack, x + 5, y, Math.min(midBarWidth, percentPixels - 5), 5, 5, v, 256);

			if (percentPixels > width - 5)
				drawSimpleTexture(poseStack, x + 5 + midBarWidth, y, Math.min(5, percentPixels - 5), 5, 177, v, 256);
		}

		if (withBarOverlay && width > 10) {
			RenderSystem.color4f(1, 1, 1, 0.75f * opacity);
			drawSimpleTexture(poseStack, x, y, width, 5, 0, 80, 182, 5, 256, 256);
		}
	}

	/**
	 * Render a statically-positioned view of an {@link LivingEntity entity} instance, optionally including the frame TES usually renders with
	 */
	public static void renderEntityIcon(MatrixStack poseStack, Minecraft mc, float partialTick, LivingEntity entity, float opacity, boolean includeFrame) {
		float scale = 0.04f * (float)Math.pow(Math.min(30 / entity.getBbWidth(), 40 / entity.getBbHeight()), 0.95f);
		IRenderTypeBuffer.Impl bufferSource = mc.renderBuffers().bufferSource();

		poseStack.pushPose();


		if (includeFrame) {
			TESClientUtil.prepRenderForTexture(TESClientUtil.CREATIVE_INVENTORY_TEXTURE);
			RenderSystem.enableBlend();
			RenderSystem.color4f(1, 1, 1, 0.5f * opacity);
			TESClientUtil.drawSimpleTexture(poseStack, 2, 2, 34, 45, 72, 5, 256);

			poseStack.translate(20, 25, 0);
			poseStack.scale(-20, -20, 20);
		}

		poseStack.scale(scale, scale, scale);
		poseStack.translate(0, entity.getBbHeight() * -0.5f, entity.getBbWidth());

		float yBodyRotPrev = entity.yBodyRot;
		float yRotPrev = entity.yRot;
		float xRotPrev = entity.xRot;
		float yHeadRotOldPrev = entity.yHeadRotO;
		float yHeadRotPrev = entity.yHeadRot;
		int hurtTicks = entity.hurtTime;
		float limbSwingPrev = entity.animationPosition;
		float limbSwingSpeedPrev = entity.animationSpeed;
		float attackTimePrev = entity.attackAnim;
		float attackTimeOldPrev = entity.oAttackAnim;

		entity.yRot = 22.5f;
		entity.xRot = 0;

		entity.yBodyRot = 22.5f;
		entity.yHeadRot = entity.yRot;
		entity.yHeadRotO = entity.yRot;

		entity.hurtTime = TESAPI.getConfig().hudEntityDamageOverlay() ? entity.hurtTime : 0;
		entity.animationPosition = 0;
		entity.animationSpeed = 0;
		entity.attackAnim = 0;
		entity.oAttackAnim = 0;

		RenderSystem.runAsFancy(() -> renderEntityRaw(poseStack, entity, partialTick, 0, 15728880, bufferSource));

		bufferSource.endBatch();

		entity.yRot = yRotPrev;
		entity.xRot = xRotPrev;

		entity.yBodyRot = yBodyRotPrev;
		entity.yHeadRot = yHeadRotPrev;
		entity.yHeadRotO = yHeadRotOldPrev;

		entity.hurtTime = hurtTicks;
		entity.animationPosition = limbSwingPrev;
		entity.animationSpeed = limbSwingSpeedPrev;
		entity.attackAnim = attackTimePrev;
		entity.oAttackAnim = attackTimeOldPrev;

		poseStack.popPose();
	}

	/**
	 * Render a static instance of an entity, skipping its shadows and hitbox
	 */
	public static <T extends Entity> void renderEntityRaw(MatrixStack poseStack, T entity, float partialTick, float rotYaw, int packedLight, IRenderTypeBuffer bufferSource) {
		EntityRendererManager entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
		EntityRenderer<T> entityRenderer = (EntityRenderer<T>)entityRenderDispatcher.getRenderer(entity);

		try {
			Vector3d renderOffset = entityRenderer.getRenderOffset(entity, partialTick);

			poseStack.pushPose();
			poseStack.translate(renderOffset.x, renderOffset.y, renderOffset.z);
			entityRenderer.render(entity, rotYaw, 1, poseStack, bufferSource, packedLight);
			poseStack.popPose();
		}
		catch (Exception ex) {
			CrashReport wrappedReport = CrashReport.forThrowable(ex, "Rendering entity in world");
			CrashReportCategory entityReportCategory = wrappedReport.addCategory("Entity being rendered");

			entity.fillCrashReportCategory(entityReportCategory);

			CrashReportCategory rendererReportCategory = wrappedReport.addCategory("Renderer details");

			rendererReportCategory.setDetail("Assigned renderer", entityRenderer);

			throw new ReportedException(wrappedReport);
		}
	}

	/**
	 * Wrapper for {@link Screen#blit} to make it easier to use
	 * @param poseStack The MatrixStack for the current matrix state
	 * @param posX The x position on the screen to render at
	 * @param posY The y position on the screen to render at
	 * @param width The width of the image
	 * @param height The height of the image
	 * @param u The x position on the texture image to render from
	 * @param v The y position on the texture image to render from
	 * @param pngSize The pixel-size of the png file (only for square png files)
	 */
	public static void drawSimpleTexture(MatrixStack poseStack, int posX, int posY, int width, int height, float u, float v, int pngSize) {
		drawSimpleTexture(poseStack, posX, posY, width, height, u, v, width, height, pngSize, pngSize);
	}

	/**
	 * Wrapper for {@link Screen#blit} to make it easier to use
	 * @param poseStack The MatrixStack for the current matrix state
	 * @param posX The x position on the screen to render at
	 * @param posY The y position on the screen to render at
	 * @param width The width of the in-game render
	 * @param height The height of the in-game render
	 * @param u The x position on the texture image to render from
	 * @param v The y position on the texture image to render from
	 * @param uWidth The width of the texture on the image to render
	 * @param vHeight The height of the texture on the image to render
	 * @param pngWidth The width of the entire png file
	 * @param pngHeight The height of the entire png file
	 */
	public static void drawSimpleTexture(MatrixStack poseStack, int posX, int posY, int width, int height, float u, float v, int uWidth, int vHeight, int pngWidth, int pngHeight) {
		Screen.blit(poseStack, posX, posY, width, height, u, v, uWidth, vHeight, pngWidth, pngHeight);
	}

	/**
	 * Wrapper for {@link FontRenderer#draw} to make it easier/more consistent to use
	 * @param poseStack The MatrixStack for the current matrix state
	 * @param text The text to draw
	 * @param x The x position on the screen to render at
	 * @param y The y position on the screen to render at
	 * @param colour The {@link net.minecraft.util.text.Color packed int} colour for the text
	 */
	public static void drawText(MatrixStack poseStack, String text, float x, float y, int colour) {
		drawText(poseStack, new StringTextComponent(text), x, y, colour);
	}

	/**
	 * Wrapper for {@link FontRenderer#draw} to make it easier/more consistent to use
	 * @param poseStack The MatrixStack for the current matrix state
	 * @param text The text to draw
	 * @param x The x position on the screen to render at
	 * @param y The y position on the screen to render at
	 * @param colour The {@link net.minecraft.util.text.Color packed int} colour for the text
	 */
	public static void drawText(MatrixStack poseStack, ITextComponent text, float x, float y, int colour) {
		Minecraft.getInstance().font.draw(poseStack, text, x, y, colour);
	}

	/**
	 * Wrapper for {@link FontRenderer#drawShadow} to make it easier/more consistent to use
	 * @param poseStack The MatrixStack for the current matrix state
	 * @param text The text to draw
	 * @param x The x position on the screen to render at
	 * @param y The y position on the screen to render at
	 * @param colour The {@link net.minecraft.util.text.Color packed int} colour for the text
	 */
	public static void drawTextWithShadow(MatrixStack poseStack, String text, float x, float y, int colour) {
		drawText(poseStack, new StringTextComponent(text), x, y, colour);
	}

	/**
	 * Wrapper for {@link FontRenderer#drawShadow} to make it easier/more consistent to use
	 * @param poseStack The MatrixStack for the current matrix state
	 * @param text The text to draw
	 * @param x The x position on the screen to render at
	 * @param y The y position on the screen to render at
	 * @param colour The {@link net.minecraft.util.text.Color packed int} colour for the text
	 */
	public static void drawTextWithShadow(MatrixStack poseStack, ITextComponent text, float x, float y, int colour) {
		Minecraft.getInstance().font.drawShadow(poseStack, text, x, y, colour);
	}

	/**
	 * Wrapper for {@link Screen#fill} to make it easier/more consistent to use
	 * @param poseStack The MatrixStack for the current matrix state
	 * @param posX The x position on the screen to render at
	 * @param posY The y position on the screen to render at
	 * @param width The width of the square
	 * @param height The height of the square
	 * @param colour The {@link net.minecraft.util.text.Color packed int} colour for the square
	 */
	public static void drawColouredSquare(MatrixStack poseStack, int posX, int posY, int width, int height, int colour) {
		Screen.fill(poseStack, posX, posY, posX + width, posY + height, colour);
	}

	/**
	 * Translate a given locale key to its locally-translated string equivalent
	 */
	public static String translateKey(String key, Object... args) {
		return I18n.get(key, args);
	}

	/**
	 * Proxy-style getter for the client player
	 */
	public static PlayerEntity getClientPlayer() {
		return Minecraft.getInstance().player;
	}

	/**
	 * Packs an ARGB integer group into a single int for rendering
	 */
	public static int packColour(int alpha, int red, int green, int blue) {
		return alpha << 24 | red << 16 | green << 8 | blue;
	}
}
