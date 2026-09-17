package dev.neko.client.suschunk;

import dev.neko.client.settings.SliderSetting;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_310;
import net.minecraft.class_4076;
import net.minecraft.class_2338.class_2339;

public class SusChunkScanner {
   public static final int POINTS_PER_SENSITIVITY = 5;
   private static final int GEODE_LINK_DISTANCE = 5;
   private static final int LIGHT_ZERO = 0;
   private static final int LIGHT_AIR_BOUNDARY = 4;
   private static final boolean DEBUG_LOG = Boolean.getBoolean("neko.sus.debug");
   private final SliderSetting sensitivity;
   private final int fixedThreshold;
   private final Map<Long, SusChunkScanner.ChunkScore> scores = new ConcurrentHashMap<>();
   private final Deque<Long> queue = new ArrayDeque<>();
   private final Set<Long> lightRescans = ConcurrentHashMap.newKeySet();
   private final Set<Long> alertedChunks = new HashSet<>();
   private volatile List<SusChunkScanner.Flag> flags = List.of();
   private volatile List<SusChunkScanner.Zone> zones = List.of();
   private class_1923 lastQueueCenter;
   private int tickCounter;

   public SusChunkScanner(SliderSetting sensitivity) {
      this.sensitivity = sensitivity;
      this.fixedThreshold = 0;
      ServerLightCache var10000 = ServerLightCache.get();
      Set var10001 = this.lightRescans;
      var10000.addDirtyListener(var10001::add);
   }

   public SusChunkScanner(int fixedThreshold) {
      this.sensitivity = null;
      this.fixedThreshold = fixedThreshold;
      ServerLightCache var10000 = ServerLightCache.get();
      Set var10001 = this.lightRescans;
      var10000.addDirtyListener(var10001::add);
   }

   public List<SusChunkScanner.Flag> flags() {
      return this.flags;
   }

   public List<SusChunkScanner.Zone> zones() {
      return this.zones;
   }

   public Set<class_2338> amethystCells() {
      Set<class_2338> cells = new HashSet<>();
      int threshold = this.threshold();

      for (SusChunkScanner.ChunkScore score : this.scores.values()) {
         if (score.score >= threshold) {
            cells.addAll(score.amethystCells);
         }
      }

      return cells;
   }

   public int threshold() {
      return this.sensitivity != null ? this.sensitivity.getInt() * 5 : this.fixedThreshold;
   }

   public void clear() {
      this.scores.clear();
      this.queue.clear();
      this.lightRescans.clear();
      this.alertedChunks.clear();
      this.flags = List.of();
      this.zones = List.of();
      this.lastQueueCenter = null;
   }

   public void tick() {
      class_310 mc = class_310.method_1551();
      if (mc.field_1687 != null && mc.field_1724 != null) {
         try {
            this.refillQueueIfNeeded(mc);
            int budget = 10;
            long deadline = System.nanoTime() + 2000000L;
            int scanned = this.scanLightRescans(mc, budget, deadline);
            this.scanQueue(mc, budget - scanned, deadline);
            if (++this.tickCounter % 10 == 0) {
               this.rebuild(mc);
            }
         } catch (Exception var6) {
         }
      }
   }

   private int scanLightRescans(class_310 mc, int budget, long deadline) {
      if (this.lightRescans.isEmpty()) {
         return 0;
      } else {
         int scanned = 0;
         Iterator<Long> iterator = this.lightRescans.iterator();

         while (iterator.hasNext() && scanned < budget && System.nanoTime() < deadline) {
            long key = iterator.next();
            iterator.remove();
            class_2818 chunk = mc.field_1687.method_2935().method_12126(class_1923.method_8325(key), class_1923.method_8332(key), false);
            if (chunk != null && this.scores.containsKey(key)) {
               this.scores.put(key, this.scanChunk(mc, chunk));
               scanned++;
            }
         }

         return scanned;
      }
   }

   private void scanQueue(class_310 mc, int budget, long deadline) {
      int scanned = 0;
      int polled = 0;

      while (scanned < budget && polled < 128 && !this.queue.isEmpty() && System.nanoTime() < deadline) {
         polled++;
         long key = this.queue.pollFirst();
         if (!this.scores.containsKey(key)) {
            class_2818 chunk = mc.field_1687.method_2935().method_12126(class_1923.method_8325(key), class_1923.method_8332(key), false);
            if (chunk != null) {
               this.scores.put(key, this.scanChunk(mc, chunk));
               scanned++;
            }
         }
      }
   }

   private void refillQueueIfNeeded(class_310 mc) {
      class_1923 center = mc.field_1724.method_31476();
      if (this.queue.isEmpty()
         || this.lastQueueCenter == null
         || Math.max(Math.abs(center.field_9181 - this.lastQueueCenter.field_9181), Math.abs(center.field_9180 - this.lastQueueCenter.field_9180)) >= 3) {
         this.lastQueueCenter = center;
         this.queue.clear();
         int radius = Math.min((Integer)mc.field_1690.method_42503().method_41753() + 1, 16);
         List<Long> order = new ArrayList<>();

         for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
               long key = class_1923.method_8331(center.field_9181 + dx, center.field_9180 + dz);
               if (!this.scores.containsKey(key)) {
                  order.add(key);
               }
            }
         }

         order.sort(Comparator.comparingDouble(k -> Math.hypot(class_1923.method_8325(k) - center.field_9181, class_1923.method_8332(k) - center.field_9180)));
         this.queue.addAll(order);
      }
   }

   private static boolean isPlantTarget(class_2680 state) {
      class_2248 block = state.method_26204();
      return block == class_2246.field_9993
         || block == class_2246.field_10463
         || block == class_2246.field_10211
         || block == class_2246.field_16999
         || block == class_2246.field_10597
         || block == class_2246.field_28048;
   }

   private SusChunkScanner.ChunkScore scanChunk(class_310 mc, class_2818 chunk) {
      SusChunkScanner.ChunkScore result = new SusChunkScanner.ChunkScore(chunk.method_12004().method_8324());
      this.detectAmethyst(mc, chunk, result);
      result.computeScore();
      if (DEBUG_LOG && result.score > 0.0) {
      }

      return result;
   }

   private void detectAmethyst(class_310 mc, class_2818 chunk, SusChunkScanner.ChunkScore result) {
      class_2338 center = findGeodeCenter(mc, chunk);
      if (center != null) {
         int minX = center.method_10263() - 8;
         int maxX = center.method_10263() + 8;
         int minY = Math.max(-58, center.method_10264() - 8);
         int maxY = Math.min(30, center.method_10264() + 8);
         int minZ = center.method_10260() - 8;
         int maxZ = center.method_10260() + 8;
         class_2339 cursor = new class_2339();

         for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
               for (int z = minZ; z <= maxZ; z++) {
                  if (x != center.method_10263() || y != center.method_10264() || z != center.method_10260()) {
                     class_2338 cell = new class_2338(x, y, z);
                     class_2680 state = mc.field_1687.method_8320(cell);
                     if (state.method_27852(class_2246.field_27161) || state.method_26215()) {
                        int ownLight = ServerLightCache.get().serverBlockLight(x, y, z);
                        if (ownLight == 0) {
                           int maxLight = 0;
                           boolean hasLight4Air = false;

                           for (class_2350 dir : class_2350.values()) {
                              cursor.method_10101(cell).method_10098(dir);
                              int light = ServerLightCache.get().serverBlockLight(cursor.method_10263(), cursor.method_10264(), cursor.method_10260());
                              if (light > maxLight) {
                                 maxLight = light;
                              }

                              class_2680 nState = mc.field_1687.method_8320(cursor);
                              if (light == 4 && (nState.method_26215() || nState.method_27852(class_2246.field_27161))) {
                                 hasLight4Air = true;
                              }
                           }

                           if (maxLight == 4 && hasLight4Air) {
                              result.amethystCells.add(cell);
                              result.add(SusChunkScanner.SignalType.AMETHYST, cell);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static class_2338 findGeodeCenter(class_310 mc, class_2818 chunk) {
      int minX = chunk.method_12004().method_8326();
      int minZ = chunk.method_12004().method_8328();

      for (int y = -58; y <= 30; y++) {
         for (int x = minX; x < minX + 16; x++) {
            for (int z = minZ; z < minZ + 16; z++) {
               class_2338 pos = new class_2338(x, y, z);
               class_2680 state = mc.field_1687.method_8320(pos);
               if (state.method_27852(class_2246.field_27160) || state.method_27852(class_2246.field_27159)) {
                  return pos;
               }
            }
         }
      }

      return null;
   }

   private void detectGrowth(class_310 mc, class_2818 chunk, SusChunkScanner.ChunkScore result) {
      class_2826[] sections = chunk.method_12006();
      int minSectionY = chunk.method_32891();
      int loSection = class_4076.method_18675(-12);
      int hiSection = class_4076.method_18675(80);
      int baseX = chunk.method_12004().method_8326();
      int baseZ = chunk.method_12004().method_8328();
      class_2339 cursor = new class_2339();

      for (int s = 0; s < sections.length; s++) {
         int sectionY = minSectionY + s;
         if (sectionY >= loSection && sectionY <= hiSection) {
            class_2826 section = sections[s];
            if (!section.method_38292() && section.method_12265().method_19526(SusChunkScanner::isPlantTarget)) {
               int baseY = sectionY << 4;

               for (int y = 0; y < 16; y++) {
                  int worldY = baseY + y;
                  if (worldY >= -12 && worldY <= 80) {
                     for (int z = 0; z < 16; z++) {
                        for (int x = 0; x < 16; x++) {
                           class_2680 state = section.method_12254(x, y, z);
                           if (isPlantTarget(state)) {
                              cursor.method_10103(baseX + x, worldY, baseZ + z);
                              this.inspectPlant(mc, state, cursor, result);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void inspectPlant(class_310 mc, class_2680 state, class_2338 pos, SusChunkScanner.ChunkScore result) {
      class_2248 block = state.method_26204();
      if (block != class_2246.field_9993 && block == class_2246.field_10463) {
      }

      if (block == class_2246.field_10211) {
      }

      if (block == class_2246.field_16999) {
      }

      if (block == class_2246.field_10597) {
      }

      if (block == class_2246.field_28048) {
      }
   }

   private static int columnLength(class_310 mc, class_2338 start, class_2350 dir, class_2248... blocks) {
      int length = 1;
      class_2339 cursor = start.method_25503();
      if (length < 40) {
         cursor.method_10098(dir);
         class_2680 state = mc.field_1687.method_8320(cursor);

         for (class_2248 block : blocks) {
            if (state.method_27852(block)) {
            }
         }
      }

      return length;
   }

   private void rebuild(class_310 mc) {
      this.scores.keySet().removeIf(key -> mc.field_1687.method_2935().method_12126(class_1923.method_8325(key), class_1923.method_8332(key), false) == null);
      int threshold = this.threshold();
      Map<Long, SusChunkScanner.FlagAggregate> aggregates = new HashMap<>();

      for (SusChunkScanner.ChunkScore score : this.scores.values()) {
         if (score.score >= threshold) {
            SusChunkScanner.FlagAggregate agg = aggregates.computeIfAbsent(score.chunkKey, SusChunkScanner.FlagAggregate::new);
            agg.score = Math.max(agg.score, score.score);
            agg.hitWeight = agg.hitWeight + score.hitWeight;
            agg.hitX = agg.hitX + score.hitX;
            agg.hitZ = agg.hitZ + score.hitZ;
         }
      }

      for (SusChunkScanner.Geode geode : this.clusterGeodes()) {
         if (!(geode.score() < threshold)) {
            for (long chunkKey : geode.chunks()) {
               SusChunkScanner.FlagAggregate agg = aggregates.computeIfAbsent(chunkKey, SusChunkScanner.FlagAggregate::new);
               agg.score = Math.max(agg.score, geode.score());
            }

            double weight = SusChunkScanner.SignalType.AMETHYST.weight;

            for (class_2338 cell : geode.cells()) {
               SusChunkScanner.FlagAggregate agg = aggregates.get(class_1923.method_8331(cell.method_10263() >> 4, cell.method_10260() >> 4));
               if (agg != null) {
                  agg.hitWeight += weight;
                  agg.hitX = agg.hitX + (cell.method_10263() + 0.5) * weight;
                  agg.hitZ = agg.hitZ + (cell.method_10260() + 0.5) * weight;
               }
            }

            if (DEBUG_LOG) {
            }
         }
      }

      List<SusChunkScanner.Flag> flagged = new ArrayList<>();

      for (SusChunkScanner.FlagAggregate agg : aggregates.values()) {
         flagged.add(new SusChunkScanner.Flag(agg.chunkKey, agg.score));
      }

      this.flags = List.copyOf(flagged);
      this.zones = this.buildZones(flagged, aggregates);
   }

   private List<SusChunkScanner.Geode> clusterGeodes() {
      List<class_2338> cells = new ArrayList<>();

      for (SusChunkScanner.ChunkScore score : this.scores.values()) {
         cells.addAll(score.amethystCells);
      }

      int n = cells.size();
      if (n == 0) {
         return List.of();
      } else {
         int[] parent = new int[n];
         int i = 0;

         while (i < n) {
            parent[i] = i++;
         }

         for (int ix = 0; ix < n; ix++) {
            class_2338 a = cells.get(ix);

            for (int j = ix + 1; j < n; j++) {
               class_2338 b = cells.get(j);
               int chebyshev = Math.max(
                  Math.abs(a.method_10263() - b.method_10263()),
                  Math.max(Math.abs(a.method_10264() - b.method_10264()), Math.abs(a.method_10260() - b.method_10260()))
               );
               if (chebyshev <= 5) {
                  parent[find(parent, ix)] = find(parent, j);
               }
            }
         }

         Map<Integer, List<class_2338>> groups = new HashMap<>();

         for (int ix = 0; ix < n; ix++) {
            groups.computeIfAbsent(find(parent, ix), k -> new ArrayList<>()).add(cells.get(ix));
         }

         List<SusChunkScanner.Geode> geodes = new ArrayList<>();

         for (List<class_2338> group : groups.values()) {
            Set<Long> chunks = new HashSet<>();
            double sx = 0.0;
            double sz = 0.0;

            for (class_2338 c : group) {
               chunks.add(class_1923.method_8331(c.method_10263() >> 4, c.method_10260() >> 4));
               sx += c.method_10263() + 0.5;
               sz += c.method_10260() + 0.5;
            }

            double score = SusChunkScanner.SignalType.AMETHYST.weight * Math.min(group.size(), SusChunkScanner.SignalType.AMETHYST.cap);
            geodes.add(new SusChunkScanner.Geode(List.copyOf(group), Set.copyOf(chunks), score, sx / group.size(), sz / group.size()));
         }

         return geodes;
      }
   }

   private static int find(int[] parent, int i) {
      while (parent[i] != i) {
         parent[i] = parent[parent[i]];
         i = parent[i];
      }

      return i;
   }

   private List<SusChunkScanner.Zone> buildZones(List<SusChunkScanner.Flag> flagged, Map<Long, SusChunkScanner.FlagAggregate> aggregates) {
      int radius = 3;
      Map<Long, SusChunkScanner.Flag> byKey = new HashMap<>();

      for (SusChunkScanner.Flag flag : flagged) {
         byKey.put(flag.chunkKey(), flag);
      }

      List<SusChunkScanner.Zone> built = new ArrayList<>();
      Set<Long> visited = new HashSet<>();

      for (SusChunkScanner.Flag seed : flagged) {
         if (visited.add(seed.chunkKey())) {
            List<SusChunkScanner.Flag> group = new ArrayList<>();
            Deque<SusChunkScanner.Flag> frontier = new ArrayDeque<>(List.of(seed));

            while (!frontier.isEmpty()) {
               SusChunkScanner.Flag current = frontier.poll();
               group.add(current);
               int cx = class_1923.method_8325(current.chunkKey());
               int cz = class_1923.method_8332(current.chunkKey());

               for (int dx = -radius; dx <= radius; dx++) {
                  for (int dz = -radius; dz <= radius; dz++) {
                     if (dx != 0 || dz != 0) {
                        SusChunkScanner.Flag neighbour = byKey.get(class_1923.method_8331(cx + dx, cz + dz));
                        if (neighbour != null && visited.add(neighbour.chunkKey())) {
                           frontier.add(neighbour);
                        }
                     }
                  }
               }
            }

            built.add(this.makeZone(group, aggregates));
         }
      }

      built.sort(Comparator.comparingDouble(SusChunkScanner.Zone::totalScore).reversed());
      return built;
   }

   private SusChunkScanner.Zone makeZone(List<SusChunkScanner.Flag> group, Map<Long, SusChunkScanner.FlagAggregate> aggregates) {
      Set<Long> members = new HashSet<>();
      double totalScore = 0.0;
      double maxScore = 0.0;
      double hitWeight = 0.0;
      double hitX = 0.0;
      double hitZ = 0.0;
      double chunkX = 0.0;
      double chunkZ = 0.0;

      for (SusChunkScanner.Flag flag : group) {
         members.add(flag.chunkKey());
         totalScore += flag.score();
         maxScore = Math.max(maxScore, flag.score());
         SusChunkScanner.FlagAggregate agg = aggregates.get(flag.chunkKey());
         if (agg != null && agg.hitWeight > 0.0) {
            hitWeight += agg.hitWeight;
            hitX += agg.hitX;
            hitZ += agg.hitZ;
         }

         chunkX += (class_1923.method_8325(flag.chunkKey()) * 16 + 8) * flag.score();
         chunkZ += (class_1923.method_8332(flag.chunkKey()) * 16 + 8) * flag.score();
      }

      double cx = hitWeight > 0.0 ? hitX / hitWeight : chunkX / totalScore;
      double cz = hitWeight > 0.0 ? hitZ / hitWeight : chunkZ / totalScore;
      return new SusChunkScanner.Zone(Set.copyOf(members), cx, cz, totalScore, maxScore);
   }

   private void fireAlerts(class_310 mc) {
   }

   public static final class ChunkScore {
      public final long chunkKey;
      public final EnumMap<SusChunkScanner.SignalType, Integer> hits = new EnumMap<>(SusChunkScanner.SignalType.class);
      public final List<class_2338> amethystCells = new ArrayList<>();
      public double score;
      double hitWeight;
      double hitX;
      double hitZ;

      ChunkScore(long chunkKey) {
         this.chunkKey = chunkKey;
      }

      void add(SusChunkScanner.SignalType type, class_2338 pos) {
         this.hits.merge(type, 1, Integer::sum);
         this.hitWeight = this.hitWeight + type.weight;
         this.hitX = this.hitX + (pos.method_10263() + 0.5) * type.weight;
         this.hitZ = this.hitZ + (pos.method_10260() + 0.5) * type.weight;
      }

      void computeScore() {
         double total = 0.0;

         for (Entry<SusChunkScanner.SignalType, Integer> entry : this.hits.entrySet()) {
            total += entry.getKey().weight * Math.min(entry.getValue(), entry.getKey().cap);
         }

         this.score = total;
      }
   }

   public record Flag(long chunkKey, double score) {
   }

   private static final class FlagAggregate {
      final long chunkKey;
      double score;
      double hitWeight;
      double hitX;
      double hitZ;

      FlagAggregate(long chunkKey) {
         this.chunkKey = chunkKey;
      }
   }

   public record Geode(List<class_2338> cells, Set<Long> chunks, double score, double centroidX, double centroidZ) {
   }

   public static enum SignalType {
      AMETHYST(6.0, 16),
      KELP(2.0, 6),
      BAMBOO(2.0, 6),
      BERRIES(2.0, 5),
      VINES(2.0, 6),
      DRIPSTONE(2.0, 5);

      public final double weight;
      public final int cap;

      private SignalType(double weight, int cap) {
         this.weight = weight;
         this.cap = cap;
      }

      private static SusChunkScanner.SignalType[] $values() {
         return new SusChunkScanner.SignalType[]{AMETHYST, KELP, BAMBOO, BERRIES, VINES, DRIPSTONE};
      }

      private static SusChunkScanner.SignalType[] $values$() {
         return new SusChunkScanner.SignalType[]{AMETHYST, KELP, BAMBOO, BERRIES, VINES, DRIPSTONE};
      }
   }

   public record Zone(Set<Long> members, double centroidX, double centroidZ, double totalScore, double maxScore) {
   }
}
