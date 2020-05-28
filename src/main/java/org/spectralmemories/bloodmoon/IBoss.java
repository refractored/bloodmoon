//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.spectralmemories.bloodmoon;

import org.bukkit.entity.LivingEntity;

public interface IBoss {
    void Start();
    void Announce();
    void Kill(boolean var1);
    void Kill(boolean var1, boolean var2);
    void Kill(boolean var1, boolean var2, boolean var3);

    LivingEntity GetHost();

    String GetName();
}
