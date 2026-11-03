package knightminer.metalborn.json.predicate;

import com.google.common.collect.ImmutableSet;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalPower;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import java.util.Set;

/** Predicate that matches if the metal is in the given set of IDs */
public record MetalSetPredicate(Set<MetalId> metals) implements MetalPredicate {
  public static final RecordLoadable<MetalSetPredicate> LOADER = RecordLoadable.create(
    MetalId.LOADABLE.set(1).requiredField("metals", MetalSetPredicate::metals),
    MetalSetPredicate::new);

  public MetalSetPredicate(MetalId... ids) {
    this(ImmutableSet.copyOf(ids));
  }

  @Override
  public boolean matches(MetalPower metal) {
    return this.metals.contains(metal.id());
  }

  @Override
  public RecordLoadable<? extends MetalPredicate> getLoader() {
    return LOADER;
  }
}
