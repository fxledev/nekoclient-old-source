package dev.neko.client.render.nanovg;

import dev.neko.client.util.Colors;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public final class NVGRenderer {
   public static final String FONT_INTER = "inter";
   public static final String FONT_NEKO = "neko";
   private static NVGRenderer instance;
   private final long ctx = NanoVGGL3.nvgCreate(1);
   private boolean interLoaded;
   private boolean nekoLoaded;
   private final List<ByteBuffer> retainedFontData = new ArrayList<>();
   private String activeFont = "inter";
   private final ArrayDeque<Float> alphaStack = new ArrayDeque<>();
   private float appliedAlpha = 1.0F;

   private NVGRenderer() {
      if (this.ctx == 0L) {
         throw new IllegalStateException("Failed to create NanoVG context");
      } else {
         this.interLoaded = this.loadFont("inter", "assets/nekoclient/fonts/Inter-Bold.ttf");
         this.nekoLoaded = this.loadFont("neko", "assets/nekoclient/fonts/NekoScript.ttf");
      }
   }

   public static NVGRenderer get() {
      if (instance == null) {
         instance = new NVGRenderer();
      }

      return instance;
   }

   public long ctx() {
      return this.ctx;
   }

   private boolean loadFont(String name, String resourcePath) {
      try {
         boolean var12;
         try (InputStream in = NVGRenderer.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
               return false;
            }

            byte[] bytes = in.readAllBytes();
            ByteBuffer buffer = MemoryUtil.memAlloc(bytes.length);
            buffer.put(bytes).flip();
            int handle = NanoVG.nvgCreateFontMem(this.ctx, name, buffer, false);
            if (handle == -1) {
               MemoryUtil.memFree(buffer);
               return false;
            }

            this.retainedFontData.add(buffer);
            var12 = true;
         }

         return var12;
      } catch (IOException var11) {
         return false;
      }
   }

   public void setFontMode(String mode) {
      this.activeFont = this.interLoaded ? "inter" : null;
   }

   public boolean hasFont() {
      return this.activeFont != null;
   }

   public void beginFrame(float width, float height, float pixelRatio) {
      this.alphaStack.clear();
      this.appliedAlpha = 1.0F;
      NanoVG.nvgBeginFrame(this.ctx, width, height, pixelRatio);
   }

   public void endFrame() {
      NanoVG.nvgEndFrame(this.ctx);
   }

   public void save() {
      this.alphaStack.push(this.appliedAlpha);
      NanoVG.nvgSave(this.ctx);
   }

   public void restore() {
      if (!this.alphaStack.isEmpty()) {
         this.appliedAlpha = this.alphaStack.pop();
      }

      NanoVG.nvgRestore(this.ctx);
   }

   public void scale(float s) {
      NanoVG.nvgScale(this.ctx, s, s);
   }

   public void translate(float x, float y) {
      NanoVG.nvgTranslate(this.ctx, x, y);
   }

   public void alpha(float a) {
      this.appliedAlpha = this.appliedAlpha * Math.clamp(a, 0.0F, 1.0F);
      NanoVG.nvgGlobalAlpha(this.ctx, this.appliedAlpha);
   }

   public void scissor(float x, float y, float w, float h) {
      NanoVG.nvgIntersectScissor(this.ctx, x, y, w, h);
   }

   public void rect(float x, float y, float w, float h, float radius, int argb) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         NanoVG.nvgBeginPath(this.ctx);
         NanoVG.nvgRoundedRect(this.ctx, x, y, w, h, radius);
         NanoVG.nvgFillColor(this.ctx, color(stack, argb));
         NanoVG.nvgFill(this.ctx);
      } catch (Throwable var11) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var10) {
               var11.addSuppressed(var10);
            }
         }

         throw var11;
      }

      if (stack != null) {
         stack.close();
      }
   }

   public void rectVarying(float x, float y, float w, float h, float rtl, float rtr, float rbr, float rbl, int argb) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         NanoVG.nvgBeginPath(this.ctx);
         NanoVG.nvgRoundedRectVarying(this.ctx, x, y, w, h, rtl, rtr, rbr, rbl);
         NanoVG.nvgFillColor(this.ctx, color(stack, argb));
         NanoVG.nvgFill(this.ctx);
      } finally {
         stack.close();
      }
   }

   public void rectGradient(float x, float y, float w, float h, float radius, int from, int to, boolean vertical) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         NVGPaint paint = NVGPaint.malloc(stack);
         float ex = vertical ? x : x + w;
         float ey = vertical ? y + h : y;
         NanoVG.nvgLinearGradient(this.ctx, x, y, ex, ey, color(stack, from), color(stack, to), paint);
         NanoVG.nvgBeginPath(this.ctx);
         NanoVG.nvgRoundedRect(this.ctx, x, y, w, h, radius);
         NanoVG.nvgFillPaint(this.ctx, paint);
         NanoVG.nvgFill(this.ctx);
      } catch (Throwable var141) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var131) {
               var141.addSuppressed(var131);
            }
         }

         throw var141;
      }

      if (stack != null) {
         stack.close();
      }
   }

   public void rectVaryingGradient(float x, float y, float w, float h, float rtl, float rtr, float rbr, float rbl, int from, int to) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         NVGPaint paint = NVGPaint.malloc(stack);
         NanoVG.nvgLinearGradient(this.ctx, x, y, x, y + h, color(stack, from), color(stack, to), paint);
         NanoVG.nvgBeginPath(this.ctx);
         NanoVG.nvgRoundedRectVarying(this.ctx, x, y, w, h, rtl, rtr, rbr, rbl);
         NanoVG.nvgFillPaint(this.ctx, paint);
         NanoVG.nvgFill(this.ctx);
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

   public void chevron(float cx, float cy, float size, float stroke, int argb, boolean pointDown) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         NanoVG.nvgBeginPath(this.ctx);
         if (pointDown) {
            NanoVG.nvgMoveTo(this.ctx, cx - size, cy - size / 2.0F);
            NanoVG.nvgLineTo(this.ctx, cx, cy + size / 2.0F);
            NanoVG.nvgLineTo(this.ctx, cx + size, cy - size / 2.0F);
         } else {
            NanoVG.nvgMoveTo(this.ctx, cx - size / 2.0F, cy - size);
            NanoVG.nvgLineTo(this.ctx, cx + size / 2.0F, cy);
            NanoVG.nvgLineTo(this.ctx, cx - size / 2.0F, cy + size);
         }

         NanoVG.nvgStrokeColor(this.ctx, color(stack, argb));
         NanoVG.nvgStrokeWidth(this.ctx, stroke);
         NanoVG.nvgLineCap(this.ctx, 1);
         NanoVG.nvgLineJoin(this.ctx, 1);
         NanoVG.nvgStroke(this.ctx);
      } catch (Throwable var11) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var10) {
               var11.addSuppressed(var10);
            }
         }

         throw var11;
      }

      if (stack != null) {
         stack.close();
      }
   }

   public void triangle(float x0, float y0, float x1, float y1, float x2, float y2, int argb) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         NanoVG.nvgBeginPath(this.ctx);
         NanoVG.nvgMoveTo(this.ctx, x0, y0);
         NanoVG.nvgLineTo(this.ctx, x1, y1);
         NanoVG.nvgLineTo(this.ctx, x2, y2);
         NanoVG.nvgClosePath(this.ctx);
         NanoVG.nvgFillColor(this.ctx, color(stack, argb));
         NanoVG.nvgFill(this.ctx);
      } catch (Throwable var12) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var11) {
               var12.addSuppressed(var11);
            }
         }

         throw var12;
      }

      if (stack != null) {
         stack.close();
      }
   }

   public void rectOutline(float x, float y, float w, float h, float radius, float stroke, int argb) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         NanoVG.nvgBeginPath(this.ctx);
         NanoVG.nvgRoundedRect(this.ctx, x + stroke / 2.0F, y + stroke / 2.0F, w - stroke, h - stroke, radius);
         NanoVG.nvgStrokeColor(this.ctx, color(stack, argb));
         NanoVG.nvgStrokeWidth(this.ctx, stroke);
         NanoVG.nvgStroke(this.ctx);
      } catch (Throwable var12) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var11) {
               var12.addSuppressed(var11);
            }
         }

         throw var12;
      }

      if (stack != null) {
         stack.close();
      }
   }

   public void rectVaryingOutline(float x, float y, float w, float h, float rtl, float rtr, float rbr, float rbl, float stroke, int argb) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         NanoVG.nvgBeginPath(this.ctx);
         NanoVG.nvgRoundedRectVarying(this.ctx, x + stroke / 2.0F, y + stroke / 2.0F, w - stroke, h - stroke, rtl, rtr, rbr, rbl);
         NanoVG.nvgStrokeColor(this.ctx, color(stack, argb));
         NanoVG.nvgStrokeWidth(this.ctx, stroke);
         NanoVG.nvgStroke(this.ctx);
      } finally {
         stack.close();
      }
   }

   public void glow(float x, float y, float w, float h, float radius, float spread, int argb) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         NVGPaint paint = NVGPaint.malloc(stack);
         NanoVG.nvgBoxGradient(
            this.ctx, x, y, w, h, radius, spread * 2.0F, color(stack, Colors.withAlpha(argb, 0.55F)), color(stack, Colors.withAlpha(argb, 0.0F)), paint
         );
         NanoVG.nvgBeginPath(this.ctx);
         NanoVG.nvgRoundedRect(this.ctx, x - spread, y - spread, w + spread * 2.0F, h + spread * 2.0F, radius + spread);
         NanoVG.nvgFillPaint(this.ctx, paint);
         NanoVG.nvgFill(this.ctx);
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

   public void circle(float cx, float cy, float r, int argb) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         NanoVG.nvgBeginPath(this.ctx);
         NanoVG.nvgCircle(this.ctx, cx, cy, r);
         NanoVG.nvgFillColor(this.ctx, color(stack, argb));
         NanoVG.nvgFill(this.ctx);
      } catch (Throwable var9) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var8) {
               var9.addSuppressed(var8);
            }
         }

         throw var9;
      }

      if (stack != null) {
         stack.close();
      }
   }

   public void circleGlow(float cx, float cy, float r, float spread, int argb) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         NVGPaint paint = NVGPaint.malloc(stack);
         NanoVG.nvgRadialGradient(
            this.ctx, cx, cy, r * 0.25F, r + spread, color(stack, Colors.withAlpha(argb, 0)), color(stack, Colors.withAlpha(argb, 0)), paint
         );
         NanoVG.nvgBeginPath(this.ctx);
         NanoVG.nvgCircle(this.ctx, cx, cy, r + spread);
         NanoVG.nvgFillPaint(this.ctx, paint);
         NanoVG.nvgFill(this.ctx);
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

   public void line(float x1, float y1, float x2, float y2, float width, int argb) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         NanoVG.nvgBeginPath(this.ctx);
         NanoVG.nvgMoveTo(this.ctx, x1, y1);
         NanoVG.nvgLineTo(this.ctx, x2, y2);
         NanoVG.nvgStrokeColor(this.ctx, color(stack, argb));
         NanoVG.nvgStrokeWidth(this.ctx, width);
         NanoVG.nvgLineCap(this.ctx, 1);
         NanoVG.nvgStroke(this.ctx);
      } catch (Throwable var11) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var10) {
               var11.addSuppressed(var10);
            }
         }

         throw var11;
      }

      if (stack != null) {
         stack.close();
      }
   }

   public void checkmark(float x, float y, float size, float stroke, int argb) {
      float x1 = x + size * 0.22F;
      float y1 = y + size * 0.55F;
      float x2 = x + size * 0.42F;
      float y2 = y + size * 0.74F;
      float x3 = x + size * 0.78F;
      float y3 = y + size * 0.3F;
      MemoryStack stack = MemoryStack.stackPush();

      try {
         NanoVG.nvgBeginPath(this.ctx);
         NanoVG.nvgMoveTo(this.ctx, x1, y1);
         NanoVG.nvgLineTo(this.ctx, x2, y2);
         NanoVG.nvgLineTo(this.ctx, x3, y3);
         NanoVG.nvgStrokeColor(this.ctx, color(stack, argb));
         NanoVG.nvgStrokeWidth(this.ctx, stroke);
         NanoVG.nvgLineCap(this.ctx, 1);
         NanoVG.nvgLineJoin(this.ctx, 1);
         NanoVG.nvgStroke(this.ctx);
      } catch (Throwable var16) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var15) {
               var16.addSuppressed(var15);
            }
         }

         throw var16;
      }

      if (stack != null) {
         stack.close();
      }
   }

   public void cross(float x, float y, float size, float stroke, int argb) {
      float pad = size * 0.3F;
      this.line(x + pad, y + pad, x + size - pad, y + size - pad, stroke, argb);
      this.line(x + size - pad, y + pad, x + pad, y + size - pad, stroke, argb);
   }

   public float text(String str, float x, float y, float size, int argb) {
      return this.text(str, x, y, size, argb, this.activeFont);
   }

   public float text(String str, float x, float y, float size, int argb, String font) {
      if (font == null) {
         return 0.0F;
      } else {
         MemoryStack stack = MemoryStack.stackPush();

         float var8;
         try {
            NanoVG.nvgFontFace(this.ctx, font);
            NanoVG.nvgFontSize(this.ctx, size);
            NanoVG.nvgTextAlign(this.ctx, 17);
            NanoVG.nvgFillColor(this.ctx, color(stack, argb));
            var8 = NanoVG.nvgText(this.ctx, x, y, str) - x;
         } catch (Throwable var12) {
            if (stack != null) {
               try {
                  stack.close();
               } catch (Throwable var11) {
                  var12.addSuppressed(var11);
               }
            }

            throw var12;
         }

         if (stack != null) {
            stack.close();
         }

         return var8;
      }
   }

   public float textGradient(String str, float x, float y, float size, int top, int bottom) {
      if (this.activeFont == null) {
         return 0.0F;
      } else {
         MemoryStack stack = MemoryStack.stackPush();

         float var9;
         try {
            NVGPaint paint = NVGPaint.malloc(stack);
            NanoVG.nvgLinearGradient(this.ctx, x, y - size / 2.0F, x, y + size / 2.0F, color(stack, top), color(stack, bottom), paint);
            NanoVG.nvgFontFace(this.ctx, this.activeFont);
            NanoVG.nvgFontSize(this.ctx, size);
            NanoVG.nvgTextAlign(this.ctx, 17);
            NanoVG.nvgFillPaint(this.ctx, paint);
            var9 = NanoVG.nvgText(this.ctx, x, y, str) - x;
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

         return var9;
      }
   }

   public void textGlow(String str, float x, float y, float size, int argb) {
   }

   public float textTruncated(String str, float x, float y, float size, int argb, float maxWidth) {
      if (this.textWidth(str, size) <= maxWidth) {
         return this.text(str, x, y, size, argb);
      } else {
         String cut = str;

         while (cut.length() > 1 && this.textWidth(cut + "…", size) > maxWidth) {
            cut = cut.substring(0, cut.length() - 1);
         }

         return this.text(cut + "…", x, y, size, argb);
      }
   }

   public float textWidth(String str, float size) {
      return this.textWidthFor(this.activeFont, str, size);
   }

   public float textWidthFor(String font, String str, float size) {
      if (font != null && str != null) {
         NanoVG.nvgFontFace(this.ctx, font);
         NanoVG.nvgFontSize(this.ctx, size);
         NanoVG.nvgTextAlign(this.ctx, 17);
         return NanoVG.nvgTextBounds(this.ctx, 0.0F, 0.0F, str, (FloatBuffer)null);
      } else {
         return 0.0F;
      }
   }

   public void rotate(float radians) {
      NanoVG.nvgRotate(this.ctx, radians);
   }

   public void imagePattern(int image, float patternX, float patternY, float patternW, float patternH, float x, float y, float w, float h, float alphaMul) {
      if (image > 0) {
         MemoryStack stack = MemoryStack.stackPush();

         try {
            NVGPaint paint = NVGPaint.malloc(stack);
            NanoVG.nvgImagePattern(this.ctx, patternX, patternY, patternW, patternH, 0.0F, image, alphaMul, paint);
            NanoVG.nvgBeginPath(this.ctx);
            NanoVG.nvgRect(this.ctx, x, y, w, h);
            NanoVG.nvgFillPaint(this.ctx, paint);
            NanoVG.nvgFill(this.ctx);
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
   }

   public void circleOutline(float cx, float cy, float r, float stroke, int argb) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         NanoVG.nvgBeginPath(this.ctx);
         NanoVG.nvgCircle(this.ctx, cx, cy, r);
         NanoVG.nvgStrokeColor(this.ctx, color(stack, argb));
         NanoVG.nvgStrokeWidth(this.ctx, stroke);
         NanoVG.nvgStroke(this.ctx);
      } catch (Throwable var10) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var9) {
               var10.addSuppressed(var9);
            }
         }

         throw var10;
      }

      if (stack != null) {
         stack.close();
      }
   }

   public void image(int image, float x, float y, float w, float h, int tint) {
      if (image > 0) {
         MemoryStack stack = MemoryStack.stackPush();

         try {
            NVGPaint paint = NVGPaint.malloc(stack);
            NanoVG.nvgImagePattern(this.ctx, x, y, w, h, 0.0F, image, 1.0F, paint);
            paint.innerColor(color(stack, tint));
            NanoVG.nvgBeginPath(this.ctx);
            NanoVG.nvgRect(this.ctx, x, y, w, h);
            NanoVG.nvgFillPaint(this.ctx, paint);
            NanoVG.nvgFill(this.ctx);
         } catch (Throwable var111) {
            if (stack != null) {
               try {
                  stack.close();
               } catch (Throwable var10) {
                  var111.addSuppressed(var10);
               }
            }

            throw var111;
         }

         if (stack != null) {
            stack.close();
         }
      }
   }

   public float textScript(String str, float x, float y, float size, int argb) {
      return this.text(str, x, y, size, argb, this.nekoLoaded ? "neko" : this.activeFont);
   }

   public void neuRaised(float x, float y, float w, float h, float r, int base) {
      this.rect(x, y + 6.0F, w, h, r, Colors.withAlpha(-16777216, 0.55F));
      this.rect(x, y + 3.0F, w, h, r, Colors.withAlpha(-16777216, 0.45F));
      this.rect(x, y - 2.0F, w, h, r, Colors.withAlpha(-1, 0.08F));
      this.rect(x, y, w, h, r, base);
      this.line(x + r * 0.5F, y + 1.0F, x + w - r * 0.5F, y + 1.0F, 1.4F, Colors.withAlpha(-1, 0.14F));
      this.line(x + r * 0.5F, y + h - 1.2F, x + w - r * 0.5F, y + h - 1.2F, 1.4F, Colors.withAlpha(-16777216, 0.5F));
   }

   public void neuPressed(float x, float y, float w, float h, float r, int base, int accent) {
      this.rect(x, y + 3.0F, w, h, r, Colors.withAlpha(-16777216, 0.5F));
      this.rect(x, y, w, h, r, Colors.darken(base, 0.22F));
      this.line(x + r * 0.5F, y + 1.5F, x + w - r * 0.5F, y + 1.5F, 2.0F, Colors.withAlpha(-16777216, 0.6F));
      this.line(x + r * 0.5F, y + h - 1.0F, x + w - r * 0.5F, y + h - 1.0F, 1.2F, Colors.withAlpha(accent, 0.45F));
   }

   public void imageTinted(int image, float x, float y, float w, float h, int tint) {
      if (image > 0) {
         MemoryStack stack = MemoryStack.stackPush();

         try {
            NVGPaint paint = NVGPaint.malloc(stack);
            NanoVG.nvgImagePattern(this.ctx, x, y, w, h, 0.0F, image, 1.0F, paint);
            paint.innerColor(color(stack, tint));
            paint.outerColor(color(stack, tint));
            NanoVG.nvgBeginPath(this.ctx);
            NanoVG.nvgRect(this.ctx, x, y, w, h);
            NanoVG.nvgFillPaint(this.ctx, paint);
            NanoVG.nvgFill(this.ctx);
         } catch (Throwable var111) {
            if (stack != null) {
               try {
                  stack.close();
               } catch (Throwable var10) {
                  var111.addSuppressed(var10);
               }
            }

            throw var111;
         }

         if (stack != null) {
            stack.close();
         }
      }
   }

   public void glass(float x, float y, float w, float h, float r) {
      this.rect(x, y + 2.0F, w, h, r, Colors.withAlpha(-16777216, 0.3F));
      this.rect(x, y, w, h, r, Colors.withAlpha(-1, 0.13F));
      this.line(x + r * 0.5F, y + 1.0F, x + w - r * 0.5F, y + 1.0F, 1.2F, Colors.withAlpha(-1, 0.28F));
      this.rectOutline(x, y, w, h, r, 1.0F, Colors.withAlpha(-1, 0.14F));
   }

   public void glassPressed(float x, float y, float w, float h, float r, int accent) {
      this.rect(x, y + 1.5F, w, h, r, Colors.withAlpha(-16777216, 0.35F));
      this.rect(x, y, w, h, r, Colors.withAlpha(accent, 0.22F));
      this.line(x + r * 0.5F, y + 1.5F, x + w - r * 0.5F, y + 1.5F, 1.5F, Colors.withAlpha(-16777216, 0.4F));
      this.rectOutline(x, y, w, h, r, 1.0F, Colors.withAlpha(accent, 0.55F));
   }

   public void iosToggle(float x, float y, float w, float h, boolean on, int accent) {
      float r = h / 2.0F;
      if (on) {
         this.rect(x, y, w, h, r, Colors.withAlpha(accent, 0.95F));
      } else {
         this.rect(x, y, w, h, r, Colors.withAlpha(-1, 0.28F));
      }

      float knobR = h / 2.0F - 2.5F;
      float kx = on ? x + w - r : x + r;
      this.circle(kx + 0.8F, y + r + 1.2F, knobR, Colors.withAlpha(-16777216, 0.35F));
      this.circle(kx, y + r, knobR, -1);
   }

   public float[] imageSize(int image) {
      MemoryStack stack = MemoryStack.stackPush();

      float[] hh;
      try {
         int[] w = new int[1];
         int[] hhx = new int[1];
         NanoVG.nvgImageSize(this.ctx, image, w, hhx);
         return new float[]{w[0], hhx[0]};
      } catch (Throwable var9) {
         hh = new float[]{16.0F, 9.0F};
      } finally {
         stack.close();
      }

      return hh;
   }

   public void imageCover(int image, float x, float y, float w, float h, float radius) {
      if (image > 0) {
         float[] s = this.imageSize(image);
         if (!(s[0] < 1.0F) && !(s[1] < 1.0F)) {
            MemoryStack stack = MemoryStack.stackPush();

            try {
               float scale = Math.max(w / s[0], h / s[1]);
               float dw = s[0] * scale;
               float dh = s[1] * scale;
               float dx = x + (w - dw) / 2.0F;
               float dy = y + (h - dh) / 2.0F;
               NVGPaint paint = NVGPaint.malloc(stack);
               NanoVG.nvgImagePattern(this.ctx, dx, dy, dw, dh, 0.0F, image, 1.0F, paint);
               NanoVG.nvgBeginPath(this.ctx);
               NanoVG.nvgRoundedRect(this.ctx, x, y, w, h, radius);
               NanoVG.nvgFillPaint(this.ctx, paint);
               NanoVG.nvgFill(this.ctx);
            } catch (Throwable var18) {
               throw new RuntimeException(var18);
            } finally {
               stack.close();
            }
         }
      }
   }

   private static NVGColor color(MemoryStack stack, int argb) {
      return NanoVG.nvgRGBA((byte)Colors.red(argb), (byte)Colors.green(argb), (byte)Colors.blue(argb), (byte)Colors.alpha(argb), NVGColor.malloc(stack));
   }
}
