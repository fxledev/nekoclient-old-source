package dev.neko.client.render;

import dev.neko.client.NekoClient;
import dev.neko.client.hud.HudDragController;
import dev.neko.client.hud.HudManager;
import dev.neko.client.module.impl.MotionBlurModule;
import dev.neko.client.notification.NotificationManager;
import dev.neko.client.render.nanovg.GlStateSnapshot;
import dev.neko.client.render.nanovg.NVGRenderer;
import dev.neko.client.theme.Theme;
import dev.neko.client.util.Colors;
import net.minecraft.class_10868;
import net.minecraft.class_276;
import net.minecraft.class_310;
import net.minecraft.class_408;
import org.lwjgl.opengl.GL33C;

public final class OverlayRenderer {
   private static HudManager hudManager;
   private static int fbo = -1;
   private static boolean crashed;

   private OverlayRenderer() {
   }

   public static void init(HudManager hud, NotificationManager toasts) {
      hudManager = hud;
   }

   public static float uiScale() {
      class_310 mc = class_310.method_1551();
      return Math.max(1.0F, mc.method_22683().method_4506() / 1080.0F);
   }

   public static void render() {
      if (!crashed && hudManager != null) {
         class_310 mc = class_310.method_1551();
         class_276 target = mc.method_1522();
         if (target != null && target.method_30277() instanceof class_10868 colorTexture) {
            boolean var22 = mc.field_1755 instanceof NvgDrawable;
            boolean chatEditing = mc.field_1755 instanceof class_408;
            boolean drawHud = !mc.field_1690.field_1842 && mc.field_1687 != null && !var22;
            MotionBlurModule mbModule = NekoClient.modules() != null ? NekoClient.modules().motionBlur : null;
            boolean motionBlur = mbModule != null && mbModule.isEnabled() && mc.field_1687 != null && !var22;
            if (!var22 && !drawHud && !motionBlur) {
               return;
            }

            try {
               GlStateSnapshot state = GlStateSnapshot.capture();

               try {
                  if (!bindOverlayFbo(colorTexture, target.field_1482, target.field_1481)) {
                     return;
                  }

                  NVGRenderer vg = NVGRenderer.get();
                  if (motionBlur) {
                     MotionBlurRenderer.render(vg, target.field_1482, target.field_1481, mbModule);
                  }

                  if (vg.hasFont()) {
                     float scale = uiScale();
                     float uiWidth = target.field_1482 / scale;
                     float uiHeight = target.field_1481 / scale;
                     vg.beginFrame(target.field_1482, target.field_1481, 1.0F);
                     vg.save();
                     vg.scale(scale);
                     if (drawHud) {
                        if (chatEditing && HudDragController.isDragging()) {
                           HudDragController.updateDrag(vg);
                        }

                        if (mc.field_1755 == null) {
                           WorldNametagRenderer.render(vg);
                        }

                        hudManager.render(vg, uiWidth, uiHeight);
                        if (chatEditing) {
                           renderChatEditOverlay(vg, uiWidth, uiHeight);
                        }
                     }

                     if (var22) {
                        ((NvgDrawable)mc.field_1755).renderNvg(vg, uiMouseX(), uiMouseY(), uiWidth, uiHeight);
                     }

                     vg.restore();
                     vg.endFrame();
                     return;
                  }
               } finally {
                  state.restore();
               }

               return;
            } catch (Throwable var181) {
               crashed = true;
               return;
            }
         }
      }
   }

   private static void renderChatEditOverlay(NVGRenderer vg, float uiWidth, float uiHeight) {
      Theme theme = NekoClient.themes().current();
      float mx = uiMouseX();
      float my = uiMouseY();

      for (HudManager.Placement p : hudManager.layout(vg, uiWidth, uiHeight, true)) {
         boolean hidden = !p.component().visible();
         if (!p.contains(mx, my) && p.component() != HudDragController.draggedComponent()) {
            boolean var13 = false;
         } else {
            boolean var9 = true;
         }

         if (hidden) {
            vg.save();
            vg.alpha(1.0F);
            hudManager.renderPlacement(vg, p);
            vg.restore();
         }
      }

      if (HudDragController.isDragging()) {
         int guide = Colors.withAlpha(theme.textPrimary(), 0.55F);
         if (HudDragController.snapCenterX()) {
            vg.line(uiWidth / 2.0F, 0.0F, uiWidth / 2.0F, uiHeight, 1.0F, guide);
         } else if (HudDragController.snapEdgeX()) {
            float x = mx < uiWidth / 2.0F ? 0.0F : uiWidth;
            vg.line(x, 0.0F, x, uiHeight, 1.0F, guide);
         }

         if (HudDragController.snapCenterY()) {
            vg.line(0.0F, uiHeight / 2.0F, uiWidth, uiHeight / 2.0F, 1.0F, guide);
         } else if (HudDragController.snapEdgeY()) {
            float y = my < uiHeight / 2.0F ? 0.0F : uiHeight;
            vg.line(0.0F, y, uiWidth, y, 1.0F, guide);
         }
      }
   }

   private static boolean bindOverlayFbo(class_10868 colorTexture, int width, int height) {
      if (fbo == -1) {
         fbo = GL33C.glGenFramebuffers();
      }

      GL33C.glBindFramebuffer(36160, fbo);
      GL33C.glFramebufferTexture2D(36160, 36064, 3553, colorTexture.method_68427(), 0);
      if (GL33C.glCheckFramebufferStatus(36160) != 36053) {
         return false;
      } else {
         GL33C.glViewport(0, 0, width, height);
         GL33C.glDisable(3089);
         return true;
      }
   }

   public static float uiMouseX() {
      class_310 mc = class_310.method_1551();
      return (float)(mc.field_1729.method_1603() * mc.method_22683().method_4489() / Math.max(1, mc.method_22683().method_4480())) / uiScale();
   }

   public static float uiMouseY() {
      class_310 mc = class_310.method_1551();
      return (float)(mc.field_1729.method_1604() * mc.method_22683().method_4506() / Math.max(1, mc.method_22683().method_4507())) / uiScale();
   }

   public static float guiToUi(double guiCoord) {
      class_310 mc = class_310.method_1551();
      float pixels = (float)(guiCoord * mc.method_22683().method_4495());
      return pixels / uiScale();
   }

   public static float uiWidth() {
      class_310 mc = class_310.method_1551();
      return mc.method_22683().method_4489() / uiScale();
   }

   public static float uiHeight() {
      class_310 mc = class_310.method_1551();
      return mc.method_22683().method_4506() / uiScale();
   }
}
