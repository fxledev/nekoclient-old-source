package dev.neko.client.hud;

import dev.neko.client.render.OverlayRenderer;
import dev.neko.client.render.nanovg.NVGRenderer;
import net.minecraft.class_310;
import net.minecraft.class_408;

public final class HudDragController {
   private static HudComponent dragging;
   private static float grabDx;
   private static float grabDy;
   private static boolean snapCenterX;
   private static boolean snapCenterY;
   private static boolean snapEdgeX;
   private static boolean snapEdgeY;

   private HudDragController() {
   }

   public static boolean isEditing() {
      return class_310.method_1551().field_1755 instanceof class_408;
   }

   public static boolean isDragging() {
      return dragging != null;
   }

   public static boolean tryStartDrag(HudManager hud) {
      NVGRenderer vg = NVGRenderer.get();
      float mx = OverlayRenderer.uiMouseX();
      float my = OverlayRenderer.uiMouseY();

      for (HudManager.Placement p : hud.layout(vg, OverlayRenderer.uiWidth(), OverlayRenderer.uiHeight(), true)) {
         if (p.contains(mx, my)) {
            float scale = p.component().getScale();
            if (p.component().onEditClick((mx - p.x()) / scale, (my - p.y()) / scale)) {
               return true;
            }

            dragging = p.component();
            grabDx = mx - p.x();
            grabDy = my - p.y();
            return true;
         }
      }

      return false;
   }

   public static boolean tryResize(HudManager hud, double scrollY) {
      NVGRenderer vg = NVGRenderer.get();
      float mx = OverlayRenderer.uiMouseX();
      float my = OverlayRenderer.uiMouseY();

      for (HudManager.Placement p : hud.layout(vg, OverlayRenderer.uiWidth(), OverlayRenderer.uiHeight(), true)) {
         if (p.contains(mx, my)) {
            HudComponent component = p.component();
            component.setScale(component.getScale() + (float)scrollY * 0.06F);
            return true;
         }
      }

      return false;
   }

   public static void updateDrag(NVGRenderer vg) {
      if (dragging != null) {
         float uiWidth = OverlayRenderer.uiWidth();
         float uiHeight = OverlayRenderer.uiHeight();
         float scale = dragging.getScale();
         float w = dragging.measureWidth(vg) * scale;
         float h = dragging.measureHeight(vg) * scale;
         float freeW = Math.max(1.0F, uiWidth - w);
         float freeH = Math.max(1.0F, uiHeight - h);
         float x = OverlayRenderer.uiMouseX() - grabDx;
         float y = OverlayRenderer.uiMouseY() - grabDy;
         float centerX = uiWidth / 2.0F - w / 2.0F;
         float centerY = uiHeight / 2.0F - h / 2.0F;
         snapCenterX = Math.abs(x + w / 2.0F - uiWidth / 2.0F) <= 8.0F;
         snapCenterY = Math.abs(y + h / 2.0F - uiHeight / 2.0F) <= 8.0F;
         snapEdgeX = Math.abs(x) <= 8.0F || Math.abs(x + w - uiWidth) <= 8.0F;
         snapEdgeY = Math.abs(y) <= 8.0F || Math.abs(y + h - uiHeight) <= 8.0F;
         if (snapCenterX) {
            x = centerX;
         }

         if (snapCenterY) {
            y = centerY;
         }

         if (Math.abs(x) <= 8.0F) {
            x = 0.0F;
         }

         if (Math.abs(x + w - uiWidth) <= 8.0F) {
            x = uiWidth - w;
         }

         if (Math.abs(y) <= 8.0F) {
            y = 0.0F;
         }

         if (Math.abs(y + h - uiHeight) <= 8.0F) {
            y = uiHeight - h;
         }

         dragging.setPosition(x / freeW, y / freeH);
      }
   }

   public static void stopDrag() {
      dragging = null;
      snapCenterX = false;
      snapCenterY = false;
      snapEdgeX = false;
      snapEdgeY = false;
   }

   public static HudComponent draggedComponent() {
      return dragging;
   }

   public static boolean snapCenterX() {
      return dragging != null && snapCenterX;
   }

   public static boolean snapCenterY() {
      return dragging != null && snapCenterY;
   }

   public static boolean snapEdgeX() {
      return dragging != null && snapEdgeX;
   }

   public static boolean snapEdgeY() {
      return dragging != null && snapEdgeY;
   }
}
