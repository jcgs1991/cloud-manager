package com.cloudmanager.core.services;

import com.cloudmanager.core.model.ModelFile;
import com.cloudmanager.core.services.login.LoginProcedure;
import com.cloudmanager.core.transfers.FileTransfer;

import java.util.List;

public interface FileService {

    String APP_NAME = "CloudManager";

    // Account
    String getAccountId();

    String getAccountName();


    // Service Info
    String getServiceName();

    String getAccountOwner();

    String getIcon();


    // Service methods
    LoginProcedure startLoginProcedure();

    boolean authenticate();

    void logout();


    ModelFile getRootFile();

    List<ModelFile> getChildren(ModelFile parent);


    ModelFile getCurrentDir();

    void setCurrentDir(ModelFile file);


    FileTransfer sendFile(ModelFile file);

    boolean receiveFile(FileTransfer transfer);


    boolean createFolder(ModelFile parent, String name);

    boolean moveFile(ModelFile file, ModelFile targetFolder);

    boolean copyFile(ModelFile file, ModelFile targetFolder);

    boolean deleteFile(ModelFile file);
}
