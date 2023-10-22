package org.ladysnake.blabber.impl.compat;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlabberMixinPlugin implements IMixinConfigPlugin {
    private static final String COMPAT_PREFIX = "org.ladysnake.blabber.mixin.compat.";
    private static final Pattern COMPAT_MIXIN_PATTERN = Pattern.compile(Pattern.quote(COMPAT_PREFIX) + "(?<modid>[a-z_]+?)\\..*");
    private final FabricLoader loader = FabricLoader.getInstance();

    @Override
    public void onLoad(String mixinPackage) {
        // NO-OP
    }

    @Override
    public @Nullable String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!mixinClassName.startsWith(COMPAT_PREFIX)) return true;
        Matcher matcher = COMPAT_MIXIN_PATTERN.matcher(mixinClassName);
        if (!matcher.matches()) throw new IllegalStateException("Bad compat mixin name " + mixinClassName);
        String modId = matcher.group("modid");
        return loader.isModLoaded(modId);
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        // NO-OP
    }

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // NO-OP
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // NO-OP
    }
}
