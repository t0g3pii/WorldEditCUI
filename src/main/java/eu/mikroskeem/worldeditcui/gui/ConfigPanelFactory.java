package eu.mikroskeem.worldeditcui.gui;

import io.github.prospector.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;

import java.util.function.Function;

/**
 * @author Mark Vainomaa
 */
public final class ConfigPanelFactory implements ModMenuApi {
    @Override
    public String getModId() {
        return "wecui";
    }

    @Override
    public Function<Screen, ? extends Screen> getConfigScreenFactory() {
        return FabricCUIConfigPanel::new;
    }
}
