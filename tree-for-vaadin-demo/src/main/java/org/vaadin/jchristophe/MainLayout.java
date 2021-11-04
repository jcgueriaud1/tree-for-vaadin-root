package org.vaadin.jchristophe;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;

public class MainLayout extends AppLayout {

    public MainLayout() {
        final DrawerToggle drawerToggle = new DrawerToggle();
        final RouterLink simple = new RouterLink("Tree", SimpleView.class);
        final RouterLink file = new RouterLink("FileExplorerView", FileExplorerView.class);
        final RouterLink multiple = new RouterLink("MultipleSelectView", MultipleSelectView.class);
        final VerticalLayout menuLayout = new VerticalLayout(simple, file, multiple);
        addToDrawer(menuLayout);
        addToNavbar(drawerToggle);
    }

}