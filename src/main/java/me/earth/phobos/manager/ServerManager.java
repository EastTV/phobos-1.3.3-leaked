package me.earth.phobos.manager;

import me.earth.phobos.features.Feature;
import me.earth.phobos.features.modules.client.Managers;
import me.earth.phobos.util.Timer;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Objects;

public class ServerManager extends Feature {

    private float TPS = 20.0f;
    private long lastUpdate = -1;
    private final float[] tpsCounts = new float[10];
    private final DecimalFormat format = new DecimalFormat("##.00#");
    private String serverBrand = "";
    private final Timer timer = new Timer();

    public void onPacketReceived() {
        timer.reset();
    }

    public boolean isServerNotResponding() {
        return timer.passedMs(Managers.getInstance().respondTime.getValue());
    }

    public long serverRespondingTime() {
        return timer.getPassedTimeMs();
    }

    public void update() {
        long currentTime = System.currentTimeMillis();
        if (lastUpdate == -1) {
            lastUpdate = currentTime;
            return;
        }
        long timeDiff = currentTime - lastUpdate;
        float tickTime = timeDiff / 20.0f;
        if (tickTime == 0) {
            tickTime = 50;
        }
        float tps = 1000 / tickTime;
        if (tps > 20.0f) {
            tps = 20.00f;
        }
        System.arraycopy(tpsCounts, 0, tpsCounts, 1, tpsCounts.length - 1);
        tpsCounts[0] = tps;
        double total = 0.0;
        for (float f : tpsCounts) {
            total += f;
        }
        total /= tpsCounts.length;

        if (total > 20.0) {
            total = 20.0;
        }

        TPS = Float.parseFloat(format.format(total));
        lastUpdate = currentTime;
    }

    public void reset() {
        Arrays.fill(tpsCounts, 20.0f);
        TPS = 20.0f;
    }

    public float getTpsFactor() {
        return  20.0f / this.TPS;
    }

    public float getTPS() {
        return this.TPS;
    }

    public String getServerBrand() {
        return this.serverBrand;
    }

    public void setServerBrand(String brand) {
        this.serverBrand = brand;
    }

    public int getPing() {
        if(fullNullCheck()) {
            return 0;
        }

        try {
            return Objects.requireNonNull(mc.getConnection()).getPlayerInfo(mc.getConnection().getGameProfile().getId()).getResponseTime();
        } catch(Exception e) {
            return 0;
        }
    }
}
