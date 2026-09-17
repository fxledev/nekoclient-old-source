package dev.neko.client.hud.components;

import dev.neko.client.hud.HudComponent;
import dev.neko.client.module.Modules;
import dev.neko.client.render.nanovg.NVGImages;
import dev.neko.client.render.nanovg.NVGRenderer;
import dev.neko.client.spotify.SpotifyService;
import dev.neko.client.spotify.SpotifyState;
import dev.neko.client.theme.Theme;
import dev.neko.client.theme.ThemeManager;
import dev.neko.client.util.Colors;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.system.MemoryStack;

public class SpotifyHud extends HudComponent {
   public static final float WIDTH = 252.0F;
   public static final float HEIGHT = 74.0F;
   private static final float ART = 54.0F;
   private static final float TEXT_X = 76.0F;
   private final Modules.SpotifyModule module;
   private final SpotifyService service;
   private final ThemeManager themes;

   public SpotifyHud(Modules.SpotifyModule module, SpotifyService service, ThemeManager themes) {
      super("spotify", 0.62F, 0.86F, module::isEnabled);
      this.module = module;
      this.service = service;
      this.themes = themes;
      this.setScale(1.2F);
   }

   private SpotifyState state() {
      return this.service.state();
   }

   @Override
   public float measureWidth(NVGRenderer vg) {
      return 252.0F;
   }

   @Override
   public float measureHeight(NVGRenderer vg) {
      return 74.0F;
   }

   @Override
   public void render(NVGRenderer vg, float x, float y, float w, float h) {
      SpotifyState state = this.state();
      Theme theme = this.themes.current();
      if (state.active()) {
         vg.glow(x, y, w, h, 13.0F, 8.0F, Colors.withAlpha(-16777216, 0.3F));
         vg.rect(x, y, w, h, 10.0F, Colors.withAlpha(theme.background(), 0.94F));
         vg.rectOutline(x, y, w, h, 10.0F, 1.5F, -1);
         float ax = x + 10.0F;
         float ay = y + 10.0F;
         int art = NVGImages.fromFile(this.service.artPath(), state.artVersion());
         if (art > 0) {
            this.drawRoundedImage(vg, art, ax, ay, 54.0F, 8.0F);
         } else {
            this.drawVinyl(vg, theme, ax, ay, 54.0F);
         }

         if (!state.active()) {
            vg.text("Nothing playing", x + 76.0F, y + h / 2.0F, 13.5F, theme.textMuted());
         } else {
            float textWidth = w - 88.0F;
            vg.textTruncated(state.title(), x + 76.0F, y + 20.0F, 14.0F, theme.textPrimary(), textWidth);
            vg.textTruncated(state.artist(), x + 76.0F, y + 38.0F, 11.5F, theme.textMuted(), textWidth);
            float barX = x + 76.0F;
            float barW = w - 76.0F - 12.0F;
            float barY = y + 55.0F;
            float frac = state.durMs() > 0L ? Math.clamp((float)state.livePosMs() / (float)state.durMs(), 0.0F, 1.0F) : 0.0F;
            vg.text(time(state.livePosMs()), barX, y + 49.0F, 9.5F, theme.textDisabled());
            String total = time(state.durMs());
            vg.text(total, barX + barW - vg.textWidth(total, 9.5F), y + 49.0F, 9.5F, theme.textDisabled());
            vg.rect(barX, barY, barW, 4.0F, 2.0F, Colors.withAlpha(-16777216, 0.6F));
            vg.rectGradient(barX, barY, Math.max(4.0F, barW * frac), 4.0F, 2.0F, theme.accent(), theme.accentBright(), false);
            float visualizerY = y + 68.0F;
            float visualizerWidth = barW / 18.0F;
            float visualizerTime = (float)System.nanoTime() / 1.8E8F;

            for (int i = 0; i < 18; i++) {
               float wave = 0.35F + 0.65F * Math.abs((float)Math.sin(visualizerTime + i * 0.72F));
               float height = state.playing() ? 2.0F + wave * 6.0F : 2.0F;
               float vx = barX + i * visualizerWidth;
               vg.rect(
                  vx,
                  visualizerY - height,
                  Math.max(1.5F, visualizerWidth - 2.0F),
                  height,
                  1.0F,
                  Colors.withAlpha(theme.accentBright(), state.playing() ? 0.82F : 0.35F)
               );
            }
         }
      }
   }

   private void drawSpeaker(NVGRenderer vg, float cx, float cy, int color, boolean muted) {
      long ctx = vg.ctx();
      MemoryStack stack = MemoryStack.stackPush();

      try {
         NVGColor col = NanoVG.nvgRGBA(
            (byte)Colors.red(color), (byte)Colors.green(color), (byte)Colors.blue(color), (byte)Colors.alpha(color), NVGColor.malloc(stack)
         );
         vg.rect(cx - 7.0F, cy - 3.0F, 5.0F, 6.0F, 1.0F, color);
         NanoVG.nvgFillColor(ctx, col);
         NanoVG.nvgBeginPath(ctx);
         NanoVG.nvgMoveTo(ctx, cx - 6.0F, cy);
         NanoVG.nvgLineTo(ctx, cx + 2.0F, cy - 7.0F);
         NanoVG.nvgLineTo(ctx, cx + 2.0F, cy + 7.0F);
         NanoVG.nvgClosePath(ctx);
         NanoVG.nvgFill(ctx);
         NanoVG.nvgStrokeColor(ctx, col);
         NanoVG.nvgStrokeWidth(ctx, 1.7F);
         NanoVG.nvgLineCap(ctx, 1);
         if (muted) {
            NanoVG.nvgBeginPath(ctx);
            NanoVG.nvgMoveTo(ctx, cx + 5.0F, cy - 3.5F);
            NanoVG.nvgLineTo(ctx, cx + 10.0F, cy + 3.5F);
            NanoVG.nvgMoveTo(ctx, cx + 10.0F, cy - 3.5F);
            NanoVG.nvgLineTo(ctx, cx + 5.0F, cy + 3.5F);
            NanoVG.nvgStroke(ctx);
         } else {
            for (float r : new float[]{4.5F, 8.0F}) {
               NanoVG.nvgBeginPath(ctx);
               NanoVG.nvgArc(ctx, cx + 2.0F, cy, r, -0.6F, 0.6F, 2);
               NanoVG.nvgStroke(ctx);
            }
         }
      } catch (Throwable var151) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var14) {
               var151.addSuppressed(var14);
            }
         }

         throw var151;
      }

      if (stack != null) {
         stack.close();
      }
   }

   private static String time(long ms) {
      long s = ms / 1000L;
      return String.format("%d:%02d", s / 60L, s % 60L);
   }

   private void drawRoundedImage(NVGRenderer vg, int image, float x, float y, float size, float radius) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         long ctx = vg.ctx();
         NVGPaint paint = NVGPaint.malloc(stack);
         NanoVG.nvgImagePattern(ctx, x, y, size, size, 0.0F, image, 1.0F, paint);
         NanoVG.nvgBeginPath(ctx);
         NanoVG.nvgRoundedRect(ctx, x, y, size, size, radius);
         NanoVG.nvgFillPaint(ctx, paint);
         NanoVG.nvgFill(ctx);
      } catch (Throwable var121) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var11) {
               var121.addSuppressed(var11);
            }
         }

         throw var121;
      }

      if (stack != null) {
         stack.close();
      }
   }

   private void drawVinyl(NVGRenderer vg, Theme theme, float x, float y, float size) {
      float cx = x + size / 2.0F;
      float cy = y + size / 2.0F;
      vg.rect(x, y, size, size, 8.0F, Colors.withAlpha(-16119795, 0.9F));
      vg.circle(cx, cy, size * 0.4F, -15330789);
      vg.circleOutline(cx, cy, size * 0.3F, 1.0F, Colors.withAlpha(theme.accent(), 0.35F));
      vg.circleOutline(cx, cy, size * 0.22F, 1.0F, Colors.withAlpha(theme.accent(), 0.25F));
      vg.circle(cx, cy, size * 0.12F, theme.accent());
      vg.textGradient("LC", cx - vg.textWidth("LC", 9.0F) / 2.0F, cy, 9.0F, -1, -1122834);
   }

   private void drawPrev(NVGRenderer vg, float cx, float cy, int color) {
      this.triangle(vg, cx + 4.0F, cy, -7.0F, color);
      vg.rect(cx - 7.0F, cy - 5.5F, 2.0F, 11.0F, 1.0F, color);
   }

   private void drawNext(NVGRenderer vg, float cx, float cy, int color) {
      this.triangle(vg, cx - 4.0F, cy, 7.0F, color);
      vg.rect(cx + 5.0F, cy - 5.5F, 2.0F, 11.0F, 1.0F, color);
   }

   private void drawPlayPause(NVGRenderer vg, float cx, float cy, int color, boolean playing) {
      vg.circleOutline(cx, cy, 10.0F, 1.4F, color);
      if (playing) {
         vg.rect(cx - 3.5F, cy - 4.5F, 2.4F, 9.0F, 1.2F, color);
         vg.rect(cx + 1.1F, cy - 4.5F, 2.4F, 9.0F, 1.2F, color);
      } else {
         this.triangle(vg, cx - 2.5F, cy, 7.0F, color);
      }
   }

   private void triangle(NVGRenderer vg, float x, float cy, float dir, int color) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         long ctx = vg.ctx();
         NanoVG.nvgBeginPath(ctx);
         NanoVG.nvgMoveTo(ctx, x, cy - 5.5F);
         NanoVG.nvgLineTo(ctx, x, cy + 5.5F);
         NanoVG.nvgLineTo(ctx, x + dir, cy);
         NanoVG.nvgClosePath(ctx);
         NanoVG.nvgFillColor(
            ctx,
            NanoVG.nvgRGBA((byte)Colors.red(color), (byte)Colors.green(color), (byte)Colors.blue(color), (byte)Colors.alpha(color), NVGColor.malloc(stack))
         );
         NanoVG.nvgFill(ctx);
      } catch (Throwable var101) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var9) {
               var101.addSuppressed(var9);
            }
         }

         throw var101;
      }

      if (stack != null) {
         stack.close();
      }
   }

   @Override
   public boolean onEditClick(float lx, float ly) {
      SpotifyState state = this.state();
      if (state.active() && state.canSeek() && state.durMs() > 0L) {
         float barX = 76.0F;
         float barW = 164.0F;
         if (ly >= 52.0F && ly <= 66.0F && lx >= barX && lx <= barX + barW) {
            long target = (long)((lx - barX) / barW * (float)state.durMs());
            this.service.seekTo(target);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean hit(float lx, float centerX) {
      return Math.abs(lx - centerX) <= 11.0F;
   }
}
