package com.cloudmanager.gui.view;

import com.cloudmanager.core.api.service.FileService;
import com.cloudmanager.core.model.ModelFile;
import com.cloudmanager.gui.util.ResourceManager;
import javafx.beans.binding.Bindings;
import javafx.scene.control.*;

/**
 * Represents a cell in the file table view.
 */
public class DraggableTableRow extends TableRow<ModelFile> {

    /**
     * Construct a cell from the table and the service
     *
     * @param table   The parent table
     * @param service The file service
     */
    public DraggableTableRow(TableView<ModelFile> table, FileService service) {
        DraggableFileHandler.getInstance().setOnDragEvents(this, service.getInstanceId(), this::getItem);

        // Create the context menu
        createContextMenu(service);
    }

    private void createContextMenu(FileService service) {
        MenuItem removeMenuItem = new MenuItem(ResourceManager.getString("remove"));
        removeMenuItem.setOnAction(e -> {
            service.deleteFile(getItem());
            getItem().refreshFile();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText(ResourceManager.getString("file_removed", getItem().getName()));
            alert.show();
        });

        ContextMenu contextMenu = new ContextMenu(removeMenuItem);

        // Set context menu on row, but use a binding to make it only show for non-empty rows:
        contextMenuProperty().bind(
                Bindings.when(emptyProperty())
                        .then((ContextMenu) null)
                        .otherwise(contextMenu)
        );
    }
}
