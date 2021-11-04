package org.vaadin.jchristophe;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.shared.Registration;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author jcgueriaud
 */
@Tag("xof-tree")
/*@NpmPackage(value = "xof-tree", version = "0.1.4")
@JsModule("xof-tree/dist/index.js")*/
@JsModule("./src/index.ts")
public class Tree<T> extends Component {

    private TreeData<T> treeData;
    private final ValueProvider<T,Integer> itemIdProvider;
    private final ValueProvider<T, JsonObject> itemValueProvider;

    private Set<T> selectedItems = new HashSet<>();

    public Tree(ValueProvider<T, Integer> itemIdProvider, ValueProvider<T, JsonObject> itemValueProvider) {
        this.itemIdProvider = itemIdProvider;
        this.itemValueProvider = itemValueProvider;
    }

    public void setTitle(String title) {
        getElement().setAttribute("title", title);
    }

    public void setMultiselect(Boolean multiselect) {
        getElement().setAttribute("multiselect", multiselect);
    }

    public void setTreeData(TreeData<T> treeData) {
        this.treeData = treeData;
        List<T> rootItems = treeData.getRootItems();
        JsonArray data = Json.createArray();
        for (int i = 0; i <rootItems.size(); i++) {
            data.set(i, convertItem(rootItems.get(i)));
        }
        getElement().setPropertyJson("data", data);
    }

    private JsonObject convertItem(T item) {
        JsonObject object = Json.createObject();
        JsonObject itemData = itemValueProvider.apply(item);
        itemData.put("id", itemIdProvider.apply(item));
        object.put("itemdata", itemData);
        List<T> children = treeData.getChildren(item);
        JsonArray array = Json.createArray();
        for (int i = 0; i <children.size(); i++) {
            array.set(i, convertItem(children.get(i)));
        }
        object.put("children", array);
        return object;
    }

    public void expandAll() {
        getElement().executeJs("$0.expandAll()", getElement());
    }

    public void collapseAll() {
        getElement().executeJs("$0.collapseAll()", getElement());
    }

    /**
     * Collapse items
     *
     * @param item
     */
    public void collapseItem(T item) {
        /*JsonArray array = Json.createArray();
        for (int i = 0; i <items.length; i++) {
            array.set(i, itemIdProvider.apply(items[i]));
        }
        getElement().executeJs("$0.collapseId( item => $1.includes(item.id))", getElement(), array);
        */
        getElement().executeJs("$0.collapseId($1)", getElement(), itemIdProvider.apply(item));
    }

    /**
     * Expand the item and its parents
     *
     * @param item item to expand
     */
    public void expandItem(T item){
        getElement().executeJs("$0.expandId($1)", getElement(), itemIdProvider.apply(item));
    }

    private Stream<T> getAncestorsAndItem(T item) {
        T parent = treeData.getParent(item);
        if (parent == null) {
            return Stream.of(item);
        } else {
            return Stream.concat(Stream.of(item), getAncestorsAndItem(parent));
        }
    }

    protected Stream<T> flatten(T element) {
        return Stream.concat(Stream.of(element), treeData
                .getChildren(element).stream().flatMap(this::flatten));
    }

    public void selectAll() {
        getElement().executeJs("$0.selectAll()", getElement());
        selectedItems.addAll(treeData.getRootItems().stream().flatMap(this::flatten).collect(Collectors.toSet()));
    }

    public void deselectAll() {
        getElement().executeJs("$0.deselectAll()", getElement());
        selectedItems.clear();
    }

    public TreeData<T> getTreeData() {
        return treeData;
    }

    /**
     * select the items
     *
     * @param items items to select
     */
    @SafeVarargs
    public final void selectItem(T... items){
        Objects.requireNonNull(items);
        JsonArray array = Json.createArray();
        for (int i = 0; i <items.length; i++) {
            array.set(i, itemIdProvider.apply(items[i]));
        }
        getElement().executeJs("$0.select( item => $1.includes(item.id))", getElement(), array);
        selectedItems.addAll(Arrays.asList(items));
    }


    /**
     * Deselect the items
     *
     * @param items items to deselect
     */
    @SafeVarargs
    public final void deselectItem(T... items){
        Objects.requireNonNull(items);
        JsonArray array = Json.createArray();
        for (int i = 0; i <items.length; i++) {
            array.set(i, itemIdProvider.apply(items[i]));
        }
        getElement().executeJs("$0.deselect( item => $1.includes(item.id))", getElement(), array);
        Arrays.asList(items).forEach(selectedItems::remove);
    }

    public Set<T> getSelectedItems() {
        return selectedItems;
    }

    public Registration addSelectionListener(ComponentEventListener<SelectionEvent<T>> listener) {
        return addListener(SelectionEvent.class, (ComponentEventListener) listener);
    }

    @DomEvent("item-selected")
    public static class SelectionEvent<T>  extends ComponentEvent<Tree<T>> {

        private final Set<T> value;
        private final Set<T> oldValue;

        public SelectionEvent(Tree<T> source, boolean fromClient,
                              @EventData("event.detail.old") JsonArray _oldItemsSelected,
                              @EventData("event.detail.new") JsonArray _newItemsSelected
                              ) {
            super(source, fromClient);

            Map<Integer, T> items = source.getTreeData().getRootItems().stream().flatMap(source::flatten).collect(Collectors.toMap(source.itemIdProvider, i -> i));

            this.oldValue = new LinkedHashSet<>();
            for (int i = 0; i < _oldItemsSelected.length(); i++) {
                oldValue.add(items.get((int) _oldItemsSelected.getObject(i).getNumber("id")));
            }
            this.value = new LinkedHashSet<>();
            for (int i = 0; i < _newItemsSelected.length(); i++) {
                value.add(items.get((int) _newItemsSelected.getObject(i).getNumber("id")));
            }
        }

        public SelectionEvent(Tree<T> source, Set<T> _oldItemsSelected, Set<T> _newItemsSelected) {
            super(source, false);
            this.oldValue = Collections.unmodifiableSet(_oldItemsSelected);
            this.value = Collections.unmodifiableSet(_newItemsSelected);
        }

        public Optional<T> getFirstSelectedItem() {
            return this.getValue().stream().findFirst();
        }

        public Set<T> getAllSelectedItems() {
            return this.getValue();
        }

        public Set<T> getValue() {
            return Collections.unmodifiableSet(value);
        }

        public Set<T> getOldSelection() {
            return Collections.unmodifiableSet(oldValue);
        }

        public Set<T> getRemovedSelection() {
            Set<T> copy = new LinkedHashSet<>(oldValue);
            copy.removeAll(value);
            return copy;
        }

        public Set<T> getAddedSelection() {
            Set<T> copy = new LinkedHashSet<>(value);
            copy.removeAll(oldValue);
            return copy;
        }
    }
}
