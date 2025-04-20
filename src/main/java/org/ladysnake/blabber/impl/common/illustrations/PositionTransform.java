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
package org.ladysnake.blabber.impl.common.illustrations;

import org.joml.Vector2i;
import org.ladysnake.blabber.impl.common.model.IllustrationAnchor;

import java.util.EnumMap;

public class PositionTransform {
    private final EnumMap<IllustrationAnchor, Vector2i> anchors;

    public PositionTransform(EnumMap<IllustrationAnchor, Vector2i> anchors) {
        this.anchors = anchors;
    }

    private Vector2i getPos(IllustrationAnchor anchor) {
        return anchors.get(anchor);
    }

    public int transformX(IllustrationAnchor anchor, int x) {
        return getPos(anchor).x() + x;
    }

    public int inverseTransformX(IllustrationAnchor anchor, int x) {
        return x - getPos(anchor).x();
    }

    public int transformY(IllustrationAnchor anchor, int y) {
        return getPos(anchor).y() + y;
    }

    public int inverseTransformY(IllustrationAnchor anchor, int y) {
        return y - getPos(anchor).y();
    }

    public void setControlPoints(int x1, int y1, int x2, int y2) {
        getPos(IllustrationAnchor.TOP_LEFT).set(x1, y1);
        getPos(IllustrationAnchor.TOP_RIGHT).set(x2, y1);
        getPos(IllustrationAnchor.BOTTOM_LEFT).set(x1, y2);
        getPos(IllustrationAnchor.BOTTOM_RIGHT).set(x2, y2);
        getPos(IllustrationAnchor.CENTER).set((x1 + x2) / 2, (y1 + y2) / 2);
    }
}
