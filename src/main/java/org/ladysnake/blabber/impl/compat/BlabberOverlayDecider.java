package org.ladysnake.blabber.impl.compat;

import me.shedaniel.rei.api.client.registry.screen.OverlayDecider;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ActionResult;
import org.ladysnake.blabber.impl.client.BlabberDialogueScreen;

public class BlabberOverlayDecider implements OverlayDecider {
    @Override
    public <R extends Screen> boolean isHandingScreen(Class<R> screen) {
        return screen == BlabberDialogueScreen.class;
    }

    @Override
    public <R extends Screen> ActionResult shouldScreenBeOverlaid(R screen) {
        return ActionResult.FAIL;
    }
}
