package com.neusoft.oddc.multimedia.gles;

import android.graphics.SurfaceTexture;
import android.view.Surface;

/**
 * Recordable EGL window surface.
 * <p/>
 * It's good practice to explicitly release() the surface, preferably from a "finally" block.
 */
public class WindowSurface extends EglSurfaceBase {

    private static final String TAG = WindowSurface.class.getSimpleName();

    private Surface surface;

    /**
     * Associates an EGL surface with the native window surface.
     */
    public WindowSurface(EglCore eglCore, Surface surface) {
        super(eglCore);
        createWindowSurface(surface);
        this.surface = surface;
    }

    /**
     * Associates an EGL surface with the SurfaceTexture.
     */
    public WindowSurface(EglCore eglCore, SurfaceTexture surfaceTexture) {
        super(eglCore);
        createWindowSurface(surfaceTexture);
    }

    /**
     * Releases any resources associated with the EGL surface (and, if configured to do so,
     * with the Surface as well).
     * <p/>
     * Does not require that the surface's EGL context be current.
     */
    public void release() {
        releaseEglSurface();
        if (surface != null) {
            surface.release();
            surface = null;
        }
    }

    /**
     * Recreate the EGLSurface, using the new EglBase.  The caller should have already
     * freed the old EGLSurface with releaseEglSurface().
     * <p/>
     * This is useful when we want to update the EGLSurface associated with a Surface.
     * For example, if we want to share with a different EGLContext, which can only
     * be done by tearing down and recreating the context.  (That's handled by the caller;
     * this just creates a new EGLSurface for the Surface we were handed earlier.)
     * <p/>
     * If the previous EGLSurface isn't fully destroyed, e.g. it's still current on a
     * context somewhere, the create call will fail with complaints from the Surface
     * about already being connected.
     */
    public void recreate(EglCore newEglCore) {
        if (surface == null) {
            throw new RuntimeException("not yet implemented for SurfaceTexture");
        }
        eglCore = newEglCore; // switch to new context
        createWindowSurface(surface); // create new surface
    }
}
