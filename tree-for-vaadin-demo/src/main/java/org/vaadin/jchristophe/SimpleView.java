package org.vaadin.jchristophe;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;
import elemental.json.Json;
import elemental.json.JsonObject;
import org.vaadin.jchristophe.bean.Person;
import org.vaadin.jchristophe.service.PersonData;

@Route(value = "", layout = MainLayout.class)
public class SimpleView extends Div {

    private PersonData personData = new PersonData();

    public SimpleView() {
        ValueProvider<Person, Integer> itemIdProvider = Person::getId;

        ValueProvider<Person, JsonObject> itemValueProvider = person -> {
            JsonObject object = Json.createObject();
            object.put("name", person.getLastName());
            return object;
        };
        Tree<Person> tree = new Tree<>(itemIdProvider, itemValueProvider);
        tree.setTitle("test");
        TreeData<Person> treeData = new TreeData<>();
        treeData.addRootItems(personData.getPersons());
        tree.setTreeData(treeData);
        add(tree);
    }

}
