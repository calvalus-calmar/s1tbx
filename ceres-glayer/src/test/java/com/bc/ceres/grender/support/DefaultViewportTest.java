package com.bc.ceres.grender.support;

import static com.bc.ceres.glayer.Assert2D.assertEquals;
import com.bc.ceres.grender.Viewport;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import org.junit.Test;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class DefaultViewportTest {

    @Test
    public void testDefaultSettings() {
        final DefaultViewport viewport = new DefaultViewport();
        assertEquals(new AffineTransform(), viewport.getModelToViewTransform());
        assertEquals(new AffineTransform(), viewport.getViewToModelTransform());
        assertEquals(p(0.0, 0.0), getModelOffset(viewport));
        assertEquals(1.0, viewport.getZoomFactor());
    }

    @Test
    public void testTransformsAreNotLife() {
        final DefaultViewport viewport = new DefaultViewport();

        final AffineTransform m2v = viewport.getModelToViewTransform();
        assertNotSame(m2v, viewport.getModelToViewTransform());

        final AffineTransform v2u = viewport.getViewToModelTransform();
        assertNotSame(v2u, viewport.getViewToModelTransform());

        viewport.move(150.0, -10.0);

        assertNotSame(m2v, viewport.getModelToViewTransform());
        assertNotSame(v2u, viewport.getViewToModelTransform());
    }

    @Test
    public void testInverse() {
        final DefaultViewport viewport = new DefaultViewport();

        final AffineTransform m2v = viewport.getModelToViewTransform();
        assertNotSame(m2v, viewport.getModelToViewTransform());

        final AffineTransform v2u = viewport.getViewToModelTransform();
        assertNotSame(v2u, viewport.getViewToModelTransform());

        viewport.move(150.0, -10.0);

        assertEquals(p(-150, 10), t(viewport.getViewToModelTransform(), p(0, 0)));
        assertEquals(p(0, 0), t(viewport.getModelToViewTransform(), p(-150, 10)));

        assertEquals(p(150, -10), t(viewport.getModelToViewTransform(), p(0, 0)));
        assertEquals(p(0.0, 0.0), t(viewport.getViewToModelTransform(), p(150, -10)));
    }

    @Test
    public void testMove() {
        final DefaultViewport viewport = new DefaultViewport();
        viewport.move(15.0, 10.0);
        assertEquals(p(-15.0, -10.0), getModelOffset(viewport));
        viewport.move(-15.0, -10.0);
        assertEquals(p(0.0, 0.0), getModelOffset(viewport));
    }

    @Test
    public void testZooming() {
        final DefaultViewport viewport = new DefaultViewport();
        Point2D vc, uc;
        viewport.move(-10, -10);

        /////////////////////////////
        // view center 1

        vc = p(15, 10);
        uc = t(viewport.getViewToModelTransform(), vc);

        viewport.setZoomFactor(0.5, vc);

        assertEquals(0.5, viewport.getZoomFactor(), 1e-10);
        assertEquals(p(-5.0, 0.0), getModelOffset(viewport));
        assertEquals(uc, t(viewport.getViewToModelTransform(), vc));

        viewport.setZoomFactor(2, vc);
        assertEquals(2, viewport.getZoomFactor(), 1e-10);
        assertEquals(p(17.5, 15.0), getModelOffset(viewport));
        assertEquals(uc, t(viewport.getViewToModelTransform(), vc));

        /////////////////////////////
        // view center 2

        vc = p(50, 25);
        uc = t(viewport.getViewToModelTransform(), vc);

        viewport.setZoomFactor(1 / 1.2, vc);

        assertEquals(1 / 1.2, viewport.getZoomFactor(), 1e-10);
        assertEquals(p(-17.5, -2.5), getModelOffset(viewport));
        assertEquals(uc, t(viewport.getViewToModelTransform(), vc));

        viewport.setZoomFactor(1 / 0.8, vc);

        assertEquals(1 / 0.8, viewport.getZoomFactor(), 1e-10);
        assertEquals(p(2.5, 7.5), getModelOffset(viewport));
        assertEquals(uc, t(viewport.getViewToModelTransform(), vc));
    }

    @Test
    public void testRelativeZoomWithAffineTransform() {
        Point2D vc; // zoom center in view CS
        final Point2D v0 = p(0, 0);
        Point2D u0;

        AffineTransform v2u = new AffineTransform(); // view to model CS transformation
        v2u.translate(10, 10); // (10,10) are the model coordinates at (0,0) in view coordinates
        u0 = t(v2u, v0);

        assertEquals(1.0, v2u.getScaleX(), 1.0e-10);
        assertEquals(1.0, v2u.getScaleY(), 1.0e-10);
        assertEquals(p(10, 10), u0);

        /////////////////////////////
        // view center 1

        vc = p(15, 10);
        assertEquals(p(25.0, 20.0), t(v2u, vc));

        zoom(v2u, vc, 2.0);

        assertEquals(p(25.0, 20.0), t(v2u, vc));
        assertEquals(2.0, v2u.getScaleX(), 1.0e-10);
        assertEquals(2.0, v2u.getScaleY(), 1.0e-10);
        assertEquals(p(-5.0, 0.0), t(v2u, v0));

        zoom(v2u, vc, 0.5);

        assertEquals(p(25.0, 20.0), t(v2u, vc));
        assertEquals(0.5, v2u.getScaleX(), 1.0e-10);
        assertEquals(0.5, v2u.getScaleY(), 1.0e-10);
        assertEquals(p(17.5, 15.0), t(v2u, v0));

        /////////////////////////////
        // view center 2

        vc = p(50, 25);
        assertEquals(p(42.5, 27.5), t(v2u, vc));

        zoom(v2u, vc, 1.2);

        assertEquals(p(42.5, 27.5), t(v2u, vc));
        assertEquals(1.2, v2u.getScaleX(), 1.0e-10);
        assertEquals(1.2, v2u.getScaleY(), 1.0e-10);
        assertEquals(p(-17.5, -2.5), t(v2u, v0));

        zoom(v2u, vc, 0.8);

        assertEquals(p(42.5, 27.5), t(v2u, vc));
        assertEquals(0.8, v2u.getScaleX(), 1.0e-10);
        assertEquals(0.8, v2u.getScaleY(), 1.0e-10);
        assertEquals(p(2.5, 7.5), t(v2u, v0));

    }

    // V0 = {0,0}
    // U0 = T x V0
    // Uc = T x Vc
    // U0' = Uc - s'/s * (Uc - U0)
    // U0' = T' x V0
    // --> T x ((1 - s'/s) * Vc) = T' x V0
    //
    private static void zoom(AffineTransform t, Point2D vc, double s) {
        final double m00 = t.getScaleX();
        final double m10 = t.getShearY();
        final double m01 = t.getShearX();
        final double m11 = t.getScaleY();
        final double sx = Math.sqrt(m00 * m00 + m10 * m10);
        final double sy = Math.sqrt(m01 * m01 + m11 * m11);
        t.translate(vc.getX(), vc.getY());
        t.scale(s / sx, s / sy);
        t.translate(-vc.getX(), -vc.getY());
    }


    private static Point2D t(AffineTransform t, Point2D p) {
        return t.transform(p, null);
    }

    static Point2D p(double x, double y) {
        return new Point2D.Double(x, y);
    }

    public static Point2D getModelOffset(Viewport vp) {
        return vp.getViewToModelTransform().transform(new Point(0, 0), null);
    }

}
