package com.gamesense.client.module.modules.render;

import com.gamesense.api.setting.values.*;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.api.util.render.shaders.impl.fill.*;
import com.gamesense.api.util.render.shaders.impl.outline.GlowShader;
import com.gamesense.api.util.render.shaders.impl.outline.GradientOutlineShader;
import com.gamesense.api.util.render.shaders.impl.outline.Default;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

@Module.Declaration(name = "Shaders", category = Category.Render)
public class Shaders extends Module {

    ModeSetting glowESP = registerMode("Glow ESP", Arrays.asList("None", "Color", "Astral", "RainbowCube", "Gradient"), "None");
    ColorSetting colorESP = registerColor("Color ESP", new GSColor(255, 255, 255, 255));
    DoubleSetting radius = registerDouble("Radius ESP", 1, 0, 5);
    DoubleSetting quality = registerDouble("Quality ESP", 1, 0, 20);
    BooleanSetting GradientAlpha = registerBoolean("Gradient Alpha", false);
    IntegerSetting alphaValue = registerInteger("Alpha Outline", 255, 0, 255, () -> !GradientAlpha.getValue());


    ModeSetting fillShader = registerMode("Fill Shader", Arrays.asList("Astral", "Aqua", "Smoke", "RainbowCube", "Gradient", "None"), "None");
    DoubleSetting moreGradientFill = registerDouble("More Gradient", 1, 0, 10, () -> fillShader.getValue().equals("Gradient"));
    DoubleSetting creepyFill = registerDouble("Creepy", 1, 0, 20, () -> fillShader.getValue().equals("Gradient"));
    IntegerSetting WaveLenghtFIll = registerInteger("Wave Lenght", 555, 0, 2000, () -> fillShader.getValue().equals("RainbowCube"));
    IntegerSetting RSTARTFill = registerInteger("RSTART", 0, 0, 1000, () -> fillShader.getValue().equals("RainbowCube"));
    IntegerSetting GSTARTFill = registerInteger("GSTART", 0, 0, 1000, () -> fillShader.getValue().equals("RainbowCube"));
    IntegerSetting BSTARTFIll = registerInteger("BSTART", 0, 0, 1000, () -> fillShader.getValue().equals("RainbowCube"));
    ColorSetting colorImgFill = registerColor("Color Img", new GSColor(0, 0, 0, 255), () -> fillShader.getValue().equals("Aqua") || fillShader.getValue().equals("Smoke") || fillShader.getValue().equals("RainbowCube"), true);
    ColorSetting secondColorImgFill = registerColor("Second Color Img", new GSColor(255, 255, 255, 255), () -> fillShader.getValue().equals("Smoke"));
    ColorSetting thirdColorImgFIll = registerColor("Third Color Img", new GSColor(255, 255, 255, 255), () -> fillShader.getValue().equals("Smoke"));
    IntegerSetting NUM_OCTAVESFill = registerInteger("NUM_OCTAVES", 5, 1, 30, () -> fillShader.getValue().equals("Smoke"));
    IntegerSetting MaxIterFill = registerInteger("Max Iter", 5, 0, 30, () -> fillShader.getValue().equals("Aqua"));
    DoubleSetting tauFill = registerDouble("TAU", 6.28318530718, 0, 20, () -> fillShader.getValue().equals("Aqua"));
    IntegerSetting redFill = registerInteger("Red", 0, 0, 100, () -> fillShader.getValue().equals("Astral"));
    DoubleSetting greenFill = registerDouble("Green", 0, 0, 5, () -> fillShader.getValue().equals("Astral"));
    DoubleSetting blueFill = registerDouble("Blue", 0, 0, 5, () -> fillShader.getValue().equals("Astral"));
    DoubleSetting alphaFill = registerDouble("Alpha", 1, 0, 1, () -> fillShader.getValue().equals("Astral") || fillShader.getValue().equals("Gradient"));
    IntegerSetting iterationsFill = registerInteger("Iteration", 4, 3, 20, () -> fillShader.getValue().equals("Astral"));
    DoubleSetting formuparam2Fill = registerDouble("formuparam2", 0.89, 0, 1.5, () -> fillShader.getValue().equals("Astral"));
    DoubleSetting zoomFill = registerDouble("Zoom", 3.9, 0, 20, () -> fillShader.getValue().equals("Astral"));
    IntegerSetting volumStepsFill = registerInteger("Volum Steps", 10, 0, 10, () -> fillShader.getValue().equals("Astral"));
    DoubleSetting stepSizeFill = registerDouble("Step Size", 0.190, 0.0, 0.7, () -> fillShader.getValue().equals("Astral"));
    DoubleSetting titleFill = registerDouble("Tile", 0.45, 0, 1.3, () -> fillShader.getValue().equals("Astral"));
    DoubleSetting distfadingFill = registerDouble("distfading", 0.56, 0, 1, () -> fillShader.getValue().equals("Astral"));
    DoubleSetting saturationFill = registerDouble("saturation", 0.4, 0, 3, () -> fillShader.getValue().equals("Astral"));
    BooleanSetting fadeFill = registerBoolean("Fade Fill", false, () -> fillShader.getValue().equals("Astral"));
    BooleanSetting itemsFill = registerBoolean("Items Fill", false);
    BooleanSetting mobsFill = registerBoolean("Mobs Fill", false);
    BooleanSetting playersFill = registerBoolean("Players Fill", false);
    BooleanSetting crystalsFill = registerBoolean("Crystals Fill", false);
    BooleanSetting xpFill = registerBoolean("XP Fill", false);
    BooleanSetting rangeCheck = registerBoolean("Range Check", true);
    DoubleSetting minRange = registerDouble("Min range", 1, 0, 5, () -> rangeCheck.getValue());
    DoubleSetting maxRange = registerDouble("Max Range", 20, 10, 100, () -> rangeCheck.getValue());
    IntegerSetting maxEntities = registerInteger("Max Entities", 100, 10, 500);
    DoubleSetting speedFill = registerDouble("Speed Fill", 0.1, 0.001, 0.1);
    DoubleSetting speedOutline = registerDouble("Speed Outline", 0.1, 0.001, 0.1);
    DoubleSetting duplicateFill = registerDouble("Duplicate Fill", 1, 0, 5);
    DoubleSetting duplicateOutline = registerDouble("Duplicate Outline", 1, 0, 20);

    public boolean renderTags = true,
            renderCape = true;


    @EventHandler
    private final Listener<RenderGameOverlayEvent.Pre> renderGameOverlayEventListener = new Listener<>(event -> {

        if (event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR) {
            if (mc.world == null)
                return;


            GlStateManager.pushMatrix();
            renderTags = false;
            renderCape = false;

            switch (fillShader.getValue()) {
                case "Astral":
                    FlowShader.INSTANCE.startDraw(event.getPartialTicks());
                    renderPlayers(event.getPartialTicks());
                    FlowShader.INSTANCE.stopDraw(Color.WHITE, 1f, 1f, duplicateFill.getValue().floatValue(),
                            redFill.getValue().floatValue(), greenFill.getValue().floatValue(), blueFill.getValue().floatValue(), alphaFill.getValue().floatValue(),
                            iterationsFill.getValue(), formuparam2Fill.getValue().floatValue(), zoomFill.getValue().floatValue(), volumStepsFill.getValue(), stepSizeFill.getValue().floatValue(), titleFill.getValue().floatValue(), distfadingFill.getValue().floatValue(),
                            saturationFill.getValue().floatValue(), 0f, fadeFill.getValue() ? 1 : 0);
                    FlowShader.INSTANCE.update(speedFill.getValue());
                    break;
                case "Aqua":
                    AquaShader.INSTANCE.startDraw(event.getPartialTicks());
                    renderPlayers(event.getPartialTicks());
                    AquaShader.INSTANCE.stopDraw(colorImgFill.getColor(), 1f, 1f, duplicateFill.getValue().floatValue(), MaxIterFill.getValue(), tauFill.getValue());
                    AquaShader.INSTANCE.update(speedFill.getValue());
                    break;
                case "Smoke":
                    SmokeShader.INSTANCE.startDraw(event.getPartialTicks());
                    renderPlayers(event.getPartialTicks());
                    SmokeShader.INSTANCE.stopDraw(Color.WHITE, 1f, 1f, duplicateFill.getValue().floatValue(), colorImgFill.getColor(), secondColorImgFill.getColor(), thirdColorImgFIll.getColor(), NUM_OCTAVESFill.getValue());
                    SmokeShader.INSTANCE.update(speedFill.getValue());
                    break;
                case "RainbowCube":
                    RainbowCubeShader.INSTANCE.startDraw(event.getPartialTicks());
                    renderPlayers(event.getPartialTicks());
                    RainbowCubeShader.INSTANCE.stopDraw(Color.WHITE, 1f, 1f, duplicateFill.getValue().floatValue(), colorImgFill.getColor(), WaveLenghtFIll.getValue(), RSTARTFill.getValue(), GSTARTFill.getValue(), BSTARTFIll.getValue());
                    RainbowCubeShader.INSTANCE.update(speedFill.getValue());
                    break;
                case "Gradient":
                    GradientShader.INSTANCE.startDraw(event.getPartialTicks());
                    renderPlayers(event.getPartialTicks());
                    GradientShader.INSTANCE.stopDraw(colorESP.getValue(), 1f, 1f, duplicateFill.getValue().floatValue(), moreGradientFill.getValue().floatValue(), creepyFill.getValue().floatValue(), alphaFill.getValue().floatValue(), NUM_OCTAVESFill.getValue());
                    GradientShader.INSTANCE.update(speedFill.getValue());
                    break;
            }

            switch (glowESP.getValue()) {
                case "Color":
                    GlowShader.INSTANCE.startDraw(event.getPartialTicks());
                    renderPlayers(event.getPartialTicks());
                    GlowShader.INSTANCE.stopDraw(colorESP.getValue(), radius.getValue().floatValue(), quality.getValue().floatValue(), GradientAlpha.getValue(), alphaValue.getValue());
                    break;
                case "RainbowCube":
                    Default.INSTANCE.startDraw(event.getPartialTicks());
                    renderPlayers(event.getPartialTicks());
                    Default.INSTANCE.stopDraw(colorESP.getValue(), radius.getValue().floatValue(), quality.getValue().floatValue(), GradientAlpha.getValue(), alphaValue.getValue(), duplicateOutline.getValue().floatValue());
                    break;
                case "Gradient":
                    GradientOutlineShader.INSTANCE.startDraw(event.getPartialTicks());
                    renderPlayers(event.getPartialTicks());
                    GradientOutlineShader.INSTANCE.stopDraw(colorESP.getValue(), radius.getValue().floatValue(), quality.getValue().floatValue(), GradientAlpha.getValue(), alphaValue.getValue(), duplicateOutline.getValue().floatValue(), moreGradientFill.getValue().floatValue(), creepyFill.getValue().floatValue(), alphaFill.getValue().floatValue(), NUM_OCTAVESFill.getValue());
                    GradientOutlineShader.INSTANCE.update(speedOutline.getValue());
                    break;

            }

            renderTags = true;
            renderCape = true;

            GlStateManager.popMatrix();

        }
    });


    void renderPlayers(float tick) {
        boolean rangeCheck = this.rangeCheck.getValue();
        double minRange = this.minRange.getValue() * this.minRange.getValue();
        double maxRange = this.maxRange.getValue() * this.maxRange.getValue();
        AtomicInteger nEntities = new AtomicInteger();
        int maxEntities = this.maxEntities.getValue();
        mc.world.loadedEntityList.stream().filter(e -> {
                    if (nEntities.getAndIncrement() > maxEntities)
                        return false;
                    if (e instanceof EntityPlayer) {
                        if (playersFill.getValue())
                            if (e != mc.player || mc.gameSettings.thirdPersonView != 0)
                                return true;
                    } else if (e instanceof EntityItem) {
                        if (itemsFill.getValue())
                            return true;
                    } else if (e instanceof EntityCreature) {
                        if (mobsFill.getValue())
                            return true;
                    } else if (e instanceof EntityEnderCrystal) {
                        if (crystalsFill.getValue())
                            return true;
                    } else if (e instanceof EntityXPOrb) {
                        if (xpFill.getValue())
                            return true;
                    }
                    return false;
                }
        ).filter(e -> {
            if (!rangeCheck)
                return true;
            else {
                double distancePl = mc.player.getDistanceSq(e);
                return distancePl > minRange && distancePl < maxRange;
            }
        }).forEach(e -> mc.getRenderManager().renderEntityStatic(e, tick, true));
    }




}