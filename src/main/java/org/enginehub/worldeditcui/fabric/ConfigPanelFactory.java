package org.enginehub.worldeditcui.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import org.enginehub.worldeditcui.gui.CUIConfigPanel;

/**
 * @author Mark Vainomaa
 */
public final class ConfigPanelFactory implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new CUIConfigPanel(parent, FabricModWorldEditCUI.getInstance().getController().getConfiguration());
    }
}
