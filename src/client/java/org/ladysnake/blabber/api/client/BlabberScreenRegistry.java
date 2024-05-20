/*
 * Blabber
 * Copyright (C) 2022-2024 Ladysnake
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
package org.ladysnake.blabber.api.client;

import net.minecraft.client.gui.screen.ingame.HandledScreens;
import org.jetbrains.annotations.ApiStatus;
import org.ladysnake.blabber.api.layout.DialogueLayout;
import org.ladysnake.blabber.api.layout.DialogueLayoutType;
import org.ladysnake.blabber.impl.client.BlabberClient;
import org.ladysnake.blabber.impl.common.DialogueScreenHandler;

@ApiStatus.Experimental
public final class BlabberScreenRegistry {
    /**
     * Registers a custom screen provider for a {@link org.ladysnake.blabber.api.layout.DialogueLayoutType}.
     */
    public static <P extends DialogueLayout.Params> void register(
            DialogueLayoutType<P> layoutType,
            HandledScreens.Provider<DialogueScreenHandler, BlabberDialogueScreen<P>> screenProvider
    ) {
        BlabberClient.registerLayoutScreen(layoutType, screenProvider);
    }
}
