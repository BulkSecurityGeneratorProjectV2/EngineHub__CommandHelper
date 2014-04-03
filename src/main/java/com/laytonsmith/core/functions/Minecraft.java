package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.entities.MCHorse;
import com.laytonsmith.abstraction.enums.MCCreeperType;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.MCEffect;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCFireworkType;
import com.laytonsmith.abstraction.enums.MCMobs;
import com.laytonsmith.abstraction.enums.MCOcelotType;
import com.laytonsmith.abstraction.enums.MCPigType;
import com.laytonsmith.abstraction.enums.MCProfession;
import com.laytonsmith.abstraction.enums.MCSkeletonType;
import com.laytonsmith.abstraction.enums.MCWolfType;
import com.laytonsmith.abstraction.enums.MCZombieType;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 */
public class Minecraft {

	public static String docs() {
		return "These functions provide a hook into game functionality.";
	}
	private static final SortedMap<String, Construct> DataValueLookup = new TreeMap<String, Construct>();
	private static final SortedMap<String, Construct> DataNameLookup = new TreeMap<String, Construct>();

	static {
		Properties p1 = new Properties();
		try {
			p1.load(Minecraft.class.getResourceAsStream("/data_values.txt"));
			Enumeration e = p1.propertyNames();
			while (e.hasMoreElements()) {
				String name = e.nextElement().toString();
				DataValueLookup.put(name, new CString(p1.getProperty(name).toString(), Target.UNKNOWN));
			}
		} catch (IOException ex) {
			Logger.getLogger(Minecraft.class.getName()).log(Level.SEVERE, null, ex);
		}

		Properties p2 = new Properties();
		try {
			p2.load(Minecraft.class.getResourceAsStream("/data_names.txt"));
			Enumeration e = p2.propertyNames();
			while (e.hasMoreElements()) {
				String name = e.nextElement().toString();
				DataNameLookup.put(name, new CString(p2.getProperty(name).toString(), Target.UNKNOWN));
			}
		} catch (IOException ex) {
			Logger.getLogger(Minecraft.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@api
	public static class data_values extends AbstractFunction {

		@Override
		public String getName() {
			return "data_values";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			if (args[0] instanceof CInt) {
				return new CInt(Static.getInt(args[0], t), t);
			} else {
				String c = args[0].val();
				int number = StaticLayer.LookupItemId(c);
				if (number != -1) {
					return new CInt(number, t);
				}
				String changed = c;
				if (changed.contains(":")) {
					//Split on that, and reverse. Change wool:red to redwool
					String split[] = changed.split(":");
					if (split.length == 2) {
						changed = split[1] + split[0];
					}
				}
				//Remove anything that isn't a letter or a number
				changed = changed.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
				//Do a lookup in the DataLookup table
				if (DataValueLookup.containsKey(changed)) {
					String split[] = DataValueLookup.get(changed).toString().split(":");
					if (split[1].equals("0")) {
						return new CInt(split[0], t);
					}
					return new CString(split[0] + ":" + split[1], t);
				}
				return CNull.NULL;
			}
		}

		@Override
		public String docs() {
			return "int {var1} Does a lookup to return the data value of a name. For instance, returns 1 for 'stone'. If an integer is given,"
					+ " simply returns that number. If the data value cannot be found, null is returned.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}
	}

	@api
	public static class data_name extends AbstractFunction {

		@Override
		public String getName() {
			return "data_name";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {int | itemArray} Performs the reverse functionality as data_values. Given 1, returns 'Stone'. Note that the enum value"
					+ " given in bukkit's Material class is what is returned as a fallback, if the id doesn't match a value in the internally maintained list."
					+ " If a completely invalid argument is passed"
					+ " in, null is returned.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			int i = -1;
			int i2 = -1;
			if (args[0] instanceof CString) {
				//We also accept item notation
				if (args[0].val().contains(":")) {
					String[] split = args[0].val().split(":");
					try {
						i = Integer.parseInt(split[0]);
						i2 = Integer.parseInt(split[1]);
					} catch (NumberFormatException e) {
					} catch (ArrayIndexOutOfBoundsException e){
						throw new Exceptions.FormatException("Incorrect format for the item notation: " + args[0].val(), t);
					}
				}
			} else if (args[0] instanceof CArray) {
				MCItemStack is = ObjectGenerator.GetGenerator().item(args[0], t);
				i = is.getTypeId();
				i2 = (int) is.getData().getData();
			}
			if (i == -1) {
				i = Static.getInt32(args[0], t);
			}
			if (i2 == -1) {
				i2 = 0;
			}
			if (DataNameLookup.containsKey(i + "_" + i2)) {
				return DataNameLookup.get(i + "_" + i2);
			} else if (DataNameLookup.containsKey(i + "_0")) {
				return DataNameLookup.get(i + "_0");
			} else {
				try {
					return new CString(StaticLayer.LookupMaterialName(i), t);
				} catch (NullPointerException e) {
					return CNull.NULL;
				}
			}
		}
	}

	@api
	public static class max_stack_size extends AbstractFunction {

		@Override
		public String getName() {
			return "max_stack_size";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "integer {itemType | itemArray} Given an item type, returns"
					+ " the maximum allowed stack size. This method will accept either"
					+ " a single data value (i.e. 278) or an item array like is returned"
					+ " from pinv(). Additionally, if a single value, it can also be in"
					+ " the old item notation (i.e. '35:11'), though for the purposes of this"
					+ " function, the data is unneccesary.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			if (args[0] instanceof CArray) {
				MCItemStack is = ObjectGenerator.GetGenerator().item(args[0], t);
				return new CInt(is.getType().getMaxStackSize(), t);
			} else {
				String item = args[0].val();
				if (item.contains(":")) {
					String[] split = item.split(":");
					item = split[0];
				}
				try {
					int iitem = Integer.parseInt(item);
					int max = StaticLayer.GetItemStack(iitem, 1).getType().getMaxStackSize();
					return new CInt(max, t);
				} catch (NumberFormatException e) {
				}
			}
			throw new ConfigRuntimeException("Improper value passed to max_stack. Expecting a number, or an item array, but received \"" + args[0].val() + "\"", ExceptionType.CastException, t);
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class spawn_mob extends AbstractFunction {

		@Override
		public String getName() {
			return "spawn_mob";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		@Override
		public String docs() {
			return "array {mobType, [qty], [location]} Spawns qty mob of one of the following types at location. qty defaults to 1, and location defaults"
					+ " to the location of the player. An array of the entity IDs spawned is returned."
					+ " ---- mobType can be one of: " + StringUtils.Join(MCMobs.values(), ", ", ", or ", " or ") + "."
					+ " Spelling matters, but capitalization doesn't. At this time, the function is limited to spawning a maximum of 50 at a time."
					+ " Further, subtypes can be applied by specifying MOBTYPE:SUBTYPE, for example the sheep subtype can be any of the dye colors: " 
					+ StringUtils.Join(MCDyeColor.values(), ", ", ", or ", " or ") + ". COLOR defaults to white if not specified. For mobs with multiple"
					+ " subtypes, separate each type with a \"-\", currently only zombies which, using ZOMBIE:TYPE1-TYPE2 can be any non-conflicting two of: "
					+ StringUtils.Join(MCZombieType.values(), ", ", ", or ", " or ") + ", but default to normal zombies. Ocelots may be one of: " 
					+ StringUtils.Join(MCOcelotType.values(), ", ", ", or ", " or ") + ", defaulting to the wild variety. Villagers can have a profession as a subtype: "
					+ StringUtils.Join(MCProfession.values(), ", ", ", or ", " or ") + ", defaulting to farmer if not specified. Skeletons can be "
					+ StringUtils.Join(MCSkeletonType.values(), ", ", ", or ", " or ") + ". PigZombies' subtype represents their anger,"
					+ " and accepts an integer, where 0 is neutral and 400 is the normal response to being attacked. Defaults to 0. Similarly, Slime"
					+ " and MagmaCube size can be set by integer, otherwise will be a random natural size. If a material is specified as the subtype"
					+ " for Endermen, they will hold that material, otherwise they will hold nothing. Creepers can be set to " 
					+ StringUtils.Join(MCCreeperType.values(), ", ", ", or ", " or ") + ", wolves can be " + StringUtils.Join(MCWolfType.values(), ", ", ", or ", " or ") 
					+ ", and pigs can be " + StringUtils.Join(MCPigType.values(), ", ", ", or ", " or ") + "."
					+ " Horses can have three different subTypes, the variant: " + StringUtils.Join(MCHorse.MCHorseVariant.values(), ", ", ", or ", " or ") + ","
					+ " the color: " + StringUtils.Join(MCHorse.MCHorseColor.values(), ", ", ", or ", " or ") + ","
					+ " and the pattern: " + StringUtils.Join(MCHorse.MCHorsePattern.values(), ", ", ", or ", " or ") + ".";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.RangeException, ExceptionType.FormatException, ExceptionType.PlayerOfflineException, ExceptionType.InvalidWorldException};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_1_2;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			String mob = args[0].val();
			String secondary = "";
			if (mob.contains(":")) {
				secondary = mob.substring(mob.indexOf(':') + 1);
				mob = mob.substring(0, mob.indexOf(':'));
			}
			int qty = 1;
			if (args.length > 1) {
				qty = Static.getInt32(args[1], t);
			}
			if (qty > 50) {
				throw new ConfigRuntimeException("A bit excessive, don't you think? Let's scale that back some, huh?",
						ExceptionType.RangeException, t);
			}
			MCLocation l;
			MCPlayer p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if (args.length == 3) {
				l = ObjectGenerator.GetGenerator().location(args[2], (p != null ? p.getWorld() : null), t);
			} else if (p != null) {
				l = p.getLocation();
			} else {
				throw new ConfigRuntimeException("Invalid sender!", ExceptionType.PlayerOfflineException, t);
			}
			try{
				return l.getWorld().spawnMob(MCMobs.valueOf(mob.toUpperCase().replaceAll(" ", "")), secondary, qty, l, t);
			} catch(IllegalArgumentException e){
				throw new ConfigRuntimeException("Invalid mob name: " + mob, ExceptionType.FormatException, t);
			}
		}
	}

	@api(environments={CommandHelperEnvironment.class})
	public static class tame_mob extends AbstractFunction {

		@Override
		public String getName() {
			return "tame_mob";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {[player], entityID} Tames the entity specified to the player. Wolves and ocelots are supported. Offline players"
					+ " are supported, but this means that partial matches are NOT supported. You must type the players name exactly. Setting"
					+ " the player to null will untame the mob. If the entity doesn't exist, nothing happens.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.UntameableMobException, ExceptionType.CastException, ExceptionType.BadEntityException};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			String player = environment.getEnv(CommandHelperEnvironment.class).GetPlayer().getName();
			Construct entityID = null;
			if (args.length == 2) {
				if (args[0] instanceof CNull) {
					player = null;
				} else {
					player = args[0].val();
				}
				entityID = args[1];
			} else {
				entityID = args[0];
			}
			int id = Static.getInt32(entityID, t);
			MCLivingEntity e = Static.getLivingEntity(id, t);
			if (e == null) {
				return CVoid.VOID;
			} else if (e instanceof MCTameable) {
				MCTameable mct = ((MCTameable) e);
				if (player != null) {
					mct.setOwner(Static.getServer().getOfflinePlayer(player));
				} else {
					mct.setOwner(null);
				}
				return CVoid.VOID;
			} else {
				throw new ConfigRuntimeException("The specified entity is not tameable", ExceptionType.UntameableMobException, t);
			}
		}
	}

	@api
	public static class get_mob_owner extends AbstractFunction {

		@Override
		public String getName() {
			return "get_mob_owner";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {entityID} Returns the owner's name, or null if the mob is unowned. An UntameableMobException is thrown if"
					+ " mob isn't tameable to begin with.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.UntameableMobException, ExceptionType.CastException, ExceptionType.BadEntityException};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			int id = Static.getInt32(args[0], t);
			MCLivingEntity e = Static.getLivingEntity(id, t);
			if (e == null) {
				return CNull.NULL;
			} else if (e instanceof MCTameable) {
				MCAnimalTamer at = ((MCTameable) e).getOwner();
				if (null != at) {
					return new CString(at.getName(), t);
				} else {
					return CNull.NULL;
				}
			} else {
				throw new ConfigRuntimeException("The specified entity is not tameable", ExceptionType.UntameableMobException, t);
			}
		}
	}

	@api
	public static class is_tameable extends AbstractFunction {

		@Override
		public String getName() {
			return "is_tameable";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {entityID} Returns true or false if the specified entity is tameable";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.BadEntityException};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			int id = Static.getInt32(args[0], t);
			MCEntity e = Static.getEntity(id, t);
			boolean ret;
			if (e == null) {
				ret = false;
			} else if (e instanceof MCTameable) {
				ret = true;
			} else {
				ret = false;
			}
			return new CBoolean(ret, t);
		}
	}

	@api(environments={CommandHelperEnvironment.class})
	public static class make_effect extends AbstractFunction {

		@Override
		public String getName() {
			return "make_effect";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {xyzArray, effect, [radius]} Plays the specified effect (sound effect) at the given location, for all players within"
					+ " the radius (or 64 by default). The effect can be one of the following: "
					+ StringUtils.Join(MCEffect.values(), ", ", ", or ", " or ")
					+ ". Additional data can be supplied with the syntax EFFECT:DATA. The RECORD_PLAY effect takes the item"
					+ " id of a disc as data, STEP_SOUND takes a blockID and SMOKE takes a direction bit (4 is upwards).";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_1_3;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			MCLocation l = ObjectGenerator.GetGenerator().location(args[0], (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer ? env.getEnv(CommandHelperEnvironment.class).GetPlayer().getWorld() : null), t);
			MCEffect e = null;
			String preEff = args[1].val();
			int data = 0;
			int radius = 64;
			if (preEff.contains(":")) {
				try {
					data = Integer.parseInt(preEff.substring(preEff.indexOf(':') + 1));
				} catch (NumberFormatException ex) {
					throw new ConfigRuntimeException("Effect data expected an integer", ExceptionType.CastException, t);
				}
				preEff = preEff.substring(0, preEff.indexOf(':'));
			}
			try {
				e = MCEffect.valueOf(preEff.toUpperCase());
			} catch (IllegalArgumentException ex) {
				throw new ConfigRuntimeException("The effect type " + args[1].val() + " is not valid", ExceptionType.FormatException, t);
			}
			if (e.equals(MCEffect.STEP_SOUND) && (data < 0 || data > StaticLayer.GetConvertor().getMaxBlockID())) {
				throw new ConfigRuntimeException("This effect requires a valid BlockID", ExceptionType.FormatException, t);
			}
			if (args.length == 3) {
				radius = Static.getInt32(args[2], t);
			}
			l.getWorld().playEffect(l, e, data, radius);
			return CVoid.VOID;
		}
	}

	@api
	public static class set_entity_health extends AbstractFunction {

		@Override
		public String getName() {
			return "set_entity_health";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {entityID, healthPercent} Sets the specified entity's health as a percentage,"
					+ " where 0 kills it and 100 gives it full health."
					+ " An exception is thrown if the entityID doesn't exist or isn't a LivingEntity.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.BadEntityException,
					ExceptionType.RangeException};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCLivingEntity e = Static.getLivingEntity(Static.getInt32(args[0], t), t);
			double percent = Static.getDouble(args[1], t);
			if (percent < 0 || percent > 100) {
				throw new ConfigRuntimeException("Health was expected to be a percentage between 0 and 100",
						ExceptionType.RangeException, t);
			} else {
				e.setHealth(percent / 100.0 * e.getMaxHealth());
			}
			return CVoid.VOID;
		}
	}

	@api
	public static class get_entity_health extends AbstractFunction {

		@Override
		public String getName() {
			return "get_entity_health";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "double {entityID} Returns the entity's health as a percentage of its maximum health."
					+ " If the specified entity doesn't exist, or is not a LivingEntity, a format exception is thrown.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.BadEntityException};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCLivingEntity e = Static.getLivingEntity(Static.getInt32(args[0], t), t);
			return new CDouble(e.getHealth() / e.getMaxHealth() * 100.0, t);
		}
	}

	@api(environments={CommandHelperEnvironment.class})
	public static class get_server_info extends AbstractFunction {

		@Override
		public String getName() {
			return "get_server_info";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "mixed {[value]} Returns various information about server."
					+ "If value is set, it should be an integer of one of the following indexes, and only that information for that index"
					+ " will be returned. ---- Otherwise if value is not specified (or is -1), it returns an array of"
					+ " information with the following pieces of information in the specified index: "
					+ "<ul><li>0 - Server name; the name of the server in server.properties. "
					+ "</li><li>1 - API version; The bukkit api version that is implemented in this build.</li><li>2 - Bukkit version; The version of craftbukkit your using.  "
					+ "</li><li>3 - Allow flight; If true, minecrafts inbuild anti fly check is enabled.</li><li>4 - Allow nether; is true, nether is enabled"
					+ "</li><li>5 - Allow end; if true, end is enabled"
					+ "</li><li>6 - World container; The path to the world container.</li><li>7 - "
					+ "Max player limit; returns the player limit.</li><li>8 - Operators; An array of operators on the server.</li>"
					+ "<li>9 - Plugins; An array of plugins loaded by the server.</li>"
					+ "<li>10 - Online Mode; If true, users are authenticated with Mojang before login</li>"
					+ "<li>11 - Server port; Get the game port that the server runs on</li></ul>";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.RangeException};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return true;
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			MCServer server = StaticLayer.GetServer();
			int index = -1;
			if (args.length == 0) {
				index = -1;
			} else if (args.length == 1) {
				index = Static.getInt32(args[0], t);
			}

			if (index < -1 || index > 11) {
				throw new ConfigRuntimeException("get_server_info expects the index to be between -1 and 11 (inclusive)",
						ExceptionType.RangeException, t);
			}

			assert index >= -1 && index <= 11; // Is this needed? Above statement should cause this to never be true. - entityreborn
			ArrayList<Construct> retVals = new ArrayList<Construct>();

			if (index == 0 || index == -1) {
				//Server name
				retVals.add(new CString(server.getServerName(), t));
			}

			if (index == 1 || index == -1) {
				//Server Version
				retVals.add(new CString(server.getVersion(), t));
			}
			if (index == 2 || index == -1) {
				//Bukkit Version
				retVals.add(new CString(server.getModVersion(), t));
			}
			if (index == 3 || index == -1) {
				//Allow flight
				retVals.add(new CBoolean(server.getAllowFlight(), t));
			}
			if (index == 4 || index == -1) {
				//Allow nether
				retVals.add(new CBoolean(server.getAllowNether(), t));
			}
			if (index == 5 || index == -1) {
				//Allow end
				retVals.add(new CBoolean(server.getAllowEnd(), t));
			}
			if (index == 6 || index == -1) {
				//World container
				retVals.add(new CString(server.getWorldContainer(), t));
			}
			if (index == 7 || index == -1) {
				//Max player limit
				retVals.add(new CInt(server.getMaxPlayers(), t));
			}
			if (index == 8 || index == -1) {
				//Array of op's
				CArray co = new CArray(t);
				List<MCOfflinePlayer> so = server.getOperators();
				for (MCOfflinePlayer o : so) {
					if (o == null) {
						continue;
					}
					CString os = new CString(o.getName(), t);
					co.push(os);
				}
				retVals.add(co);
			}
			if (index == 9 || index == -1) {
				//Array of plugins
				CArray co = new CArray(t);
				List<MCPlugin> plugs = server.getPluginManager().getPlugins();
				
				for (MCPlugin p : plugs) {
					if (p == null) {
						continue;
					}
					
					CString name = new CString(p.getName(), t);
					co.push(name);
				}
				
				retVals.add(co);
			}
			if (index == 10 || index == -1) {
				//Online Mode
				retVals.add(new CBoolean(server.getOnlineMode(), t));
			}
			if (index == 11 || index == -1) {
				//Server port
				retVals.add(new CInt(server.getPort(), t));
			}

			if (retVals.size() == 1) {
				return retVals.get(0);
			} else {
				CArray ca = new CArray(t);
				for (Construct c : retVals) {
					ca.push(c);
				}
				return ca;
			}
		}
	}

	@api(environments={CommandHelperEnvironment.class})
	public static class get_banned_players extends AbstractFunction {

		@Override
		public String getName() {
			return "get_banned_players";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "Array {} An array of players banned on the server.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return true;
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			MCServer server = env.getEnv(CommandHelperEnvironment.class).GetCommandSender().getServer();

			CArray co = new CArray(t);
			List<MCOfflinePlayer> so = server.getBannedPlayers();
			for (MCOfflinePlayer o : so) {
				if (o == null) {
					continue;
				}
				CString os = new CString(o.getName(), t);
				co.push(os);
			}
			return co;
		}
	}

	@api(environments={CommandHelperEnvironment.class})
	public static class get_whitelisted_players extends AbstractFunction {

		@Override
		public String getName() {
			return "get_whitelisted_players";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "Array {} An array of players whitelisted on the server.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return true;
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			MCServer server = env.getEnv(CommandHelperEnvironment.class).GetCommandSender().getServer();

			CArray co = new CArray(t);
			List<MCOfflinePlayer> so = server.getWhitelistedPlayers();
			for (MCOfflinePlayer o : so) {
				if (o == null) {
					continue;
				}
				CString os = new CString(o.getName(), t);
				co.push(os);
			}
			return co;
		}
	}
	
	@api(environments={CommandHelperEnvironment.class})
	public static class get_spawner_type extends AbstractFunction{

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCWorld w = null;
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(p != null){
				w = p.getWorld();
			}
			MCLocation location = ObjectGenerator.GetGenerator().location(args[0], w, t);
			
			if(location.getBlock().getState() instanceof MCCreatureSpawner){
				String type = ((MCCreatureSpawner)location.getBlock().getState()).getSpawnedType().name();
				return new CString(type, t);
			} else {
				throw new Exceptions.FormatException("The block at " + location.toString() + " is not a spawner block", t);
			}
		}

		@Override
		public String getName() {
			return "get_spawner_type";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {locationArray} Gets the spawner type of the specified mob spawner. ----"
					+ " Valid types will be one of the mob types.";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api(environments={CommandHelperEnvironment.class})
	public static class set_spawner_type extends AbstractFunction{

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCWorld w = null;
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(p != null){
				w = p.getWorld();
			}
			MCLocation location = ObjectGenerator.GetGenerator().location(args[0], w, t);
			MCEntityType type = MCEntityType.valueOf(args[1].val().toUpperCase());
			if(location.getBlock().getState() instanceof MCCreatureSpawner){
				((MCCreatureSpawner)location.getBlock().getState()).setSpawnedType(type);
				return CVoid.VOID;
			} else {
				throw new Exceptions.FormatException("The block at " + location.toString() + " is not a spawner block", t);
			}
		}

		@Override
		public String getName() {
			return "set_spawner_type";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {locationArray, type} Sets the mob spawner type at the location specified. If the location is not a mob spawner,"
					+ " or if the type is invalid, a FormatException is thrown. The type may be one of either " 
					+ StringUtils.Join(MCEntityType.values(), ", ", ", or ");
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api(environments={CommandHelperEnvironment.class})
	public static class launch_firework extends AbstractFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			MCWorld w = null;
			if(p != null){
				w = p.getWorld();
			}
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], w, t);
			CArray options = new CArray(t);
			if(args.length == 2){
				options = Static.getArray(args[1], t);
			}
			int strength = 2;
			boolean flicker = false;
			boolean trail = true;
			Set<MCColor> colors = new HashSet<MCColor>();
			colors.add(MCColor.WHITE);
			Set<MCColor> fade = new HashSet<MCColor>();
			MCFireworkType type = MCFireworkType.BALL;
			
			if(options.containsKey("strength")){
				strength = Static.getInt32(options.get("strength"), t);
				if (strength < 0 || strength > 128) {
					throw new ConfigRuntimeException("Strength must be between 0 and 128", ExceptionType.RangeException, t);
				}
			}
			if(options.containsKey("flicker")){
				flicker = Static.getBoolean(options.get("flicker"));
			}
			if(options.containsKey("trail")){
				trail = Static.getBoolean(options.get("trail"));
			}
			if(options.containsKey("colors")){
				colors = parseColors(options.get("colors"), t);
			}
			if(options.containsKey("fade")){
				fade = parseColors(options.get("fade"), t);
			}
			if(options.containsKey("type")){
				try{
					type = MCFireworkType.valueOf(options.get("type").val().toUpperCase());
				} catch(IllegalArgumentException e){
					throw new Exceptions.FormatException("Invalid type: " + options.get("type").val(), t);
				}
			}
			
			MCFireworkBuilder fw = StaticLayer.GetConvertor().GetFireworkBuilder();
			fw.setStrength(strength);
			fw.setFlicker(flicker);
			fw.setTrail(trail);
			fw.setType(type);
			for(MCColor color : colors){
				fw.addColor(color);
			}
			
			for(MCColor color : fade){
				fw.addFadeColor(color);
			}
			
			return new CInt(fw.launch(loc), t);
		}
		
		private Set<MCColor> parseColors(Construct c, Target t){
			Set<MCColor> colors = new HashSet<MCColor>();
			if(c instanceof CArray){
				CArray ca = ((CArray)c);
				if(ca.size() == 3
						&& ca.get(0) instanceof CInt
						&& ca.get(1) instanceof CInt
						&& ca.get(2) instanceof CInt
						){
					//It's a single custom color
					colors.add(parseColor(ca, t));
				} else {
					for(String key : ca.stringKeySet()){
						Construct val = ca.get(key);
						if(val instanceof CArray){
							colors.add(parseColor(((CArray)val), t));
						} else if(val instanceof CString){
							colors.addAll(parseColor(((CString)val), t));
						}
					}
				}
			} else if(c instanceof CString){
				colors.addAll(parseColor(((CString)c), t));
			}
			return colors;
		}
		
		private MCColor parseColor(CArray ca, Target t){
			return StaticLayer.GetConvertor().GetColor(
							Static.getInt32(ca.get(0), t), 
							Static.getInt32(ca.get(1), t), 
							Static.getInt32(ca.get(2), t)
						);
		}
		
		private List<MCColor> parseColor(CString cs, Target t){
			String split[] = cs.val().split("\\|");
			List<MCColor> colors = new ArrayList<MCColor>();
			for(String s : split){
				if(MCColor.STANDARD_COLORS.containsKey(s.toUpperCase())){
					 colors.add(MCColor.STANDARD_COLORS.get(s.toUpperCase()));
				} else {
					throw new Exceptions.FormatException("Unknown color type: " + s, t);
				}
			}
			return colors;
		}

		@Override
		public String getName() {
			return "launch_firework";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			Class c;
			try {
				//Since MCColor actually depends on a bukkit server, we don't want to require that for
				//the sake of documentation, so we'll build the color list much more carefully.
				//Note the false, so we don't actually initialize the class.
				c = Class.forName(MCColor.class.getName(), false, this.getClass().getClassLoader());
			} catch (ClassNotFoundException ex) {
				//Hrm...
				Logger.getLogger(Minecraft.class.getName()).log(Level.SEVERE, null, ex);
				return "";
			}
			List<String> names = new ArrayList<String>();
			for(Field f : c.getFields()){
				if(f.getType() == MCColor.class){
					names.add(f.getName());
				}
			}
			return "void {locationArray, [optionsArray]} Launches a firework. The location array specifies where it is launched from,"
					+ " and the options array is an associative array described below. All parameters in the associative array are"
					+ " optional, and default to the specified values if not set. The default options being set will make it look like"
					+ " a normal firework, with a white explosion. ----"
					+ " The options array may have the following keys:\n"
					+ "{| cellspacing=\"1\" cellpadding=\"1\" border=\"1\" class=\"wikitable\"\n"
					+ "! Array key !! Description !! Default\n"
					+ "|-\n"
					+ "| strength || A number specifying how far up the firework should go || 2\n"
					+ "|-\n"
					+ "| flicker || A boolean, determining if the firework will flicker\n || false\n"
					+ "|-\n"
					+ "| trail || A boolean, determining if the firework will leave a trail || true\n"
					+ "|-\n"
					+ "| colors || An array of colors, or a pipe seperated string of color names (for the named colors only)"
					+ " for instance: array('WHITE') or 'WHITE<nowiki>|</nowiki>BLUE'. If you want custom colors, you must use an array, though"
					+ " you can still use color names as an item in the array, for instance: array('ORANGE', array(30, 45, 150))."
					+ " These colors are used as the primary colors. || 'WHITE'\n"
					+ "|-\n"
					+ "| fade || An array of colors to be used as the fade colors. This parameter should be formatted the same as"
					+ " the colors parameter || array()\n"
					+ "|-\n"
					+ "| type || An enum value of one of the firework types, one of: " + StringUtils.Join(MCFireworkType.values(), ", ", " or ")
					+ " || " + MCFireworkType.BALL.name() + "\n"
					+ "|}\n"
					+ "The \"named colors\" can be one of: " + StringUtils.Join(names, ", ", " or ");
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api
	public static class send_texturepack extends AbstractFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCPlayer p = Static.GetPlayer(args[0], t);
			p.sendTexturePack(args[1].val());
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "send_texturepack";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {player, url} Sends a texturepack URL to the player's client."
					+ " If the client has not been requested to change textures in the"
					+ " past, they will recieve a confirmation dialog before downloading"
					+ " and switching to the new pack. Clients that ignore server textures"
					+ " will not recieve the request, so this function will not affect them.";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}	
	}
	
	@api
	public static class get_ip_bans extends AbstractFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCServer s = Static.getServer();
			CArray ret = new CArray(t);
			for (String ip : s.getIPBans()) {
				ret.push(new CString(ip, t));
			}
			return ret;
		}

		@Override
		public String getName() {
			return "get_ip_bans";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "array {} Returns an array of entries from banned-ips.txt.";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
	
	@api
	public static class set_ip_banned extends AbstractFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCServer s = Static.getServer();
			String ip = args[0].val();
			if (Static.getBoolean(args[1])) {
				s.banIP(ip);
			} else {
				s.unbanIP(ip);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_ip_banned";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {address, banned} If banned is true, address is added to banned-ips.txt,"
					+ " if false, the address is removed.";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
	
	@api
	public static class material_info extends AbstractFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCMaterial i = StaticLayer.GetConvertor().getMaterial(Static.getInt32(args[0], t));
			CArray ret = new CArray(t);
			ret.set("maxStacksize", new CInt(i.getMaxStackSize(), t), t);
			ret.set("maxDurability", new CInt(i.getMaxDurability(), t), t);
			ret.set("hasGravity", new CBoolean(i.hasGravity(), t), t);
			ret.set("isBlock", new CBoolean(i.isBlock(), t), t);
			ret.set("isBurnable", new CBoolean(i.isBurnable(), t), t);
			ret.set("isEdible", new CBoolean(i.isEdible(), t), t);
			ret.set("isFlammable", new CBoolean(i.isFlammable(), t), t);
			ret.set("isOccluding", new CBoolean(i.isOccluding(), t), t);
			ret.set("isRecord", new CBoolean(i.isRecord(), t), t);
			ret.set("isSolid", new CBoolean(i.isSolid(), t), t);
			ret.set("isTransparent", new CBoolean(i.isTransparent(), t), t);
			return ret;
		}

		@Override
		public String getName() {
			return "material_info";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "array {int} Returns an array of info about the material.";
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}
	}
	
	@api(environments={CommandHelperEnvironment.class})
    public static class drop_item extends AbstractFunction {

		@Override
        public String getName() {
            return "drop_item";
        }

		@Override
        public Integer[] numArgs() {
            return new Integer[]{1, 2, 3};
        }

		@Override
		public String docs() {
			return "int {[player/LocationArray], item, [spawnNaturally]} Drops the specified item stack at the specified player's feet (or "
					+ " at an arbitrary Location, if an array is given), and returns its entity id"
					+ " Like the vanilla /give command. player defaults to the current player, and qty defaults to 1."
					+ " item takes an item array."
					+ " spawnNaturally takes a boolean, which forces the way the item will be spawned. If true, the item will be dropped with a random offset.";
		}

		@Override
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException, ExceptionType.PlayerOfflineException, ExceptionType.InvalidWorldException};
        }

		@Override
        public boolean isRestricted() {
            return true;
        }
		@Override
        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

		@Override
        public Boolean runAsync() {
            return false;
        }

		@Override
        public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			MCLocation l;
            MCItemStack is;
            boolean natural;
			if (args.length == 1) {
				if (env.getEnv(CommandHelperEnvironment.class).GetPlayer() != null) {
					l = env.getEnv(CommandHelperEnvironment.class).GetPlayer().getEyeLocation();
					natural = false;
				} else {
					throw new ConfigRuntimeException("Invalid sender!", ExceptionType.PlayerOfflineException, t);
				}
				is = ObjectGenerator.GetGenerator().item(args[0], t);
			} else {
				MCPlayer p;
				if (args[0] instanceof CArray) {
					p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
					l = ObjectGenerator.GetGenerator().location(args[0], (p != null ? p.getWorld() : null), t);
					natural = true;
				} else {
					p = Static.GetPlayer(args[0].val(), t);
					Static.AssertPlayerNonNull(p, t);
					l = p.getEyeLocation();
					natural = false;
				}
				is = ObjectGenerator.GetGenerator().item(args[1], t);
			}
			if (args.length == 3) {
				natural = Static.getBoolean(args[2]);
			}
			MCItem item;
			if (natural) {
				item = l.getWorld().dropItemNaturally(l, is);
			} else {
				item = l.getWorld().dropItem(l, is);
			}
			if (item != null) {
				return new CInt(item.getEntityId(), t);
			} else {
				return CNull.NULL;
			}
        }
    }

	@api
	public static class shutdown_server extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "shutdown_server";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public String docs() {
			return "void {} Shuts down the minecraft server instance.";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws CancelCommandException {
			Static.getServer().shutdown();
			throw new CancelCommandException("", t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
				OptimizationOption.TERMINAL
			);
		}
	}
}
