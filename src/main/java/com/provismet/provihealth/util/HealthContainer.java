package com.provismet.provihealth.util;

import net.minecraft.util.math.MathHelper;

public final class HealthContainer {
    private float maxHealth;
    private float currentHealth;
    private float previousHealth;
    private float lerpedHealth;

    public HealthContainer (float health) {
        this.currentHealth = health;
        this.previousHealth = health;
    }

    public HealthContainer (float health, float maxHealth) {
        this(health);
        this.maxHealth = maxHealth;
    }

    public void setMaxHealth (float health) {
        this.maxHealth = health;
    }

    public void set (float health) {
        this.previousHealth = this.currentHealth;
        this.currentHealth = health;
    }

    public void setFrom (HealthContainer other) {
        this.set(other.currentHealth);
        this.maxHealth = other.maxHealth;
    }

    public void lerp (float progress) {
        this.lerpedHealth = MathHelper.lerp(progress, this.previousHealth, this.currentHealth);
    }

    public float getMax () {
        return this.maxHealth;
    }

    public float getCurrent () {
        return this.currentHealth;
    }

    public float getPrevious () {
        return this.previousHealth;
    }

    public float getLerped () {
        return this.lerpedHealth;
    }

    public float getPercentage () {
        return Math.clamp(this.currentHealth / this.maxHealth, 0f, 1f);
    }
}
