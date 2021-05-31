package com.tis.smb;

import java.util.concurrent.Callable;

import org.slf4j.LoggerFactory;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command( 
  name = "SmbApp", 
  mixinStandardHelpOptions = true, 
  version = "smbapp 0.1", 
  description = "Communication with windows share using SMB3."
)
public class SmbApp implements Callable<Integer> {

  @Option(names = { "-u", "--username"}, description = "Windows user to connect to share.")
  private String username;
  
  @Option(names = { "-p", "--password"}, description = "User password.")
  private String password;
  
  @Option(names = { "-m", "--domain"}, description = "Domain.")
  private String domain;
  
  @Option(names = { "-s", "--shareHost"}, description = "Windows share host name or Ip address.")
  private String host;
  
  @Option(names = { "-d", "--remotePath"}, description = "Remote path.")
  private String remotePath;
  
  @Option(names = { "-i", "--localPath"}, description = "Local path.")
  private String localPath;
  
  @Option(names = { "-f", "--fileName"}, description = "File name.")
  private String fileName;
  
  // actions
  
  @Option(names = { "-l", "--listDirectory" }, description = "List directory.")
  boolean listDirectoryOption;
  
  @Option(names = { "-g", "--getFile" }, description = "Get file.")
  boolean getFileOption;
  
  @Option(names = { "-b", "--putFile" }, description = "Put file.")
  boolean putFileOption;
  
  @Option(names = { "-r", "--removeFile" }, description = "Remove file.")
  boolean removeFileOption;
  
  @Option(names = { "-t", "--testConnection" }, description = "Test connection.")
  boolean testConnectionOption;
  
  
  public static void main(String[] args) {
    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "ERROR");
    System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_DATE_TIME_KEY, "TRUE");
    System.setProperty(org.slf4j.impl.SimpleLogger.DATE_TIME_FORMAT_KEY, "<yyyy-MM-dd HH:mm:ss:SSS>");
    final org.slf4j.Logger log = LoggerFactory.getLogger(SmbApp.class);
    int exitCode = new CommandLine(new SmbApp()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public Integer call() throws Exception {
    String action = "";

    if (listDirectoryOption)
      action = "listDirectory";
    
    else if (getFileOption)
      action = "getFile";
    
    else if (putFileOption)
      action = "putFile";
    
    else if (removeFileOption)
      action = "removeFile";

    else if (testConnectionOption)
      action = "testConnection";
    
    SmbManager smbManager = new SmbManager(host, domain, username, password, remotePath, localPath, fileName, action);
    smbManager.handleAction();
    return 0;
  }

}
