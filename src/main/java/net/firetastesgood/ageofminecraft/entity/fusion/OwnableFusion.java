// Put this in its own file, e.g. net.firetastesgood.ageofminecraft.entity.fusion.OwnableFusion.java
package net.firetastesgood.ageofminecraft.entity.fusion;

import net.minecraft.world.entity.player.Player;

public interface OwnableFusion {
    void setFusionOwner(Player p);
    void setFusionFirstSpawnXP(int xp);
    void setFusionAttackOverride(double value);
}