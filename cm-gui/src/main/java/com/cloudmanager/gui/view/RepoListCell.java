package com.cloudmanager.gui.view;

import com.cloudmanager.core.model.FileRepo;
import com.cloudmanager.core.services.factories.ServiceFactoryLocator;
import com.cloudmanager.gui.util.ResourceManager;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableStringValue;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Represents a repository in the repository dropdown.
 */
public class RepoListCell extends ListCell<FileRepo> {

    private ObservableStringValue otherSelection;

    /**
     * Constructs the cell from an observable of the selection of the other dropdown.
     * This observable is used to gray out the repository used on the other side
     *
     * @param otherSelection The selection on the other dropdown
     */
    public RepoListCell(ObservableStringValue otherSelection) {
        this.otherSelection = otherSelection;
    }

    @Override
    public void updateItem(FileRepo item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setGraphic(null);

        } else {
            // Get the icon
            String iconName = ServiceFactoryLocator.find(item.getServiceName()).getIcon(); // TODO Cambiar esto
            Image icon = ResourceManager.loadImage(iconName);
            ImageView view = new ImageView(icon);

            // Set its size
            final int imgSize = 16;
            view.setFitWidth(imgSize);
            view.setFitHeight(imgSize);

            // Set the image
            setGraphic(view);

            setText(item.getName());

            // If the service is selected on the other panel, disable it here
            BooleanBinding serviceInUse = Bindings.equal(item.getId(), otherSelection);

            disableProperty().bind(serviceInUse);
            styleProperty().bind(Bindings.when(serviceInUse)
                    .then("-fx-opacity: 0.3")
                    .otherwise("-fx-opacity: 1"));
        }
    }
}
