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

package uk.co.caprica.vlcj.component;

import uk.co.caprica.vlcj.binding.RuntimeUtil;
import uk.co.caprica.vlcj.component.callback.CallbackImagePainter;
import uk.co.caprica.vlcj.component.callback.ScaledCallbackImagePainter;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.callback.RenderCallback;
import uk.co.caprica.vlcj.player.embedded.callback.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.embedded.fullscreen.FullScreenStrategy;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 *
 */
@SuppressWarnings("serial")
public class CallbackMediaPlayerComponent extends EmbeddedMediaPlayerComponentBase implements MediaPlayerComponent {

    /**
     * Default factory initialisation arguments.
     */
    static final String[] DEFAULT_FACTORY_ARGUMENTS = MediaPlayerComponentDefaults.EMBEDDED_MEDIA_PLAYER_ARGS;

    /**
     *
     */
    private boolean ownFactory;

    /**
     * Media player factory.
     */
    protected final MediaPlayerFactory mediaPlayerFactory;

    /**
     *
     */
    private final JComponent videoSurfaceComponent;

    private final CallbackImagePainter imagePainter;

    /**
     * Media player.
     */
    private final EmbeddedMediaPlayer mediaPlayer;

    private BufferedImage image;

    /**
     *
     * <p>
     * This component will provide a reasonable default implementation, but a client application is free to override
     * these defaults with their own implementation.
     * <p>
     * To rely on the defaults and have this component render the video, the <code>size</code> parameter <em>must</em>
     * be provided. This size governs the size of the video buffer that will be used.
     * <p>
     * If a client application wishes to perform its own rendering, then it may omit the size parameter and instead
     * provide a <code>renderCallback</code>. In this case, the <code>videoSurfaceComponent</code> parameter should also
     * be provided if the client application wants the video surface they are rendering in to be incorporated into this
     * component's layout.
     *
     * @param mediaPlayerFactory
     * @param fullScreenStrategy
     * @param inputEvents
     * @param bufferFormatCallback
     * @param lockBuffers
     * @param size
     * @param videoSurfaceComponent
     * @param renderCallback
     */
    public CallbackMediaPlayerComponent(MediaPlayerFactory mediaPlayerFactory, FullScreenStrategy fullScreenStrategy, InputEvents inputEvents, BufferFormatCallback bufferFormatCallback, boolean lockBuffers, Dimension size, CallbackImagePainter imagePainter, JComponent videoSurfaceComponent, RenderCallback renderCallback) {
        this.ownFactory = mediaPlayerFactory == null;
        this.mediaPlayerFactory = initMediaPlayerFactory(mediaPlayerFactory);

        this.videoSurfaceComponent = initVideoSurfaceComponent(videoSurfaceComponent, size);
        this.imagePainter = renderCallback == null ? initImagePainter(imagePainter) : null;

        this.mediaPlayer = this.mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
        this.mediaPlayer.videoSurface().setVideoSurface(this.mediaPlayerFactory.videoSurfaces().newVideoSurface(bufferFormatCallback, initRenderCallback(renderCallback, size), lockBuffers));
        this.mediaPlayer.fullScreen().setFullScreenStrategy(fullScreenStrategy);
        this.mediaPlayer.events().addMediaPlayerEventListener(this);

        setBackground(Color.black);
        setLayout(new BorderLayout());
        add(this.videoSurfaceComponent, BorderLayout.CENTER);

        initInputEvents(inputEvents);

        onAfterConstruct();
    }

    public CallbackMediaPlayerComponent(MediaPlayerFactory mediaPlayerFactory, FullScreenStrategy fullScreenStrategy, InputEvents inputEvents, BufferFormatCallback bufferFormatCallback, boolean lockBuffers, Dimension size, CallbackImagePainter imagePainter) {
        this(mediaPlayerFactory, fullScreenStrategy, inputEvents, bufferFormatCallback, lockBuffers, size, null, null, null);
    }

    public CallbackMediaPlayerComponent(MediaPlayerFactory mediaPlayerFactory, FullScreenStrategy fullScreenStrategy, InputEvents inputEvents, BufferFormatCallback bufferFormatCallback, boolean lockBuffers, JComponent videoSurfaceComponent, RenderCallback renderCallback) {
        this(mediaPlayerFactory, fullScreenStrategy, inputEvents, bufferFormatCallback, lockBuffers, null, null, videoSurfaceComponent, renderCallback);
    }

    public CallbackMediaPlayerComponent(MediaPlayerSpecs.CallbackMediaPlayerSpec spec) {
        this(spec.factory, spec.fullScreenStrategy, spec.inputEvents, spec.bufferFormatCallback, spec.lockedBuffers, spec.size, spec.imagePainter, spec.videoSurfaceComponent, spec.renderCallback);
    }

    public CallbackMediaPlayerComponent(Dimension size) {
        this(null, null, null, null, true, size, null, null, null);
    }

    private MediaPlayerFactory initMediaPlayerFactory(MediaPlayerFactory mediaPlayerFactory) {
        if (mediaPlayerFactory == null) {
            mediaPlayerFactory = new MediaPlayerFactory(DEFAULT_FACTORY_ARGUMENTS);
        }
        return mediaPlayerFactory;
    }

    private JComponent initVideoSurfaceComponent(JComponent videoSurfaceComponent, Dimension size) {
        if (videoSurfaceComponent == null) {
            videoSurfaceComponent = new DefaultVideoSurfaceComponent();
            if (size != null) {
                videoSurfaceComponent.setPreferredSize(new Dimension(size));
            }
        }
        return videoSurfaceComponent;
    }

    private CallbackImagePainter initImagePainter(CallbackImagePainter imagePainter) {
        if (imagePainter == null) {
            imagePainter = new ScaledCallbackImagePainter();
        }
        return imagePainter;
    }

    private RenderCallback initRenderCallback(RenderCallback renderCallback, Dimension size) {
        if (renderCallback == null) {
            image = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(size.width, size.height);
            renderCallback = new DefaultRenderCallbackAdapter();
        }
        return renderCallback;
    }

    private void initInputEvents(InputEvents inputEvents) {
        if (inputEvents == null) {
            inputEvents = RuntimeUtil.isNix() || RuntimeUtil.isMac() ? InputEvents.DEFAULT : InputEvents.DISABLE_NATIVE;
        }
        switch (inputEvents) {
            case NONE:
                break;
            case DISABLE_NATIVE:
                mediaPlayer.input().setEnableKeyInputHandling(false);
                mediaPlayer.input().setEnableMouseInputHandling(false);
                // Case fall-through is by design
            case DEFAULT:
                videoSurfaceComponent.addMouseListener(this);
                videoSurfaceComponent.addMouseMotionListener(this);
                videoSurfaceComponent.addMouseWheelListener(this);
                videoSurfaceComponent.addKeyListener(this);
                break;
        }
    }

    /**
     * Get the embedded media player reference.
     * <p>
     * An application uses this handle to control the media player, add listeners and so on.
     *
     * @return media player
     */
    public final EmbeddedMediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    /**
     * Get the video surface {@link Canvas} component.
     * <p>
     * An application may want to add key/mouse listeners to the video surface component.
     *
     * @return video surface component
     */
    public final JComponent getVideoSurfaceComponent() {
        return videoSurfaceComponent;
    }

    /**
     * Release the media player component and the associated native media player resources.
     * <p>
     * The associated media player factory will <em>not</em> be released, the client
     * application is responsible for releasing the factory at the appropriate time.
     */
    public final void release() {
        onBeforeRelease();

        // It is safe to remove listeners like this even if none were added (depends on configured InputEvents in the
        // constructor)
        videoSurfaceComponent.removeMouseListener(this);
        videoSurfaceComponent.removeMouseMotionListener(this);
        videoSurfaceComponent.removeMouseWheelListener(this);
        videoSurfaceComponent.removeKeyListener(this);

        mediaPlayer.release();

        if (ownFactory) {
            mediaPlayerFactory.release();
        }

        onAfterRelease();
    }

    /**
     * Get the media player factory reference.
     *
     * @return media player factory
     */
    public final MediaPlayerFactory getMediaPlayerFactory() {
        return mediaPlayerFactory;
    }

    /**
     *
     */
    private class DefaultVideoSurfaceComponent extends JPanel {

        private DefaultVideoSurfaceComponent() {
            setBackground(Color.black);
            setIgnoreRepaint(true); // FIXME dunno?
        }

        @Override
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D)g;

            int width = getWidth();
            int height = getHeight();

            g2.setColor(getBackground());
            g2.fillRect(0, 0,width, height);

            imagePainter.prepare(g2);
            imagePainter.paint(g2, width, height, image);

            onDrawOverlay(g2);
        }
    }

    /**
     *
     */
    private class DefaultRenderCallbackAdapter extends RenderCallbackAdapter {

        private DefaultRenderCallbackAdapter() {
            super (((DataBufferInt) image.getRaster().getDataBuffer()).getData());
        }

        @Override
        protected void onDisplay(MediaPlayer mediaPlayer, int[] rgbBuffer) {
            videoSurfaceComponent.repaint();
        }

    }

    protected void onDrawOverlay(Graphics2D g2) {
    }

}