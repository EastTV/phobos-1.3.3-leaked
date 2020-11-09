package me.earth.phobos.features.modules.misc;

import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.TextUtil;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketClientSettings;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.client.CPacketEnchantItem;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.client.CPacketKeepAlive;
import net.minecraft.network.play.client.CPacketPlaceRecipe;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerAbilities;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketRecipeInfo;
import net.minecraft.network.play.client.CPacketResourcePackStatus;
import net.minecraft.network.play.client.CPacketSeenAdvancements;
import net.minecraft.network.play.client.CPacketSpectate;
import net.minecraft.network.play.client.CPacketSteerBoat;
import net.minecraft.network.play.client.CPacketTabComplete;
import net.minecraft.network.play.client.CPacketUpdateSign;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketVehicleMove;
import net.minecraft.network.play.server.SPacketAdvancementInfo;
import net.minecraft.network.play.server.SPacketAnimation;
import net.minecraft.network.play.server.SPacketBlockAction;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketCamera;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.network.play.server.SPacketCloseWindow;
import net.minecraft.network.play.server.SPacketCollectItem;
import net.minecraft.network.play.server.SPacketCombatEvent;
import net.minecraft.network.play.server.SPacketConfirmTransaction;
import net.minecraft.network.play.server.SPacketCooldown;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.network.play.server.SPacketCustomSound;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.network.play.server.SPacketDisplayObjective;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketEntity;
import net.minecraft.network.play.server.SPacketEntityAttach;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketEntityEquipment;
import net.minecraft.network.play.server.SPacketEntityHeadLook;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import net.minecraft.network.play.server.SPacketEntityProperties;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketEntityTeleport;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketHeldItemChange;
import net.minecraft.network.play.server.SPacketJoinGame;
import net.minecraft.network.play.server.SPacketKeepAlive;
import net.minecraft.network.play.server.SPacketMaps;
import net.minecraft.network.play.server.SPacketMoveVehicle;
import net.minecraft.network.play.server.SPacketMultiBlockChange;
import net.minecraft.network.play.server.SPacketOpenWindow;
import net.minecraft.network.play.server.SPacketParticles;
import net.minecraft.network.play.server.SPacketPlaceGhostRecipe;
import net.minecraft.network.play.server.SPacketPlayerAbilities;
import net.minecraft.network.play.server.SPacketPlayerListHeaderFooter;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketRecipeBook;
import net.minecraft.network.play.server.SPacketRemoveEntityEffect;
import net.minecraft.network.play.server.SPacketResourcePackSend;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketScoreboardObjective;
import net.minecraft.network.play.server.SPacketSelectAdvancementsTab;
import net.minecraft.network.play.server.SPacketServerDifficulty;
import net.minecraft.network.play.server.SPacketSetExperience;
import net.minecraft.network.play.server.SPacketSetPassengers;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.network.play.server.SPacketSignEditorOpen;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnExperienceOrb;
import net.minecraft.network.play.server.SPacketSpawnGlobalEntity;
import net.minecraft.network.play.server.SPacketSpawnMob;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.network.play.server.SPacketSpawnPainting;
import net.minecraft.network.play.server.SPacketSpawnPlayer;
import net.minecraft.network.play.server.SPacketSpawnPosition;
import net.minecraft.network.play.server.SPacketStatistics;
import net.minecraft.network.play.server.SPacketTabComplete;
import net.minecraft.network.play.server.SPacketTeams;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.network.play.server.SPacketUnloadChunk;
import net.minecraft.network.play.server.SPacketUpdateBossInfo;
import net.minecraft.network.play.server.SPacketUpdateHealth;
import net.minecraft.network.play.server.SPacketUpdateScore;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.network.play.server.SPacketUseBed;
import net.minecraft.network.play.server.SPacketWindowItems;
import net.minecraft.network.play.server.SPacketWindowProperty;
import net.minecraft.network.play.server.SPacketWorldBorder;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AntiPackets extends Module {

    private Setting<Mode> mode = register(new Setting("Packets", Mode.CLIENT));
    private Setting<Integer> page = register(new Setting("SPackets",1, 1, 10, v -> mode.getValue() == Mode.SERVER));
    private Setting<Integer> pages = register(new Setting("CPackets", 1, 1, 4, v -> mode.getValue() == Mode.CLIENT));

    private Setting<Boolean> AdvancementInfo = register(new Setting("AdvancementInfo", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 1));
    private Setting<Boolean> Animation = register(new Setting("Animation", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 1));
    private Setting<Boolean> BlockAction = register(new Setting("BlockAction", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 1));
    private Setting<Boolean> BlockBreakAnim = register(new Setting("BlockBreakAnim", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 1));
    private Setting<Boolean> BlockChange = register(new Setting("BlockChange", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 1));
    private Setting<Boolean> Camera = register(new Setting("Camera", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 1));
    private Setting<Boolean> ChangeGameState = register(new Setting("ChangeGameState", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 1));
    private Setting<Boolean> Chat = register(new Setting("Chat", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 1));

    private Setting<Boolean> ChunkData = register(new Setting("ChunkData", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 2));
    private Setting<Boolean> CloseWindow = register(new Setting("CloseWindow", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 2));
    private Setting<Boolean> CollectItem = register(new Setting("CollectItem", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 2));
    private Setting<Boolean> CombatEvent = register(new Setting("Combatevent", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 2));
    private Setting<Boolean> ConfirmTransaction = register(new Setting("ConfirmTransaction", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 2));
    private Setting<Boolean> Cooldown = register(new Setting("Cooldown", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 2));
    private Setting<Boolean> CustomPayload = register(new Setting("CustomPayload", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 2));
    private Setting<Boolean> CustomSound = register(new Setting("CustomSound", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 2));

    private Setting<Boolean> DestroyEntities = register(new Setting("DestroyEntities", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 3));
    private Setting<Boolean> Disconnect = register(new Setting("Disconnect", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 3));
    private Setting<Boolean> DisplayObjective = register(new Setting("DisplayObjective", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 3));
    private Setting<Boolean> Effect = register(new Setting("Effect", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 3));
    private Setting<Boolean> Entity = register(new Setting("Entity", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 3));
    private Setting<Boolean> EntityAttach = register(new Setting("EntityAttach", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 3));
    private Setting<Boolean> EntityEffect = register(new Setting("EntityEffect", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 3));
    private Setting<Boolean> EntityEquipment = register(new Setting("EntityEquipment", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 3));

    private Setting<Boolean> EntityHeadLook = register(new Setting("EntityHeadLook", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 4));
    private Setting<Boolean> EntityMetadata = register(new Setting("EntityMetadata", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 4));
    private Setting<Boolean> EntityProperties = register(new Setting("EntityProperties", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 4));
    private Setting<Boolean> EntityStatus = register(new Setting("EntityStatus", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 4));
    private Setting<Boolean> EntityTeleport = register(new Setting("EntityTeleport", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 4));
    private Setting<Boolean> EntityVelocity = register(new Setting("EntityVelocity", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 4));
    private Setting<Boolean> Explosion = register(new Setting("Explosion", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 4));
    private Setting<Boolean> HeldItemChange = register(new Setting("HeldItemChange", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 4));

    private Setting<Boolean> JoinGame = register(new Setting("JoinGame", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 5));
    private Setting<Boolean> KeepAlive = register(new Setting("KeepAlive", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 5));
    private Setting<Boolean> Maps = register(new Setting("Maps", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 5));
    private Setting<Boolean> MoveVehicle = register(new Setting("MoveVehicle", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 5));
    private Setting<Boolean> MultiBlockChange = register(new Setting("MultiBlockChange", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 5));
    private Setting<Boolean> OpenWindow = register(new Setting("OpenWindow", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 5));
    private Setting<Boolean> Particles = register(new Setting("Particles", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 5));
    private Setting<Boolean> PlaceGhostRecipe = register(new Setting("PlaceGhostRecipe", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 5));

    private Setting<Boolean> PlayerAbilities = register(new Setting("PlayerAbilities", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 6));
    private Setting<Boolean> PlayerListHeaderFooter = register(new Setting("PlayerListHeaderFooter", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 6));
    private Setting<Boolean> PlayerListItem = register(new Setting("PlayerListItem", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 6));
    private Setting<Boolean> PlayerPosLook = register(new Setting("PlayerPosLook", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 6));
    private Setting<Boolean> RecipeBook = register(new Setting("RecipeBook", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 6));
    private Setting<Boolean> RemoveEntityEffect = register(new Setting("RemoveEntityEffect", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 6));
    private Setting<Boolean> ResourcePackSend = register(new Setting("ResourcePackSend", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 6));
    private Setting<Boolean> Respawn = register(new Setting("Respawn", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 6));

    private Setting<Boolean> ScoreboardObjective = register(new Setting("ScoreboardObjective", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 7));
    private Setting<Boolean> SelectAdvancementsTab = register(new Setting("SelectAdvancementsTab", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 7));
    private Setting<Boolean> ServerDifficulty = register(new Setting("ServerDifficulty", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 7));
    private Setting<Boolean> SetExperience = register(new Setting("SetExperience", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 7));
    private Setting<Boolean> SetPassengers = register(new Setting("SetPassengers", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 7));
    private Setting<Boolean> SetSlot = register(new Setting("SetSlot", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 7));
    private Setting<Boolean> SignEditorOpen = register(new Setting("SignEditorOpen", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 7));
    private Setting<Boolean> SoundEffect = register(new Setting("SoundEffect", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 7));

    private Setting<Boolean> SpawnExperienceOrb = register(new Setting("SpawnExperienceOrb", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 8));
    private Setting<Boolean> SpawnGlobalEntity = register(new Setting("SpawnGlobalEntity", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 8));
    private Setting<Boolean> SpawnMob = register(new Setting("SpawnMob", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 8));
    private Setting<Boolean> SpawnObject = register(new Setting("SpawnObject", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 8));
    private Setting<Boolean> SpawnPainting = register(new Setting("SpawnPainting", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 8));
    private Setting<Boolean> SpawnPlayer = register(new Setting("SpawnPlayer", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 8));
    private Setting<Boolean> SpawnPosition = register(new Setting("SpawnPosition", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 8));
    private Setting<Boolean> Statistics = register(new Setting("Statistics", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 8));

    private Setting<Boolean> TabComplete = register(new Setting("TabComplete", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 9));
    private Setting<Boolean> Teams = register(new Setting("Teams", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 9));
    private Setting<Boolean> TimeUpdate = register(new Setting("TimeUpdate", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 9));
    private Setting<Boolean> Title = register(new Setting("Title", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 9));
    private Setting<Boolean> UnloadChunk = register(new Setting("UnloadChunk", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 9));
    private Setting<Boolean> UpdateBossInfo = register(new Setting("UpdateBossInfo", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 9));
    private Setting<Boolean> UpdateHealth = register(new Setting("UpdateHealth", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 9));
    private Setting<Boolean> UpdateScore = register(new Setting("UpdateScore", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 9));

    private Setting<Boolean> UpdateTileEntity = register(new Setting("UpdateTileEntity", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 10));
    private Setting<Boolean> UseBed = register(new Setting("UseBed", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 10));
    private Setting<Boolean> WindowItems = register(new Setting("WindowItems", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 10));
    private Setting<Boolean> WindowProperty = register(new Setting("WindowProperty", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 10));
    private Setting<Boolean> WorldBorder = register(new Setting("WorldBorder", false, v -> mode.getValue() == Mode.SERVER && page.getValue() == 10));

    private Setting<Boolean> Animations = register(new Setting("Animations", false, v -> mode.getValue() == Mode.CLIENT && pages.getValue() == 1));
    private Setting<Boolean> ChatMessage = register(new Setting("ChatMessage", false, v -> mode.getValue() == Mode.CLIENT && pages.getValue() == 1));
    private Setting<Boolean> ClickWindow = register(new Setting("ClickWindow", false, v -> mode.getValue() == Mode.CLIENT && pages.getValue() == 1));
    private Setting<Boolean> ClientSettings = register(new Setting("ClientSettings", false, v -> mode.getValue() == Mode.CLIENT && pages.getValue() == 1));
    private Setting<Boolean> ClientStatus = register(new Setting("ClientStatus", false, v -> mode.getValue() == Mode.CLIENT && pages.getValue() == 1));
    private Setting<Boolean> CloseWindows = register(new Setting("CloseWindows", false, v -> mode.getValue() == Mode.CLIENT && pages.getValue() == 1));
    private Setting<Boolean> ConfirmTeleport = register(new Setting("ConfirmTeleport", false, v -> mode.getValue() == Mode.CLIENT && pages.getValue() == 1));
    private Setting<Boolean> ConfirmTransactions = register(new Setting("ConfirmTransactions", false, v -> mode.getValue() == Mode.CLIENT && pages.getValue() == 1));

    private Setting<Boolean> CreativeInventoryAction = register(new Setting("CreativeInventoryAction", false, v -> mode.getValue() == Mode.CLIENT && pages.getValue() == 2));
    private Setting<Boolean> CustomPayloads = register(new Setting("CustomPayloads", false, v -> mode.getValue() == Mode.CLIENT && pages.getValue() == 2));
    private Setting<Boolean> EnchantItem = register(new Setting("EnchantItem", false, v -> mode.getValue() == Mode.CLIENT && pages.getValue() == 2));
    private Setting<Boolean> EntityAction = register(new Setting("EntityAction", false, v -> mode.getValue() == Mode.CLIENT && pages.getValue() == 2));
    private Setting<Boolean> HeldItemChanges = register(new Setting("HeldItemChanges", false, v -> mode.getValue() == Mode.CLIENT && pages.getValue() == 2));
    private Setting<Boolean> Input = register(new Setting("Input", false, v -> mode.getValue() == Mode.CLIENT && pages.getValue() == 2));
    private Setting<Boolean> KeepAlives = register(new Setting("KeepAlives", false, v -> mode.getValue() == Mode.CLIENT && pages.getValue() == 2));
    private Setting<Boolean> PlaceRecipe = register(new Setting("PlaceRecipe", false, v -> mode.getValue() == Mode.CLIENT && pages.getValue() == 2));

    private Setting<Boolean> Player = register(new Setting("Player", false, v -> mode.getValue() == Mode.CLIENT && pages.getValue() == 3));
    private Setting<Boolean> PlayerAbility = register(new Setting("PlayerAbility", false, v -> mode.getValue() == Mode.CLIENT && pages.getValue() == 3));
    private Setting<Boolean> PlayerDigging = register(new Setting("PlayerDigging", false, v -> mode.getValue() == Mode.CLIENT && page.getValue() == 3));
    private Setting<Boolean> PlayerTryUseItem = register(new Setting("PlayerTryUseItem", false, v -> mode.getValue() == Mode.CLIENT && pages.getValue() == 3));
    private Setting<Boolean> PlayerTryUseItemOnBlock = register(new Setting("TryUseItemOnBlock", false, v -> mode.getValue() == Mode.CLIENT && pages.getValue() == 3));
    private Setting<Boolean> RecipeInfo = register(new Setting("RecipeInfo", false, v -> mode.getValue() == Mode.CLIENT && pages.getValue() == 3));
    private Setting<Boolean> ResourcePackStatus = register(new Setting("ResourcePackStatus", false, v -> mode.getValue() == Mode.CLIENT && pages.getValue() == 3));
    private Setting<Boolean> SeenAdvancements = register(new Setting("SeenAdvancements", false, v -> mode.getValue() == Mode.CLIENT && pages.getValue() == 3));

    private Setting<Boolean> PlayerPackets = register(new Setting("PlayerPackets", false, v -> mode.getValue() == Mode.CLIENT && pages.getValue() == 4));
    private Setting<Boolean> Spectate = register(new Setting("Spectate", false, v -> mode.getValue() == Mode.CLIENT && pages.getValue() == 4));
    private Setting<Boolean> SteerBoat = register(new Setting("SteerBoat", false, v -> mode.getValue() == Mode.CLIENT && pages.getValue() == 4));
    private Setting<Boolean> TabCompletion = register(new Setting("TabCompletion", false, v -> mode.getValue() == Mode.CLIENT && pages.getValue() == 4));
    private Setting<Boolean> UpdateSign = register(new Setting("UpdateSign", false, v -> mode.getValue() == Mode.CLIENT && pages.getValue() == 4));
    private Setting<Boolean> UseEntity = register(new Setting("UseEntity", false, v -> mode.getValue() == Mode.CLIENT && pages.getValue() == 4));
    private Setting<Boolean> VehicleMove = register(new Setting("VehicleMove", false, v -> mode.getValue() == Mode.CLIENT && pages.getValue() == 4));

    private int hudAmount = 0;

    public AntiPackets() {
        super("AntiPackets", "Blocks Packets", Category.MISC, true, false, false);
    }

    public enum Mode {
        CLIENT, SERVER
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {

        if (!this.isEnabled()) {
            return;
        }

        if (event.getPacket() instanceof CPacketAnimation && Animations.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof CPacketChatMessage && ChatMessage.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof CPacketClickWindow && ClickWindow.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof CPacketClientSettings && ClientSettings.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof CPacketClientStatus && ClientStatus.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof CPacketCloseWindow && CloseWindows.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof CPacketConfirmTeleport && ConfirmTeleport.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof CPacketConfirmTransaction && ConfirmTransactions.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof CPacketCreativeInventoryAction && CreativeInventoryAction.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof CPacketCustomPayload && CustomPayloads.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof CPacketEnchantItem && EnchantItem.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof CPacketEntityAction && EntityAction.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof CPacketHeldItemChange && HeldItemChanges.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof CPacketInput && Input.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof CPacketKeepAlive && KeepAlives.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof CPacketPlaceRecipe && PlaceRecipe.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof CPacketPlayer && Player.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof CPacketPlayerAbilities && PlayerAbility.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof CPacketPlayerDigging && PlayerDigging.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof CPacketPlayerTryUseItem && PlayerTryUseItem.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock && PlayerTryUseItemOnBlock.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof CPacketRecipeInfo && RecipeInfo.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof CPacketResourcePackStatus && ResourcePackStatus.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof CPacketSeenAdvancements && SeenAdvancements.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof CPacketSpectate && Spectate.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof CPacketSteerBoat && SteerBoat.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof CPacketTabComplete && TabCompletion.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof CPacketUpdateSign && UpdateSign.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof CPacketUseEntity && UseEntity.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof CPacketVehicleMove && VehicleMove.getValue()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {

        if (!this.isEnabled()) {
            return;
        }

        if (event.getPacket() instanceof SPacketAdvancementInfo && AdvancementInfo.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketAnimation && Animation.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketBlockAction && BlockAction.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketBlockBreakAnim && BlockBreakAnim.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketBlockChange && BlockChange.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketCamera && Camera.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketChangeGameState && ChangeGameState.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketChat && Chat.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketChunkData && ChunkData.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketCloseWindow && CloseWindow.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketCollectItem && CollectItem.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketCombatEvent && CombatEvent.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketConfirmTransaction && ConfirmTransaction.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketCooldown && Cooldown.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketCustomPayload && CustomPayload.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketCustomSound && CustomSound.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketDestroyEntities && DestroyEntities.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketDisconnect && Disconnect.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketChunkData && ChunkData.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketCloseWindow && CloseWindow.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketCollectItem && CollectItem.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketDisplayObjective && DisplayObjective.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketEffect && Effect.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketEntity && Entity.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketEntityAttach && EntityAttach.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketEntityEffect && EntityEffect.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketEntityEquipment && EntityEquipment.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketEntityHeadLook && EntityHeadLook.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketEntityMetadata && EntityMetadata.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketEntityProperties && EntityProperties.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketEntityStatus && EntityStatus.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketEntityTeleport && EntityTeleport.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketEntityVelocity && EntityVelocity.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketExplosion && Explosion.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketHeldItemChange && HeldItemChange.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketJoinGame && JoinGame.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketKeepAlive && KeepAlive.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketMaps && Maps.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketMoveVehicle && MoveVehicle.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketMultiBlockChange && MultiBlockChange.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketOpenWindow && OpenWindow.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketParticles && Particles.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketPlaceGhostRecipe && PlaceGhostRecipe.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketPlayerAbilities && PlayerAbilities.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketPlayerListHeaderFooter && PlayerListHeaderFooter.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketPlayerListItem && PlayerListItem.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketPlayerPosLook && PlayerPosLook.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketRecipeBook && RecipeBook.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketRemoveEntityEffect && RemoveEntityEffect.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketResourcePackSend && ResourcePackSend.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketRespawn && Respawn.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketScoreboardObjective && ScoreboardObjective.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketSelectAdvancementsTab && SelectAdvancementsTab.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketServerDifficulty && ServerDifficulty.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketSetExperience && SetExperience.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketSetPassengers && SetPassengers.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketSetSlot && SetSlot.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketSignEditorOpen && SignEditorOpen.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketSoundEffect && SoundEffect.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketSpawnExperienceOrb && SpawnExperienceOrb.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketSpawnGlobalEntity && SpawnGlobalEntity.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketSpawnMob && SpawnMob.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketSpawnObject && SpawnObject.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketSpawnPainting && SpawnPainting.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketSpawnPlayer && SpawnPlayer.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketSpawnPosition && SpawnPosition.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketStatistics && Statistics.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketTabComplete && TabComplete.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketTeams && Teams.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketTimeUpdate && TimeUpdate.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketTitle && Title.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketUnloadChunk && UnloadChunk.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketUpdateBossInfo && UpdateBossInfo.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketUpdateHealth && UpdateHealth.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketUpdateScore && UpdateScore.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketUpdateTileEntity && UpdateTileEntity.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketUseBed && UseBed.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketWindowItems && WindowItems.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketWindowProperty && WindowProperty.getValue()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketWorldBorder && WorldBorder.getValue()) {
            event.setCanceled(true);
        }
    }

    @Override
    public void onEnable() {
        String standart = TextUtil.GREEN + "AntiPackets On!" + TextUtil.WHITE + " Cancelled Packets: ";
        StringBuilder text = new StringBuilder(standart);
        if (!this.settings.isEmpty()) {
            for (Setting setting : this.settings) {
                if(!(setting.getValue() instanceof Boolean) || !((boolean)setting.getValue()) || setting.getName().equalsIgnoreCase("Enabled") || setting.getName().equalsIgnoreCase("drawn")) {
                    continue;
                }
                String name = setting.getName();
                text.append(name).append(", ");
            }
        }

        if(text.toString().equals(standart)) {
            Command.sendMessage(TextUtil.GREEN + "AntiPackets On!" + TextUtil.WHITE + " Currently not cancelling any Packets.");
        } else {
            String output = removeLastChar(removeLastChar(text.toString()));
            Command.sendMessage(output);
        }
    }

    @Override
    public void onUpdate() {
        int amount = 0;
        if (!this.settings.isEmpty()) {
            for (Setting setting : this.settings) {
                if (!(setting.getValue() instanceof Boolean) || !((boolean)setting.getValue()) || setting.getName().equalsIgnoreCase("Enabled") || setting.getName().equalsIgnoreCase("drawn")) {
                    continue;
                }
                amount++;
            }
        }
        hudAmount = amount;
    }

    @Override
    public String getDisplayInfo() {
        if (hudAmount == 0) {
            return "";
        }
        return hudAmount + (hudAmount == 1 ? " Packet" : " Packets");
    }

    public String removeLastChar(String str) {
        if (str != null && str.length() > 0) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }
}
