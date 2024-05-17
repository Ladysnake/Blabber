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
package org.ladysnake.blabber.impl.common.illustrations;

import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.ladysnake.blabber.impl.common.model.IllustrationAnchor;

public class PositionTransform {
    private static final Vector2ic ORIGIN = new Vector2i();
    private int x1;
    private int y1;
    private int x2;
    private int y2;
    private final Vector2ic[] slots;

    public PositionTransform(Vector2ic[] slots) {
        this.slots = slots;
    }

    private Vector2ic getSlotPos(IllustrationAnchor anchor) {
        int i = anchor.slotId();
        if (i < 0) {
            throw new IllegalStateException(anchor + " is not a slot-based anchor");
        } else if (i >= slots.length) {
            return ORIGIN;
        }
        return slots[i];
    }

    public int transformX(IllustrationAnchor anchor, int x) {
        return switch (anchor) {
            case TOP_LEFT, BOTTOM_LEFT -> x1 + x;
            case TOP_RIGHT, BOTTOM_RIGHT -> x2 - x;
            case CENTER -> (x1 + x2) / 2 + x;
            case SLOT_1, SLOT_2 -> getSlotPos(anchor).x() + x;
        };
    }

    public int inverseTransformX(IllustrationAnchor anchor, int x) {
        return switch (anchor) {
            case TOP_LEFT, BOTTOM_LEFT -> x1 + x;
            case TOP_RIGHT, BOTTOM_RIGHT -> x2 - x;
            case CENTER -> x - (x1 + x2) / 2;
            case SLOT_1, SLOT_2 -> x - getSlotPos(anchor).x();
        };
    }

    public int transformY(IllustrationAnchor anchor, int y) {
        return switch (anchor) {
            case TOP_LEFT, TOP_RIGHT -> y1 + y;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> y2 - y;
            case CENTER -> (y1 + y2) / 2 + y;
            case SLOT_1, SLOT_2 -> getSlotPos(anchor).y() + y;
        };
    }

    public int inverseTransformY(IllustrationAnchor anchor, int y) {
        return switch (anchor) {
            case TOP_LEFT, TOP_RIGHT -> y1 + y;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> y2 - y;
            case CENTER -> y - (y1 + y2) / 2;
            case SLOT_1, SLOT_2 -> y - getSlotPos(anchor).y();
        };
    }

    public void setControlPoints(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }
}
