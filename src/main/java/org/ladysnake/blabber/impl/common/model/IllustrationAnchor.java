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
package org.ladysnake.blabber.impl.common.model;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

public enum IllustrationAnchor implements StringIdentifiable {
    /**
     * Starting at top-left, increasing towards bottom-right
     */
    TOP_LEFT("top_left", -1),
    /**
     * Starting at top-right, increasing towards bottom-left
     */
    TOP_RIGHT("top_right", -1),
    /**
     * Starting at bottom-left, increasing towards top-right
     */
    BOTTOM_LEFT("bottom_left", -1),
    /**
     * Starting at bottom-right, increasing towards top-left
     */
    BOTTOM_RIGHT("bottom_right", -1),
    /**
     * Starting at center, increasing towards bottom-right
     */
    CENTER("center", -1),
    SLOT_1("slot_1", 0),
    SLOT_2("slot_2", 1);

    public static final Codec<IllustrationAnchor> CODEC = StringIdentifiable.createBasicCodec(IllustrationAnchor::values);

    private final String id;
    private final int slotId;

    IllustrationAnchor(String id, int slotId) {
        this.id = id;
        this.slotId = slotId;
    }

    public int slotId() {
        return slotId;
    }

    @Override
    public String asString() {
        return this.id;
    }
}
