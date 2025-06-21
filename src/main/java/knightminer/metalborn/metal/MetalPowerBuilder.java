package knightminer.metalborn.metal;

import knightminer.metalborn.metal.effects.MetalEffect;

import java.util.ArrayList;
import java.util.List;

/** Builder for {@link MetalPower} */
public class MetalPowerBuilder {
  private final MetalId id;
  private String name;
  private final int index;
  private int capacity = 20 * 5 * 60; // 5 minutes
  private final List<MetalEffect> feruchemy = new ArrayList<>();
  private final List<MetalEffect> hemalurgy = new ArrayList<>();

  /** Builder constructor */
  private MetalPowerBuilder(MetalId id, int index) {
    this.id = id;
    this.name = id.getPath();
    this.index = index;
  }

  /** Creates a new builder instance */
  public static MetalPowerBuilder builder(MetalId id, int index) {
    return new MetalPowerBuilder(id, index);
  }

  /** Sets the name in this builder. By default, it will be the ID path. */
  public MetalPowerBuilder name(String name) {
    this.name = name;
    return this;
  }


  /* Effects */

  /** Adds a new feruchemy effect */
  public MetalPowerBuilder feruchemy(MetalEffect effect) {
    this.feruchemy.add(effect);
    return this;
  }

  /** Sets the metalmind capacity for this metal. */
  public MetalPowerBuilder capacity(int capacity) {
    this.capacity = capacity;
    return this;
  }

  /** Adds a new hemalurgy effect */
  public MetalPowerBuilder hemalurgy(MetalEffect effect) {
    this.hemalurgy.add(effect);
    return this;
  }



  /** Builds the final power */
  public MetalPower build() {
    return new MetalPower(id, name, index, feruchemy, feruchemy.isEmpty() ? 0 : capacity, hemalurgy);
  }
}
