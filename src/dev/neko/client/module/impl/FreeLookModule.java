package dev.neko.client.module.impl;

import dev.neko.client.module.Category;
import dev.neko.client.module.Module;
import dev.neko.client.settings.SliderSetting;
import net.minecraft.class_1923;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_5498;
import net.minecraft.class_631;

public class FreeLookModule extends Module {
   private static FreeLookModule instance;
   public final SliderSetting sensitivity = this.addSetting(
      new SliderSetting("Camera Sensitivity", "How fast the camera moves in Camera mode.", 8.0, 0.0, 10.0, 0.1)
   );
   private float cameraYaw;
   private float cameraPitch;
   private class_5498 prePers;
   private class_1923 lastCameraChunk;
   private int lastLoadDistance = Integer.MIN_VALUE;

   public FreeLookModule() {
      super("FreeLook", "Allows more rotation options in third person.", Category.MISC);
      instance = this;
   }

   public static FreeLookModule get() {
      return instance;
   }

   @Override
   protected void onEnable() {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null) {
         this.cameraYaw = mc.field_1724.method_36454();
         this.cameraPitch = mc.field_1724.method_36455();
         this.prePers = mc.field_1690.method_31044();
         if (this.prePers != class_5498.field_26665) {
            mc.field_1690.method_31043(class_5498.field_26665);
         }
      }
   }

   @Override
   protected void onDisable() {
      class_310 mc = class_310.method_1551();
      if (this.prePers != null && mc.field_1690.method_31044() != this.prePers) {
         mc.field_1690.method_31043(this.prePers);
      }

      this.restoreChunkLoading(mc);
   }

   @Override
   public void onTick() {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null) {
         mc.field_1724.method_36457(class_3532.method_15363(mc.field_1724.method_36455(), -90.0F, 90.0F));
         this.cameraPitch = class_3532.method_15363(this.cameraPitch, -90.0F, 90.0F);
      }
   }

   public void syncChunkLoading(class_4184 camera) {
      class_310 mc = class_310.method_1551();
      if (this.isActive() && mc.field_1687 != null && mc.field_1724 != null) {
         class_631 chunks = mc.field_1687.method_2935();
         class_1923 cameraChunk = new class_1923((int)Math.floor(camera.method_71156().field_1352) >> 4, (int)Math.floor(camera.method_71156().field_1350) >> 4);
         int distance = Math.max(2, Math.min(32, mc.field_1690.method_38521()));
         if (distance != this.lastLoadDistance) {
            chunks.method_20180(distance);
            this.lastLoadDistance = distance;
         }

         if (this.lastCameraChunk == null
            || cameraChunk.field_9181 != this.lastCameraChunk.field_9181
            || cameraChunk.field_9180 != this.lastCameraChunk.field_9180) {
            chunks.method_20317(cameraChunk.field_9181, cameraChunk.field_9180);
            this.lastCameraChunk = cameraChunk;
            if (mc.field_1769 != null) {
               mc.field_1769.method_3292();
            }
         }
      }
   }

   private void restoreChunkLoading(class_310 mc) {
      this.lastCameraChunk = null;
      this.lastLoadDistance = Integer.MIN_VALUE;
      if (mc.field_1687 != null && mc.field_1724 != null) {
         class_631 chunks = mc.field_1687.method_2935();
         class_1923 playerChunk = mc.field_1724.method_31476();
         chunks.method_20317(playerChunk.field_9181, playerChunk.field_9180);
         chunks.method_20180(mc.field_1690.method_38521());
      }
   }

   public boolean isActive() {
      return this.isEnabled() && class_310.method_1551().field_1724 != null;
   }

   public boolean seeThroughWalls() {
      return this.isActive();
   }

   public boolean cameraMode() {
      return this.isActive();
   }

   public boolean playerMode() {
      return false;
   }

   public void addCameraLook(double deltaX, double deltaY) {
      float sens = this.sensitivity.getFloat();
      if (sens <= 0.0F) {
         sens = 1.0F;
      }

      this.cameraYaw += (float)(deltaX / sens);
      this.cameraPitch += (float)(deltaY / sens);
      if (Math.abs(this.cameraPitch) > 90.0F) {
         this.cameraPitch = this.cameraPitch > 0.0F ? 90.0F : -90.0F;
      }
   }

   public float getCameraYaw() {
      return this.cameraYaw;
   }

   public float getCameraPitch() {
      return this.cameraPitch;
   }
}
