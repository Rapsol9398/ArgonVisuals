package com.rapsol.gui.components.settings;

import com.rapsol.Argon;
import com.rapsol.gui.components.ModuleButton;
import com.rapsol.module.modules.client.ClickGUI;
import com.rapsol.module.setting.Setting;
import com.rapsol.module.setting.StringSetting;
import com.rapsol.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public final class StringBox extends RenderableSetting {
    private final StringSetting setting;
    private Color currentAlpha;

    public StringBox(ModuleButton parent, Setting<?> setting, int offset) {
        super(parent, setting, offset);
        this.setting = (StringSetting) setting;
    }

    @Override
    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        TextRenderer.text(setting.getName() + ": " + (setting.getValue().length() <= 9 ? setting.getValue() : (setting.getValue().substring(0, 9) + "...")), context, parentX() + 9 ,(parentY() + parentOffset() + offset) + 9, new Color(245, 245, 245, 255).getRGB());

        if (!parent.parent.dragging) {
            int toHoverAlpha = isHovered(mouseX, mouseY) ? 15 : 0;

            if (currentAlpha == null)
                currentAlpha = new Color(255, 255, 255, toHoverAlpha);
            else currentAlpha = new Color(255, 255, 255, currentAlpha.getAlpha());

            if (currentAlpha.getAlpha() != toHoverAlpha)
                currentAlpha = ColorUtils.smoothAlphaTransition(0.05F, toHoverAlpha, currentAlpha);

            context.fill(parentX(), parentY() + parentOffset() + offset, parentX() + parentWidth(), parentY() + parentOffset() + offset + parentHeight(), currentAlpha.getRGB());
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if(isHovered(mouseX, mouseY) && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            mc.setScreen(new Screen(Component.empty()) {
                private String content = setting.getValue();

                @Override
                public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
                    RenderUtils.unscaledProjection(context);
                    //mouseX *= (int) Minecraft.getInstance().getWindow().getGuiScale();
                    //mouseY *= (int) Minecraft.getInstance().getWindow().getGuiScale();
                    super.extractRenderState(context, mouseX, mouseY, delta);

                    context.fill(0, 0, mc.getWindow().getScreenWidth(), mc.getWindow().getScreenHeight(), new Color(0, 0, 0, ClickGUI.background.getValue() ? 200 : 0).getRGB());

                    int screenMidX = mc.getWindow().getScreenWidth() / 2;
                    int screenMidY = mc.getWindow().getScreenHeight() / 2;

                    int contentWidth = Math.max(TextRenderer.getWidth(content), 600);
                    int width = contentWidth + 30;

                    int startX = screenMidX - (width / 2);
                    int startY = screenMidY - 30;

                    RenderUtils.renderRoundedQuad(context, new Color(0, 0, 0, ClickGUI.alphaWindow.getValueInt()), startX, startY, startX + width, screenMidY + 30, 5, 5, 0, 0, 20);
                    TextRenderer.centeredText(setting.getName(), context, screenMidX, startY + 10, new Color(245, 245, 245, 255).getRGB());
                    context.fill(startX, screenMidY, startX + width, screenMidY + 30, new Color(0, 0, 0, 120).getRGB());

                    RenderUtils.renderRoundedOutline(context, new Color(50, 50, 50, 255), startX + 10, screenMidY + 5, startX + (width - 10), screenMidY + 25, 5, 5, 5, 5, 2, 20);

                    TextRenderer.text(content, context, startX + 15, screenMidY + 8, new Color(245, 245, 245, 255).getRGB());
                    context.fill(startX, screenMidY, startX + width, screenMidY + 1, Utils.getMainColor(255, 1).getRGB());

                    RenderUtils.scaledProjection(context);
                }

                @Override
                public boolean keyPressed(KeyEvent event) {
                    int keyCode = event.key();
                    if(keyCode == GLFW.GLFW_KEY_ESCAPE) {
                        setting.setValue(content.strip());
                        mc.setScreen(Argon.INSTANCE.clickGui);
                    }

                    if(keyCode == GLFW.GLFW_KEY_V && (event.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0)
                        content += mc.keyboardHandler.getClipboard();

                    if(keyCode == GLFW.GLFW_KEY_C && (event.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0)
                        GLFW.glfwSetClipboardString(mc.getWindow().handle(), content);

                    if(keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                        if(!content.isEmpty()) {
                            content = content.substring(0, content.length() - 1);
                        }
                    }

                    return super.keyPressed(event);
                }

                public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
                }

                @Override
                public boolean charTyped(CharacterEvent event) {
                    content += event.codepointAsString();
                    return super.charTyped(event);
                }

                @Override
                public boolean shouldCloseOnEsc() {
                    return false;
                }
            });
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

}
