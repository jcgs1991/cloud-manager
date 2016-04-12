package com.cloudmanager.core.services;


import com.cloudmanager.core.model.ModelFile;
import com.cloudmanager.core.model.ServiceAccount;

public abstract class AbstractFileService implements FileService {

    private ServiceAccount account;
    private ModelFile currentDir;

    // Mostly unused, override if needed
    @Override
    public void logout() { }

    @Override
    public String getAccountId() {
        return getAccount().getId();
    }

    protected ServiceAccount getAccount() {
        return account;
    }

    public FileService setAccount(ServiceAccount account) {
        if (this.account != null)
            throw new IllegalStateException("Account already set");

        this.account = account;
        return this;
    }

    @Override
    public String getAccountName() {
        return getAccount().getName();
    }

    @Override
    public ModelFile getCurrentDir() {
        if (currentDir == null)
            currentDir = getRootFile();

        return currentDir;
    }

    @Override
    public void setCurrentDir(ModelFile file) {
        currentDir = file;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractFileService that = (AbstractFileService) o;

        return getAccountId() != null ? getAccountId().equals(that.getAccountId()) : that.getAccountId() == null;

    }

    @Override
    public int hashCode() {
        return getAccountId() != null ? getAccountId().hashCode() : 0;
    }
}
