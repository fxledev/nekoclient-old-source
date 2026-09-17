package dev.neko.client.hud;

import com.google.gson.JsonObject;
import dev.neko.client.hud.components.ArmorHud;
import dev.neko.client.hud.components.ArrayListHud;
import dev.neko.client.hud.components.CoordsHud;
import dev.neko.client.hud.components.CpsHud;
import dev.neko.client.hud.components.DirectionHud;
import dev.neko.client.hud.components.FpsHud;
import dev.neko.client.hud.components.KeystrokesHud;
import dev.neko.client.hud.components.PingHud;
import dev.neko.client.hud.components.PotionsHud;
import dev.neko.client.hud.components.RadarHud;
import dev.neko.client.hud.components.SpotifyHud;
import dev.neko.client.hud.components.StaffListHud;
import dev.neko.client.hud.components.WatermarkHud;
import dev.neko.client.module.ModuleManager;
import dev.neko.client.module.Modules;
import dev.neko.client.notification.NotificationManager;
import dev.neko.client.render.nanovg.NVGRenderer;
import dev.neko.client.spotify.SpotifyService;
import dev.neko.client.theme.ThemeManager;
import java.util.ArrayList;
import java.util.List;

public class HudManager {
   private static final float MINIMUM_HUD_SCALE = 1.25F;
   private final List<HudComponent> components = new ArrayList<>();

   public HudManager(ModuleManager modules, ThemeManager themes, SpotifyService spotify, NotificationManager notifications) {
      Modules.HudModule hud = modules.hud;
      this.components.add(new WatermarkHud(themes, () -> hud.isEnabled() && hud.watermark.get()));
      this.components.add(new ArrayListHud(modules, hud, themes, () -> hud.isEnabled() && hud.arrayList.get()));
      this.components.add(new FpsHud(themes, () -> hud.isEnabled() && hud.fps.get()));
      this.components.add(new PingHud(themes, () -> hud.isEnabled() && hud.ping.get()));
      this.components.add(new DirectionHud(themes, () -> hud.isEnabled() && hud.direction.get()));
      this.components.add(new CoordsHud(themes, () -> hud.isEnabled() && hud.coordinates.get()));
      this.components.add(new CpsHud(themes, () -> hud.isEnabled() && hud.cps.get()));
      this.components.add(new ArmorHud(themes, () -> hud.isEnabled() && hud.armor.get()));
      this.components.add(new PotionsHud(themes, () -> hud.isEnabled() && hud.potions.get()));
      this.components.add(new KeystrokesHud(themes, () -> hud.isEnabled() && hud.keystrokes.get()));
      this.components.add(new RadarHud(hud, themes, () -> hud.isEnabled() && hud.radar.get()));
      this.components.add(new StaffListHud(modules.staffList, themes));
      this.components.add(new SpotifyHud(modules.spotify, spotify, themes));
   }

   public List<HudComponent> getComponents() {
      return this.components;
   }

   public List<HudManager.Placement> layout(NVGRenderer vg, float uiWidth, float uiHeight, boolean includeHidden) {
      List<HudManager.Placement> placements = new ArrayList<>();

      for (HudComponent component : this.components) {
         if (includeHidden || component.visible()) {
            float scale = component.getScale();
            float w = component.measureWidth(vg) * scale;
            float h = component.measureHeight(vg) * scale;
            float x = component.getFx() * (uiWidth - w);
            float y = component.getFy() * (uiHeight - h);
            placements.add(new HudManager.Placement(component, x, y, w, h));
         }
      }

      return placements;
   }

   public void render(NVGRenderer vg, float uiWidth, float uiHeight) {
      for (HudManager.Placement p : this.layout(vg, uiWidth, uiHeight, false)) {
         this.renderPlacement(vg, p);
      }
   }

   public void renderPlacement(NVGRenderer vg, HudManager.Placement p) {
      float scale = p.component().getScale();
      vg.save();
      vg.translate(Math.round(p.x()), Math.round(p.y()));
      vg.scale(scale);
      p.component().render(vg, 0.0F, 0.0F, p.w() / scale, p.h() / scale);
      vg.restore();
   }

   public JsonObject toJson() {
      JsonObject json = new JsonObject();

      for (HudComponent component : this.components) {
         JsonObject entry = new JsonObject();
         entry.addProperty("fx", component.getFx());
         entry.addProperty("fy", component.getFy());
         entry.addProperty("scale", component.getScale());
         json.add(component.getId(), entry);
      }

      return json;
   }

   public void fromJson(JsonObject json) {
      for (HudComponent component : this.components) {
         JsonObject entry = json.getAsJsonObject(component.getId());
         if (entry != null && entry.has("fx") && entry.has("fy")) {
            component.setPosition(entry.get("fx").getAsFloat(), entry.get("fy").getAsFloat());
            if (entry.has("scale")) {
               component.setScale(Math.max(1.25F, entry.get("scale").getAsFloat()));
            }
         }
      }
   }

   public record Placement(HudComponent component, float x, float y, float w, float h) {
      public boolean contains(float px, float py) {
         return px >= this.x && px <= this.x + this.w && py >= this.y && py <= this.y + this.h;
      }
   }
}
