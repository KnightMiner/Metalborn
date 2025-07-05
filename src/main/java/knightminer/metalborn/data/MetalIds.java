package knightminer.metalborn.data;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.metal.MetalId;

/** List of metal IDs in the mod */
public class MetalIds {
  private MetalIds() {}

  // physical
  public static final MetalId iron = id("iron");
  public static final MetalId steel = id("steel");
  public static final MetalId tin = id("tin");
  public static final MetalId pewter = id("pewter");
  // cognitive
  public static final MetalId copper = id("copper");
  public static final MetalId bronze = id("bronze");
  // hybrid
  public static final MetalId gold = id("gold");
  public static final MetalId roseGold = id("rose_gold");

  // textures
  public static final MetalId netherite = id("netherite");

  // compat cognitive
  public static final MetalId silver = id("silver");
  public static final MetalId electrum = id("electrum");
  // compat hybrid
  public static final MetalId zinc = id("zinc");
  public static final MetalId brass = id("brass");

  /** Creates a metalborn effect ID */
  private static MetalId id(String name) {
    return new MetalId(Metalborn.MOD_ID, name);
  }
}
