package eu.mikroskeem.worldeditcui.gui;

import eu.mikroskeem.worldeditcui.FabricModWorldEditCUI;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;

/**
 * @author Mark Vainomaa
 */
public final class ConfigPanelFactory implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new FabricCUIConfigPanel(parent, FabricModWorldEditCUI.getInstance().getController().getConfiguration());
    }
}
