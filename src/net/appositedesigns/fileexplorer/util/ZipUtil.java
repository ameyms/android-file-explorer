package net.appositedesigns.fileexplorer.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

import android.util.Log;

/**
 * The class provides the utility method to zip files or folders uses
 * java.util.zip package underneath
 */
public class ZipUtil
{

	private static String TAG = ZipUtil.class.getName();
	private String parentDir; // String to store the parent directory of the
								// folder to be zipped
	private List<String> extensionList;

	private ZipOutputStream zipOut = null;

	private String outFilePath;

	/**
	 * This function will zip the folder and all its content maintaining the
	 * directory structure intact
	 * 
	 * @param srcFolderName
	 *            Name of the source folder that resides in parentFolder that
	 *            you want to zip
	 * @param parentFolder
	 *            - Parent folder where source folder resides
	 * @param outFileName
	 *            name of the output zip file that's created in the parent folder
	 * @throws FileNotFoundException
	 *             thrown in case specified file is not found
	 * @throws IOException
	 *             thrown in case any prob occurs while I/O
	 * 
	 * @return The path for zipped file [path]\yourFile.zip
	 */
	public String zipFolder(String srcFolderName, String parentFolder,
			String outFilename) throws FileNotFoundException, IOException
	{
		String zipFilePath = null;
		setParentDir(srcFolderName);
		setOutFilePath(parentFolder + File.separator + outFilename);
		File inFolder = new File(parentFolder + File.separator + srcFolderName);

		// Add files or folders to the stream
		addFileToZip(inFolder);

		// Close the Output stream
		ZipOutputStream zipOutStream = getZipOut();
		if (zipOutStream == null)
		{
			throw new IOException("Unable to create the Zip file : "
					+ getOutFilePath());
		}
		else
		{
			zipOutStream.flush();
			zipOutStream.close();
			File returnFile = new File(getOutFilePath());
			zipFilePath = returnFile.getPath();
			return zipFilePath;
		}

	}

	/**
	 * This function will zip all the files in the folder maintaining the
	 * directory structure intact
	 * 
	 * @param srcFolder
	 *            -Parent directory in which to search for the file/s that you
	 *            want to zip
	 * @param destFolder
	 *            - Parent folder of reportDir
	 * @param extnList
	 *            of format *.xxx, the search criteria for the file
	 * @param zipFileName
	 *            name of the output zip file, default name is "Report.zip"
	 * 
	 * @throws FileNotFoundException
	 *             thrown in case specified file is not found
	 * @throws IOException
	 *             thrown in case any prob occurs while I/O
	 * 
	 * @return The path for zipped file [path]\yourFile.zip
	 */
	public String zipFileWithExtension(String srcFolder, String destFolder,
			List<String> extnList, String zipFileName)
			throws FileNotFoundException, IOException
	{
		try
		{
			setExtensionList(extnList);
			return zipFolder(srcFolder, destFolder, zipFileName);
		}
		catch (IOException ioe)
		{
			Log.i(TAG,"No file exists that match the given extension list");
			throw ioe;
		}

	}

	/**
	 * Adds given files to the zip files flattened out.
	 * 
	 * @param zipFile
	 * @param files
	 */
	public static void zip(File zipFile, List<File> files) throws Exception
	{
		ZipOutputStream out = null;
		try
		{
			zipFile.delete();

			Log.i(TAG,"Creating zip: " + zipFile);

			out = new ZipOutputStream(new FileOutputStream(zipFile));

			// Set the compression ratio
			out.setLevel(Deflater.BEST_COMPRESSION);

			// iterate through the array of files, adding each to the zip file
			for (File file : files)
			{
				// Associate a file input stream for the current file
				FileInputStream in = null;
				try
				{
					in = new FileInputStream(file);

					// Add ZIP entry to output stream.
					Log.i(TAG, "Adding: " + file);
					out.putNextEntry(new ZipEntry(file.getName()));

					IOUtils.copy(in, out);

					// Close the current entry
					out.closeEntry();
				}
				finally
				{
					IOUtils.closeQuietly(in);
				}
			}

		}
		finally
		{
			IOUtils.closeQuietly(out);
		}
	}

	/**
	 * Creates zip file containing files in the given folder. Name of the zip
	 * file is name of the folder.zip and it's placed in the parent folder of
	 * "folder".
	 * 
	 * @param zipFileName
	 * @param folder
	 * @throws IOException
	 */
	public File zip(File folder) throws IOException
	{

		ZipOutputStream out = null;
		try
		{
			String folderPath = folder.getCanonicalPath();
			int folderPathSize = folderPath.length();

			File zipFile = new File(folder.getParentFile(), folder.getName()
					+ ".zip");
			zipFile.delete();

			Log.i(TAG, "Compressing " + folder + " to " + zipFile);

			out = new ZipOutputStream(new FileOutputStream(zipFile));

			// Set the compression ratio
			out.setLevel(Deflater.BEST_COMPRESSION);

			// iterate through the array of files, adding each to the zip file
			ArrayList<String> files = new ArrayList<String>();
			ls(folder, files);

			byte[] buffer = new byte[18024];
			for (String file : files)
			{
				// Associate a file input stream for the current file
				FileInputStream in = null;
				try
				{
					in = new FileInputStream(file);

					// Add ZIP entry to output stream.
					out.putNextEntry(new ZipEntry(file
							.substring(folderPathSize + 1)));

					int len;
					while ((len = in.read(buffer)) > 0)
					{
						out.write(buffer, 0, len);
					}

					// Close the current entry
					out.closeEntry();
				}
				finally
				{
					if (in != null)
					{
						in.close();
					}
				}
			}

			return zipFile;
		}
		catch (Exception e)
		{
			throw (IOException) new IOException("Error zipping file")
					.initCause(e);
		}
		finally
		{
			if (out != null)
			{
				out.close();
			}
		}
	}

	/**
	 * Unzips the specified zip file to given folder. It will create the toDir
	 * folder if it does not exist.
	 * 
	 * @param archive
	 * @param toDir
	 * @return true if at least one entry extracted from archive, false otherwise
	 * @throws IOException
	 */
	public boolean unzip(String archive, String toDir) throws IOException
	{
		FileInputStream fis = null;
		boolean extracted = false;
		try
		{
			final int BUFFER = 2048;
			byte data[] = new byte[BUFFER];
			fis = new FileInputStream(archive);
			ZipInputStream zis = new ZipInputStream(
					new BufferedInputStream(fis));
			ZipEntry entry;
			
			while ((entry = zis.getNextEntry()) != null)
			{
				Log.i(TAG, "Extracting " + entry);
				int count;
				// write the files to the disk
				FileOutputStream fos = null;
				try
				{
					File file = new File(toDir, entry.getName());
					
					file.getParentFile().mkdirs();
					
					// TODO This is a fix for nested folders in zip, uncomment post 2.0
					/*
					if(entry.isDirectory())
					{
						file.mkdirs();
						continue;
					}
					*/
				
					fos = new FileOutputStream(file);
					BufferedOutputStream dest = new BufferedOutputStream(fos,
							BUFFER);
					while ((count = zis.read(data, 0, BUFFER)) != -1)
					{
						dest.write(data, 0, count);
					}
					dest.flush();
					extracted = true;
				}
				finally
				{
					if (fos != null)
					{
						try
						{
							fos.close();
						}
						catch (Exception e)
						{
							Log.w(TAG,"Error closing fos",e);
						}
					}
				}
			}
			zis.close();
		}
		finally
		{
			if (fis != null)
			{
				try
				{
					fis.close();
				}
				catch (Exception e)
				{
					Log.w(TAG,"Error closing file",e);
				}
			}
		}
		return extracted;
	}

	/**
	 * Unzips the specified zip file to given folder. It will create the toDir
	 * folder if it does not exist.
	 * 
	 * @param archive
	 * @param toDir
	 * @return true if at least one entry extracted from archive, false otherwise
	 * @throws IOException
	 */
	public boolean unzipNested(String archive, String toDir) throws IOException
	{
		FileInputStream fis = null;
		boolean extracted = false;
		try
		{
			final int BUFFER = 2048;
			byte data[] = new byte[BUFFER];
			fis = new FileInputStream(archive);
			ZipInputStream zis = new ZipInputStream(
					new BufferedInputStream(fis));
			ZipEntry entry;
			
			while ((entry = zis.getNextEntry()) != null)
			{
				Log.i(TAG, "Extracting " + entry);
				int count;
				// write the files to the disk
				FileOutputStream fos = null;
				try
				{
					File file = new File(toDir, entry.getName());
					
					file.getParentFile().mkdirs();
					
					if(entry.isDirectory())
					{
						file.mkdirs();
						continue;
					}
				
					fos = new FileOutputStream(file);
					BufferedOutputStream dest = new BufferedOutputStream(fos,
							BUFFER);
					while ((count = zis.read(data, 0, BUFFER)) != -1)
					{
						dest.write(data, 0, count);
					}
					dest.flush();
					extracted = true;
				}
				finally
				{
					if (fos != null)
					{
						try
						{
							fos.close();
						}
						catch (Exception e)
						{
							Log.w(TAG,"Error closing fos",e);
						}
					}
				}
			}
			zis.close();
		}
		finally
		{
			if (fis != null)
			{
				try
				{
					fis.close();
				}
				catch (Exception e)
				{
					Log.w(TAG,"Error closing file",e);
				}
			}
		}
		return extracted;
	}

	/**
	 * Lists files in the given folder and adds it to the file list. This is a
	 * recursive command.
	 * 
	 * @param folder
	 * @param list
	 * @throws IOException
	 */
	public static void ls(File file, ArrayList<String> list)
			throws IOException
	{
		if (file.isFile())
		{
			list.add(file.getCanonicalPath());
		}
		else if (file.isDirectory())
		{
			File[] files = file.listFiles();
			if (files != null)
			{
				for (File file1 : files)
				{
					if (file1.isFile())
					{
						list.add(file1.getCanonicalPath());
					}
					else
					{
						ls(file1, list);
					}
				}
			}
		}
	}

	/**
	 * Adds the specified file to zip
	 * 
	 * @param file
	 *            file to zip
	 * @throws FileNotFoundException
	 *             thrown in case specified file is not found
	 * @throws IOException
	 *             thrown in case any prob occurs while I/O
	 * 
	 * @see #addFolderToZip(File, ZipOutputStream)
	 */
	private void addFileToZip(File file) throws FileNotFoundException,
			IOException
	{
		byte[] buffer = new byte[1024];
		int bufSize;
		BufferedInputStream in = null;
		ZipOutputStream zipOutStream = null;

		if (file != null && file.exists())
		{
			if (file.isDirectory())
			{
				addFolderToZip(file);
			}
			else
			{
				String filename = file.getName();
				// Apply the extension filter, we do not want to zip any file
				// that does not meet the extension criteria
				if (!isFileExtnMatched(filename))
				{
					return;
				}

				zipOutStream = getZipOut();
				/*
				 * if(zipOutStream == null) { zipOutStream = createZipFile(); }
				 */

				in = new BufferedInputStream(new FileInputStream(file));
				File parentFile = file.getParentFile();
				if (parentFile != null && parentFile.exists())
				{
					String parentDirTemp = getParentDir();
					if (parentFile.isDirectory()
							&& (parentDirTemp != null && parentFile.getName()
									.compareTo(parentDirTemp) != 0))
					{
						File outputDir = new File(getOutFilePath()).getParentFile();
						File srcDir = new File(outputDir, getParentDir());
						
						zipOutStream.putNextEntry(new ZipEntry(getRelativePath(srcDir, parentFile)
								+ File.separator + filename));
					}
					else
					{
						zipOutStream.putNextEntry(new ZipEntry(filename));
					}
					while ((bufSize = in.read(buffer)) > 0)
					{
						zipOutStream.write(buffer, 0, bufSize);
					}
					zipOutStream.closeEntry();
				}
			}
		}
		else
		{
			String msg = "";
			if (file != null)
			{
				msg = "Invalid source " + file.getPath()
						+ " provided to create zip archive";
			}
			else
			{
				msg = "Invalid source provided to create zip archive";
			}
			Log.i(TAG, msg);
			throw new IOException(msg);
		}
	} // end method

	public static void addFilesToZip(File zipFile, List<String> files) throws FileNotFoundException,
	IOException
	{
		addFilesToZip(zipFile,zipFile.getParent(),files);
	}

	public static void addFilesToZip(File zipFile, String baseParentName,
			List<String> files) throws FileNotFoundException, IOException
	{

		ZipOutputStream out = null;
		try
		{
			zipFile.delete();

			Log.i(TAG, "Creating zip: " + zipFile);

			out = new ZipOutputStream(new FileOutputStream(zipFile));

			// Set the compression ratio
			out.setLevel(Deflater.BEST_COMPRESSION);

			// iterate through the array of files, adding each to the zip file
			for (String fileName : files)
			{
				
				File file = new File(fileName);
				// Associate a file input stream for the current file
				FileInputStream in = null;
				try
				{
					
					File parentFile = file.getParentFile();
					in = new FileInputStream(file);
					if (parentFile.isDirectory()
							&& (baseParentName != null && parentFile.getCanonicalPath()
									.compareTo(baseParentName) != 0))
					{
						// Add ZIP entry to output stream.
						Log.i(TAG, "Adding: " + file);
						String name = file.getCanonicalPath().substring(baseParentName.length()+1);
						out.putNextEntry(new ZipEntry(name));
					}else{
						out.putNextEntry(new ZipEntry(file.getName()));
					}
					

					IOUtils.copy(in, out);

					// Close the current entry
					out.closeEntry();
				}
				finally
				{
					IOUtils.closeQuietly(in);
				}
			}

		}
		finally
		{
			IOUtils.closeQuietly(out);
		}
	} // end method
	
	private void addFolderToZip(File folder) throws FileNotFoundException,
			IOException
	{
		if (folder.exists() && folder.isDirectory())
		{
			File[] fileList = folder.listFiles();
			for (File file : fileList)
			{
				addFileToZip(file);
			}
		}
	}

	/**
	 * Adds the specified folder contents to zip
	 * <p>
	 * This method call is similar to the call
	 * {@link #addFolderToZip(File, FileFilter, ZipOutputStream)} passing
	 * <code>filFilter</code> as <code>null</code>
	 * 
	 * @param folder
	 *            folder to zip
	 * @param zipOut
	 *            ZipOutputStream
	 * 
	 * @throws FileNotFoundException
	 *             thrown in case specified file is not found
	 * @throws IOException
	 *             thrown in case any prob occurs while I/O
	 * 
	 * @see #addFolderToZip(File, FileFilter, ZipOutputStream)
	 */
	/*
	 * private static void addFolderToZip(File folder) throws
	 * FileNotFoundException, IOException { addFolderToZip(folder, null); }
	 */
	/**
	 * Adds the specified folder contents (after applying specified file filter)
	 * to zip
	 * <p>
	 * If the passed in <code>fileFilter</code> is <code>null</code> then all
	 * the folder contents will be zipped
	 * 
	 * @param folder
	 *            folder to zip
	 * @param fileFilter
	 *            file filter
	 * @param zipOut
	 *            ZipOutputStream
	 * 
	 * @throws FileNotFoundException
	 *             thrown in case specified file is not found
	 * @throws IOException
	 *             thrown in case any prob occurs while I/O
	 * 
	 * @see #addFolderToZip(File, List<String>, ZipOutputStream)
	 */
	/*
	 * public static void addFolderToZip(File folder, FileFilter fileFilter,
	 * ZipOutputStream zipOut) throws FileNotFoundException, IOException {
	 * setZipOut(zipOut); if(folder.exists() && folder.isDirectory()) { File[]
	 * fileList; if ( fileFilter != null ) { fileList = folder.listFiles(
	 * fileFilter ); } else { fileList = folder.listFiles(); }
	 * 
	 * for(File file:fileList) { addFileToZip(file); } } } // end method
	 */
	/**
	 * @return the parentDir
	 */

	private String getParentDir()
	{
		return parentDir;
	}

	/**
	 * @param parentDirName
	 *            the parentDir to set
	 */

	private void setParentDir(String parentDirName)
	{
		if (parentDirName.endsWith("/"))
		{
			parentDir = parentDirName.substring(0, parentDirName.length() - 1);
		}
		else
		{
			parentDir = parentDirName;
		}
	}

	/**
	 * This functon checks if the filename's extn matches any of the exten in
	 * the extension list provided by the user
	 * 
	 * @param filenamefile
	 *            name to be compared
	 * @return boolean true if the filename's extn matches any of the exten in
	 *         the extension list provided by the user
	 */
	private boolean isFileExtnMatched(String filename)
	{
		List<String> extnList = getExtensionList();
		if (filename == null || filename.length() == 0 || extnList == null
				|| extnList.isEmpty())
			return true;

		String fileExt = (filename.lastIndexOf(".") == -1) ? "" : filename
				.substring(filename.lastIndexOf(".") + 1, filename.length());
		for (String extn : extnList)
		{
			if (extn.compareToIgnoreCase("*." + fileExt) == 0)
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * @return the extensionList
	 */
	private List<String> getExtensionList()
	{
		return extensionList;
	}

	/**
	 * @param extnList
	 *            the extensionList to set
	 */
	private void setExtensionList(List<String> extnList)
	{
		extensionList = extnList;
	}

	/**
	 * @return the zipOut
	 * @throws FileNotFoundException
	 */
	private ZipOutputStream getZipOut() throws FileNotFoundException
	{
		if (zipOut == null)
		{
			zipOut = new ZipOutputStream(new FileOutputStream(getOutFilePath()));
		}
		return zipOut;
	}

	/**
	 * @return the outFilePath
	 */
	private String getOutFilePath()
	{
		return outFilePath;
	}

	/**
	 * @param outFileName
	 *            the outFilePath to set
	 */
	private void setOutFilePath(String outFileName)
	{
		String filename = outFileName;
		// If the output file path specified doesn't have a .zip extension add
		// it :)
		final String ZIP_EXTN = ".zip";
		if (!filename.endsWith(ZIP_EXTN))
		{
			// String fileExt =
			// (filename.lastIndexOf(".")==-1)?"":filename.substring
			// (filename.lastIndexOf(".")+1,filename.length());
			String fileExt = getFileExtension(filename);
			if (fileExt.compareTo("") == 0)
			{
				filename += ZIP_EXTN;
			}
			else
			{
				String name = filename.substring(0, filename.lastIndexOf("."));
				filename = name + ZIP_EXTN;
			}
		}
		outFilePath = filename;
	}

	/**
	 * Get the file extension for the specified file name
	 * <p>
	 * If the file doesn't have any extension then an empty string is returned
	 * 
	 * @param aFileName
	 *            file name
	 * @return String file extension
	 */
	private String getFileExtension(String aFileName)
	{
		String fileExtn = "";

		String fileName = aFileName.trim();
		int index = fileName.lastIndexOf(".");
		if (index != -1)
		{
			fileExtn = fileName.substring(index + ".".length());
		}

		return fileExtn;
	}
	
	/**
	 * Get the relative path of the <tt>file</tt> from the parent directory.
	 * @param srcDir
	 * @param file
	 * @return the relative path
	 */
	public static String getRelativePath(File srcDir, File file)
	{
		if(!file.getAbsolutePath().contains(srcDir.getAbsolutePath()))
		{
			throw new IllegalArgumentException("File " + file.getAbsolutePath() + " is not located under source directory " + srcDir.getAbsolutePath());
		}
		
		String relativePath = file.getName();
		while(true)
		{
			File parent = file.getParentFile();
			if(parent.getAbsolutePath().equalsIgnoreCase(srcDir.getAbsolutePath()))
			{
				break;
			}
			relativePath = parent.getName() + File.separator + relativePath;
			file = parent;
		}
		return relativePath;
	}

	public static void main(String[] args) throws Exception
	{
		ZipUtil util = new ZipUtil();
		System.out.println(util.unzip("c:\\temp\\engine.zip", "c:\\temp\\unzip-test"));
	}
	
}
