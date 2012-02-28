package net.appositedesigns.fileexplorer.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import net.appositedesigns.fileexplorer.FileExplorerApp;
import net.appositedesigns.fileexplorer.exception.LocationInvalidException;
import net.appositedesigns.fileexplorer.model.FileListEntry;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class PreferenceUtil {

	public enum SortField {
		NAME, MTIME, SIZE
	}

	private Activity mContext;
	public static final String EULA_ACCEPTED = "eula_accepted_v2.5";
	public static final String EULA_MARKER = "eula_marker_file_v2.5";

	public static final String BOOKMARKS_FILE = "bookmarks_v2.5";
	public static final String BOOKMARKS = "bookmarks";

	public static final String PREF_HOME_DIR = "homeDir";
	public static final String PREF_SDCARD_OPTIONS = "sdCardOptions";
	public static final String PREF_SHOW_DIR_SIZES = "showDirSizes";
	public static final String PREF_SHOW_DIRS_FIRST = "showDirsFirst";
	public static final String PREF_SHOW_HIDDEN = "showHidden";
	public static final String PREF_SHOW_SYSFILES = "showSysFiles";
	public static final String PREF_SORT_DIR = "sort.dir";
	public static final String PREF_SORT_FIELD = "sort.field";
	public static final String PREF_THEME = "theme";
	public static final String PREF_USE_BACK_BUTTON = "useBackButton";
	public static final String PREF_USE_QUICKACTIONS = "useQuickActions";
	public static final String PREF_ZIP_ENABLE = "zipEnable";
	public static final String PREF_ZIP_USE_ZIP_FOLDER = "useZipFolder";
	public static final String PREF_ZIP_LOCATION = "zipLocation";
	public static final String KEY_RESTART_DIR = "net.appositedesigns.fileexplorer.RestartDirectory";

	public static final String VALUE_SORT_DIR_ASC = "asc";
	public static final String VALUE_SORT_DIR_DESC = "desc";
	public static final String VALUE_SORT_FIELD_M_TIME = "mtime";
	public static final String VALUE_SORT_FIELD_NAME = "name";
	public static final String VALUE_SORT_FIELD_SIZE = "size";
	public static final String VALUE_THEME_BLACK = "theme_black";
	public static final String VALUE_THEME_WHITE = "theme_white";
	public static final String VALUE_THEME_WHITE_BLACK = "theme_white_black";

	private static List<String> bookmarkedPaths = new ArrayList<String>();

	public PreferenceUtil(Activity context) {
		mContext = context;

		refreshBookmarkCache();
	}

	private void refreshBookmarkCache() {
		String bookmarkCsv = mContext.getSharedPreferences(BOOKMARKS_FILE,
				Context.MODE_PRIVATE).getString(BOOKMARKS, "");

		StringTokenizer tokens = new StringTokenizer(bookmarkCsv, "\n");
		bookmarkedPaths.clear();
		while (tokens.hasMoreTokens()) {
			String bookmark = tokens.nextToken();

			File dir = new File(bookmark);
			if (dir.exists() && dir.isDirectory()) {
				synchronized (bookmarkedPaths) {
				
					bookmarkedPaths.add(bookmark);
				}
				
			}
		}

	}

	public boolean isShowDirsOnTop() {
		return PreferenceManager.getDefaultSharedPreferences(mContext)
				.getBoolean(PREF_SHOW_DIRS_FIRST, true);
	}

	public boolean isShowHidden() {
		return PreferenceManager.getDefaultSharedPreferences(mContext)
				.getBoolean(PREF_SHOW_HIDDEN, true);
	}

	public boolean useBackNavigation() {
		return PreferenceManager.getDefaultSharedPreferences(mContext)
				.getBoolean(PREF_USE_BACK_BUTTON, true);
	}

	public boolean useQuickActions() {
		return PreferenceManager.getDefaultSharedPreferences(mContext)
				.getBoolean(PREF_USE_QUICKACTIONS, true);
	}

	public SortField getSortField() {
		String field = PreferenceManager.getDefaultSharedPreferences(mContext)
				.getString(PREF_SORT_FIELD, VALUE_SORT_FIELD_NAME);
		if (VALUE_SORT_FIELD_NAME.equalsIgnoreCase(field)) {
			return SortField.NAME;
		} else if (VALUE_SORT_FIELD_M_TIME.equalsIgnoreCase(field)) {
			return SortField.MTIME;
		} else if (VALUE_SORT_FIELD_SIZE.equalsIgnoreCase(field)) {
			return SortField.SIZE;
		} else {
			return SortField.NAME;
		}
	}

	public int getSortDir() {
		String field = PreferenceManager.getDefaultSharedPreferences(mContext)
				.getString(PREF_SORT_DIR, "asc");
		if (VALUE_SORT_DIR_ASC.equalsIgnoreCase(field)) {
			return 1;
		} else {
			return -1;
		}
	}

	public File getStartDir() {
		String dirPath = PreferenceManager
				.getDefaultSharedPreferences(mContext).getString(PREF_HOME_DIR,
						"/");
		File homeDir = new File(dirPath);

		if (homeDir.exists() && homeDir.isDirectory()) {
			return homeDir;
		} else {
			return new File("/");
		}
	}

	public boolean isFindDirSizes() {

		return false;
		// return
		// PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(PREF_SHOW_DIR_SIZES,
		// false);
	}

	public boolean isShowSystemFiles() {
		return PreferenceManager.getDefaultSharedPreferences(mContext)
				.getBoolean(PREF_SHOW_SYSFILES, true);
	}

	public File getZipDestinationDir() throws LocationInvalidException {
		String dirPath = PreferenceManager
				.getDefaultSharedPreferences(mContext).getString(
						PREF_ZIP_LOCATION, "/sdcard/zipped");
		Boolean useZipFolder = PreferenceManager.getDefaultSharedPreferences(
				mContext).getBoolean(PREF_ZIP_USE_ZIP_FOLDER, false);

		if (!useZipFolder) {
			return null;
		}
		File dir = new File(dirPath);

		if (dir.exists() && dir.isDirectory()) {
			return dir;
		} else if (!dir.exists()) {
			dir.mkdirs();
			return dir;
		} else {
			throw new LocationInvalidException(dirPath);
		}
	}

	public boolean isEnableSdCardOptions() {

		return PreferenceManager.getDefaultSharedPreferences(mContext)
				.getBoolean(PREF_SDCARD_OPTIONS, true);
	}

	public boolean isZipEnabled() {
		return PreferenceManager.getDefaultSharedPreferences(mContext)
				.getBoolean(PREF_ZIP_ENABLE, false);
	}

	public boolean isEulaAccepted() {
		return mContext.getSharedPreferences(EULA_MARKER, Context.MODE_PRIVATE)
				.getBoolean(EULA_ACCEPTED, false);
	}

	public void markEulaAccepted() {
		SharedPreferences.Editor editor = mContext.getSharedPreferences(
				EULA_MARKER, Context.MODE_WORLD_WRITEABLE).edit();
		editor.putBoolean(EULA_ACCEPTED, true);
		editor.commit();
	}

	public int getTheme() {

		String theme = PreferenceManager.getDefaultSharedPreferences(mContext)
				.getString(PREF_THEME, VALUE_THEME_WHITE);
		if (VALUE_THEME_BLACK.equalsIgnoreCase(theme)) {
			return FileExplorerApp.THEME_BLACK;
		} else if (VALUE_THEME_WHITE_BLACK.equalsIgnoreCase(theme)) {
			return FileExplorerApp.THEME_WHITE_BLACK;
		} else {
			return FileExplorerApp.THEME_WHITE;
		}
	}

	public boolean isBookmarked(String path) {

		return bookmarkedPaths.contains(path);
	}

	public void addBookmark(final String path) {

		new Thread(new Runnable() {

			@Override
			public void run() {
				final String bookmarkCsv = mContext.getSharedPreferences(
						BOOKMARKS_FILE, Context.MODE_PRIVATE).getString(
						BOOKMARKS, "");

				StringTokenizer tokens = new StringTokenizer(bookmarkCsv, "\n");
				boolean found = false;
				while (tokens.hasMoreTokens()) {
					String bookmark = tokens.nextToken();

					if (bookmark != null && bookmark.equalsIgnoreCase(path)) {
						found = true;
						break;
					}
				}

				if (!found) {

					SharedPreferences.Editor editor = mContext
							.getSharedPreferences(BOOKMARKS_FILE,
									Context.MODE_WORLD_WRITEABLE).edit();
					editor.putString(BOOKMARKS, bookmarkCsv + "\n" + path);
					editor.commit();

					refreshBookmarkCache();
				}

			}
		}).start();
	}

	public void removeBookmark(final String path) {

		new Thread(new Runnable() {

			@Override
			public void run() {
				String bookmarkCsv = mContext.getSharedPreferences(
						BOOKMARKS_FILE, Context.MODE_PRIVATE).getString(
						BOOKMARKS, "");

				StringTokenizer tokens = new StringTokenizer(bookmarkCsv, "\n");
				final StringBuffer buffer = new StringBuffer();

				while (tokens.hasMoreTokens()) {
					String bookmark = tokens.nextToken();

					if (bookmark != null && !bookmark.equals(path)) {
						buffer.append("\n");
						buffer.append(path);
					}
				}

				SharedPreferences.Editor editor = mContext
						.getSharedPreferences(BOOKMARKS_FILE,
								Context.MODE_WORLD_WRITEABLE).edit();
				editor.putString(BOOKMARKS, buffer.toString());
				editor.commit();

				refreshBookmarkCache();

			}
		}).start();
	}

	public List<FileListEntry> getBookmarks() {

		String bookmarkCsv = mContext.getSharedPreferences(BOOKMARKS_FILE,
				Context.MODE_PRIVATE).getString(BOOKMARKS, "");

		StringTokenizer tokens = new StringTokenizer(bookmarkCsv, "\n");
		List<FileListEntry> files = new ArrayList<FileListEntry>();

		while (tokens.hasMoreTokens()) {
			String bookmark = tokens.nextToken();

			File dir = new File(bookmark);
			if (dir.exists() && dir.isDirectory()) {
				FileListEntry entry = new FileListEntry(bookmark);
				files.add(entry);
			}
		}
		return files;

	}
}
