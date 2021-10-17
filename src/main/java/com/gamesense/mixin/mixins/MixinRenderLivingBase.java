package com.gamesense.mixin.mixins;

import com.gamesense.api.event.events.RenderEntityEvent;
import com.gamesense.client.GameSense;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.render.NoRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author linustouchtips
 * @author Hoosiers
 * @since 12/14/2020
 * @since 12/31/2020
 */

@Mixin(RenderLivingBase.class)
public abstract class MixinRenderLivingBase<T extends EntityLivingBase> extends Render<T> {

    protected MixinRenderLivingBase() {
        super(null);
    }

    protected final Minecraft mc = Minecraft.getMinecraft();

    private boolean isClustered;

    @Inject(method = "renderModel", at = @At("HEAD"), cancellable = true)
    protected void renderModel(T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, CallbackInfo callbackInfo) {
        if (!this.bindEntityTexture(entitylivingbaseIn)) {
            return;
        }

        NoRender noRender = ModuleManager.getModule(NoRender.class);

        if (noRender.isEnabled() && noRender.noCluster.getValue() && mc.player.getDistance(entitylivingbaseIn) < 1 && entitylivingbaseIn != mc.player) {
            GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
            isClustered = true;
            if (!noRender.incrementNoClusterRender()) {
                callbackInfo.cancel();
            }
        } else {
            isClustered = false;
        }

        RenderEntityEvent.Head renderEntityHeadEvent = new RenderEntityEvent.Head(entitylivingbaseIn, RenderEntityEvent.Type.COLOR);

        GameSense.EVENT_BUS.post(renderEntityHeadEvent);

        if (renderEntityHeadEvent.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "renderModel", at = @At("RETURN"), cancellable = true)
    protected void renderModelReturn(T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, CallbackInfo callbackInfo) {
        RenderEntityEvent.Return renderEntityReturnEvent = new RenderEntityEvent.Return(entitylivingbaseIn, RenderEntityEvent.Type.COLOR);

        GameSense.EVENT_BUS.post(renderEntityReturnEvent);

        if (!renderEntityReturnEvent.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "renderLayers", at = @At("HEAD"), cancellable = true)
    protected void renderLayers(T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scaleIn, CallbackInfo callbackInfo) {
        if (isClustered) {
            if (!ModuleManager.getModule(NoRender.class).getNoClusterRender()) {
                callbackInfo.cancel();
            }
        }
    }

    //:troll:
    @Inject(method = "applyRotations", at = @At("TAIL"))
    protected void applyRotations(T entityLiving, float ageInTicks, float rotationYaw, float partialTicks, CallbackInfo ci) {
        String s = TextFormatting.getTextWithoutFormattingCodes(entityLiving.getName());
        if (("ToxicAven".equals(s) && (!(entityLiving instanceof EntityPlayer) || ((EntityPlayer) entityLiving).isWearing(EnumPlayerModelParts.CAPE)))) {
            GlStateManager.translate(0.0F, entityLiving.height + 0.1F, 0.0F);
            GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        }
    }

    /*
      """Done like this or with 9 mixins. You choose?"""
      Never mind!!
     */
    @Redirect(method = "setBrightness", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;glTexEnvi(III)V", ordinal = 6))
    protected void glTexEnvi0(int target, int parameterName, int parameter) {
        if (!isClustered) {
            GlStateManager.glTexEnvi(target, parameterName, parameter);
        }
    }

    @Redirect(method = "setBrightness", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;glTexEnvi(III)V", ordinal = 7))
    protected void glTexEnvi1(int target, int parameterName, int parameter) {
        if (!isClustered) {
            GlStateManager.glTexEnvi(target, parameterName, parameter);
        }
    }

    @Redirect(method = "setBrightness", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;glTexEnvi(III)V", ordinal = 8))
    protected void glTexEnvi2(int target, int parameterName, int parameter) {
        if (!isClustered) {
            GlStateManager.glTexEnvi(target, parameterName, parameter);
        }
    }
}