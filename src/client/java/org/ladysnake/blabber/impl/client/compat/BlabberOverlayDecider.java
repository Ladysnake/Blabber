/*
 * Blabber
 * Copyright (C) 2022-2025 Ladysnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package org.ladysnake.blabber.impl.client.compat;

import me.shedaniel.rei.api.client.registry.screen.OverlayDecider;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ActionResult;
import org.ladysnake.blabber.api.client.BlabberDialogueScreen;

public class BlabberOverlayDecider implements OverlayDecider {
    @Override
    public <R extends Screen> boolean isHandingScreen(Class<R> screen) {
        return BlabberDialogueScreen.class.isAssignableFrom(screen);
    }

    @Override
    public <R extends Screen> ActionResult shouldScreenBeOverlaid(R screen) {
        return ActionResult.FAIL;
    }
}
