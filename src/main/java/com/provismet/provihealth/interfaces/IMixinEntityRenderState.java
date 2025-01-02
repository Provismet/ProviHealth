package com.provismet.provihealth.interfaces;

import com.provismet.provihealth.util.HealthContainer;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IMixinEntityRenderState {
    void provi_Health$setShouldRenderHealth (boolean value);
    void provi_Health$setHealth (HealthContainer container);
    void provi_Health$setMountHealth (HealthContainer container);
    void provi_Health$setIsLiving (boolean value);
    void provi_Health$setTitles (List<Text> titles);
    void provi_Health$setShouldRenderLabel (boolean value);
    void provi_Health$setLabel (Text label);
    void provi_Health$setTeamColour (Integer colour);

    boolean provi_Health$shouldRenderHealth ();
    HealthContainer provi_Health$getHealth ();
    HealthContainer provi_Health$getMountHealth ();
    boolean provi_Health$isLiving ();
    List<Text> provi_Health$getTitles ();
    boolean provi_Health$shouldRenderLabel ();
    Text provi_Health$getLabel ();
    @Nullable Integer provi_Health$getTeamColour ();
}
