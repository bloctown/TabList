package hu.montlikadani.tablist.tablist.groups;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import hu.montlikadani.tablist.TabList;
import hu.montlikadani.tablist.config.ConfigValues;
import hu.montlikadani.tablist.user.TabListUser;

public class GroupTask implements Consumer<Task> {

	private final java.util.Deque<TabGroupPlayer> cachedPlayers = new ConcurrentLinkedDeque<>();

	private TabList tl;
	private Task task;

	public GroupTask() {
		Sponge.getPluginManager().getPlugin("tablist")
				.ifPresent(container -> container.getInstance().ifPresent(inst -> tl = (TabList) inst));
	}

	public TabGroupPlayer getGroupPlayer(TabListUser user) {
		for (TabGroupPlayer groupPlayer : cachedPlayers) {
			if (groupPlayer.getUser().equals(user)) {
				return groupPlayer;
			}
		}

		return null;
	}

	public TabGroupPlayer addPlayer(TabListUser user) {
		if (!ConfigValues.isTablistGroups()) {
			return null;
		}

		TabGroupPlayer tabGroupPlayer = getGroupPlayer(user);

		if (tabGroupPlayer != null) {
			return tabGroupPlayer;
		}

		tabGroupPlayer = new TabGroupPlayer(user);
		tabGroupPlayer.update();
		cachedPlayers.add(tabGroupPlayer);

		int priority = 0;

		for (TabGroupPlayer tabPl : cachedPlayers) {
			Optional<TabGroup> group = tabPl.getGroup();

			if (group.isPresent()) {
				group.get().setTeam(tabPl.getUser(), priority);
				priority++;
			}
		}

		return tabGroupPlayer;
	}

	public void removePlayer(TabListUser user) {
		TabGroupPlayer gp = getGroupPlayer(user);

		if (gp != null) {
			gp.getGroup().ifPresent(g -> user.getPlayer().ifPresent(player -> g.removeTeam(player)));
			cachedPlayers.remove(gp);
		}
	}

	public void runTask(Object plugin) {
		if (ConfigValues.isTablistGroups() && !isRunning()) {
			task = Task.builder().async().intervalTicks(4).execute(this::accept).submit(plugin);
		}
	}

	public void cancel() {
		if (isRunning()) {
			task.cancel();
			task = null;
		}
	}

	public boolean isRunning() {
		return task != null;
	}

	@Override
	public void accept(Task t) {
		if (tl.getTabUsers().isEmpty()) {
			return;
		}

		for (TabListUser user : tl.getTabUsers()) {
			TabGroupPlayer groupPlayer = user.getTabPlayer();

			if (groupPlayer.update()) {
				cachedPlayers.remove(groupPlayer);
				cachedPlayers.add(groupPlayer);
			}
		}

		int priority = 0;

		for (TabGroupPlayer tabPl : cachedPlayers) {
			Optional<TabGroup> group = tabPl.getGroup();

			if (group.isPresent()) {
				group.get().setTeam(tabPl.getUser(), priority);
				priority++;
			}
		}
	}
}
