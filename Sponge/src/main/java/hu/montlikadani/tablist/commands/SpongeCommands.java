package hu.montlikadani.tablist.commands;

import java.util.Optional;
import java.util.function.Supplier;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import hu.montlikadani.tablist.TabList;
import hu.montlikadani.tablist.tablist.TabHandler;
import hu.montlikadani.tablist.user.TabListUser;

public final class SpongeCommands extends ICommand implements Supplier<CommandCallable> {

	private final TabList plugin;
	private final CommandCallable toggleCmd;

	public SpongeCommands() {
		throw new IllegalAccessError(getClass().getSimpleName() + " can't be instantiated.");
	}

	public SpongeCommands(TabList plugin) {
		this.plugin = plugin;

		toggleCmd = CommandSpec.builder().description(Text.of("Toggles the visibility of tablist"))
				.arguments(GenericArguments.optional(GenericArguments.firstParsing(
						GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))),
						GenericArguments.string(Text.of("all")))))
				.permission("tablist.toggle").executor(this::processToggle).build();

		Sponge.getCommandManager().register(plugin, get(), new String[] { "tablist", "tl" });
	}

	private CommandResult processToggle(CommandSource src, CommandContext args) {
		if ("all".equalsIgnoreCase(args.<String>getOne("all").orElse(""))) {
			if (!hasPerm(src, "tablist.toggle.all")) {
				return CommandResult.empty();
			}

			for (TabListUser user : plugin.getTabUsers()) {
				if (TabHandler.TABENABLED.remove(user.getUniqueId()) == null) {
					TabHandler.TABENABLED.put(user.getUniqueId(), true);
				} else {
					user.getTabListManager().loadTab();
				}
			}

			sendMsg(src, plugin.getTabUsers().isEmpty() ? "&cNo one on the server"
					: "&2Tab has been switched for everyone!");
			return CommandResult.success();
		}

		Optional<Player> one = args.<Player>getOne("player");

		if (one.isPresent()) {
			Player target = one.get();

			plugin.getUser(target).ifPresent(user -> {
				if (TabHandler.TABENABLED.remove(user.getUniqueId()) == null) {
					TabHandler.TABENABLED.put(user.getUniqueId(), true);
					sendMsg(src, "&cTab has been disabled for &e" + target.getName() + "&c!");
				} else {
					user.getTabListManager().loadTab();
					sendMsg(src, "&aTab has been enabled for &e" + target.getName() + "&a!");
				}
			});

			return CommandResult.success();
		}

		if (src instanceof Player) {
			plugin.getUser((Player) src).ifPresent(user -> {
				if (TabHandler.TABENABLED.remove(user.getUniqueId()) == null) {
					TabHandler.TABENABLED.put(user.getUniqueId(), true);
					sendMsg(src, "&cTab has been disabled for you.");
				} else {
					user.getTabListManager().loadTab();
					sendMsg(src, "&aTab has been enabled for you.");
				}
			});

			return CommandResult.success();
		}

		sendMsg(src, "&cUsage: /" + args.<String>getOne("tablist").orElse("tablist") + " toggle <player;all>");
		return CommandResult.empty();
	}

	@Override
	public CommandCallable get() {
		return CommandSpec.builder().child(toggleCmd, "toggle").description(Text.of("Toggling tablist visibility"))
				.build();
	}
}
