package dev.neko.client.render;

import dev.neko.client.NekoClient;
import dev.neko.client.module.ModuleManager;
import dev.neko.client.module.Modules;
import dev.neko.client.module.impl.NameProtectModule;
import dev.neko.client.module.impl.NameTagsModule;
import dev.neko.client.render.nanovg.NVGRenderer;
import dev.neko.client.theme.Theme;
import dev.neko.client.util.Colors;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1542;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_638;
import net.minecraft.class_742;
import net.minecraft.class_746;
import org.joml.Matrix3x2fStack;

public final class WorldNametagRenderer {
   private static final int MAX_TAGS = 80;

   private WorldNametagRenderer() {
   }

   public static void render(NVGRenderer vg) {
      if (WorldProjection.isValid()) {
         ModuleManager modules = NekoClient.modules();
         if (modules != null) {
            class_310 mc = class_310.method_1551();
            class_638 level = mc.field_1687;
            class_746 self = mc.field_1724;
            if (level != null && self != null) {
               NameTagsModule names = modules.nameTags;
               boolean doNames = names != null && names.isEnabled();
               if (doNames) {
                  float pt = WorldProjection.partialTick();
                  Theme theme = NekoClient.themes().current();
                  Modules.HudModule hud = modules.hud;
                  int accent = theme.accent();
                  List<WorldNametagRenderer.Tag> tags = new ArrayList<>();
                  if (doNames) {
                     double range = names.range.get();
                     double rangeSq = range * range;
                     float nameScale = names.scale.getFloat();
                     float nameOpacity = (float)(names.opacity.get() / 100.0);
                     if (names.players.get()) {
                        NameProtectModule protect = modules.nameProtect;
                        boolean protecting = protect != null && protect.isEnabled();
                        boolean showSelf = names.self.get() && !mc.field_1690.method_31044().method_31034();

                        for (class_742 player : level.method_18456()) {
                           boolean isSelf = player == self;
                           if ((!isSelf || showSelf) && !player.method_7325() && player.method_5805()) {
                              double distSq = self.method_5858(player);
                              if (isSelf || !(distSq > rangeSq)) {
                                 String shown = player.method_7334().name();
                                 if (protecting) {
                                    String replaced = protect.replacementForDisplay(shown);
                                    if (replaced != null) {
                                       shown = replaced;
                                    }
                                 }

                                 String suffix = !isSelf && names.distance.get() ? (int)Math.sqrt(distSq) + "m" : null;
                                 if (!shown.isEmpty() || suffix != null) {
                                    tags.add(entityTag(player, pt, Math.sqrt(distSq), shown, suffix, accent, -1.0F, nameScale, nameOpacity));
                                 }
                              }
                           }
                        }
                     }

                     if (names.items.get()) {
                        for (class_1297 entity : level.method_18112()) {
                           if (entity instanceof class_1542 item && item.method_5805()) {
                              double distSq = self.method_5858(item);
                              if (!(distSq > rangeSq)) {
                                 class_1799 stack = item.method_6983();
                                 if (!stack.method_7960()) {
                                    String suffix = itemSuffix(
                                       stack.method_7947(), names.itemAmount.get(), names.distance.get() ? (int)Math.sqrt(distSq) + "m" : null
                                    );
                                    tags.add(
                                       entityTag(item, pt, Math.sqrt(distSq), stack.method_7964().getString(), suffix, accent, -1.0F, nameScale, nameOpacity)
                                    );
                                 }
                              }
                           }
                        }
                     }
                  }

                  if (!tags.isEmpty()) {
                     tags.sort(Comparator.comparingDouble(WorldNametagRenderer.Tag::dist));
                     int count = Math.min(tags.size(), 80);

                     for (int i = count - 1; i >= 0; i--) {
                        WorldNametagRenderer.Tag tag = tags.get(i);
                        float[] screen = WorldProjection.project(tag.wx, tag.wy, tag.wz);
                        if (screen != null) {
                           drawTag(vg, theme, screen[0], screen[1], tag);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static WorldNametagRenderer.Tag entityTag(
      class_1297 e, float pt, double dist, String name, String suffix, int accent, float healthFrac, float scale, float opacity
   ) {
      double x = class_3532.method_16436(pt, e.field_6038, e.method_23317());
      double y = class_3532.method_16436(pt, e.field_5971, e.method_23318()) + e.method_17682() + 0.5;
      double z = class_3532.method_16436(pt, e.field_5989, e.method_23321());
      return new WorldNametagRenderer.Tag(dist, x, y, z, name, suffix, accent, healthFrac, scale, opacity);
   }

   private static String itemSuffix(int amount, boolean showAmount, String distance) {
      StringBuilder sb = new StringBuilder();
      if (showAmount && amount > 1) {
         sb.append('x').append(amount);
      }

      if (distance != null) {
         if (sb.length() > 0) {
            sb.append("  ");
         }

         sb.append(distance);
      }

      return sb.length() == 0 ? null : sb.toString();
   }

   private static float pillHeight(float s, boolean hasHealth) {
      float textRowH = 12.5F * s + 3.5F * s * 2.0F;
      return textRowH + (hasHealth ? 3.0F * s + 3.5F * s : 0.0F);
   }

   public static void renderEquipment(class_332 gg) {
      if (WorldProjection.isValid()) {
         ModuleManager modules = NekoClient.modules();
         if (modules != null) {
            NameTagsModule names = modules.nameTags;
            if (names != null && names.isEnabled() && names.players.get()) {
               boolean showArmor = names.armor.get();
               boolean showHand = names.heldItem.get();
               if (showArmor || showHand) {
                  class_310 mc = class_310.method_1551();
                  if (mc.field_1755 == null && !mc.field_1690.field_1842) {
                     class_638 level = mc.field_1687;
                     class_746 self = mc.field_1724;
                     if (level != null && self != null) {
                        float pt = WorldProjection.partialTick();
                        double range = names.range.get();
                        double rangeSq = range * range;
                        float s = names.scale.getFloat();
                        float pillH = pillHeight(s, false);
                        float uiScale = OverlayRenderer.uiScale();
                        double guiScale = mc.method_22683().method_4495();
                        boolean showSelf = names.self.get() && !mc.field_1690.method_31044().method_31034();
                        List<class_1799> row = new ArrayList<>();

                        for (class_742 player : level.method_18456()) {
                           boolean isSelf = player == self;
                           if ((!isSelf || showSelf) && !player.method_7325() && player.method_5805() && (isSelf || !(self.method_5858(player) > rangeSq))) {
                              row.clear();
                              if (showArmor) {
                                 addItem(row, player.method_6118(class_1304.field_6169));
                                 addItem(row, player.method_6118(class_1304.field_6174));
                                 addItem(row, player.method_6118(class_1304.field_6172));
                                 addItem(row, player.method_6118(class_1304.field_6166));
                              }

                              if (showHand) {
                                 addItem(row, player.method_6047());
                                 addItem(row, player.method_6118(class_1304.field_6171));
                              }

                              if (!row.isEmpty()) {
                                 double wx = class_3532.method_16436(pt, player.field_6038, player.method_23317());
                                 double wy = class_3532.method_16436(pt, player.field_5971, player.method_23318()) + player.method_17682() + 0.5;
                                 double wz = class_3532.method_16436(pt, player.field_5989, player.method_23321());
                                 float[] px = WorldProjection.projectRaw(wx, wy, wz);
                                 if (px != null) {
                                    float guiX = (float)(px[0] / guiScale);
                                    float rowBottomY = (float)((px[1] - pillH * uiScale) / guiScale) - 3.0F;
                                    float icon = 11.0F * s;
                                    float step = icon + 1.5F;
                                    float totalW = row.size() * icon + (row.size() - 1) * 1.5F;
                                    float startX = guiX - totalW / 2.0F;
                                    float rowTopY = rowBottomY - icon;
                                    Matrix3x2fStack pose = gg.method_51448();

                                    for (int i = 0; i < row.size(); i++) {
                                       class_1799 stack = row.get(i);
                                       pose.pushMatrix();
                                       pose.translate(startX + i * step, rowTopY);
                                       pose.scale(icon / 16.0F, icon / 16.0F);
                                       gg.method_51423(player, stack, 0, 0, 0);
                                       gg.method_51431(mc.field_1772, stack, 0, 0);
                                       pose.popMatrix();
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static void addItem(List<class_1799> list, class_1799 stack) {
      if (stack != null && !stack.method_7960()) {
         list.add(stack);
      }
   }

   private static void drawTag(NVGRenderer vg, Theme theme, float uiX, float uiY, WorldNametagRenderer.Tag tag) {
      float s = tag.scale;
      int accentBright = Colors.lighten(tag.accent, 0.35F);
      float fontMain = 12.5F * s;
      float fontSub = 10.0F * s;
      float padX = 6.0F * s;
      float padY = 3.5F * s;
      float gap = 5.0F * s;
      float nameW = vg.textWidth(tag.name, fontMain);
      float suffixW = tag.suffix != null ? gap + vg.textWidth(tag.suffix, fontSub) : 0.0F;
      float w = nameW + suffixW + padX * 2.0F;
      float textRowH = fontMain + padY * 2.0F;
      float x = uiX - w / 2.0F;
      float y = uiY - textRowH;
      float radius = Math.min(6.0F * s, textRowH / 2.0F);
      boolean fade = tag.opacity < 0.999F;
      if (fade) {
         vg.save();
         vg.alpha(tag.opacity);
      }

      vg.glow(x, y, w, textRowH, radius, 4.0F, Colors.withAlpha(tag.accent, 0.12F));
      vg.rectGradient(x, y, w, textRowH, radius, theme.background(), theme.backgroundTo(), true);
      vg.rectOutline(x, y, w, textRowH, radius, 1.0F, -1);
      float cy = y + padY + fontMain / 2.0F;
      float tx = x + padX;
      tx += vg.textGradient(tag.name, tx, cy, fontMain, accentBright, tag.accent);
      if (tag.suffix != null) {
         tx += gap;
         vg.text(tag.suffix, tx, cy, fontSub, theme.textMuted());
      }

      if (fade) {
         vg.restore();
      }
   }

   private record Tag(double dist, double wx, double wy, double wz, String name, String suffix, int accent, float healthFrac, float scale, float opacity) {
   }
}
