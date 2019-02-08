/*
 * This file is part of VLCJ.
 *
 * VLCJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VLCJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VLCJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2009-2019 Caprica Software Limited.
 */

package uk.co.caprica.vlcj.component.callback;

import java.awt.*;
import java.awt.image.BufferedImage;

public class FixedCallbackImagePainter implements CallbackImagePainter {

    @Override
    public void prepare(Graphics2D g2) {
    }

    @Override
    public void paint(Graphics2D g2, int width, int height, BufferedImage image) {
        if (image != null) {
            g2.translate(
                (width - image.getWidth()) / 2,
                (height - image.getHeight()) / 2
            );
            g2.drawImage(image, null, 0, 0);
        }
    }

}
