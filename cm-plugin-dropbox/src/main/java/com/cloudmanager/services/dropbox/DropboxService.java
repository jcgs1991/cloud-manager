package com.cloudmanager.services.dropbox;

import com.cloudmanager.core.config.ConfigManager;
import com.cloudmanager.core.model.ModelFile;
import com.cloudmanager.core.services.AbstractFileService;
import com.cloudmanager.core.services.login.LoginProcedure;
import com.cloudmanager.core.transfers.FileTransfer;
import com.cloudmanager.core.util.Util;
import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class DropboxService extends AbstractFileService {
    static {
        // Load the API keys
        Map<String, String> properties = Util.getPropertiesMap(DropboxService.class, "dropbox.apikey");

        final String key = properties.get("key");
        final String secret = properties.get("secret");

        // Check if they are present
        if (key.startsWith("@@") || secret.startsWith("@@")) {
            System.err.println("Dropbox API Keys not set!");
            System.exit(-7);
        }

        // Set the API keys
        appInfo = new DbxAppInfo(key, secret);
    }

    /* Service Name */
    static final String SERVICE_NAME = "dropbox";

    /* Setting up request config and api key */
    static final DbxRequestConfig requestConfig = new DbxRequestConfig(
            APP_NAME, ConfigManager.getConfig().getLocale().toString());

    static final DbxAppInfo appInfo;


    /*-----------------------*/
    /* Dropbox service class */
    /*-----------------------*/
    private DbxClientV2 client;

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public String getIcon() {return "/branding/dropbox-icon.png";}

    @Override
    public LoginProcedure startLoginProcedure() {
        return new DropboxLoginProcedure(this);
    }

    @Override
    public boolean authenticate() {
        String accessToken = getAccount().getAuth().get("access_token");

        if (accessToken == null)
            return false;

        client = new DbxClientV2(requestConfig, accessToken);
        return true;
    }

    @Override
    public String getAccountOwner() {
        try {
            // Get current account info
            FullAccount account = client.users().getCurrentAccount();
            return account.getName().getDisplayName();

        } catch (DbxException e) {
            System.out.println("Error getting account name.");
            return null;
        }
    }

    @Override
    public boolean receiveFile(FileTransfer transfer) {
        String targetPath = getCurrentDir().getPath();
        String targetName = transfer.getTargetFileName();
        InputStream stream = transfer.getContentStream();

        try {
            FileMetadata metadata = client.files().uploadBuilder(targetPath + "/" + targetName)
                    .withAutorename(true)
                    .uploadAndFinish(stream);

            stream.close();
            return true;
        } catch (DbxException | IOException e) {
            System.err.println("Error uploading file: " + e.getMessage());
            return false;
        }
    }

    @Override
    public FileTransfer sendFile(ModelFile file) {
        try {
            InputStream stream = client.files().download(file.getPath()).getInputStream();
            return new FileTransfer(stream, file);
        } catch (DbxException e) {
            System.err.println("Error downloading file: " + e.getMessage());
            return null;
        }
    }

    @Override
    public ModelFile getRootFile() {
        return new ModelFile("", "/", "", ModelFile.Type.FOLDER, 0L, null, null);
    }

    @Override
    public List<ModelFile> getChildren(ModelFile parent) {
        if (!parent.isFolder())
            return null;

        try {
            // We get all the children
            List<Metadata> files = new ArrayList<>();

            ListFolderResult result = client.files().listFolder(parent.getPath());

            files.addAll(result.getEntries());

            while (result.getHasMore()) {
                result = client.files().listFolderContinue(result.getCursor());
                files.addAll(result.getEntries());
            }

            // We convert them to our model and return them
            return files
                    .stream()
                    .map(f -> toFile(f, parent))
                    .sorted()
                    .collect(Collectors.toList());

        } catch (DbxException e) {
            System.err.println("Error listing files: " + e.getMessage());
            return null;
        }
    }

    private ModelFile toFile(Metadata dbxFile, ModelFile parent) {
        ModelFile.Type type = ModelFile.Type.FOLDER;
        long size = 0;
        Date modified = null;

        if (dbxFile instanceof FileMetadata) {
            FileMetadata _dbxFile = (FileMetadata) dbxFile;

            type = ModelFile.Type.FILE;
            size = _dbxFile.getSize();
            modified = _dbxFile.getServerModified();
        }

        return new ModelFile(dbxFile.getPathLower(), dbxFile.getName(), dbxFile.getPathLower(), type, size, modified, parent);
    }

    @Override
    public boolean createFolder(ModelFile parent, String name) {
        try {
            client.files().createFolder(parent.getPath() + "/" + name);
            return true;
        } catch (DbxException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean moveFile(ModelFile file, ModelFile targetFolder) {
        try {
            client.files().move(file.getPath(), targetFolder.getPath() + "/" + file.getName());
            return true;
        } catch (DbxException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean copyFile(ModelFile file, ModelFile targetFolder) {
        try {
            client.files().copy(file.getPath(), targetFolder.getPath() + "/" + file.getName());
            return true;
        } catch (DbxException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteFile(ModelFile file) {
        try {
            client.files().delete(file.getPath());
            return true;
        } catch (DbxException e) {
            e.printStackTrace();
            return false;
        }
    }
}
