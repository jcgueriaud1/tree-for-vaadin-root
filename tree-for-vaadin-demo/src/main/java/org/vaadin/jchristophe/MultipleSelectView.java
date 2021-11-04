package org.vaadin.jchristophe;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;
import elemental.json.Json;
import elemental.json.JsonObject;
import org.vaadin.jchristophe.bean.DummyFile;
import org.vaadin.jchristophe.service.DummyFileService;

import java.util.stream.Stream;

@Route(value = "multiple", layout = MainLayout.class)
public class MultipleSelectView extends Div {
    TreeData<DummyFile> treeData = new TreeData<>();

    private DummyFileService dummyFileService = new DummyFileService(2, 5);

    public MultipleSelectView() {
        ValueProvider<DummyFile, Integer> itemIdProvider = DummyFile::getId;

        ValueProvider<DummyFile, JsonObject> itemValueProvider = person -> {
            JsonObject object = Json.createObject();
            object.put("name", person.getFilename());
            return object;
        };
        Tree<DummyFile> tree = new Tree<>(itemIdProvider, itemValueProvider);
        tree.setTitle("test");
        tree.setMultiselect(true);
        treeData.addItems(dummyFileService.fetchRoot(), p -> dummyFileService.fetchAllChildren(p));
        tree.setTreeData(treeData);
        add(tree);
        ComboBox<DummyFile> fileComboBox = new ComboBox<>();
        fileComboBox.setItems(treeData.getRootItems().stream().flatMap(this::flatten));
        Button selectAll = new Button("Select All",
                e -> tree.selectAll()
        );
        Button deselectAll = new Button("Deselect All",
                e -> tree.deselectAll());
        Button selectItem = new Button("Select Item",
                e -> {
                    if (fileComboBox.getValue() != null) {
                        tree.selectItem(fileComboBox.getValue());
                    }
                }
        );
        Button deselectItem = new Button("Deselect Item",
                e -> {
                    if (fileComboBox.getValue() != null) {
                        tree.deselectItem(fileComboBox.getValue());
                    }});
        add(new HorizontalLayout(selectAll, deselectAll, fileComboBox, selectItem, deselectItem));
        add(tree);
        tree.addSelectionListener(e -> {
            if (e.isFromClient()) {
                Notification.show("Client - Items added "+ e.getAddedSelection());
                Notification.show("Client - Items removed "+ e.getRemovedSelection());
            } else {
                Notification.show("Server - Items added "+ e.getAddedSelection());
                Notification.show("Server - Items removed "+ e.getRemovedSelection());
            }
        });
    }

    private Stream<DummyFile> flatten(DummyFile element) {
        return Stream.concat(Stream.of(element), treeData
                .getChildren(element).stream().flatMap(this::flatten));
    }

}
