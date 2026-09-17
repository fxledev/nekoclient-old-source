package dev.neko.client.module.impl;

import com.google.common.collect.LinkedHashMultimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import dev.neko.client.module.Category;
import dev.neko.client.module.Module;
import dev.neko.client.settings.ModeSetting;
import dev.neko.client.settings.StringSetting;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.class_310;
import net.minecraft.class_8685;

public class SkinProtectModule extends Module {
   private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8L)).build();
   public final StringSetting ign = this.addSetting(new StringSetting("Skin IGN", "Username whose skin is applied", "w3r3", 16, "Type a username…"));
   public final ModeSetting applyTo = this.addSetting(new ModeSetting("Apply To", "Whose skin gets replaced", "Everyone", "Everyone", "Others", "Self"));
   private volatile class_8685 replacement;
   private volatile String fetchedFor = "";
   private volatile boolean fetching;

   public SkinProtectModule() {
      super("SkinProtect", "Replaces skins so clips can't dox skins", Category.MISC);
   }

   @Override
   protected void onEnable() {
      if (this.replacement == null) {
         this.fetchedFor = "";
      }

      this.ensureFetched();
   }

   @Override
   public void onTick() {
      this.ensureFetched();
   }

   public class_8685 replacementSkin() {
      return this.replacement;
   }

   public boolean shouldReplace(UUID uuid) {
      if (uuid == null) {
         return false;
      } else {
         class_310 mc = class_310.method_1551();
         UUID self = mc.field_1724 == null ? null : mc.field_1724.method_5667();
         if (this.applyTo.is("Everyone")) {
            return true;
         } else {
            return this.applyTo.is("Self") ? self != null && self.equals(uuid) : self == null || !self.equals(uuid);
         }
      }
   }

   private void ensureFetched() {
      String want = this.ign.get().trim();
      if (!want.isEmpty() && !this.fetching && !want.equalsIgnoreCase(this.fetchedFor)) {
         this.fetching = true;
         this.fetchedFor = want;
         CompletableFuture.runAsync(() -> this.resolve(want)).whenComplete((v, t) -> this.fetching = false);
      }
   }

   private void resolve(String username) {
      try {
         JsonObject profile = getJson("https://api.mojang.com/users/profiles/minecraft/" + username);
         if (profile == null || !profile.has("id")) {
            return;
         }

         UUID uuid = dashify(profile.get("id").getAsString());
         String name = profile.has("name") ? profile.get("name").getAsString() : username;
         String var10000 = uuid.toString();
         String var10001 = "-";
         String var100001 = var10000.replace(var10001, "");
         JsonObject full = getJson("https://sessionserver.mojang.com/session/minecraft/profile/" + var100001 + "?unsigned=false");
         if (full == null || !full.has("properties")) {
            return;
         }

         LinkedHashMultimap<String, Property> props = LinkedHashMultimap.create();

         for (JsonElement el : full.getAsJsonArray("properties")) {
            JsonObject prop = el.getAsJsonObject();
            if ("textures".equals(prop.get("name").getAsString())) {
               String value = prop.get("value").getAsString();
               String signature = prop.has("signature") ? prop.get("signature").getAsString() : null;
               props.put("textures", signature == null ? new Property("textures", value) : new Property("textures", value, signature));
            }
         }

         GameProfile gameProfile = new GameProfile(uuid, name, new PropertyMap(props));
         class_310.method_1551().method_1582().method_52863(gameProfile).thenAccept(opt -> {
            if (opt.isPresent()) {
               this.replacement = (class_8685)opt.get();
            }
         });
      } catch (Exception var15) {
      }
   }

   private static JsonObject getJson(String url) throws Exception {
      HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(8L)).header("Accept", "application/json").GET().build();
      HttpResponse<String> response = HTTP.send(request, BodyHandlers.ofString());
      return response.statusCode() == 200 && response.body() != null && !response.body().isBlank()
         ? JsonParser.parseString(response.body()).getAsJsonObject()
         : null;
   }

   private static UUID dashify(String undashed) {
      return UUID.fromString(undashed.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
   }
}
