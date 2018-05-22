/*
 * Copyright 2014 protoc-jar developers
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cocolian.maven.protoc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Protoc1
{
	private static final Logger log=Logger.getLogger(Protoc1.class);
	private static final String PROTOCJAR="protoc";
	private static final String BUILD="-build";

	
	public static int runProtoc(String[] args) throws IOException, InterruptedException {
		ProtocVersion protocVersion = ProtocVersion.PROTOC_VERSION;
		boolean includeStdTypes = false;
		for (String arg : args) {
			ProtocVersion v = getVersion(arg);
			if (v != null) protocVersion = v;
			if (arg.equals("--include_std_types")) includeStdTypes = true;
		}
		
		try {
			File protocTemp = extractProtoc(protocVersion, includeStdTypes, null);
			return runProtoc(protocTemp.getAbsolutePath(), Arrays.asList(args));
		}
		catch (FileNotFoundException e) {
			log.error(e.getMessage(),e);
			throw e;
		}
		catch (Exception e) {
			log.error(e.getMessage(),e);
			// some linuxes don't allow exec in /tmp, try user home
			String homeDir = System.getProperty("user.home");
			File protocTemp = extractProtoc(protocVersion, includeStdTypes, new File(homeDir));
			return runProtoc(protocTemp.getAbsolutePath(), Arrays.asList(args));
		}
	}

	public static int runProtoc(String cmd, String[] args) throws IOException, InterruptedException {
		return runProtoc(cmd, Arrays.asList(args));
	}

	public static int runProtoc(String cmd, List<String> argList) throws IOException, InterruptedException {
		ProtocVersion protocVersion = ProtocVersion.PROTOC_VERSION;
		String javaShadedOutDir = null;
		
		List<String> protocCmd = new ArrayList<>();
		protocCmd.add(cmd);
		for (String arg : argList) {
			if (arg.startsWith("--java_shaded_out=")) {
				javaShadedOutDir = arg.split("--java_shaded_out=")[1];
				protocCmd.add("--java_out=" + javaShadedOutDir);
			}
			else if (arg.equals("--include_std_types")) {
				File stdTypeDir = new File(new File(cmd).getParentFile().getParentFile(), "include");
				protocCmd.add("-I" + stdTypeDir.getAbsolutePath());
			}
			else {
				ProtocVersion v = getVersion(arg);
				if (v != null) protocVersion = v; else protocCmd.add(arg);				
			}
		}
		
		Process protoc = null;
		int numTries = 1;
		while (protoc == null) {
			try {
				log.debug("executing: " + protocCmd);
				ProcessBuilder pb = new ProcessBuilder(protocCmd);
				protoc = pb.start();
			}
			catch (IOException ioe) {
				if (numTries++ >= 3) throw ioe; // retry loop, workaround text file busy issue
				log.error("caught exception, retrying: "+ioe.getMessage() + ioe);
				Thread.sleep(1000);
			}
		}
		
		new Thread(new StreamCopier(protoc.getInputStream(), System.out)).start();
		new Thread(new StreamCopier(protoc.getErrorStream(), System.err)).start();
		int exitCode = protoc.waitFor();
		
//		if (javaShadedOutDir != null) {
//			log.debug("shading (version " + protocVersion + "): " + javaShadedOutDir);
//			doShading(new File(javaShadedOutDir), protocVersion.mVersion);
//		}
		
		return exitCode;
	}

//	public static void doShading(File dir, String version) throws IOException {
//		if (dir.listFiles() == null) return;
//		for (File file : dir.listFiles()) {
//			if (file.isDirectory()) {
//				doShading(file, version);
//			}
//			else if (file.getName().endsWith(".java")) {
//				File tmpFile = null;
//				tmpFile = File.createTempFile(file.getName(), null);
//				try (FileOutputStream os =new FileOutputStream(file);
//					FileInputStream is =new FileInputStream(tmpFile);
//					PrintWriter pw = new PrintWriter(tmpFile);
//					BufferedReader br = new BufferedReader(new FileReader(file));
//				){
//					version = version.replace(".", "");
//					String line;
//					while ((line = br.readLine()) != null) {
//						pw.println(line.replace("com.google.protobuf", "org.cocolian.maven.protobuf" + version));
//					}
//					// tmpFile.renameTo(file) only works on same filesystem, make copy instead:
//					boolean delete = file.delete();
//					log.debug("delete:"+delete);
//					streamCopy(is, os);
//				}
//			}
//		}
//	}

	public static File extractProtoc(ProtocVersion protocVersion, boolean includeStdTypes) throws IOException {
		return extractProtoc(protocVersion, includeStdTypes, null);
	}

	public static File extractProtoc(ProtocVersion protocVersion, boolean includeStdTypes, File dir) throws IOException {
		File protocTemp = extractProtoc(protocVersion, dir);
		if (includeStdTypes) extractStdTypes(protocVersion, protocTemp.getParentFile().getParentFile());
		return protocTemp;
	}

	public static File extractProtoc(ProtocVersion protocVersion, File dir) throws IOException {
		log.debug("protoc version: " + protocVersion + ", detected platform: " + getPlatformVerbose());
		
		File tmpDir = File.createTempFile(PROTOCJAR, "", dir);
		Files.delete(Paths.get(tmpDir.getAbsolutePath()));
		tmpDir.mkdirs();
		tmpDir.deleteOnExit();
		File binDir = new File(tmpDir, "bin");
		binDir.mkdirs();
		binDir.deleteOnExit();
		
		File exeFile = null;
		if (protocVersion.mArtifact == null) { // look for embedded protoc and on web (maven central)
			// look for embedded version
//			String srcFilePath = "bin/" + protocVersion.mVersion + SEPARATOR + getProtocExeName(protocVersion);
//			try {
//				File protocTemp = new File(binDir, "protoc.exe");
//				populateFile(srcFilePath, protocTemp);
//				log.debug("embedded: " + srcFilePath);
//				boolean setExecutable = protocTemp.setExecutable(true);
//				log.debug("setExecutable:"+setExecutable);
//				protocTemp.deleteOnExit();
//				return protocTemp;
//			}
//			catch (FileNotFoundException e) {
////				log.error(e.getMessage(),e);
//				log.info("FileNotFoundException:"+e.getMessage());
//			}
			
			// look in cache and maven central
			exeFile = findDownloadProtoc(protocVersion);
		}
		else { // download by artifact id from maven central
//			String downloadPath = protocVersion.mGroup.replace(".", SEPARATOR) + SEPARATOR + protocVersion.mArtifact + SEPARATOR;
//			exeFile = downloadProtoc(protocVersion, downloadPath, true);
			exeFile = downloadProtoc(protocVersion, true);
		}
		
		if (exeFile == null) throw new FileNotFoundException("Unsupported platform: " + getProtocExeName(protocVersion));
		
		File protocTemp = new File(binDir, "protoc.exe");
		populateFile(exeFile.getAbsolutePath(), protocTemp);
		boolean setExecutable = protocTemp.setExecutable(true);
		log.debug("setExecutable:"+setExecutable);
		protocTemp.deleteOnExit();
		return protocTemp;
	}



//	public static File downloadProtoc(ProtocVersion protocVersion, String downloadPath, boolean trueDownload) throws IOException {
//		if (protocVersion.mVersion.endsWith("-SNAPSHOT")) {
//			return downloadProtocSnapshot(protocVersion, downloadPath);
//		}
//		
//		String releaseUrlStr = "http://central.maven.org/maven2/";
//		File webcacheDir = getWebcacheDir();
//		
//		// download maven-metadata.xml (cache for 8hrs)
//		String mdSubPath = "maven-metadata.xml";
//		URL mdUrl = new URL(releaseUrlStr + downloadPath + mdSubPath);
//		File mdFile = downloadFile(mdUrl,  new File(webcacheDir, downloadPath + mdSubPath), 8L*3600*1000);
//		
//		// find last build (if any) from maven-metadata.xml
//		try {
//			String lastBuildVersion = parseLastReleaseBuild(mdFile, protocVersion);
//			if (lastBuildVersion != null) {
//				protocVersion = new ProtocVersion(protocVersion.mGroup, protocVersion.mArtifact, lastBuildVersion);
//			}
//		}
//		catch (IOException e) {
//			log.error(e.getMessage(),e);
//		}
//		
//		// download exe
//		String exeSubPath = protocVersion.mVersion + SEPARATOR + getProtocExeName(protocVersion);
//		URL exeUrl = new URL(releaseUrlStr + downloadPath + exeSubPath);
//		File exeFile = new File(webcacheDir, downloadPath + exeSubPath);
//		if (trueDownload) {
//			return downloadFile(exeUrl, exeFile, 0);
//		}
//		else if (exeFile.exists()) { // cache only
//			log.debug("cached: " + exeFile);
//			return exeFile;
//		}
//		return null;
//	}
	


//	public static File downloadProtocSnapshot(ProtocVersion protocVersion, String downloadPath) throws IOException {
//		String snapshotUrlStr = "https://oss.sonatype.org/content/repositories/snapshots/";
//		File webcacheDir = getWebcacheDir();
//		
//		// download maven-metadata.xml (cache for 8hrs)
//		String mdSubPath = protocVersion.mVersion + "/maven-metadata.xml";
//		URL mdUrl = new URL(snapshotUrlStr + downloadPath + mdSubPath);
//		File mdFile = downloadFile(mdUrl, new File(webcacheDir, downloadPath + mdSubPath), 8L*3600*1000);
//		
//		// parse exe name from maven-metadata.xml
//		String exeName = parseSnapshotExeName(mdFile);
//		if (exeName == null) return null;
//		
//		// download exe
//		String exeSubPath = protocVersion.mVersion + SEPARATOR + exeName;
//		URL exeUrl = new URL(snapshotUrlStr + downloadPath + exeSubPath);
//		File exeFile = new File(webcacheDir, downloadPath + exeSubPath);
//		return downloadFile(exeUrl, exeFile, 0);
//	}

//	public static File downloadFile(URL srcUrl, File destFile, long cacheTime) throws IOException {
//		if (destFile.exists() && ((cacheTime <= 0) || (System.currentTimeMillis() - destFile.lastModified() <= cacheTime))) {
//			log.debug("cached: " + destFile);
//			return destFile;
//		}
//		
//		File tmpFile = File.createTempFile(PROTOCJAR, ".tmp");
//	
//		URLConnection con = srcUrl.openConnection();
//		con.setRequestProperty("User-Agent", "Mozilla"); // sonatype only returns proper maven-metadata.xml if this is set
//		try (
//				FileOutputStream os =new FileOutputStream(tmpFile);
//				InputStream is = con.getInputStream();
//		){
//			log.debug("downloading: " + srcUrl);
//			streamCopy(is, os);
//			destFile.getParentFile().mkdirs();
////			判断exe文件是否存在
//			if(destFile.exists()){
//				Files.delete(Paths.get(destFile.getAbsolutePath()));
//			}
//			FileUtils.copyFile(tmpFile, destFile);
//			log.debug("tmpFile:"+tmpFile.exists());
//			log.debug("destFile:"+destFile.exists());
//			boolean setLastModified = destFile.setLastModified(System.currentTimeMillis());
//			log.debug("setLastModified:"+setLastModified);
//		}
//		catch (IOException e) {
//			log.error(e.getMessage(),e);
//			Files.delete(Paths.get(tmpFile.getAbsolutePath()));
//			if (!destFile.exists()) throw e; // if download failed but had cached version, ignore exception
//		}
//		log.debug("saved: " + destFile);
//		return destFile;
//	}

	public static File extractStdTypes(ProtocVersion protocVersion, File tmpDir) throws IOException {
		if (tmpDir == null) {
			tmpDir = File.createTempFile(PROTOCJAR, "");
			Files.delete(Paths.get(tmpDir.getAbsolutePath()));
			tmpDir.mkdirs();
			tmpDir.deleteOnExit();
		}
		
		File tmpDirProtos = new File(tmpDir, "include/google/protobuf");
		tmpDirProtos.mkdirs();
		tmpDirProtos.getParentFile().getParentFile().deleteOnExit();
		tmpDirProtos.getParentFile().deleteOnExit();
		tmpDirProtos.deleteOnExit();
		
		final String majorProtoVersion = String.valueOf(protocVersion.mVersion.charAt(0));
		final String srcPathPrefix = String.format("proto%s/", majorProtoVersion);
		final String[] stdTypes = sStdTypesMap.get(majorProtoVersion);
		for (String srcFilePath : stdTypes) {
			File tmpFile = new File(tmpDir, srcFilePath);
			populateFile(srcPathPrefix + srcFilePath, tmpFile);
			tmpFile.deleteOnExit();
		}
		
		return tmpDir;
	}

	public static File populateFile(String srcFilePath, File destFile) throws IOException {
		String resourcePath = "/" + srcFilePath; // resourcePath for jar, srcFilePath for test
		
		try (
				FileOutputStream os = new FileOutputStream(destFile);
				InputStream is = Protoc1.class.getResourceAsStream(resourcePath)==null?new FileInputStream(srcFilePath):Protoc1.class.getResourceAsStream(resourcePath);
		){
		  ProtocUtils.streamCopy(is, os);
		}
		return destFile;
	}



	static String parseLastReleaseBuild(File mdFile, ProtocVersion protocVersion) throws IOException {
		// find last build (if any) from maven-metadata.xml
		int lastBuild = 0;
		try {
			DocumentBuilder xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document xmlDoc = xmlBuilder.parse(mdFile);
			NodeList versions = xmlDoc.getElementsByTagName("version");
			for (int i = 0; i < versions.getLength(); i++) {
				Node ver = versions.item(i);
				String verStr = ver.getTextContent();
				if (verStr.startsWith(protocVersion.mVersion+BUILD)) {
					String buildStr = verStr.substring(verStr.indexOf(BUILD)+BUILD.length());
					int build = Integer.parseInt(buildStr);
					if (build > lastBuild) lastBuild = build;
				}
			}
		}
		catch (Exception e) {
			log.error(e.getMessage(),e);
			throw new IOException(e);
		}
		if (lastBuild > 0) return protocVersion.mVersion+BUILD+lastBuild;
		return null;
	}

//	static String parseSnapshotExeName(File mdFile) throws IOException {
//		// parse exe name from maven-metadata.xml
//		String exeName = null;
//		try {
//			String clsStr = getPlatformClassifier();
//			DocumentBuilder xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//			Document xmlDoc = xmlBuilder.parse(mdFile);
//			NodeList versions = xmlDoc.getElementsByTagName("snapshotVersion");
//			for (int i = 0; i < versions.getLength(); i++) {
//				Node ver = versions.item(i);
//				Node cls = null;
//				Node val = null;
//				for (int j = 0; j < ver.getChildNodes().getLength(); j++) {
//					Node n = ver.getChildNodes().item(j);
//					if (n.getNodeName().equals("classifier")) cls = n;
//					if (n.getNodeName().equals("value")) val = n;
//				}
//				if (cls != null && val != null && cls.getTextContent().equals(clsStr))	{
//					exeName = "protoc-" + val.getTextContent() + "-" + clsStr + ".exe";
//					break;
//				}
//			}
//		}
//		catch (Exception e) {
//			log.error(e.getMessage(),e);
//			throw new IOException(e);
//		}
//		return exeName;
//	}





	static String getPlatformVerbose() {
		return getPlatformClassifier() + " (" + System.getProperty("os.name").toLowerCase() + "/" + System.getProperty("os.arch").toLowerCase() + ")";
	}
	public static String getPlatformClassifier() {
	    return new BasicPlatformDetector().getClassfier();
	}

	static ProtocVersion getVersion(String spec) {
		return ProtocVersion.getVersion(spec);
	}

	static class StreamCopier implements Runnable
	{
		public StreamCopier(InputStream in, OutputStream out) {
			mIn = in;
			mOut = out;
		}

		public void run() {
			try {
			  ProtocUtils.streamCopy(mIn, mOut);
			}
			catch (IOException e) {
				log.error(e.getMessage(),e);
			}
		}

		private InputStream mIn;
		private OutputStream mOut;
	}

	static String[] sDdownloadPaths = {
		"com/google/protobuf/protoc/",
		"org/cocolian/maven/protoc",
	};

	static String[] sStdTypesProto2 = {
		"include/google/protobuf/descriptor.proto",
	};
	static String[] sStdTypesProto3 = {
		"include/google/protobuf/any.proto",
		"include/google/protobuf/api.proto",
		"include/google/protobuf/descriptor.proto",
		"include/google/protobuf/duration.proto",
		"include/google/protobuf/empty.proto",
		"include/google/protobuf/field_mask.proto",
		"include/google/protobuf/source_context.proto",
		"include/google/protobuf/struct.proto",
		"include/google/protobuf/timestamp.proto",
		"include/google/protobuf/type.proto",
		"include/google/protobuf/wrappers.proto",
	};

	static Map<String,String[]> sStdTypesMap = new HashMap<>();
	static {
		sStdTypesMap.put("2", sStdTypesProto2);
		sStdTypesMap.put("3", sStdTypesProto3);
	}
}
