package org.vaadin.jchristophe;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;
import elemental.json.Json;
import elemental.json.JsonObject;
import org.vaadin.jchristophe.bean.DummyFile;
import org.vaadin.jchristophe.service.DummyFileService;

import java.util.stream.Stream;

@Route(value = "file", layout = MainLayout.class)
public class FileExplorerView extends Div {

    private DummyFileService dummyFileService = new DummyFileService();
    private TreeData<DummyFile> treeData = new TreeData<>();

    public FileExplorerView() {
        ValueProvider<DummyFile, Integer> itemIdProvider = DummyFile::getId;

        ValueProvider<DummyFile, JsonObject> itemValueProvider = dummyFile -> {
            JsonObject object = Json.createObject();
            object.put("name", dummyFile.getFilename());
            return object;
        };
        Tree<DummyFile> tree = new Tree<>(itemIdProvider, itemValueProvider);
        tree.setTitle("test");
        treeData.addItems(dummyFileService.fetchRoot(), p -> dummyFileService.fetchAllChildren(p));
        tree.setTreeData(treeData);
        ComboBox<DummyFile> fileComboBox = new ComboBox<>();
        fileComboBox.setItems(treeData.getRootItems().stream().flatMap(this::flatten));
        Button expandAll = new Button("Expand All",
            e -> tree.expandAll()
        );
        Button collapseAll = new Button("Collapse All",
            e -> tree.collapseAll());
        Button expandItem = new Button("Expand Item",
                e -> {
                    if (fileComboBox.getValue() != null) {
                        tree.expandItem(fileComboBox.getValue());
                    }
        }
        );
        Button collapseItem = new Button("Collapse Item",
                e -> {
                    if (fileComboBox.getValue() != null) {
                        tree.collapseItem(fileComboBox.getValue());
                    }});
        add(new HorizontalLayout(expandAll, collapseAll, fileComboBox, expandItem, collapseItem));
        add(tree);
    }
    private Stream<DummyFile> flatten(DummyFile element) {
        return Stream.concat(Stream.of(element), treeData
                .getChildren(element).stream().flatMap(this::flatten));
    }
}
