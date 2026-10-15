package knightminer.metalborn.data;

import knightminer.metalborn.metal.MetalId;

import static knightminer.metalborn.Metalborn.metal;

/** List of metal IDs in the mod */
public class MetalIds {
  private MetalIds() {}

  // physical
  public static final MetalId iron = metal("iron");
  public static final MetalId steel = metal("steel");
  public static final MetalId tin = metal("tin");
  public static final MetalId pewter = metal("pewter");
  // cognitive
  public static final MetalId copper = metal("copper");
  public static final MetalId bronze = metal("bronze");
  // hybrid
  public static final MetalId gold = metal("gold");
  public static final MetalId roseGold = metal("rose_gold");
  // spiritual
  public static final MetalId nicrosil = metal("nicrosil");
  public static final MetalId netherite = metal("netherite");

  // compat cognitive
  public static final MetalId silver = metal("silver");
  public static final MetalId electrum = metal("electrum");
  // compat hybrid
  public static final MetalId zinc = metal("zinc");
  public static final MetalId brass = metal("brass");
  // compat alternatives
  public static final MetalId nickel = metal("nickel");
  public static final MetalId constantan = metal("constantan");
  public static final MetalId cadmium = metal("cadmium");
  public static final MetalId bendalloy = metal("bendalloy");
  // compat spiritual
  public static final MetalId chromium = metal("chromium");
}
