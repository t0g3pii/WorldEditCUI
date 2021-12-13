package eu.mikroskeem.worldeditcui.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mumfrey.worldeditcui.config.CUIConfiguration;
import com.mumfrey.worldeditcui.config.Colour;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * @author Mark Vainomaa
 * @author Jesús Sanz - Modified to implement Config GUI / First Version
 */
public class FabricCUIConfigPanel extends Screen implements Supplier<Screen> {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Component TRUE = new TextComponent("true").withStyle(s -> s.applyFormat(ChatFormatting.DARK_GREEN));
    private static final Component FALSE = new TextComponent("false").withStyle(s -> s.applyFormat(ChatFormatting.DARK_RED));

    private final Screen parent;
    private final Set<String> options;
    private final CUIConfiguration configuration;
    private double scrollPercent = 0;
    private SettingsWidget configList;
    private AbstractWidget done;

    private final Component screenTitle;

    private static final int BUTTONHEIGHT = 20;

    public FabricCUIConfigPanel(Screen parent, CUIConfiguration configuration) {
        super(new TextComponent("WorldEditCUI"));
        this.parent = parent;
        this.configuration = configuration;
        this.options = this.configuration.getConfigArray().keySet();
        this.screenTitle = new TranslatableComponent("worldeditcui.options.title");
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (configList.isMouseOver(mouseX, mouseY))
            return this.configList.mouseScrolled(mouseX, mouseY, amount);
        return false;
    }

    @Override
    protected void init() {
        configList = new SettingsWidget(this.minecraft, this.width - 8, this.height, 48 + 19, this.height - 36, 25, this.configuration, this);
        configList.setLeftPos(0);
        done = this.addRenderableWidget(new AbstractWidget(this.width / 2 - 205, this.height - 27, 70, 20, new TranslatableComponent("worldeditcui.options.done")) {
            @Override
            public void updateNarration(NarrationElementOutput builder) {
                this.defaultButtonNarrationText(builder);
            }

            @Override
            public void onClick(double mouseX, double mouseY) {
                for (GuiEventListener button : children()) {
                    if (button instanceof EditBox widget) {
                        if(widget.isFocused()) {
                            widget.changeFocus(false);
                        }
                    }
                }
                configuration.configChanged();
                assert minecraft != null;
                minecraft.setScreen(parent);
            }
        });

        int y = 0;
        for (String text : options) {
            int buttonX = this.width - 140 - 20;
            Object value = configuration.getConfigArray().get(text);
            AbstractWidget element;
            TranslatableComponent textTemp = configuration.getDescription(text);
            if (value == null) {
                LOGGER.warn("value null, adding nothing");
                continue;
            } else if(value instanceof Boolean) {
                element = this.addRenderableWidget(new AbstractWidget(buttonX, y, 70, BUTTONHEIGHT, ((Boolean) value ? TRUE: FALSE)) {
                    @Override
                    public void updateNarration(NarrationElementOutput builder) {
                        this.defaultButtonNarrationText(builder);
                    }

                    @Override
                    public void onClick(double mouseX, double mouseY) {
                        if ((Boolean) (configuration.getConfigArray().get(text))) {
                            configuration.changeValue(text, false);
                        } else {
                            configuration.changeValue(text, true);
                        }
                        this.changeFocus(true);
                    }

                    @Override
                    protected void onFocusedChanged(boolean bl) {
                        this.setMessage((Boolean) configuration.getConfigArray().get(text) ? TRUE : FALSE);
                        super.onFocusedChanged(bl);
                    }
                });
            } else if(value instanceof Colour) {
                element = this.addRenderableWidget(new TextFieldWidgetTemp(this.font, buttonX, y, 70, BUTTONHEIGHT, new TextComponent(((Colour)value).getHex()), ((Colour)value).getHex()) {
                    @Override
                    protected void onFocusedChanged(boolean bl) {
                        if (bl) {
                            this.setValue(((Colour)configuration.getConfigArray().get(text)).getHex());
                        } else {
                            configuration.changeValue(text, new Colour(this.getValue()));
                        }
                        super.onFocusedChanged(bl);
                    }

                    @Override
                    public boolean charTyped(char chr, int keyCode) {
                        boolean result = super.charTyped(chr, keyCode);
                        configuration.changeValue(text, new Colour(this.getValue()));
                        return result;
                    }

                    @Override
                    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                        boolean result = super.keyPressed(keyCode, scanCode, modifiers);
                        configuration.changeValue(text, new Colour(this.getValue()));
                        return result;
                    }
                });
            } else {
                LOGGER.warn("WorldEditCUI has option " + text + " with data type " + value.getClass().getName());
                continue;
            }
            this.configList.addEntry(new SettingsEntry(this.configList, (textTemp != null) ? textTemp : new TextComponent(text), element, this.addRenderableWidget(new AbstractWidget(buttonX + 75, y, BUTTONHEIGHT, BUTTONHEIGHT, TextComponent.EMPTY) {
                @Override
                public void updateNarration(NarrationElementOutput builder) {
                    this.defaultButtonNarrationText(builder);
                }

                @Override
                public void onClick(double mouseX, double mouseY) {
                    configuration.changeValue(text, configuration.getDefaultValue(text));
                    element.changeFocus(false);
                }
            })));
        }
        this.setFocused(null);
        this.addRenderableOnly(this.configList);
    }

    @Override
    public Screen get() {
        return this;
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.configList.render(matrices, mouseX, mouseY, delta);
        drawCenteredString(matrices, this.font, screenTitle, this.configList.getWidth() / 2, BUTTONHEIGHT, 0xFFFFFF);
        this.done.render(matrices, mouseX, mouseY, delta);
    }

    double getScrollPercent() {
        return scrollPercent;
    }

    void updateScrollPercent(double scrollPercent) {
        this.scrollPercent = scrollPercent;
    }

    static class TextFieldWidgetTemp extends EditBox {

        public TextFieldWidgetTemp(Font textRenderer, int x, int y, int width, int height, Component message, String text) {
            super(textRenderer, x, y, width, height, message);
            this.setValue(text);
        }
    }
}
