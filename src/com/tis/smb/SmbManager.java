package com.tis.smb;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

//import org.slf4j.LoggerFactory;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.security.bc.BCSecurityProvider;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;

// examples
// https://www.programcreek.com/java-api-examples/?api=com.hierynomus.smbj.share.File

/**
 * SmbManager
 */
public class SmbManager {
  
  private String shareHost;
  private String shareDomain;
  private String shareUser;
  private String sharePassword;
  private String remotePath;
  private String localPath;
  private String fileName;
  private String action;
  
  public SmbManager(String shareHost, String shareDomain, String shareUser, String sharePassword, String remotePath,
      String localPath, String fileName, String action) {
    this.shareHost = shareHost;
    this.shareDomain = shareDomain;
    this.shareUser = shareUser;
    this.sharePassword = sharePassword;
    this.remotePath = remotePath;
    this.localPath = localPath;
    this.fileName = fileName;
    this.action = action;
    
//    if (action.equals("testConnection")) {
//      System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE");
//      System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_DATE_TIME_KEY, "TRUE");
//      System.setProperty(org.slf4j.impl.SimpleLogger.DATE_TIME_FORMAT_KEY, "<yyyy-MM-dd HH:mm:ss:SSS>");
//      final org.slf4j.Logger log = LoggerFactory.getLogger(App.class);
//    }
  }



  public void handleAction() {
    
    // Set the timeout (optional)
    // added withSecurityProvider(new BCSecurityProvider()) for solving error https://github.com/hierynomus/smbj/issues/617
    SmbConfig config = SmbConfig.builder()
        .withTimeout(120, TimeUnit.SECONDS)
        .withTimeout(120, TimeUnit.SECONDS)
        .withSoTimeout(180, TimeUnit.SECONDS)
        .withSecurityProvider(new BCSecurityProvider())
        .build();

    // If you do not set the timeout period SMBClient client = new SMBClient();
    SMBClient client = new SMBClient(config);

    try {
      Connection connection = client.connect(shareHost);
      
      AuthenticationContext ac = new AuthenticationContext(shareUser, sharePassword.toCharArray(), shareDomain);
      
      Session session = connection.authenticate(ac);

      // Connect to a shared folder
      DiskShare share = (DiskShare) session.connectShare(remotePath);

      if ( action.equals("listDirectory")) {
        listFiles(share, "");
      } 
      
      else if ( action.equals("putFile")) {
        putFile(share, fileName, localPath, "");
      } 
      
      else if ( action.equals("getFile")) {
        getFile(share, remotePath, fileName, localPath);
      } 
      
      else if ( action.equals("removeFile")) {
        removeFile(share, fileName);
      }
      
      else if ( action.equals("testConnection")) {
        System.out.println("Test connection...");
      }
      
      session.close();
      
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (client != null) {
        //client.close();
      }
    }
  }

  
  
  /**
   * listFiles
   */
  private void listFiles(DiskShare share, String remotePath) {
    for (FileIdBothDirectoryInformation f : share.list(remotePath, "*")) {
      System.out.println(f.getFileName());
    }
  }
  
  /**
   * wirteFile
   */
  public static void wirteFile(DiskShare share, String fileName, String fileContents) {
    
    try {
      
      Set<AccessMask> accessMask = new HashSet<AccessMask>(EnumSet.of(AccessMask.FILE_ADD_FILE));
      
      Set<SMB2CreateOptions> createOptions = new HashSet<SMB2CreateOptions>(
              EnumSet.of(SMB2CreateOptions.FILE_NON_DIRECTORY_FILE, SMB2CreateOptions.FILE_WRITE_THROUGH));
      
      final File file = share.openFile(
          fileName, 
          accessMask, 
          null, 
          SMB2ShareAccess.ALL,
          SMB2CreateDisposition.FILE_OVERWRITE_IF, createOptions
      );
      
      OutputStream oStream = file.getOutputStream();

      oStream.write(fileContents.getBytes());
      oStream.flush();
      oStream.close();
      
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
  }
  
  
  /**
   * putFile
   * 
   */
  public static void putFile(DiskShare share, String fileName, String localPath, String remotePath) {
    
    OutputStream outputStream = null;
    InputStream inputStream = null;
    
    try {
     
     Set<AccessMask> accessMask = new HashSet<AccessMask>(EnumSet.of(AccessMask.FILE_ADD_FILE));
     
     Set<SMB2CreateOptions> createOptions = new HashSet<SMB2CreateOptions>(
             EnumSet.of(SMB2CreateOptions.FILE_NON_DIRECTORY_FILE, SMB2CreateOptions.FILE_WRITE_THROUGH));
     
     final File file = share.openFile(
         fileName, 
         accessMask, 
         null, 
         SMB2ShareAccess.ALL,
         SMB2CreateDisposition.FILE_OVERWRITE_IF, createOptions
     );
     
     outputStream = file.getOutputStream();
     
     java.io.File localFile = new java.io.File(localPath + "/" + fileName);
     
     inputStream = new BufferedInputStream(new FileInputStream(localFile));
     
     byte[] buffer = new byte[1024];
     
     int bytesRead;
     
     while((bytesRead = inputStream.read(buffer, 0, buffer.length)) != -1) {
       outputStream.write(buffer, 0, bytesRead);
     }
     
   } catch (IOException e) {
     // TODO Auto-generated catch block
     e.printStackTrace();
   } finally {

     try {
       outputStream.flush();
     } catch (IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }

     try {
       outputStream.close();
     } catch (IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
     
     try {
       inputStream.close();
     } catch (IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
   }
   
 }
  

 void getFile(DiskShare share, String remotePath, String fileName, String localPath) {
   try {

     // File smbFileRead = share.openFile(filePath,
     // EnumSet.of(AccessMask.GENERIC_READ), null, SMB2ShareAccess.ALL,
     // SMB2CreateDisposition.FILE_OPEN, null);
     File smbFileRead = share.openFile(fileName, EnumSet.of(AccessMask.GENERIC_READ), null, SMB2ShareAccess.ALL,
         SMB2CreateDisposition.FILE_OPEN, null);

     FileOutputStream fos;

     fos = new FileOutputStream(localPath + "/" + fileName);

     BufferedOutputStream bos = new BufferedOutputStream(fos);

     InputStream in = smbFileRead.getInputStream();

     byte[] buffer = new byte[4096];
     int len = 0;
     while ((len = in.read(buffer, 0, buffer.length)) != -1) {
       bos.write(buffer, 0, len);
     }

     bos.flush();
     bos.close();

   } catch (FileNotFoundException e) {
     // TODO Auto-generated catch block
     e.printStackTrace();
   } catch (IOException e) {
     // TODO Auto-generated catch block
     e.printStackTrace();
   }
 }
  
  void removeFile(DiskShare share, String fileName) {
    share.rm(fileName);
  }
  
}
