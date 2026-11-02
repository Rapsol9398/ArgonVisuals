package com.rapsol.gui;

import com.rapsol.Argon;
import com.rapsol.module.modules.client.ClickGUI;
import com.rapsol.module.modules.render.Particle;
import com.rapsol.utils.ColorUtils;
import com.rapsol.utils.MathUtils;
import com.rapsol.utils.RenderUtils;
import com.rapsol.utils.TextRenderer;
import com.rapsol.utils.Utils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import com.mojang.blaze3d.platform.InputConstants;

import java.awt.*;
import java.util.*;
import java.util.List;

import static com.rapsol.Argon.mc;

public final class ParticlePanel {
	private static final int PAD = 10;
	private static final int ROW = 30;
	private static final int WIDTH = 420;
	private static final int LIST_ROWS = 12;
	private static final int CHECK = 14;
	private static final int SEARCH_Y = ROW + PAD;
	private static final int BUTTONS_Y = SEARCH_Y + ROW + PAD;
	private static final int LIST_Y = BUTTONS_Y + ROW + PAD;
	private static final int HEIGHT = LIST_Y + LIST_ROWS * ROW + PAD;

	private static final Color TEXT = new Color(245, 245, 245, 255);
	private static final Color TEXT_DIM = new Color(150, 150, 150, 255);
	private static final Color INSET = new Color(0, 0, 0, 120);
	private static final Color BORDER = new Color(50, 50, 50, 255);
	private static final Color HOVER = new Color(255, 255, 255, 15);

	private enum Group {
		FIRE("Fire"), WATER("Water"), COMBAT("Combat"), MAGIC("Magic"), EXPLOSIONS("Explosions"),
		BLOCKS("Blocks"), AMBIENT("Ambient"), MOBS("Mobs"), MISC("Misc");

		final String label;

		Group(String label) {
			this.label = label;
		}
	}

	private record Row(String label, Group group, String id, boolean hidden, int hiddenCount, int total, boolean collapsed) {
		boolean header() {
			return id == null;
		}
	}

	private static final Map<String, Group> GROUPS = new HashMap<>();
	private static final Set<Group> collapsed = EnumSet.allOf(Group.class);
	private static int savedX = -1, savedY;

	static {
		put(Group.FIRE, "flame small_flame soul_fire_flame copper_fire_flame smoke large_smoke white_smoke campfire_cosy_smoke campfire_signal_smoke lava");
		put(Group.WATER, "bubble bubble_pop bubble_column_up current_down splash underwater rain fishing dolphin nautilus dripping_water falling_water "
				+ "dripping_lava falling_lava landing_lava dripping_honey falling_honey landing_honey falling_nectar dripping_obsidian_tear "
				+ "falling_obsidian_tear landing_obsidian_tear dripping_dripstone_lava falling_dripstone_lava dripping_dripstone_water falling_dripstone_water");
		put(Group.COMBAT, "crit enchanted_hit sweep_attack damage_indicator sonic_boom totem_of_undying");
		put(Group.MAGIC, "effect entity_effect instant_effect witch enchant portal reverse_portal end_rod dragon_breath glow soul trial_omen raid_omen "
				+ "ominous_spawning trial_spawner_detection trial_spawner_detection_ominous vault_connection");
		put(Group.EXPLOSIONS, "explosion explosion_emitter firework flash gust small_gust gust_emitter_large gust_emitter_small");
		put(Group.BLOCKS, "block block_crumble block_marker falling_dust dust_pillar dust_plume dust dust_color_transition scrape wax_on wax_off "
				+ "electric_spark composter sculk_charge sculk_charge_pop sculk_soul shriek vibration trail");
		put(Group.AMBIENT, "ash white_ash crimson_spore warped_spore spore_blossom_air falling_spore_blossom cherry_leaves pale_oak_leaves "
				+ "tinted_leaves mycelium snowflake firefly cloud");
		put(Group.MOBS, "angry_villager happy_villager heart note sneeze spit squid_ink glow_squid_ink elder_guardian poof item item_slime "
				+ "item_snowball item_cobweb egg_crack infested pause_mob_growth reset_mob_growth");
	}

	private static void put(Group group, String ids) {
		for (String id : ids.split(" "))
			GROUPS.put(id, group);
	}

	private final Particle module;
	private final SearchField search = new SearchField();
	private Color currentColor;
	private int x, y;
	private int scrollRow;
	private boolean dragging;
	private int dragX, dragY;

	public ParticlePanel(Particle module) {
		this.module = module;
	}

	private static Group group(String id) {
		Group known = GROUPS.get(id);
		if (known != null) return known;
		if (id.contains("flame") || id.contains("smoke") || id.contains("fire")) return Group.FIRE;
		if (id.contains("drip") || id.contains("falling") || id.contains("landing") || id.contains("bubble") || id.contains("splash") || id.contains("water"))
			return Group.WATER;
		return Group.MISC;
	}

	private static String displayName(String id) {
		StringBuilder name = new StringBuilder();
		for (String word : id.split("_")) {
			if (word.isEmpty()) continue;
			if (!name.isEmpty()) name.append(' ');
			if (!name.isEmpty() && (word.equals("of") || word.equals("the") || word.equals("and"))) name.append(word);
			else name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
		}
		return name.toString();
	}

	private List<Row> rows(Set<Group> closed) {
		String query = search.query();
		Set<String> hidden = module.particles.hidden();
		Map<Group, List<String>> byGroup = new EnumMap<>(Group.class);
		for (String id : module.ids())
			if (query.isEmpty() || id.contains(query) || displayName(id).toLowerCase().contains(query))
				byGroup.computeIfAbsent(group(id), g -> new ArrayList<>()).add(id);

		List<Row> rows = new ArrayList<>();
		for (Map.Entry<Group, List<String>> entry : byGroup.entrySet()) {
			List<String> members = entry.getValue();
			members.sort(Comparator.comparing(ParticlePanel::displayName));
			int hiddenCount = (int) members.stream().filter(hidden::contains).count();
			boolean isClosed = query.isEmpty() && closed.contains(entry.getKey());
			rows.add(new Row(entry.getKey().label, entry.getKey(), null, false, hiddenCount, members.size(), isClosed));
			if (isClosed) continue;
			for (String id : members)
				rows.add(new Row(displayName(id), entry.getKey(), id, hidden.contains(id), 0, 0, false));
		}
		return rows;
	}

	private int listX() { return x + PAD; }
	private int listY() { return y + LIST_Y; }
	private int listWidth() { return WIDTH - 2 * PAD; }

	private boolean isInside(double mx, double my) {
		return mx >= x && mx <= x + WIDTH && my >= y && my <= y + HEIGHT;
	}

	private int buttonAt(double mx, double my) {
		if (my < y + BUTTONS_Y || my >= y + BUTTONS_Y + ROW) return -1;
		int half = (listWidth() - PAD) / 2;
		if (mx >= listX() && mx <= listX() + half) return 0;
		if (mx >= listX() + half + PAD && mx <= listX() + listWidth()) return 1;
		return -1;
	}

	public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		if (savedX < 0) {
			savedX = Math.max(0, (mc.getWindow().getScreenWidth() - WIDTH) / 2);
			savedY = Math.max(0, (mc.getWindow().getScreenHeight() - HEIGHT) / 2);
		}
		if (dragging) {
			savedX = (int) MathUtils.goodLerp(0.3F * delta, savedX, mouseX - dragX);
			savedY = (int) MathUtils.goodLerp(0.3F * delta, savedY, mouseY - dragY);
		}
		x = savedX;
		y = savedY;

		if (currentColor == null)
			currentColor = new Color(0, 0, 0, 0);
		if (currentColor.getAlpha() != ClickGUI.alphaWindow.getValueInt())
			currentColor = ColorUtils.smoothAlphaTransition(0.05F, ClickGUI.alphaWindow.getValueInt(), currentColor);

		int round = ClickGUI.roundQuads.getValueInt();
		RenderUtils.renderRoundedQuad(context, currentColor, x, y, x + WIDTH, y + HEIGHT, round, round, round, round, 50);
		context.fill(x, y + ROW - 2, x + WIDTH, y + ROW, Utils.getMainColor(255, 0).getRGB());
		TextRenderer.centeredText("Particles", context, x + WIDTH / 2, y + 6, Color.WHITE.getRGB());
		int hiddenTotal = module.particles.hidden().size();
		String summary = hiddenTotal == 0 ? "all shown" : hiddenTotal + " hidden";
		TextRenderer.text(summary, context, x + WIDTH - PAD - TextRenderer.getWidth(summary), y + 6, TEXT_DIM.getRGB());

		renderSearch(context);
		renderButton(context, 0, "Show all", buttonAt(mouseX, mouseY) == 0);
		renderButton(context, 1, "Hide all", buttonAt(mouseX, mouseY) == 1);
		renderList(context, rows(collapsed), mouseX, mouseY);
	}

	private void renderSearch(GuiGraphicsExtractor context) {
		int fx0 = listX(), fy0 = y + SEARCH_Y + 3, fx1 = listX() + listWidth(), fy1 = y + SEARCH_Y + ROW - 3;
		RenderUtils.renderRoundedQuad(context, INSET, fx0, fy0, fx1, fy1, 4, 10);
		RenderUtils.renderRoundedOutline(context, search.focused() ? Utils.getMainColor(255, 0) : BORDER, fx0, fy0, fx1, fy1, 4, 4, 4, 4, 1, 10);
		RenderUtils.renderSearchIcon(context, TEXT_DIM, fx0 + 8, y + SEARCH_Y + ROW / 2 - 4);

		int textX = fx0 + 24, textY = y + SEARCH_Y + 8;
		if (search.text().isEmpty() && !search.focused()) {
			TextRenderer.text("Search particles...", context, textX, textY, TEXT_DIM.getRGB());
			return;
		}
		TextRenderer.text(search.text(), context, textX, textY, TEXT.getRGB());
		if (search.focused() && (System.currentTimeMillis() / 500) % 2 == 0) {
			int caretX = textX + TextRenderer.getWidth(search.text()) + 1;
			context.fill(caretX, y + SEARCH_Y + 8, caretX + 1, y + SEARCH_Y + ROW - 8, TEXT.getRGB());
		}
	}

	private void renderButton(GuiGraphicsExtractor context, int index, String label, boolean hovered) {
		int half = (listWidth() - PAD) / 2;
		int bx = listX() + index * (half + PAD), by = y + BUTTONS_Y;
		RenderUtils.renderRoundedQuad(context, hovered ? Utils.getMainColor(120, 0) : HOVER, bx, by, bx + half, by + ROW, 5, 10);
		TextRenderer.centeredText(label, context, bx + half / 2, by + 9, TEXT.getRGB());
	}

	private void renderList(GuiGraphicsExtractor context, List<Row> rows, int mouseX, int mouseY) {
		int lx = listX(), ly = listY(), lw = listWidth();
		context.fill(lx, ly, lx + lw, ly + LIST_ROWS * ROW, INSET.getRGB());
		if (rows.isEmpty()) {
			TextRenderer.text("No particle matches", context, lx + 8, ly + 9, TEXT_DIM.getRGB());
			return;
		}

		scrollRow = Math.max(0, Math.min(scrollRow, rows.size() - LIST_ROWS));
		for (int i = 0; i < LIST_ROWS && scrollRow + i < rows.size(); i++) {
			Row row = rows.get(scrollRow + i);
			int ry = ly + i * ROW;
			if (mouseX >= lx && mouseX <= lx + lw && mouseY >= ry && mouseY < ry + ROW)
				context.fill(lx, ry, lx + lw, ry + ROW, HOVER.getRGB());

			if (row.header()) {
				int ax = lx + 10, ay = ry + ROW / 2;
				if (row.collapsed()) {
					RenderUtils.renderLine(context, TEXT_DIM, ax - 2, ay - 4, ax + 2, ay, 1.5);
					RenderUtils.renderLine(context, TEXT_DIM, ax + 2, ay, ax - 2, ay + 4, 1.5);
				} else {
					RenderUtils.renderLine(context, TEXT_DIM, ax - 4, ay - 2, ax, ay + 2, 1.5);
					RenderUtils.renderLine(context, TEXT_DIM, ax, ay + 2, ax + 4, ay - 2, 1.5);
				}

				int cx = lx + 22;
				drawCheck(context, cx, ry + (ROW - CHECK) / 2, row.hiddenCount() == 0 ? 0 : row.hiddenCount() == row.total() ? 1 : 2);
				TextRenderer.text(row.label(), context, cx + CHECK + 8, ry + 9, TEXT.getRGB());
				String count = row.hiddenCount() == 0 ? row.total() + " shown" : row.hiddenCount() + " of " + row.total() + " hidden";
				TextRenderer.text(count, context, lx + lw - 10 - TextRenderer.getWidth(count), ry + 9, TEXT_DIM.getRGB());
			} else {
				int cx = lx + 44;
				drawCheck(context, cx, ry + (ROW - CHECK) / 2, row.hidden() ? 1 : 0);
				TextRenderer.text(row.label(), context, cx + CHECK + 8, ry + 9, (row.hidden() ? TEXT_DIM : TEXT).getRGB());
			}
		}

		if (rows.size() > LIST_ROWS) {
			int trackX = lx + lw - 2, trackH = LIST_ROWS * ROW;
			int thumbH = Math.max(8, trackH * LIST_ROWS / rows.size());
			int thumbY = ly + (trackH - thumbH) * scrollRow / (rows.size() - LIST_ROWS);
			context.fill(trackX, ly, trackX + 2, ly + trackH, new Color(255, 255, 255, 20).getRGB());
			context.fill(trackX, thumbY, trackX + 2, thumbY + thumbH, Utils.getMainColor(200, 0).getRGB());
		}
	}

	private static void drawCheck(GuiGraphicsExtractor context, int cx, int cy, int state) {
		RenderUtils.renderRoundedOutline(context, BORDER, cx, cy, cx + CHECK, cy + CHECK, 3, 3, 3, 3, 2, 10);
		int color = Utils.getMainColor(255, 0).getRGB();
		if (state == 0) context.fill(cx + 3, cy + 3, cx + CHECK - 3, cy + CHECK - 3, color);
		else if (state == 2) context.fill(cx + 3, cy + CHECK / 2 - 1, cx + CHECK - 3, cy + CHECK / 2 + 1, color);
	}

	public void mouseClicked(double mouseX, double mouseY, int button) {
		if (!isInside(mouseX, mouseY)) {
			close();
			return;
		}
		if (button != InputConstants.MOUSE_BUTTON_LEFT) return;

		if (mouseY < y + ROW) {
			search.blur();
			dragging = true;
			dragX = (int) (mouseX - x);
			dragY = (int) (mouseY - y);
			return;
		}
		if (mouseY >= y + SEARCH_Y && mouseY < y + SEARCH_Y + ROW) {
			search.focus();
			return;
		}
		search.blur();

		int allButton = buttonAt(mouseX, mouseY);
		if (allButton >= 0) {
			for (Row row : rows(Set.of()))
				if (!row.header()) module.particles.setHidden(row.id(), allButton == 1);
			Argon.INSTANCE.getProfileManager().markDirty();
			return;
		}

		List<Row> rows = rows(collapsed);
		int lx = listX(), ly = listY();
		if (mouseX < lx || mouseX > lx + listWidth() || mouseY < ly || mouseY >= ly + LIST_ROWS * ROW) return;
		int index = scrollRow + (int) ((mouseY - ly) / ROW);
		if (index >= rows.size()) return;
		Row row = rows.get(index);

		if (!row.header()) {
			module.particles.setHidden(row.id(), !row.hidden());
		} else if (mouseX >= lx + 18 && mouseX <= lx + 26 + CHECK) {
			boolean hide = row.hiddenCount() < row.total();
			for (Row member : rows(Set.of()))
				if (!member.header() && member.group() == row.group()) module.particles.setHidden(member.id(), hide);
		} else if (search.query().isEmpty()) {
			if (!collapsed.remove(row.group())) collapsed.add(row.group());
			return;
		}
		Argon.INSTANCE.getProfileManager().markDirty();
	}

	public void mouseReleased(int button) {
		if (button == InputConstants.MOUSE_BUTTON_LEFT)
			dragging = false;
	}

	public void mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
		if (mouseX >= listX() && mouseX <= listX() + listWidth() && mouseY >= listY() && mouseY <= listY() + LIST_ROWS * ROW)
			scrollRow = Math.max(0, scrollRow - (int) Math.signum(verticalAmount) * 3);
	}

	public void keyPressed(int keyCode, int modifiers) {
		if (keyCode == InputConstants.KEY_F && (modifiers & InputConstants.MOD_CONTROL) != 0) {
			search.focus();
		} else if (keyCode == InputConstants.KEY_ESCAPE) {
			if (!search.escape()) close();
			scrollRow = 0;
		} else if (keyCode == InputConstants.KEY_BACKSPACE) {
			search.backspace();
			scrollRow = 0;
		} else if (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER) {
			search.blur();
		}
	}

	public void charTyped(String chars) {
		search.type(chars);
		scrollRow = 0;
	}

	private void close() {
		Argon.INSTANCE.clickGui.panel = null;
	}
}
