package dev.neko.client.module;

import dev.neko.client.module.impl.AnchorMacroModule;
import dev.neko.client.module.impl.AutoCrystalModule;
import dev.neko.client.module.impl.AutoInventoryTotemModule;
import dev.neko.client.module.impl.AutoToolModule;
import dev.neko.client.module.impl.AutoTotemModule;
import dev.neko.client.module.impl.BlockEntityEspModule;
import dev.neko.client.module.impl.BlockEspModule;
import dev.neko.client.module.impl.CustomAccessoriesModule;
import dev.neko.client.module.impl.DoubleAnchorModule;
import dev.neko.client.module.impl.ElytraSwapModule;
import dev.neko.client.module.impl.FakePayModule;
import dev.neko.client.module.impl.FakeRolesModule;
import dev.neko.client.module.impl.FakeStatsModule;
import dev.neko.client.module.impl.FastUseModule;
import dev.neko.client.module.impl.FreeLookModule;
import dev.neko.client.module.impl.FreecamModule;
import dev.neko.client.module.impl.FullbrightModule;
import dev.neko.client.module.impl.GambleRiggerModule;
import dev.neko.client.module.impl.HitBoxModule;
import dev.neko.client.module.impl.HitParticlesModule;
import dev.neko.client.module.impl.HoverTotemModule;
import dev.neko.client.module.impl.LightDebugModule;
import dev.neko.client.module.impl.MaceBomberModule;
import dev.neko.client.module.impl.MaceSwapModule;
import dev.neko.client.module.impl.MotionBlurModule;
import dev.neko.client.module.impl.NameProtectModule;
import dev.neko.client.module.impl.NameTagsModule;
import dev.neko.client.module.impl.PlayerEspModule;
import dev.neko.client.module.impl.PrimeChunkFinderModule;
import dev.neko.client.module.impl.ShieldBreakerModule;
import dev.neko.client.module.impl.SkinProtectModule;
import dev.neko.client.module.impl.SpawnerProtectModule;
import dev.neko.client.module.impl.SprintModule;
import dev.neko.client.module.impl.StaffListModule;
import dev.neko.client.module.impl.StorageEspModule;
import dev.neko.client.module.impl.SwingSpeedModule;
import dev.neko.client.module.impl.TotemCounterModule;
import dev.neko.client.module.impl.TriggerbotModule;
import dev.neko.client.module.impl.ZoomModule;
import dev.neko.client.settings.Setting;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ModuleManager {
   private final List<Module> modules = new ArrayList<>();
   private final Map<Category, List<Module>> byCategory = new LinkedHashMap<>();
   public final Modules.ClickGuiModule clickGui;
   public final Modules.HudModule hud;
   public final Modules.SpotifyModule spotify;
   public final Modules.SusChunkFinderModule susChunkFinder;
   public final Modules.ClusterEspModule clusterEsp;
   public final PrimeChunkFinderModule primeChunkFinder;
   public final LightDebugModule lightDebug;
   public FullbrightModule fullbright;
   public SwingSpeedModule swingSpeed;
   public StorageEspModule storageEsp;
   public BlockEspModule blockEsp;
   public AutoTotemModule autoTotem;
   public AutoToolModule autoTool;
   public MaceSwapModule maceSwap;
   public AnchorMacroModule anchorMacro;
   public AutoCrystalModule autoCrystal;
   public HitBoxModule hitBox;
   public ElytraSwapModule elytraSwap;
   public HoverTotemModule hoverTotem;
   public ShieldBreakerModule shieldBreaker;
   public TriggerbotModule triggerbot;
   public DoubleAnchorModule doubleAnchor;
   public MaceBomberModule maceBomber;
   public NameProtectModule nameProtect;
   public SkinProtectModule skinProtect;
   public NameTagsModule nameTags;
   public FastUseModule fastUse;
   public AutoInventoryTotemModule autoInventoryTotem;
   public PlayerEspModule playerEsp;
   public BlockEntityEspModule blockEntityEsp;
   public FreecamModule freecam;
   public HitParticlesModule hitParticles;
   public MotionBlurModule motionBlur;
   public CustomAccessoriesModule customAccessories;
   public FreeLookModule freeLook;
   public FakePayModule fakePay;
   public FakeStatsModule fakeStats;
   public FakeRolesModule fakeRoles;
   public StaffListModule staffList;
   public SpawnerProtectModule spawnerProtect;
   public GambleRiggerModule gambleRigger;
   public SprintModule sprint;
   public TotemCounterModule totemCounter;
   public ZoomModule zoom;
   private Runnable openGuiAction = () -> {};
   private BiConsumer<Module, Boolean> toggleListener = (m, e) -> {};
   private boolean keybindToggle;
   private boolean quietToggles;

   public ModuleManager() {
      for (Category category : Category.values()) {
         this.byCategory.put(category, new ArrayList<>());
      }

      this.susChunkFinder = new Modules.SusChunkFinderModule();
      this.clusterEsp = new Modules.ClusterEspModule();
      this.primeChunkFinder = new PrimeChunkFinderModule();
      this.lightDebug = new LightDebugModule();
      this.registerPlaceholders();
      this.register(this.hitParticles = new HitParticlesModule());
      this.register(this.customAccessories = new CustomAccessoriesModule());
      this.register(this.motionBlur = new MotionBlurModule());
      this.register(this.hud = new Modules.HudModule());
      this.register(this.spotify = new Modules.SpotifyModule());
      this.register(this.swingSpeed = new SwingSpeedModule());
      this.register(this.clickGui = new Modules.ClickGuiModule());
   }

   private void registerPlaceholders() {
      this.register(this.autoTotem = new AutoTotemModule());
      this.register(this.autoTool = new AutoToolModule());
      this.register(this.autoCrystal = new AutoCrystalModule());
      this.register(this.anchorMacro = new AnchorMacroModule());
      this.register(this.doubleAnchor = new DoubleAnchorModule());
      this.register(this.autoInventoryTotem = new AutoInventoryTotemModule());
      this.register(this.maceSwap = new MaceSwapModule());
      this.register(this.hitBox = new HitBoxModule());
      this.register(this.elytraSwap = new ElytraSwapModule());
      this.register(this.hoverTotem = new HoverTotemModule());
      this.register(this.shieldBreaker = new ShieldBreakerModule());
      this.register(this.triggerbot = new TriggerbotModule());
      this.register(this.maceBomber = new MaceBomberModule());
      this.register(this.skinProtect = new SkinProtectModule());
      this.register(this.nameProtect = new NameProtectModule());
      this.register(this.freecam = new FreecamModule());
      this.register(this.fastUse = new FastUseModule());
      this.register(this.nameTags = new NameTagsModule());
      this.register(this.fakePay = new FakePayModule());
      this.register(this.fakeStats = new FakeStatsModule());
      this.register(this.fakeRoles = new FakeRolesModule());
      this.register(this.staffList = new StaffListModule());
      this.register(this.freeLook = new FreeLookModule());
      this.register(this.spawnerProtect = new SpawnerProtectModule());
      this.register(this.gambleRigger = new GambleRiggerModule());
      this.register(this.blockEsp = new BlockEspModule());
      this.register(this.storageEsp = new StorageEspModule());
      this.register(this.blockEntityEsp = new BlockEntityEspModule());
      this.register(this.fullbright = new FullbrightModule());
      this.register(this.playerEsp = new PlayerEspModule());
      this.register(this.sprint = new SprintModule());
      this.register(this.zoom = new ZoomModule());
      this.register(this.totemCounter = new TotemCounterModule());
      this.register(this.susChunkFinder);
      this.register(this.clusterEsp);
      this.register(this.primeChunkFinder);
      this.register(this.lightDebug);
   }

   private void ph(String name, String description, Category category, Setting<?>... settings) {
      this.register(new Modules.Placeholder(name, description, category, settings));
   }

   public void register(Module module) {
      this.modules.add(module);
      this.byCategory.get(module.getCategory()).add(module);
      module.setToggleCallback(this::notifyToggle);
   }

   public List<Module> all() {
      return this.modules;
   }

   public List<Module> inCategory(Category category) {
      return this.byCategory.get(category);
   }

   public void setOpenGuiAction(Runnable action) {
      this.openGuiAction = action;
   }

   public void setToggleListener(BiConsumer<Module, Boolean> listener) {
      this.toggleListener = listener;
   }

   public void notifyToggle(Module module, boolean enabled) {
      if (!this.quietToggles) {
         this.toggleListener.accept(module, enabled);
      }
   }

   public void setAll(Category category, boolean enabled) {
      this.quietToggles = true;

      try {
         for (Module module : this.inCategory(category)) {
            module.setEnabled(enabled);
         }
      } finally {
         this.quietToggles = false;
      }
   }

   public boolean onKeyPressed(int keyCode) {
      boolean bound = this.clickGui.getKeybind().isBound();
      if (!this.clickGui.getKeybind().matches(keyCode) && (bound || keyCode != 301)) {
         boolean handled = false;

         for (Module module : this.modules) {
            if (module != this.clickGui && module.getKeybind().matches(keyCode)) {
               this.keybindToggle = true;

               try {
                  module.toggle();
               } finally {
                  this.keybindToggle = false;
               }

               handled = true;
            }
         }

         for (Module modulex : this.modules) {
            if (modulex.isEnabled() && modulex.onKeyPress(keyCode)) {
               handled = true;
            }
         }

         return handled;
      } else {
         this.openGuiAction.run();
         return true;
      }
   }

   public void onTick() {
      for (Module module : this.modules) {
         if (module.isEnabled() && module.getCategory() != Category.COMBAT) {
            module.onTick();
         }
      }
   }

   public boolean isKeybindToggle() {
      return this.keybindToggle;
   }

   public void onCombatTick() {
      for (Module module : this.modules) {
         if (module.isEnabled() && module.getCategory() == Category.COMBAT) {
            module.onTick();
         }
      }
   }
}
