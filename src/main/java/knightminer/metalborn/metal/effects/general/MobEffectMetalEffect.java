package knightminer.metalborn.metal.effects.general;

import com.mojang.datafixers.util.Function6;
import knightminer.metalborn.Metalborn;
import knightminer.metalborn.metal.MetalPower;
import knightminer.metalborn.metal.effects.MetalEffect;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.primitive.BooleanLoadable;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import java.util.ArrayList;
import java.util.List;

/** Metal effect that applies a mob effect every second */
public interface MobEffectMetalEffect extends MetalEffect {
  String KEY_EFFECT = Metalborn.key("metal_effect", "apply_effect");

  /** Creates a builder for an effect applied on tapping */
  static Builder tapping(MobEffect effect) {
    return new Builder(TappingEffect::new, effect);
  }

  /** Creates a builder for an effect applied on storing */
  static Builder storing(MobEffect effect) {
    return new Builder(StoringEffect::new, effect);
  }


  /** Gets the effect to apply */
  MobEffect effect();

  /** Gets the base level of effect to apply. */
  int base();

  /** Gets the amount of levels to add every level. */
  int multiplier();

  /** Gets how often the effect is reapplied */
  int frequency();

  /** Gets how often the effect is reapplied */
  int duration();

  /** If true, power is stored every tick. If false, power is only stored if the effect was added this tick. */
  boolean alwaysStore();

  /** Call on update to remove the existing effect */
  default void removeEffect(LivingEntity entity) {
    MobEffectInstance instance = entity.getEffect(effect());
    // do our best to guess that this effect came from us
    if (instance != null && !instance.isVisible() && !instance.showIcon() && instance.isAmbient()) {
      entity.removeEffect(effect());
    }
  }

  /** Creates a new effect instance */
  private MobEffectInstance makeEffect(int level) {
    return new MobEffectInstance(effect(), duration(), base() + multiplier() * level - 1, true, false, false);
  }

  /** Applies the effect to the target */
  default int applyEffect(LivingEntity entity, int level) {
    // add a new effect every so often
    if (entity.tickCount % frequency() == 0) {
      MobEffectInstance instance = makeEffect(level);
      instance.setCurativeItems(new ArrayList<>());
      if (entity.addEffect(instance)) {
        return multiplier() > 0 ? level : 1;
      }

    // if not adding the effect and we want to store every tick, ensure its at least possible apply
    } else if (alwaysStore() && entity.canBeAffected(makeEffect(level))) {
      return multiplier() > 0 ? level : 1;
    }
    return 0;
  }

  /** Gets the component to display in {@link #getTooltip(MetalPower, LivingEntity, int, List)} */
  default Component effectComponent(int metalLevel) {
    MobEffect effect = effect();
    MutableComponent name = Component.translatable(effect.getDescriptionId());
    int level = base() + multiplier() * metalLevel;
    if (level > 1) {
      name = name.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + level));
    }
    return Component.translatable(KEY_EFFECT, name).withStyle(effect.isBeneficial() ? ChatFormatting.BLUE : ChatFormatting.RED);
  }

  /** Creates a standard loader for a mob effect metal effect */
  static RecordLoadable<MobEffectMetalEffect> makeLoader(Function6<MobEffect,Integer,Integer,Integer,Integer,Boolean,MobEffectMetalEffect> constructor) {
    return RecordLoadable.create(
      Loadables.MOB_EFFECT.requiredField("effect", MobEffectMetalEffect::effect),
      IntLoadable.FROM_ZERO.defaultField("base", 0, MobEffectMetalEffect::base),
      IntLoadable.FROM_ZERO.defaultField("multiplier", 0, MobEffectMetalEffect::multiplier),
      IntLoadable.FROM_ONE.defaultField("frequency", 20, MobEffectMetalEffect::frequency),
      IntLoadable.FROM_ONE.defaultField("duration", 40, MobEffectMetalEffect::duration),
      BooleanLoadable.INSTANCE.requiredField("always_store", MobEffectMetalEffect::alwaysStore),
      constructor
    );
  }

  /** Effect that applies on tapping */
  record TappingEffect(MobEffect effect, int base, int multiplier, int frequency, int duration, boolean alwaysStore) implements MobEffectMetalEffect {
    public static final RecordLoadable<MobEffectMetalEffect> LOADER = makeLoader(TappingEffect::new);

    @Override
    public RecordLoadable<MobEffectMetalEffect> getLoader() {
      return LOADER;
    }

    @Override
    public void onChange(MetalPower power, LivingEntity entity, int level, int previous) {
      if (level <= 0 && previous > 0) {
        removeEffect(entity);
      }
    }

    @Override
    public int onTap(MetalPower power, LivingEntity entity, int level) {
      return applyEffect(entity, level);
    }

    @Override
    public int onStore(MetalPower power, LivingEntity entity, int level) {
      return 0;
    }

    @Override
    public void getTooltip(MetalPower power, LivingEntity entity, int level, List<Component> tooltip) {
      if (level > 0) {
        tooltip.add(effectComponent(level));
      }
    }
  }

  /** Effect that applies on storing */
  record StoringEffect(MobEffect effect, int base, int multiplier, int frequency, int duration, boolean alwaysStore) implements MobEffectMetalEffect {
    public static final RecordLoadable<MobEffectMetalEffect> LOADER = makeLoader(StoringEffect::new);

    @Override
    public RecordLoadable<MobEffectMetalEffect> getLoader() {
      return LOADER;
    }

    @Override
    public int onStore(MetalPower power, LivingEntity entity, int level) {
      return applyEffect(entity, level);
    }

    @Override
    public int onTap(MetalPower power, LivingEntity entity, int level) {
      return 0;
    }

    @Override
    public void onChange(MetalPower power, LivingEntity entity, int level, int previous) {
      if (level >= 0 && previous < 0) {
        removeEffect(entity);
      }
    }

    @Override
    public void getTooltip(MetalPower power, LivingEntity entity, int level, List<Component> tooltip) {
      if (level < 0) {
        tooltip.add(effectComponent(-level));
      }
    }
  }

  /** Builder for a metal effect */
  class Builder {
    private final Function6<MobEffect,Integer,Integer,Integer,Integer,Boolean,MobEffectMetalEffect> constructor;
    private final MobEffect effect;
    private int frequency = 20;
    private int duration = 40;
    private boolean alwaysStore = false;

    private Builder(Function6<MobEffect,Integer,Integer,Integer,Integer,Boolean,MobEffectMetalEffect> constructor, MobEffect effect) {
      this.constructor = constructor;
      this.effect = effect;
    }

    /** Causes the effect to store every tick */
    public Builder alwaysStore() {
      this.alwaysStore = true;
      return this;
    }

    /** Sets the frequency */
    public Builder frequency(int frequency) {
      this.frequency = frequency;
      return this;
    }

    /** Sets the duration */
    public Builder duration(int duration) {
      this.duration = duration;
      return this;
    }

    /** Builds the final effect */
    public MobEffectMetalEffect build(int base, int multiplier) {
      return constructor.apply(effect, base, multiplier, frequency, duration, alwaysStore);
    }

    /** Creates an effect with a flat value */
    public MobEffectMetalEffect flat(int value) {
      return build(value, 0);
    }

    /** Creates an effect that scales with the amount being tapped */
    public MobEffectMetalEffect eachLevel(int value) {
      return build(0, value);
    }
  }
}
