package hu.montlikadani.tablist.tablist;

import java.lang.reflect.Field;

import org.bukkit.entity.Player;

import hu.montlikadani.tablist.utils.ServerVersion;
import hu.montlikadani.tablist.utils.Util;
import hu.montlikadani.tablist.utils.reflection.ClazzContainer;
import hu.montlikadani.tablist.utils.reflection.ReflectionUtils;

public abstract class TabTitle {

	private static java.lang.reflect.Constructor<?> playerListHeaderFooterConstructor;
	private static Field headerField, footerField;

	static {
		ReflectionUtils.getJsonComponent();

		Class<?> playerListHeaderFooter = null;

		try {
			playerListHeaderFooter = getPacketClass("net.minecraft.network.protocol.game", "PacketPlayOutPlayerListHeaderFooter");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		if (playerListHeaderFooter != null) {
			try {
				playerListHeaderFooterConstructor = playerListHeaderFooter.getConstructor();
			} catch (NoSuchMethodException s) {
				try {
					playerListHeaderFooterConstructor = playerListHeaderFooter
							.getConstructor(ClazzContainer.getIChatBaseComponent(), ClazzContainer.getIChatBaseComponent());
				} catch (NoSuchMethodException e) {
					try {
						playerListHeaderFooterConstructor = playerListHeaderFooter
								.getConstructor(ClazzContainer.getIChatBaseComponent());
					} catch (NoSuchMethodException ex) {
						ex.printStackTrace();
					}
				}
			}
		}
	}

	private static Class<?> getPacketClass(String newPackageName, String name) throws ClassNotFoundException {
		if (ServerVersion.isCurrentLower(ServerVersion.v1_17_R1) || newPackageName == null) {
			newPackageName = "net.minecraft.server." + ServerVersion.getArrayVersion()[3];
		}

		return Class.forName(newPackageName + "." + name);
	}

	public static void h() {
	}

	public static void sendTabTitle(Player player, String header, String footer) {
		Object tabHeader = ReflectionUtils.EMPTY_COMPONENT;
		Object tabFooter = ReflectionUtils.EMPTY_COMPONENT;

		if (!header.isEmpty()) {
			if (ServerVersion.isCurrentEqualOrLower(ServerVersion.v1_15_R2)) {
				header = Util.colorText(header);
			}

			try {
				tabHeader = ReflectionUtils.getAsIChatBaseComponent(header);
			} catch (Exception e1) {
				e1.printStackTrace();
				return;
			}
		}

		if (!footer.isEmpty()) {
			if (ServerVersion.isCurrentEqualOrLower(ServerVersion.v1_15_R2)) {
				footer = Util.colorText(footer);
			}

			try {
				tabFooter = ReflectionUtils.getAsIChatBaseComponent(footer);
			} catch (Exception e1) {
				e1.printStackTrace();
				return;
			}
		}

		if (tabHeader == null || tabFooter == null) {
			return;
		}

		try {
			Object packet;

			if (ServerVersion.isCurrentEqualOrHigher(ServerVersion.v1_17_R1)) {
				packet = playerListHeaderFooterConstructor.newInstance(tabHeader, tabFooter);
			} else {
				packet = playerListHeaderFooterConstructor.newInstance();

				if (ServerVersion.isCurrentEqualOrHigher(ServerVersion.v1_13_R2)) {
					if (headerField == null) {
						(headerField = packet.getClass().getDeclaredField("header")).setAccessible(true);
					}

					if (footerField == null) {
						(footerField = packet.getClass().getDeclaredField("footer")).setAccessible(true);
					}
				} else {
					if (headerField == null) {
						(headerField = packet.getClass().getDeclaredField("a")).setAccessible(true);
					}

					if (footerField == null) {
						(footerField = packet.getClass().getDeclaredField("b")).setAccessible(true);
					}
				}

				headerField.set(packet, tabHeader);
				footerField.set(packet, tabFooter);
			}

			ReflectionUtils.sendPacket(player, packet);
		} catch (Exception f) {
			Object packet = null;

			try {
				try {
					packet = playerListHeaderFooterConstructor.newInstance(tabHeader);
				} catch (IllegalArgumentException e) {
					try {
						packet = playerListHeaderFooterConstructor.newInstance();
					} catch (IllegalArgumentException ex) {
					}
				}

				if (packet != null) {
					if (footerField == null) {
						(footerField = packet.getClass().getDeclaredField("b")).setAccessible(true);
					}

					footerField.set(packet, tabFooter);
					ReflectionUtils.sendPacket(player, packet);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}