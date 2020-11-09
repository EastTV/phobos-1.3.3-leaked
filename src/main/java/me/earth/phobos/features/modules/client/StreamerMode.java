package me.earth.phobos.features.modules.client;

import me.earth.phobos.Phobos;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.TextUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.PotionEffect;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class StreamerMode extends Module {

    private SecondScreenFrame window = null;

    public StreamerMode() {
        super("StreamerMode", "Displays client info in a second window.", Category.CLIENT, false, false, false);
    }

    public Setting<Integer> width = register(new Setting("Width", 600, 100, 3160));
    public Setting<Integer> height = register(new Setting("Height", 900, 100, 2140));

    @Override
    public void onEnable() {
        EventQueue.invokeLater(() -> {
            if(window == null) {
                window = new SecondScreenFrame();
            }
            window.setVisible(true);
        });
    }

    @Override
    public void onDisable() {
        if(window != null) {
            window.setVisible(false);
        }
        window = null;
    }

    @Override
    public void onLogout() {
        if(window != null) {
            ArrayList<String> drawInfo = new ArrayList<>();
            drawInfo.add("Phobos v" + Phobos.MODVER);
            drawInfo.add("");
            drawInfo.add("No Connection.");
            window.setToDraw(drawInfo);
        }
    }

    @Override
    public void onUnload() {
        this.disable();
    }

    @Override
    public void onLoad() {
        this.disable();
    }

    @Override
    public void onUpdate() {
        if(window != null) {
            ArrayList<String> drawInfo = new ArrayList<>();
            drawInfo.add("Phobos v" + Phobos.MODVER);
            drawInfo.add("");
            drawInfo.add("Fps: " + Minecraft.debugFPS);
            drawInfo.add("TPS: " + Phobos.serverManager.getTPS());
            drawInfo.add("Ping: " + Phobos.serverManager.getPing() + "ms");
            drawInfo.add("Speed: " + Phobos.speedManager.getSpeedKpH() + "km/h");
            drawInfo.add("Time: " + (new SimpleDateFormat("h:mm a").format(new Date())));
            boolean inHell = (mc.world.getBiome(mc.player.getPosition()).getBiomeName().equals("Hell"));

            int posX = (int) mc.player.posX;
            int posY = (int) mc.player.posY;
            int posZ = (int) mc.player.posZ;

            float nether = !inHell ? 0.125f : 8;
            int hposX = (int) (mc.player.posX * nether);
            int hposZ = (int) (mc.player.posZ * nether);

            String coordinates = "XYZ " + posX + ", " + posY + ", " + posZ + " " + "[" + hposX + ", " + hposZ + "]";
            String text = Phobos.rotationManager.getDirection4D(false);
            drawInfo.add("");
            drawInfo.add(text);
            drawInfo.add(coordinates);
            drawInfo.add("");

            for (Module module : Phobos.moduleManager.sortedModules) {
                String moduleName = TextUtil.stripColor(module.getFullArrayString());
                drawInfo.add(moduleName);
            }

            drawInfo.add("");
            for(PotionEffect effect : Phobos.potionManager.getOwnPotions()) {
                String potionText = TextUtil.stripColor(Phobos.potionManager.getColoredPotionString(effect));
                drawInfo.add(potionText);
            }

            drawInfo.add("");
            Map<String, Integer> map = EntityUtil.getTextRadarPlayers();
            if (!map.isEmpty()) {
                for (Map.Entry<String, Integer> player : map.entrySet()) {
                    String playerText = TextUtil.stripColor(player.getKey());
                    drawInfo.add(playerText);
                }
            }

            window.setToDraw(drawInfo);
        }
    }

    public class SecondScreenFrame extends JFrame {

        private SecondScreen panel;

        public SecondScreenFrame() {
            initUI();
        }

        private void initUI() {
            panel = new SecondScreen();
            add(panel);
            setResizable(true);
            pack();
            setTitle("Phobos - Info");
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }

        public void setToDraw(ArrayList<String> list) {
            SecondScreenFrame.this.panel.setToDraw(list);
        }
    }

    public class SecondScreen extends JPanel {

        private final int B_WIDTH = width.getValue();
        private final int B_HEIGHT = height.getValue();

        private Font font = new Font("Verdana", Font.PLAIN, 20);

        private ArrayList<String> toDraw = new ArrayList<>();

        public void setToDraw(ArrayList<String> list) {
            this.toDraw = list;
            repaint();
        }

        public void setFont(Font font) {
            this.font = font;
        }

        public SecondScreen() {
            initBoard();
        }

        public void setWindowSize(int width, int height) {
            setPreferredSize(new Dimension(width, height));
        }

        private void initBoard() {
            setBackground(Color.black);
            setFocusable(true);
            setPreferredSize(new Dimension(B_WIDTH, B_HEIGHT));
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawScreen(g);
        }

        private void drawScreen(Graphics g){
            Font small = this.font;
            FontMetrics metr = getFontMetrics(small);

            g.setColor(Color.white);
            g.setFont(small);
            int y = 40;
            for(String msg : toDraw) {
                g.drawString(msg, (this.getWidth() - metr.stringWidth(msg)) / 2, y);
                y += 20;
            }
            Toolkit.getDefaultToolkit().sync();
        }
    }
}
