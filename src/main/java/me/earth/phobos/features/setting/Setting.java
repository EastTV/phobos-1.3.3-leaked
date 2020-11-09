package me.earth.phobos.features.setting;

import me.earth.phobos.event.events.ClientEvent;
import me.earth.phobos.features.Feature;
import net.minecraftforge.common.MinecraftForge;

import java.util.function.Predicate;

//TODO: visibility
public class Setting<T> {

    private final String name;

    private final T defaultValue;
    private T value;
    private T plannedValue;

    private T min;
    private T max;
    private boolean hasRestriction;

    private Predicate<T> visibility;

    private String description;
    private Feature feature;

    public Setting(String name, T defaultValue)  {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.plannedValue = defaultValue;
        this.description = "";
    }

    public Setting(String name, T defaultValue, String description)  {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.plannedValue = defaultValue;
        this.description = description;
    }

    public Setting(String name, T defaultValue, T min, T max, String description)  {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.min = min;
        this.max = max;
        this.plannedValue = defaultValue;
        this.description = description;
        this.hasRestriction = true;
    }

    public Setting(String name, T defaultValue, T min, T max)  {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.min = min;
        this.max = max;
        this.plannedValue = defaultValue;
        this.description = "";
        this.hasRestriction = true;
    }

    public Setting(String name, T defaultValue, T min, T max, Predicate<T> visibility, String description)  {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.min = min;
        this.max = max;
        this.plannedValue = defaultValue;
        this.visibility = visibility;
        this.description = description;
        this.hasRestriction = true;
    }

    public Setting(String name, T defaultValue, T min, T max, Predicate<T> visibility)  {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.min = min;
        this.max = max;
        this.plannedValue = defaultValue;
        this.visibility = visibility;
        this.description = "";
        this.hasRestriction = true;
    }

    public Setting(String name, T defaultValue, Predicate<T> visibility)  {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.visibility = visibility;
        this.plannedValue = defaultValue;
    }

    public String getName() {
        return this.name;
    }

    public T getValue() {
        return this.value;
    }

    public T getPlannedValue() {
        return this.plannedValue;
    }

    public void setPlannedValue(T value) {
        this.plannedValue = value;
    }

    public T getMin() {
        return this.min;
    }

    public T getMax() {
        return this.max;
    }

    public void setValue(T value) {
        this.setPlannedValue(value);
        if(hasRestriction) {
            if(((Number)min).floatValue() > ((Number)value).floatValue()) {
                this.setPlannedValue(min);
            }

            if(((Number)max).floatValue() < ((Number)value).floatValue()) {
                this.setPlannedValue(max);
            }
        }
        ClientEvent event = new ClientEvent(this);
        MinecraftForge.EVENT_BUS.post(event);
        if(!event.isCanceled()) {
            this.value = this.plannedValue;
        } else {
            this.plannedValue = this.value;
        }
    }

    public void setValueNoEvent(T value) {
        this.setPlannedValue(value);
        if(hasRestriction) {
            if(((Number)min).floatValue() > ((Number)value).floatValue()) {
                this.setPlannedValue(min);
            }

            if(((Number)max).floatValue() < ((Number)value).floatValue()) {
                this.setPlannedValue(max);
            }
        }
        this.value = this.plannedValue;
    }

    public void setMin(T min) {
        this.min = min;
    }

    public void setMax(T max) {
        this.max = max;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public Feature getFeature() {
        return this.feature;
    }

    public int getEnum(String input) {
        for (int i = 0; i < this.value.getClass().getEnumConstants().length; i++) {
            final Enum e = (Enum)this.value.getClass().getEnumConstants()[i];
            if (e.name().equalsIgnoreCase(input)) {
                return i;
            }
        }
        return -1;
    }

    public void setEnumValue(String value) {
        for (Enum e : ((Enum) this.value).getClass().getEnumConstants()) {
            if (e.name().equalsIgnoreCase(value)) {
                this.value = (T)e;
            }
        }
    }

    public String currentEnumName() {
        return EnumConverter.getProperName((Enum)this.value);
    }

    public int currentEnum() {
        return EnumConverter.currentEnum((Enum)this.value);
    }

    public void increaseEnum() {
        this.plannedValue = (T)EnumConverter.increaseEnum((Enum)this.value);
        ClientEvent event = new ClientEvent(this);
        MinecraftForge.EVENT_BUS.post(event);
        if(!event.isCanceled()) {
            this.value = this.plannedValue;
        } else {
            this.plannedValue = this.value;
        }

    }

    public void increaseEnumNoEvent() {
        this.value = (T)EnumConverter.increaseEnum((Enum)this.value);
    }

    public String getType() {
        if(this.isEnumSetting()) {
            return "Enum";
        }
        return this.getClassName(this.defaultValue);
    }

    public <T> String getClassName(T value) {
        return value.getClass().getSimpleName();
    }

    public String getDescription() {
        if(description == null) {
            return "";
        }
        return this.description;
    }

    public boolean isNumberSetting() {
        return (value instanceof Double || value instanceof Integer || value instanceof Short || value instanceof Long || value instanceof Float);
    }

    public boolean isEnumSetting() {
        return !isNumberSetting() && !(value instanceof String) && !(value instanceof Bind) && !(value instanceof Character) && !(value instanceof Boolean);
    }

    public boolean isStringSetting() {
        return value instanceof String;
    }

    public T getDefaultValue() {
        return this.defaultValue;
    }

    public String getValueAsString() {
        return this.value.toString();
    }

    public boolean hasRestriction() {
        return this.hasRestriction;
    }

    public void setVisibility(Predicate<T> visibility) {
        this.visibility = visibility;
    }

    public boolean isVisible() {
        if(visibility == null) {
            return true;
        }
        return visibility.test(getValue());
    }
	
}
