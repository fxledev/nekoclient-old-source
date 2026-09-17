package dev.neko.client.theme;

import dev.neko.client.util.Colors;

public class Theme {
   private final String name;
   private int accent;
   private final boolean custom;
   private int bg;
   private int surface;
   private int card;
   private int inset;

   public Theme(String name, int accent, boolean custom) {
      this.name = name;
      this.accent = accent;
      this.custom = custom;
      this.derive();
   }

   private void derive() {
      this.bg = Colors.lerp(this.accent, -16053231, 0.94F);
      this.surface = Colors.lerp(this.accent, -15460835, 0.9F);
      this.card = Colors.lerp(this.accent, -14605266, 0.86F);
      this.inset = Colors.lerp(this.accent, -15855595, 0.9F);
   }

   public String getName() {
      return this.name;
   }

   public int accent() {
      return this.accent;
   }

   public void setAccent(int accent) {
      this.accent = accent;
      this.derive();
   }

   public boolean isCustom() {
      return this.custom;
   }

   public int accentBright() {
      return this.accent;
   }

   public int accentHover() {
      return Colors.withAlpha(-1, 0.14F);
   }

   public int background() {
      return this.bg;
   }

   public int backgroundTo() {
      return Colors.lighten(this.bg, 0.06F);
   }

   public int surface() {
      return this.surface;
   }

   public int card() {
      return this.card;
   }

   public int inset() {
      return this.inset;
   }

   public int headerTop() {
      return this.surface;
   }

   public int headerBottom() {
      return Colors.darken(this.surface, 0.15F);
   }

   public int moduleActiveFill() {
      return this.card;
   }

   public int textPrimary() {
      return -855310;
   }

   public int textMuted() {
      return -5592406;
   }

   public int textDisabled() {
      return -10066330;
   }

   public int statusEnabled() {
      return this.accent;
   }

   public int statusDisabled() {
      return -11184811;
   }

   public int border() {
      return Colors.darken(this.card, 0.35F);
   }

   public int borderBright() {
      return this.accent;
   }

   public String accentHex() {
      return String.format("#%06X", this.accent & 16777215);
   }
}
