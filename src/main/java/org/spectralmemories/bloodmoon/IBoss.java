//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.spectralmemories.bloodmoon;

import org.bukkit.entity.Monster;

public interface IBoss {
    void Start();
    void Announce();
    void Kill(boolean reward);
    void Kill(boolean reward, boolean effects);
    void Kill(boolean reward, boolean effects, boolean respawn);

    Monster GetHost();

    String GetName();
}
