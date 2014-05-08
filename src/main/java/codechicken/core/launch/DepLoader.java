package codechicken.core.launch;

import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.launchwrapper.LaunchClassLoader;

import cpw.mods.fml.common.versioning.ComparableVersion;
import cpw.mods.fml.relauncher.FMLInjectionData;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.IFMLCallHook;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import sun.misc.URLClassPath;
import sun.net.util.URLUtil;

/**
 * For autodownloading stuff.
 * This is really unoriginal, mostly ripped off FML, credits to cpw.
 */
public class DepLoader implements IFMLLoadingPlugin, IFMLCallHook {
	private static ByteBuffer downloadBuffer = ByteBuffer.allocateDirect(1 << 23);
	private static final String owner = "CB's DepLoader";
	private static DepLoadInst inst;

	public interface IDownloadDisplay {
		void resetProgress(int sizeGuess);

		void setPokeThread(Thread currentThread);

		void updateProgress(int fullLength);

		boolean shouldStopIt();

		void updateProgressString(String string, Object... data);

		Object makeDialog();

		void showErrorDialog(String name, String url);
	}

	@SuppressWarnings("serial")
	public static class Downloader extends JOptionPane implements IDownloadDisplay {
		private JDialog container;
		private JLabel currentActivity;
		private JProgressBar progress;
		boolean stopIt;
		Thread pokeThread;

		private Box makeProgressPanel() {
			Box box = Box.createVerticalBox();
			box.add(Box.createRigidArea(new Dimension(0, 10)));
			JLabel welcomeLabel = new JLabel("<html><b><font size='+1'>" + owner + " is setting up your minecraft environment</font></b></html>");
			box.add(welcomeLabel);
			welcomeLabel.setAlignmentY(LEFT_ALIGNMENT);
			welcomeLabel = new JLabel("<html>Please wait, " + owner + " has some tasks to do before you can play</html>");
			welcomeLabel.setAlignmentY(LEFT_ALIGNMENT);
			box.add(welcomeLabel);
			box.add(Box.createRigidArea(new Dimension(0, 10)));
			currentActivity = new JLabel("Currently doing ...");
			box.add(currentActivity);
			box.add(Box.createRigidArea(new Dimension(0, 10)));
			progress = new JProgressBar(0, 100);
			progress.setStringPainted(true);
			box.add(progress);
			box.add(Box.createRigidArea(new Dimension(0, 30)));
			return box;
		}

		@Override
		public JDialog makeDialog() {
			if (container != null)
				return container;

			setMessageType(JOptionPane.INFORMATION_MESSAGE);
			setMessage(makeProgressPanel());
			setOptions(new Object[]{"Stop"});
			addPropertyChangeListener(new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if (evt.getSource() == Downloader.this && evt.getPropertyName() == VALUE_PROPERTY) {
						requestClose("This will stop minecraft from launching\nAre you sure you want to do this?");
					}
				}
			});
			container = new JDialog(null, "Hello", ModalityType.MODELESS);
			container.setResizable(false);
			container.setLocationRelativeTo(null);
			container.add(this);
			this.updateUI();
			container.pack();
			container.setMinimumSize(container.getPreferredSize());
			container.setVisible(true);
			container.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			container.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					requestClose("Closing this window will stop minecraft from launching\nAre you sure you wish to do this?");
				}
			});
			return container;
		}

		protected void requestClose(String message) {
			int shouldClose = JOptionPane.showConfirmDialog(container, message, "Are you sure you want to stop?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (shouldClose == JOptionPane.YES_OPTION)
				container.dispose();

			stopIt = true;
			if (pokeThread != null)
				pokeThread.interrupt();
		}

		@Override
		public void updateProgressString(String progressUpdate, Object... data) {
			//FMLLog.finest(progressUpdate, data);
			if (currentActivity != null)
				currentActivity.setText(String.format(progressUpdate, data));
		}

		@Override
		public void resetProgress(int sizeGuess) {
			if (progress != null)
				progress.getModel().setRangeProperties(0, 0, 0, sizeGuess, false);
		}

		@Override
		public void updateProgress(int fullLength) {
			if (progress != null)
				progress.getModel().setValue(fullLength);
		}

		@Override
		public void setPokeThread(Thread currentThread) {
			this.pokeThread = currentThread;
		}

		@Override
		public boolean shouldStopIt() {
			return stopIt;
		}

		@Override
		public void showErrorDialog(String name, String url) {
			JEditorPane ep = new JEditorPane("text/html",
					"<html>" +
							owner + " was unable to download required library " + name +
							"<br>Check your internet connection and try restarting or download it manually from" +
							"<br><a href=\"" + url + "\">" + url + "</a> and put it in your mods folder" +
							"</html>");

			ep.setEditable(false);
			ep.setOpaque(false);
			ep.addHyperlinkListener(new HyperlinkListener() {
				@Override
				public void hyperlinkUpdate(HyperlinkEvent event) {
					try {
						if (event.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
							Desktop.getDesktop().browse(event.getURL().toURI());
					} catch (Exception e) {
					}
				}
			});

			JOptionPane.showMessageDialog(null, ep, "A download error has occured", JOptionPane.ERROR_MESSAGE);
		}
	}

	public static class DummyDownloader implements IDownloadDisplay {
		@Override
		public void resetProgress(int sizeGuess) {
		}

		@Override
		public void setPokeThread(Thread currentThread) {
		}

		@Override
		public void updateProgress(int fullLength) {
		}

		@Override
		public boolean shouldStopIt() {
			return false;
		}

		@Override
		public void updateProgressString(String string, Object... data) {
		}

		@Override
		public Object makeDialog() {
			return null;
		}

		@Override
		public void showErrorDialog(String name, String url) {
		}
	}

	public static class Dependancy {
		public String url;
		public String[] filesplit;
		public ComparableVersion version;

		public String existing;
		/**
		 * Flag set to add this dep to the classpath immediately because it is required for a coremod.
		 */
		public boolean coreLib;

		public Dependancy(String url, String[] filesplit, boolean coreLib) {
			this.url = url;
			this.filesplit = filesplit;
			this.coreLib = coreLib;
			version = new ComparableVersion(filesplit[1]);
		}

		public String getName() {
			return filesplit[0];
		}

		public String fileName() {
			return filesplit[0] + filesplit[1] + filesplit[2];
		}
	}

	public static class DepLoadInst {
		private File modsDir;
		private File v_modsDir;
		private IDownloadDisplay downloadMonitor;
		private JDialog popupWindow;

		private Map<String, Dependancy> depMap = new HashMap<String, Dependancy>();
		private HashSet<String> depSet = new HashSet<String>();

		public DepLoadInst() {
			String mcVer = (String) FMLInjectionData.data()[4];
			File mcDir = (File) FMLInjectionData.data()[6];

			modsDir = new File(mcDir, "mods");
			v_modsDir = new File(mcDir, "mods/" + mcVer);
			if (!v_modsDir.exists())
				v_modsDir.mkdirs();
		}

		private void addClasspath(String name) {
			try {
				((LaunchClassLoader) DepLoader.class.getClassLoader()).addURL(new File(v_modsDir, name).toURI().toURL());
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}

		private void deleteMod(File mod) {
			if (mod.delete())
				return;

			try {
				ClassLoader cl = DepLoader.class.getClassLoader();
				URL url = mod.toURI().toURL();
				Field f_ucp = URLClassLoader.class.getDeclaredField("ucp");
				Field f_loaders = URLClassPath.class.getDeclaredField("loaders");
				Field f_lmap = URLClassPath.class.getDeclaredField("lmap");
				f_ucp.setAccessible(true);
				f_loaders.setAccessible(true);
				f_lmap.setAccessible(true);

				URLClassPath ucp = (URLClassPath) f_ucp.get(cl);
				Closeable loader = ((Map<String, Closeable>) f_lmap.get(ucp)).remove(URLUtil.urlNoFragString(url));
				if (loader != null) {
					loader.close();
					((List<?>) f_loaders.get(ucp)).remove(loader);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (!mod.delete()) {
				mod.deleteOnExit();
				String msg = owner + " was unable to delete file " + mod.getPath() + " the game will now try to delete it on exit. If this dialog appears again, delete it manually.";
				System.err.println(msg);
				if (!GraphicsEnvironment.isHeadless())
					JOptionPane.showMessageDialog(null, msg, "An update error has occured", JOptionPane.ERROR_MESSAGE);

				System.exit(1);
			}
		}

		private void download(Dependancy dep) {
			popupWindow = (JDialog) downloadMonitor.makeDialog();
			File libFile = new File(v_modsDir, dep.fileName());
			try {
				URL libDownload = new URL(dep.url + '/' + dep.fileName());
				downloadMonitor.updateProgressString("Downloading file %s", libDownload.toString());
				System.out.format("Downloading file %s\n", libDownload.toString());
				URLConnection connection = libDownload.openConnection();
				connection.setConnectTimeout(5000);
				connection.setReadTimeout(5000);
				connection.setRequestProperty("User-Agent", "" + owner + " Downloader");
				int sizeGuess = connection.getContentLength();
				download(connection.getInputStream(), sizeGuess, libFile);
				downloadMonitor.updateProgressString("Download complete");
				System.out.println("Download complete");

				scanDepInfo(libFile);
			} catch (Exception e) {
				libFile.delete();
				if (downloadMonitor.shouldStopIt()) {
					System.err.println("You have stopped the downloading operation before it could complete");
					System.exit(1);
					return;
				}
				downloadMonitor.showErrorDialog(dep.fileName(), dep.url + '/' + dep.fileName());
				throw new RuntimeException("A download error occured", e);
			}
		}

		private void download(InputStream is, int sizeGuess, File target) throws Exception {
			if (sizeGuess > downloadBuffer.capacity())
				throw new Exception(String.format("The file %s is too large to be downloaded by " + owner + " - the download is invalid", target.getName()));

			downloadBuffer.clear();

			int bytesRead, fullLength = 0;

			downloadMonitor.resetProgress(sizeGuess);
			try {
				downloadMonitor.setPokeThread(Thread.currentThread());
				byte[] smallBuffer = new byte[1024];
				while ((bytesRead = is.read(smallBuffer)) >= 0) {
					downloadBuffer.put(smallBuffer, 0, bytesRead);
					fullLength += bytesRead;
					if (downloadMonitor.shouldStopIt()) {
						break;
					}
					downloadMonitor.updateProgress(fullLength);
				}
				is.close();
				downloadMonitor.setPokeThread(null);
				downloadBuffer.limit(fullLength);
				downloadBuffer.position(0);
			} catch (InterruptedIOException e) {
				// We were interrupted by the stop button. We're stopping now.. clear interruption flag.
				Thread.interrupted();
				throw new Exception("Stop");
			} catch (IOException e) {
				throw e;
			}

			try {
                /*String cksum = generateChecksum(downloadBuffer);
                if (cksum.equals(validationHash))
                {*/
				if (!target.exists())
					target.createNewFile();


				downloadBuffer.position(0);
				FileOutputStream fos = new FileOutputStream(target);
				fos.getChannel().write(downloadBuffer);
				fos.close();
                /*}
                else
                {
                    throw new RuntimeException(String.format("The downloaded file %s has an invalid checksum %s (expecting %s). The download did not succeed correctly and the file has been deleted. Please try launching again.", target.getName(), cksum, validationHash));
                }*/
			} catch (Exception e) {
				throw e;
			}
		}

		private String checkExisting(String[] dependency) {
			for (File f : modsDir.listFiles()) {
				String[] split = splitFileName(f.getName());
				if (split == null || !split[0].equals(dependency[0]))
					continue;

				if (f.renameTo(new File(v_modsDir, f.getName())))
					continue;

				deleteMod(f);
			}

			for (File f : v_modsDir.listFiles()) {
				String[] split = splitFileName(f.getName());
				if (split == null || !split[0].equals(dependency[0]))
					continue;

				ComparableVersion found = new ComparableVersion(split[1]);
				ComparableVersion requested = new ComparableVersion(dependency[1]);

				int cmp = found.compareTo(requested);
				if (cmp < 0) {
					System.out.println("Deleted old version " + f.getName());
					deleteMod(f);
					return null;
				}
				if (cmp > 0) {
					System.err.println("Warning: version of " + dependency[0] + ", " + split[1] + " is newer than request " + dependency[1]);
					return f.getName();
				}
				return f.getName();//found dependency
			}
			return null;
		}

		public void load() {
			scanDepInfos();
			if (depMap.isEmpty())
				return;

			loadDeps();
			activateDeps();
		}

		private void activateDeps() {
			for (Dependancy dep : depMap.values())
				if (dep.coreLib)
					addClasspath(dep.existing);
		}

		private void loadDeps() {
			downloadMonitor = FMLLaunchHandler.side().isClient() ? new Downloader() : new DummyDownloader();
			try {
				while (!depSet.isEmpty()) {
					Iterator<String> it = depSet.iterator();
					Dependancy dep = depMap.get(it.next());
					it.remove();
					load(dep);
				}
			} finally {
				if (popupWindow != null) {
					popupWindow.setVisible(false);
					popupWindow.dispose();
				}
			}
		}

		private void load(Dependancy dep) {
			dep.existing = checkExisting(dep.filesplit);
			if (dep.existing == null)//download dep
			{
				download(dep);
				dep.existing = dep.fileName();
			}
		}

		private List<File> modFiles() {
			List<File> list = new LinkedList<File>();
			list.addAll(Arrays.asList(modsDir.listFiles()));
			list.addAll(Arrays.asList(v_modsDir.listFiles()));
			return list;
		}

		private void scanDepInfos() {
			for (File file : modFiles()) {
				if (!file.getName().endsWith(".jar") && !file.getName().endsWith(".zip"))
					continue;

				scanDepInfo(file);
			}
		}

		private void scanDepInfo(File file) {
			try {
				ZipFile zip = new ZipFile(file);
				ZipEntry e = zip.getEntry("dependancies.info");
				if (e == null) e = zip.getEntry("dependencies.info");
				if (e != null)
					loadJSon(zip.getInputStream(e));
				zip.close();
			} catch (Exception e) {
				System.err.println("Failed to load dependencies.info from " + file.getName() + " as JSON");
				e.printStackTrace();
			}
		}

		private void loadJSon(InputStream input) throws IOException {
			InputStreamReader reader = new InputStreamReader(input);
			JsonElement root = new JsonParser().parse(reader);
			if (root.isJsonArray())
				loadJSonArr(root);
			else
				loadJson(root.getAsJsonObject());
			reader.close();
		}

		private void loadJSonArr(JsonElement root) throws IOException {
			for (JsonElement node : root.getAsJsonArray())
				loadJson(node.getAsJsonObject());
		}

		private void loadJson(JsonObject node) throws IOException {
			boolean obfuscated = ((LaunchClassLoader) DepLoader.class.getClassLoader())
					.getClassBytes("net.minecraft.world.World") == null;

			String testClass = node.get("class").getAsString();
			if (DepLoader.class.getResource("/" + testClass.replace('.', '/') + ".class") != null)
				return;

			String repo = node.get("repo").getAsString();
			String file = node.get("file").getAsString();
			if (!obfuscated && node.has("dev"))
				file = node.get("dev").getAsString();

			boolean coreLib = node.has("coreLib") && node.get("coreLib").getAsBoolean();

			String[] split = splitFileName(file);
			if (split == null)
				throw new RuntimeException("Invalid filename format for dependency: " + file);

			addDep(new Dependancy(repo, split, coreLib));
		}

		private void addDep(Dependancy newDep) {
			if (mergeNew(depMap.get(newDep.getName()), newDep)) {
				depMap.put(newDep.getName(), newDep);
				depSet.add(newDep.getName());
			}
		}

		private boolean mergeNew(Dependancy oldDep, Dependancy newDep) {
			if (oldDep == null)
				return true;

			Dependancy newest = newDep.version.compareTo(oldDep.version) > 0 ? newDep : oldDep;
			newest.coreLib = newDep.coreLib || oldDep.coreLib;

			return newest == newDep;
		}
	}

	public static void load() {
		if (inst == null) {
			inst = new DepLoadInst();
			inst.load();
		}
	}

	private static String[] splitFileName(String filename) {
		Pattern p = Pattern.compile("(.+?)([\\d\\.\\w]+)(\\.[^\\d]+)");
		Matcher m = p.matcher(filename);
		if (!m.matches())
			return null;

		return new String[]{m.group(1), m.group(2), m.group(3)};
	}

	@Override
	public String[] getASMTransformerClass() {
		return null;
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return getClass().getName();
	}

	@Override
	public void injectData(Map<String, Object> data) {
	}

	@Override
	public Void call() {
		load();

		return null;
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}
}
