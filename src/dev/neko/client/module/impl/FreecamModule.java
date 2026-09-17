package dev.neko.client.module.impl;

import dev.neko.client.mixin.ClientInputAccessor;
import dev.neko.client.module.Category;
import dev.neko.client.module.Module;
import dev.neko.client.settings.SliderSetting;
import net.minecraft.class_10185;
import net.minecraft.class_1923;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_315;
import net.minecraft.class_3532;
import net.minecraft.class_5498;
import net.minecraft.class_631;
import net.minecraft.class_746;

public class FreecamModule extends Module {
   private static FreecamModule instance;
   public final SliderSetting speed = this.addSetting(new SliderSetting("Speed", "Camera fly speed", 1.0, 0.1, 50.0, 0.1, "x"));
   private double currentX;
   private double currentY;
   private double currentZ;
   private double velocityX;
   private double velocityY;
   private double velocityZ;
   private double prevX;
   private double prevY;
   private double prevZ;
   private float currentYaw;
   private float currentPitch;
   private float prevYaw;
   private float prevPitch;
   private double renderX;
   private double renderY;
   private double renderZ;
   private float renderYaw;
   private float renderPitch;
   private long lastFrameNanos;
   private boolean renderStateInitialized;
   private double savedX;
   private double savedY;
   private double savedZ;
   private float savedYaw;
   private float savedPitch;
   private boolean savedAbilitiesFlying;
   private boolean savedSmartCull;
   private class_5498 perspectiveBeforeFreecam;
   private boolean switchedPerspectiveForBody;
   private class_1923 lastSyncedCamChunk;
   private int lastSyncedLoadDistance = Integer.MIN_VALUE;
   private boolean activationPending;
   private boolean active = false;
   private boolean latchedForward;
   private boolean latchedBack;
   private boolean latchedLeft;
   private boolean latchedRight;
   private boolean latchedJump;
   private boolean latchedSneak;
   private boolean latchedSprint;
   private int autopilotWarmupTicks;

   public FreecamModule() {
      super("Freecam", "Detached camera (WASD = fly). Body can keep walking; mining uses real aim.", Category.MISC);
      instance = this;
   }

   public static FreecamModule get() {
      return instance;
   }

   @Override
   protected void onEnable() {
      this.activationPending = true;
      this.active = false;
      this.velocityX = this.velocityY = this.velocityZ = 0.0;
      this.lastFrameNanos = 0L;
   }

   @Override
   protected void onDisable() {
      class_310 mc = class_310.method_1551();
      this.clearMovementLatches();
      this.activationPending = false;
      this.active = false;
      this.velocityX = this.velocityY = this.velocityZ = 0.0;
      this.lastFrameNanos = 0L;
      if (this.switchedPerspectiveForBody) {
         mc.field_1690.method_31043(this.perspectiveBeforeFreecam);
         this.switchedPerspectiveForBody = false;
      }

      this.restoreViewOnlyClientState(mc);
      this.restoreVanillaChunkLoading(mc);
      if (mc.field_1724 != null) {
         mc.field_1724.method_31549().field_7479 = this.savedAbilitiesFlying;
      }
   }

   public void tryCompleteActivation(class_310 mc) {
      if (this.isEnabled() && this.activationPending && mc.field_1724 != null && mc.field_1687 != null) {
         this.savedX = mc.field_1724.method_23317();
         this.savedY = mc.field_1724.method_23318();
         this.savedZ = mc.field_1724.method_23321();
         this.savedYaw = mc.field_1724.method_36454();
         this.savedPitch = mc.field_1724.method_36455();
         this.currentX = this.prevX = this.savedX;
         this.currentY = this.prevY = this.savedY + mc.field_1724.method_5751();
         this.currentZ = this.prevZ = this.savedZ;
         this.currentYaw = this.prevYaw = this.savedYaw;
         this.currentPitch = this.prevPitch = this.savedPitch;
         this.velocityX = this.velocityY = this.velocityZ = 0.0;
         this.lastFrameNanos = 0L;
         this.renderStateInitialized = false;
         this.savedAbilitiesFlying = mc.field_1724.method_31549().field_7479;
         this.switchedPerspectiveForBody = false;
         this.perspectiveBeforeFreecam = mc.field_1690.method_31044();
         if (this.perspectiveBeforeFreecam.method_31034()) {
            mc.field_1690.method_31043(class_5498.field_26665);
            this.switchedPerspectiveForBody = true;
         }

         this.activationPending = false;
         this.active = true;
         this.captureMovementLatches(mc);
         this.maybeLatchWalkFromVelocity(mc);
         this.autopilotWarmupTicks = 40;
         this.lastSyncedCamChunk = null;
         this.lastSyncedLoadDistance = Integer.MIN_VALUE;
         this.applyViewOnlyClientState(mc);
         this.syncFreecamChunkLoading(mc);
         reapplyBodyInput(mc);
      }
   }

   private void captureMovementLatches(class_310 mc) {
      class_315 o = mc.field_1690;
      class_746 player = mc.field_1724;
      class_10185 ki = player != null ? player.field_3913.field_54155 : class_10185.field_54098;
      this.latchedForward = o.field_1894.method_1434() || ki.comp_3159();
      this.latchedBack = o.field_1881.method_1434() || ki.comp_3160();
      this.latchedLeft = o.field_1913.method_1434() || ki.comp_3161();
      this.latchedRight = o.field_1849.method_1434() || ki.comp_3162();
      this.latchedJump = o.field_1903.method_1434() || ki.comp_3163();
      this.latchedSneak = o.field_1832.method_1434() || ki.comp_3164() || player != null && player.method_5715();
      this.latchedSprint = o.field_1867.method_1434() || ki.comp_3165();
      this.mergeLatchFromLivingSpeed(mc.field_1724);
   }

   public void mergeAutopilotFromCurrentState(class_310 mc) {
      if (mc.field_1724 != null) {
         this.latchedSneak = this.latchedSneak | mc.field_1724.method_5715();
         this.mergeLatchFromLivingSpeed(mc.field_1724);
         this.mergeVelocityIntoLatch(mc.field_1724);
      }
   }

   private void mergeLatchFromLivingSpeed(class_746 player) {
      if (player != null) {
         float fs = player.field_6250;
         float ss = player.field_6212;
         if (fs > 0.015F) {
            this.latchedForward = true;
         }

         if (fs < -0.015F) {
            this.latchedBack = true;
         }

         if (ss > 0.015F) {
            this.latchedLeft = true;
         }

         if (ss < -0.015F) {
            this.latchedRight = true;
         }
      }
   }

   private void mergeVelocityIntoLatch(class_746 player) {
      class_243 vel = player.method_18798();
      double vx = vel.field_1352;
      double vz = vel.field_1350;
      if (!(vx * vx + vz * vz < 1.0E-10)) {
         class_243 flatLook = flatLook(player.method_36454());
         double dot = vx * flatLook.field_1352 + vz * flatLook.field_1350;
         double perp = vx * -flatLook.field_1350 + vz * flatLook.field_1352;
         if (Math.abs(dot) >= Math.abs(perp)) {
            if (dot > 0.008) {
               this.latchedForward = true;
            } else if (dot < -0.008) {
               this.latchedBack = true;
            }
         } else if (perp > 0.008) {
            this.latchedLeft = true;
         } else if (perp < -0.008) {
            this.latchedRight = true;
         }
      }
   }

   private void maybeLatchWalkFromVelocity(class_310 mc) {
      if (mc.field_1724 != null && !this.latchedForward && !this.latchedBack && !this.latchedLeft && !this.latchedRight) {
         class_243 vel = mc.field_1724.method_18798();
         double vx = vel.field_1352;
         double vz = vel.field_1350;
         if (!(vx * vx + vz * vz < 1.0E-8)) {
            class_243 flatLook = flatLook(mc.field_1724.method_36454());
            double dot = vx * flatLook.field_1352 + vz * flatLook.field_1350;
            double perp = vx * -flatLook.field_1350 + vz * flatLook.field_1352;
            if (Math.abs(dot) > Math.abs(perp)) {
               if (dot > 0.008) {
                  this.latchedForward = true;
               } else if (dot < -0.008) {
                  this.latchedBack = true;
               }
            } else if (perp > 0.008) {
               this.latchedLeft = true;
            } else if (perp < -0.008) {
               this.latchedRight = true;
            }
         }
      }
   }

   private static class_243 flatLook(float yawDeg) {
      double yawRad = Math.toRadians(yawDeg);
      return new class_243(-Math.sin(yawRad), 0.0, Math.cos(yawRad));
   }

   private void clearMovementLatches() {
      this.latchedForward = this.latchedBack = this.latchedLeft = this.latchedRight = false;
      this.latchedJump = this.latchedSneak = this.latchedSprint = false;
      this.autopilotWarmupTicks = 0;
   }

   @Override
   public void onTick() {
      class_310 client = class_310.method_1551();
      if (this.isEnabled()) {
         this.tryCompleteActivation(client);
         if (this.active && client.field_1724 != null) {
            if (this.autopilotWarmupTicks > 0) {
               this.autopilotWarmupTicks--;
               this.mergeAutopilotFromCurrentState(client);
            }

            class_746 p = client.field_1724;
            if (p.method_31549().field_7477) {
               p.method_31549().field_7479 = false;
            }

            this.syncFreecamChunkLoading(client);
         }
      }
   }

   public void updatePerFrame(class_310 client) {
      if (this.active && client.field_1724 != null) {
         long now = System.nanoTime();
         if (this.lastFrameNanos == 0L) {
            this.lastFrameNanos = now;
         } else {
            double deltaSeconds = Math.min(0.1, Math.max(0.0, (now - this.lastFrameNanos) / 1.0E9));
            this.lastFrameNanos = now;
            if (!(deltaSeconds < 0.001)) {
               class_315 options = client.field_1690;
               double forward = options.field_1894.method_1434() ? 1.0 : 0.0;
               forward -= options.field_1881.method_1434() ? 1.0 : 0.0;
               double strafe = options.field_1913.method_1434() ? 1.0 : 0.0;
               strafe -= options.field_1849.method_1434() ? 1.0 : 0.0;
               double vertical = options.field_1903.method_1434() ? 1.0 : 0.0;
               vertical -= options.field_1832.method_1434() ? 1.0 : 0.0;
               double horizontalLength = Math.sqrt(forward * forward + strafe * strafe);
               if (horizontalLength > 1.0) {
                  forward /= horizontalLength;
                  strafe /= horizontalLength;
               }

               double yawRad = Math.toRadians(this.currentYaw);
               double targetX = (-Math.sin(yawRad) * forward + Math.cos(yawRad) * strafe) * this.speed.getFloat() * 7.0;
               double targetZ = (Math.cos(yawRad) * forward + Math.sin(yawRad) * strafe) * this.speed.getFloat() * 7.0;
               double targetY = vertical * this.speed.getFloat() * 7.0;
               double accelerationBlend = 1.0 - Math.exp(-8.0 * deltaSeconds);
               double frictionBlend = 1.0 - Math.exp(-6.0 * deltaSeconds);
               this.velocityX = this.velocityX + (targetX - this.velocityX) * accelerationBlend;
               this.velocityY = this.velocityY + (targetY - this.velocityY) * accelerationBlend;
               this.velocityZ = this.velocityZ + (targetZ - this.velocityZ) * accelerationBlend;
               if (forward == 0.0 && strafe == 0.0) {
                  this.velocityX = this.velocityX - this.velocityX * frictionBlend;
                  this.velocityZ = this.velocityZ - this.velocityZ * frictionBlend;
               }

               if (vertical == 0.0) {
                  this.velocityY = this.velocityY - this.velocityY * frictionBlend;
               }

               this.prevX = this.currentX;
               this.prevY = this.currentY;
               this.prevZ = this.currentZ;
               this.currentX = this.currentX + this.velocityX * deltaSeconds;
               this.currentY = this.currentY + this.velocityY * deltaSeconds;
               this.currentZ = this.currentZ + this.velocityZ * deltaSeconds;
            }
         }
      }
   }

   public static void reapplyBodyInput(class_310 client) {
      FreecamModule f = instance;
      if (f != null && f.isActive() && client.field_1724 != null) {
         class_746 player = client.field_1724;
         class_315 o = client.field_1690;
         boolean mergePhysical = false;
         boolean fwd = f.latchedForward || mergePhysical && o.field_1894.method_1434();
         boolean back = f.latchedBack || mergePhysical && o.field_1881.method_1434();
         boolean left = f.latchedLeft || mergePhysical && o.field_1913.method_1434();
         boolean right = f.latchedRight || mergePhysical && o.field_1849.method_1434();
         boolean jump = f.latchedJump;
         boolean sneak = f.latchedSneak || mergePhysical && o.field_1832.method_1434();
         boolean sprint = f.latchedSprint || mergePhysical && o.field_1867.method_1434();
         if (mergePhysical) {
            if (o.field_1881.method_1434()) {
               f.latchedForward = false;
            }

            if (o.field_1894.method_1434()) {
               f.latchedBack = false;
            }

            if (o.field_1849.method_1434()) {
               f.latchedLeft = false;
            }

            if (o.field_1913.method_1434()) {
               f.latchedRight = false;
            }
         }

         player.field_3913.field_54155 = new class_10185(fwd, back, left, right, jump, sneak, sprint);
         float sx = (left ? 1.0F : 0.0F) - (right ? 1.0F : 0.0F);
         float sz = (fwd ? 1.0F : 0.0F) - (back ? 1.0F : 0.0F);
         class_241 moveVector = new class_241(sx, sz).method_35581();
         ((ClientInputAccessor)player.field_3913).nekoclient$setMoveVector(moveVector);
         player.method_5660(sneak);
      }
   }

   public boolean hasLatchedLocomotion() {
      return this.latchedForward || this.latchedBack || this.latchedLeft || this.latchedRight || this.latchedJump || this.latchedSneak;
   }

   private void syncFreecamChunkLoading(class_310 client) {
      if (this.active && client.field_1687 != null && client.field_1724 != null) {
         class_631 ccm = client.field_1687.method_2935();
         class_1923 camChunk = new class_1923((int)Math.floor(this.currentX) >> 4, (int)Math.floor(this.currentZ) >> 4);
         class_1923 bodyChunk = client.field_1724.method_31476();
         class_1923 centerChunk = camChunk;
         int sepCamBody = Math.max(Math.abs(camChunk.field_9181 - bodyChunk.field_9181), Math.abs(camChunk.field_9180 - bodyChunk.field_9180));
         if (sepCamBody + 4 > 32) {
            int mx = (camChunk.field_9181 + bodyChunk.field_9181) / 2;
            int mz = (camChunk.field_9180 + bodyChunk.field_9180) / 2;
            centerChunk = new class_1923(mx, mz);
         }

         int spread = Math.max(
            Math.max(Math.abs(centerChunk.field_9181 - camChunk.field_9181), Math.abs(centerChunk.field_9180 - camChunk.field_9180)),
            Math.max(Math.abs(centerChunk.field_9181 - bodyChunk.field_9181), Math.abs(centerChunk.field_9180 - bodyChunk.field_9180))
         );
         int userDist = 12;
         int dist = Math.min(32, Math.max(userDist, spread + 4));
         if (dist != this.lastSyncedLoadDistance) {
            ccm.method_20180(dist);
            this.lastSyncedLoadDistance = dist;
         }

         if (this.lastSyncedCamChunk == null
            || centerChunk.field_9181 != this.lastSyncedCamChunk.field_9181
            || centerChunk.field_9180 != this.lastSyncedCamChunk.field_9180) {
            ccm.method_20317(centerChunk.field_9181, centerChunk.field_9180);
            this.lastSyncedCamChunk = centerChunk;
            if (client.field_1769 != null) {
               client.field_1769.method_3292();
            }
         }
      }
   }

   private void restoreVanillaChunkLoading(class_310 client) {
      this.lastSyncedCamChunk = null;
      this.lastSyncedLoadDistance = Integer.MIN_VALUE;
      if (client.field_1687 != null) {
         class_631 ccm = client.field_1687.method_2935();
         if (client.field_1724 != null) {
            class_1923 p = client.field_1724.method_31476();
            ccm.method_20317(p.field_9181, p.field_9180);
         }

         ccm.method_20180(client.field_1690.method_38521());
         if (client.field_1769 != null) {
            client.field_1769.method_3292();
         }
      }
   }

   private void applyViewOnlyClientState(class_310 mc) {
      this.savedSmartCull = mc.field_1730;
      mc.field_1730 = false;
   }

   private void restoreViewOnlyClientState(class_310 mc) {
      mc.field_1730 = this.savedSmartCull;
      if (mc.field_1761 != null) {
         mc.field_1761.method_2925();
      }
   }

   public boolean isActive() {
      return this.isEnabled() && this.active;
   }

   public boolean isShowPlayerModel() {
      return true;
   }

   public boolean isShowHands() {
      return true;
   }

   public boolean renderHands() {
      return !this.isActive() || this.isShowHands();
   }

   public boolean wasHoldingSneak() {
      return this.latchedSneak;
   }

   public double getInterpolatedX(float tickDelta) {
      return class_3532.method_16436(this.smoothTick(tickDelta), this.prevX, this.currentX);
   }

   public double getInterpolatedY(float tickDelta) {
      return class_3532.method_16436(this.smoothTick(tickDelta), this.prevY, this.currentY);
   }

   public double getInterpolatedZ(float tickDelta) {
      return class_3532.method_16436(this.smoothTick(tickDelta), this.prevZ, this.currentZ);
   }

   public float getInterpolatedYaw(float tickDelta) {
      return class_3532.method_17821(this.smoothTick(tickDelta), this.prevYaw, this.currentYaw);
   }

   public float getInterpolatedPitch(float tickDelta) {
      return class_3532.method_16439(this.smoothTick(tickDelta), this.prevPitch, this.currentPitch);
   }

   private float smoothTick(float tickDelta) {
      return Math.clamp(tickDelta, 0.0F, 1.0F);
   }

   public class_243 getInterpolatedPos(float tickDelta) {
      return new class_243(this.getInterpolatedX(tickDelta), this.getInterpolatedY(tickDelta), this.getInterpolatedZ(tickDelta));
   }

   public void updateRenderState(float tickDelta) {
      if (!this.renderStateInitialized) {
         this.renderStateInitialized = true;
      }

      this.renderX = this.currentX;
      this.renderY = this.currentY;
      this.renderZ = this.currentZ;
      this.renderYaw = this.currentYaw;
      this.renderPitch = this.currentPitch;
   }

   public class_243 getRenderPos() {
      return new class_243(this.renderX, this.renderY, this.renderZ);
   }

   public float getRenderYaw() {
      return this.renderYaw;
   }

   public float getRenderPitch() {
      return this.renderPitch;
   }

   public void setRotation(float yaw, float pitch) {
      this.currentYaw = yaw;
      this.currentPitch = class_3532.method_15363(pitch, -90.0F, 90.0F);
   }

   public float getCurrentYaw() {
      return this.currentYaw;
   }

   public float getCurrentPitch() {
      return this.currentPitch;
   }

   public float getLookSensitivity() {
      return 1.0F;
   }

   public void adjustSpeed(double wheelDelta) {
      if (wheelDelta != 0.0) {
         this.speed.set(this.speed.get() + Math.signum(wheelDelta) * 0.5);
      }
   }
}
