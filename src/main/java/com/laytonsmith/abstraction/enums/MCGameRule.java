package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.MEnum;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.natives.interfaces.Mixed;

@MEnum("com.commandhelper.GameRule")
public enum MCGameRule {
	ANNOUNCEADVANCEMENTS("announceAdvancements"),
	COMMANDBLOCKOUTPUT("commandBlockOutput"),
	DISABLEELYTRAMOVEMENTCHECK("disableElytraMovementCheck"),
	DISABLERAIDS("disableRaids"),
	DODAYLIGHTCYCLE("doDaylightCycle"),
	DOENTITYDROPS("doEntityDrops"),
	DOFIRETICK("doFireTick"),
	DOIMMEDIATERESPAWN("doImmediateRespawn"),
	DOINSOMNIA("doInsomnia"),
	DOLIMITEDCRAFTING("doLimitedCrafting"),
	DOMOBLOOT("doMobLoot"),
	DOMOBSPAWNING("doMobSpawning"),
	DOPATROLSPAWNING("doPatrolSpawning"),
	DOTILEDROPS("doTileDrops"),
	DOTRADERSPAWNING("doTraderSpawning"),
	DOWEATHERCYCLE("doWeatherCycle"),
	DROWNINGDAMAGE("drowningDamage"),
	FALLDAMAGE("fallDamage"),
	FIREDAMAGE("fireDamage"),
	FORGIVEDEADPLAYERS("forgiveDeadPlayers"),
	FREEZEDAMAGE("freezeDamage"),
	KEEPINVENTORY("keepInventory"),
	LOGADMINCOMMANDS("logAdminCommands"),
	MAXCOMMANDCHAINLENGTH("maxCommandChainLength", CInt.class),
	MAXENTITYCRAMMING("maxEntityCramming", CInt.class),
	MOBGRIEFING("mobGriefing"),
	NATURALREGENERATION("naturalRegeneration"),
	PLAYERSSLEEPINGPERCENTAGE("playersSleepingPercentage", CInt.class),
	RANDOMTICKSPEED("randomTickSpeed", CInt.class),
	REDUCEDDEBUGINFO("reducedDebugInfo"),
	SENDCOMMANDFEEDBACK("sendCommandFeedback"),
	SHOWDEATHMESSAGES("showDeathMessages"),
	SPAWNRADIUS("spawnRadius", CInt.class),
	SPECTATORSGENERATECHUNKS("spectatorsGenerateChunks"),
	UNIVERSALANGER("universalAnger"),
	DOWARDENSPAWNING("doWardenSpawning");

	private final String gameRule;
	private final Class<? extends Mixed> ruleType;

	MCGameRule(String gameRule) {
		this.gameRule = gameRule;
		this.ruleType = CBoolean.class;
	}

	MCGameRule(String gameRule, Class<? extends Mixed> type) {
		this.gameRule = gameRule;
		this.ruleType = type;
	}

	public String getGameRule() {
		return this.gameRule;
	}

	public Class<? extends Mixed> getRuleType() {
		return this.ruleType;
	}
}
