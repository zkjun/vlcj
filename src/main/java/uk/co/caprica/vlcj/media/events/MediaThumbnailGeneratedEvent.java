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

package uk.co.caprica.vlcj.media.events;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.binding.internal.*;
import uk.co.caprica.vlcj.media.Media;
import uk.co.caprica.vlcj.model.Picture;

final class MediaThumbnailGeneratedEvent extends MediaEvent {

    private final libvlc_picture_t thumbnail;

    MediaThumbnailGeneratedEvent(LibVlc libvlc, Media media, libvlc_event_t event) {
        super(libvlc, media);
        this.thumbnail = ((media_thumbnail_generated) event.u.getTypedValue(media_thumbnail_generated.class)).p_thumbnail;
    }

    @Override
    public void notify(MediaEventListener listener) {
        Picture picture = new Picture(libvlc, thumbnail);
        listener.mediaThumbnailGenerated(media, picture);
    }

}