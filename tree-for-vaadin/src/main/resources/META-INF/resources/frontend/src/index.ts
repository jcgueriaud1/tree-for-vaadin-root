import { XofTree } from './XofTree';
import { XofTreeItem } from './XofTreeItem';

window.customElements.define('xof-tree', XofTree);

declare global {
    interface HTMLElementTagNameMap {
        'xof-tree': XofTree;
    }
}

window.customElements.define('xof-tree-item', XofTreeItem);

declare global {
    interface HTMLElementTagNameMap {
        'xof-tree-item': XofTreeItem;
    }
}
