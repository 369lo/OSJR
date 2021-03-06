package game;

import java.applet.Applet;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TrayIcon.MessageType;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;

import cache.ActorListener;
import cache.ItemDefinitionManager;
import cache.MessageManager;
import cache.ObjectManager;
import cache.TileListener;
import discord.DiscordManager;
import hooks.Hooks;
import hooks.accessors.Client;
import hooks.accessors.Region;
import paint.InputListeners;
import paint.MenuHandler;
import paint.PaintListener;
import paint.TextPaintListener;
import paint.misc.ActorNames;
import paint.misc.DecorativeObjects;
import paint.misc.FpsPaintListener;
import paint.misc.GameObjects;
import paint.misc.GroundItems;
import paint.misc.GroundObjects;
import paint.misc.WallObjects;
import paint.misc.XpGlobe;
import paint.skills.AgilityOverlay;
import paint.skills.FishingOverlay;
import reflection.JarLoader;

public class Game extends Canvas implements Runnable {

	public static Applet applet;
	public static JarLoader jarLoader;
	private static final long serialVersionUID = 1L;
	public static boolean debug;
	private static BufferedImage gameImage;
	static InputListeners inputListeners;
	public boolean loading = true;
	public BufferedImage paintImage;
	static List<PaintListener> paintListeners;
	static private Thread paintThread;
	static private List<TextPaintListener> textPaintListeners;
	public static Region oldRegion = new Region(null);
	public static ThreadGroup threadGroup;
	public static boolean vanilla = false;
	public ObjectManager objectManager = new ObjectManager();

	Graphics2D g2d;
	static Graphics paintGraphics;
	private Rectangle r;
	private String[] lines;
	public static Font runescapeFont;
	public static boolean ctrlPressed;

	public Game(String[] args) {
		for (String s : args) {
			if (s.compareTo("vanilla") == 0) {
				vanilla = true;
			} else if (s.compareTo("debug") == 0) {
				debug = true;
			}
		}
		if (vanilla) {
			jarLoader = new JarLoader();

			Class<?> c;
			try {
				c = jarLoader.loadClass("client");
				applet = (Applet) c.newInstance();
				applet.setStub(jarLoader.getAppletStub());
				JFrame vanillaFrame = new JFrame("Vanilla OSRS");
				applet.setSize(800, 600);
				vanillaFrame.setSize(800, 600);
				applet.init();
				applet.start();
				vanillaFrame.add(applet);
				vanillaFrame.setVisible(true);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {

			threadGroup = new ThreadGroup("RSGame");
			Game.gameImage = new BufferedImage(765, 503, BufferedImage.TYPE_INT_RGB);
			this.paintImage = new BufferedImage(765, 503, BufferedImage.TYPE_INT_RGB);

			new Thread(new Runnable() {
				@SuppressWarnings("unused")
				@Override
				public void run() {
					try {
						jarLoader = new JarLoader();

						Class<?> c = jarLoader.loadClass("client");
						applet = (Applet) c.newInstance();
						applet.setStub(jarLoader.getAppletStub());
						applet.init();
						applet.setSize(OSRSLauncher.loaderWindow.getContentPane().getSize());
						applet.start();

						// Sleeping to let the game load
						Thread.sleep(1000);
						Hooks.client = new Client(applet);
						new Hooks();
						while (!Client.isLoaded()) {
							Thread.sleep(10);
						}

						DiscordManager.run();

						Game.inputListeners = new InputListeners(true, applet);
						requestFocus();
						addMouseListener(Game.inputListeners);
						addMouseMotionListener(Game.inputListeners);
						addMouseWheelListener(Game.inputListeners);
						addKeyListener(Game.inputListeners);
						addFocusListener(Game.inputListeners);

						Game.paintListeners.add(new FpsPaintListener(Hooks.client));
						Game.paintListeners.add(new ActorNames(Hooks.client));
						Game.paintListeners.add(new GroundObjects(Hooks.client));
						Game.paintListeners.add(new GameObjects());
						Game.paintListeners.add(new DecorativeObjects(Hooks.client));
						Game.paintListeners.add(new WallObjects(Hooks.client));

						Game.paintListeners.add(new AgilityOverlay());
						Game.paintListeners.add(new FishingOverlay());
						Game.paintListeners.add(new XpGlobe());
						Game.paintListeners.add(new MessageManager());

						Game.paintListeners.add(new TileListener());
						Game.paintListeners.add(new ActorListener());
						Game.paintListeners.add(new GroundItems());

						Game.this.objectManager.t.start();
					} catch (Exception e) {
						e.printStackTrace();
					}

					ItemDefinitionManager.init();

					Game.this.loading = false;
					System.out.println("[OSRS] Init Complete.");

				}
			}).start();

			Game.paintListeners = new ArrayList<>();
			Game.textPaintListeners = new ArrayList<>();

			Game.paintThread = new Thread(threadGroup, this, "paint");
			Game.paintThread.start();

			try {
				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("./resources/Runescape.ttf")));
			} catch (IOException | FontFormatException e) {
				e.printStackTrace();
			}

			for (Font f : GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()) {
				if (f.getName().compareTo("RuneScape") == 0) {
					Font g = f.deriveFont(16);
					g = f.deriveFont(Font.BOLD, 16);
					runescapeFont = g;
					System.out.println("[OSRS] Loaded Runescape Font.");
				}
			}

			this.setSize(765, 503);
		}

	}

	public void focus() {
		requestFocus();
	}

	public static Graphics gamePaint() {
		return Game.gameImage.getGraphics();
	}

	public static Applet getApplet() {
		return applet;
	}

	@Override
	public void paint(Graphics g) {
		try {
			if (!this.isVisible())
				return;

			if (OSRSLauncher.loaderWindow.getHeight() != Game.gameImage.getHeight()
					|| OSRSLauncher.loaderWindow.getWidth() != Game.gameImage.getWidth()) {
				Game.gameImage = new BufferedImage(OSRSLauncher.loaderWindow.getWidth(),
						OSRSLauncher.loaderWindow.getHeight(), BufferedImage.TYPE_INT_RGB);
				this.paintImage = new BufferedImage(OSRSLauncher.loaderWindow.getWidth(),
						OSRSLauncher.loaderWindow.getHeight(), BufferedImage.TYPE_INT_RGB);
				if (applet != null)
					applet.setSize(OSRSLauncher.loaderWindow.getContentPane().getSize());
			}

			this.g2d = (Graphics2D) g;
			this.g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			this.g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			this.r = this.g2d.getClipBounds();
			if (this.loading) {
				this.g2d.drawImage(Game.gameImage, this.r.x, this.r.y, this.r.x + this.r.width,
						this.r.y + this.r.height, this.r.x, this.r.y, this.r.x + this.r.width, this.r.y + this.r.height,
						null);
				return;
			}

			if (Game.gameImage != null) {
				this.paintImage.flush();

				Game.paintGraphics = this.paintImage.getGraphics();
				Game.paintGraphics.drawImage(Game.gameImage, this.r.x, this.r.y, this.r.x + this.r.width,
						this.r.y + this.r.height, this.r.x, this.r.y, this.r.x + this.r.width, this.r.y + this.r.height,
						null);

				for (PaintListener pl : Game.paintListeners) {
					pl.onRepaint(Game.paintGraphics);
				}

				for (TextPaintListener tpl : Game.textPaintListeners) {
					int y = 40;
					Game.paintGraphics.setColor(Color.cyan);
					this.lines = tpl.onTextRepaint();
					if (this.lines != null) {
						for (String line : this.lines) {
							if (line == null)
								continue;
							Game.paintGraphics.drawString(line, 20, y);
							y += 15;
						}
					}
				}

				this.g2d.drawImage(this.paintImage, this.r.x, this.r.y, this.r.x + this.r.width,
						this.r.y + this.r.height, this.r.x, this.r.y, this.r.x + this.r.width, this.r.y + this.r.height,
						null);
				Game.paintGraphics.dispose();
			}
		} catch (RasterFormatException ignored) {
		}
	}

	@Override
	public void paintAll(Graphics g) {
		paint(g);
	}

	@Override
	public void repaint(int x, int y, int width, int height) {
		super.repaint(0, 0, OSRSLauncher.loaderWindow.getWidth(), OSRSLauncher.loaderWindow.getHeight());
	}

	@Override
	public void repaint(long tm, int x, int y, int width, int height) {
		super.repaint(tm, 0, 0, OSRSLauncher.loaderWindow.getWidth(), getHeight());
	}

	public static void sendTrayMessage(String caption, String message) {
		if (OSRSLauncher.trayIcon != null)
			OSRSLauncher.trayIcon.displayMessage(caption, message, MessageType.NONE);
	}

	@Override
	public void run() {
		while (true)
			try {
				if (this.isShowing()) {
					repaint();
					Thread.sleep(1000 / 50);
					if (!hasFocus()) {
						if (MenuHandler.colorPick != null)
							if (!MenuHandler.colorPick.isVisible())
								requestFocus();
					}

				} else {
					Thread.sleep(300);
				}
			} catch (InterruptedException ignored) {
			}
	}

	@Override
	public void update(Graphics g) {
		paint(g);
	}

}
