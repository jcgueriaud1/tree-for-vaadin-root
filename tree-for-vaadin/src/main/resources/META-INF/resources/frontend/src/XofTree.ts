import {
  css,
  LitElement,
  TemplateResult,
  property,
  queryAssignedNodes,
} from 'lit-element';
import { html, nothing } from 'lit-html';
import { XofTreeItem } from './XofTreeItem.js';

/**
 *
 * `<xof-tree>` is a Tree element to show hierarchical data as a tree.
 *
 *
 * ### Item rendering
 *
 * ```html
 * <xof-tree id="tree"></xof-tree>
 * ```
 * ```js
 * const tree = document.querySelector('#reee');
 * tree.data = [{"itemdata": "Branch1","children": [{"itemdata": "Branch1.1"}, {"itemdata": "Branch1.2"}]}];
 * ```
 *
 * The following properties are available for item template bindings:
 *
 * Property name | Type | Description
 * --------------|------|------------
 * `title`| String | Title of the tree
 * `data` | TreeItemDataArray | The tree data
 * `multiselect` | Boolean | True when the tree is in multiselect mode
 * `renderer` | TreeItemRenderer | renderer for the item
 *
 *
 * ```javascript
 * ```
 *
 * ### Styling
 *
 * The following custom properties are available for styling:
 *
 * Custom property | Description | Default
 * ----------------|-------------|-------------
 *
 * The following shadow DOM parts are available for styling:
 *
 * Part name | Description
 * ----------------|----------------
 *
 * The following state attributes are available for styling:
 *
 * Attribute    | Description | Part name
 * -------------|-------------|------------
 *
 *
 *
 * @element xof-tree
 *
 * @fires {CustomEvent<{old: HasId[], new:HasId[]>} item-selected - fires event
 * @fires {CustomEvent<HasId>} item-expanded
 *
 *
 * @slot items - To document, the tree item are added in the `items` slot
 *
 */
export class XofTree extends LitElement {
  static styles = css`
    ul,
    ul li,
    ul ul li {
      margin: 0;
      text-indent: 0;
      list-style-type: none;
    }
    .expander {
      content: ' ';
      display: inline-block;
      border-top: 5px solid transparent;
      border-bottom: 5px solid transparent;
      border-left: 5px solid currentColor;
      vertical-align: middle;
      margin-right: 0.7rem;
      transform: translateY(-2px);
      transition: transform 0.2s ease-out;
    }
    .leaf.expander {
      content: none;
    }
    .expanded.expander {
      transform: rotate(90deg) translateX(-3px);
    }
  `;

  /**
   * Title of the tree
   * @attr
   */
  @property({ type: String }) title = '';

  /**
   * The tree data
   * @type {TreeItemDataArray}
   * @prop
   */
  @property({ attribute: false }) data: TreeItemDataArray = [];

  /**
   * is tree in multi select?
   *
   * @attr
   */
  @property({ type: Boolean }) multiselect = false;

  /**
   * renderer for the item
   * @type {TreeItemRenderer<HasId>}
   * @prop
   */
  @property({ attribute: false })
  renderer: TreeItemRenderer<HasId> = (item: HasId) => html`${item.id}`;

  @property({ attribute: false }) focusedItem = null;

  @property({ attribute: false })
  public idsSelected: Array<Id> = [];

  @property({ attribute: false })
  public idsExpanded: Array<Id> = [];

  @queryAssignedNodes('items', true)
  private _itemNodes!: NodeListOf<XofTreeItem>;

  protected firstUpdated() {
    this.addEventListener('keydown', this.handleKeydown);
    this.addEventListener('__item-selected', this.handleItemSelected);
    this.addEventListener('__item-expanded', this.handleItemExpanded);
  }

  private handleItemSelected(e1: Event) {
    const e = e1 as CustomEvent<{ selected: boolean; item: XofTreeItem }>;
    const _oldItemsSelected = [...this.idsSelected];
    if (e.detail.selected) {
      this.selectId(e.detail.item.itemdata!.id);
    } else {
      this.deselectId(e.detail.item.itemdata!.id);
    }

    const event = new CustomEvent('item-selected', {
      detail: { old: _oldItemsSelected, new: this.idsSelected },
    });
    this.dispatchEvent(event);
  }

  private handleItemExpanded(e1: Event) {
    const e = e1 as CustomEvent<{ expanded: boolean; item: XofTreeItem }>;
    const _oldItemsExpanded = [...this.idsExpanded];

    if (e.detail.expanded) {
      this.expandId(e.detail.item.itemdata!.id);
    } else {
      this.collapseId(e.detail.item.itemdata!.id);
    }

    const event = new CustomEvent('item-expanded', {
      detail: { old: _oldItemsExpanded, new: this.idsExpanded },
    });
    this.dispatchEvent(event);
  }

  leaf() {
    return !(this.data && this.data.length > 0);
  }

  handleKeydown(event: KeyboardEvent) {
    event.preventDefault();
    const treeitem: XofTreeItem = event
        .composedPath()
        .find(
            p => (p as HTMLElement).nodeName === 'XOF-TREE-ITEM'
        ) as XofTreeItem;
    treeitem.tabIndex = -1;
    switch (event.key) {
        // Tab.
      case 'Down': // IE/Edge specific value
      case 'ArrowDown':
        //target = this.setNextItem(target);
        if (treeitem.expanded) {
          this.navigateToFirstChildItem(treeitem);
        } else {
          this.navigateToNextItem(treeitem);
        }
        break;
      case 'Up': // IE/Edge specific value
      case 'ArrowUp':
        this.navigateToPreviousItem(treeitem);
        break;
      case 'Right': // IE/Edge specific value
      case 'ArrowRight':
        if (treeitem.leaf) {
          this.navigateToNextItem(treeitem);
        } else {
          if (!treeitem.expanded && !treeitem.leaf) {
            this._loadTreeItem(treeitem);
          } else {
            // go to next item
            this.navigateToFirstChildItem(treeitem);
          }
        }
        break;
      case 'Left': // IE/Edge specific value
      case 'ArrowLeft':
        if (treeitem.expanded) {
          this.collapseId(treeitem.itemdata!.id);
        } else {
          // go to next item
          this.navigateToParentItem(treeitem);
        }
        break;

      case ' ':
        if (this.multiselect) {
          treeitem.toggleSelection();
        }
        break;
      default:
        break;
    }
    this.requestUpdate();
  }

  private _loadTreeItem(treeitem: XofTreeItem) {
    if (treeitem.childElementCount == 0) {
      this.expandId(treeitem.itemdata!.id);
    }
  }

  navigateToNextItem(treeitem: XofTreeItem) {
    const nextElement = treeitem.nextElementSibling as XofTreeItem;

    if (nextElement) {
      nextElement.focus();
    } else {
      const parentElement = treeitem.parentElement!.closest('xof-tree-item');
      if (parentElement) {
        this.navigateToNextItem(parentElement as XofTreeItem);
      }
    }
  }

  navigateToPreviousItem(treeitem: XofTreeItem) {
    const previousElement = treeitem.previousElementSibling as XofTreeItem;
    if (previousElement) {
      if (previousElement.expanded) {
        previousElement.focusLastChild();
      } else {
        previousElement.focus();
      }
    } else {
      const parentElement = treeitem.parentElement!.closest('xof-tree-item');
      if (parentElement) {
        (parentElement as XofTreeItem).focus();
      }
    }
  }

  // last Element Child
  navigateToFirstChildItem(treeitem: XofTreeItem) {
    treeitem.focusFirstChild();
  }

  navigateToParentItem(treeitem: XofTreeItem) {
    const parentElement = treeitem.parentElement!.closest('xof-tree-item');
    if (parentElement) {
      (parentElement as XofTreeItem).focus();
    }
  }

  _isleaf(item: TreeItemData) {
    return !(item.children && item.children.length > 0);
  }

  /**
   * Collapse all the nodes
   */
  public collapseAll() {
    this.idsExpanded = [];
  }

  /**
   * Expand all the nodes
   */
  public expandAll() {
    this.idsExpanded = this.__getAllFolderId(this.data);
  }

  /**
   * Collapse the item
   *
   * @param item item to collapse
   */
  public collapseItem(item: HasId) {
    this.collapseId(item.id);
  }

  /**
   * Collapse the item id
   *
   * @param id id to collapse
   */
  public collapseId(id: Id) {
    this.idsExpanded = this.__expandOrCollapseIf(this.idsExpanded, false, id);
  }

  /**
   * expand the item
   *
   * @param item item to expand
   */
  public expandItem(item: HasId) {
    this.expandId(item.id);
  }
  /**
   * expand the item id
   *
   * @param id id to expand
   */
  public expandId(id: Id) {
    this.idsExpanded = this.__expandOrCollapseIf(this.idsExpanded, true, id);
  }

  private __expandOrCollapseIf(itemsExpanded: Id[], expanded: boolean, id: Id) {
    if (expanded) {
      return [...itemsExpanded, id];
    } else {
      return itemsExpanded.filter(tid => tid !== id);
    }
  }

  public selectAll() {
    this.idsSelected = this.__getAllIds(this.data);
  }

  public deselectAll() {
    this.idsSelected = [];
  }

  /**
   * select the item
   *
   * @param item item to select
   */
  public selectItem(item: HasId) {
    this.selectId(item.id);
  }
  /**
   * select the item id
   *
   * @param id id to select
   */
  public selectId(id: Id) {
    this.idsSelected = this.__selectOrDeselectIf(this.idsSelected, true, id);
  }

  /**
   * deselect the item
   *
   * @param item item to deselect
   */
  public deselectItem(item: HasId) {
    this.deselectId(item.id);
  }
  /**
   * deselect the item id
   *
   * @param id id to deselect
   */
  public deselectId(id: Id) {
    this.idsSelected = this.__selectOrDeselectIf(this.idsSelected, false, id);
  }

  private __selectOrDeselectIf(itemsExpanded: Id[], selected: boolean, id: Id) {
    if (selected) {
      return [...itemsExpanded, id];
    } else {
      return itemsExpanded.filter(tid => tid !== id);
    }
  }

  isExpanded(item: HasId) {
    return this.idsExpanded.includes(item.id);
  }

  isSelected(item: HasId) {
    return this.idsSelected.includes(item.id);
  }

  private __getAllFolderId(data: TreeItemDataArray) {
    const folderIds: Id[] = [];
    data.forEach(item => {
      if (!this._isleaf(item)) {
        folderIds.push(item.itemdata.id);
        folderIds.push(...this.__getAllFolderId(item.children!));
      }
    });
    return folderIds;
  }

  private __getAllIds(data: TreeItemDataArray) {
    const folderIds: Id[] = [];
    data.forEach(item => {
      folderIds.push(item.itemdata.id);
      if (!this._isleaf(item)) {
        folderIds.push(...this.__getAllIds(item.children!));
      }
    });
    return folderIds;
  }

  renderItems(
      items: TreeItemDataArray,
      level: number,
      multiselect: boolean
  ): TemplateResult {
    if (items) {
      return html`${items.map(item => {
        return html`<xof-tree-item
            .itemdata=${item.itemdata}
            .item=${item}
            ?expanded=${this.isExpanded(item.itemdata)}
            ?selected=${this.isSelected(item.itemdata)}
            ?leaf=${this._isleaf(item)}
            ?multiselect=${multiselect}
            .renderer=${this.renderer}
        >
          ${this._isleaf(item) || !this.isExpanded(item.itemdata)
              ? nothing
              : html`<div slot="items">
                ${this.renderItems(item.children!, level + 1, multiselect)}
              </div>`}
        </xof-tree-item>`;
      })}`;
    } else {
      return html``;
    }
  }

  render() {
    return html`
      <h3 id="tree_label">${this.title}</h3>
      ${this.leaf()
          ? nothing
          : html`<ul role="tree" id="tree" aria-labelledby="tree_label">
            <slot name="items">
              ${this.renderItems(this.data, 0, this.multiselect)}
            </slot>
          </ul>`}
    `;
  }

  public addEventListener<T extends keyof XofTreeEventMap>(
      type: T,
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      listener: (this: XofTree, ev: XofTreeEventMap[T]) => any,
      options?: boolean | AddEventListenerOptions
  ): void;

  public addEventListener(
      type: string,
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      listener: (this: XofTree, ev: Event) => any,
      options?: boolean | AddEventListenerOptions
  ): void {
    super.addEventListener(type, listener, options);
  }

  public removeEventListener<K extends keyof XofTreeEventMap>(
      type: K,
      listener: (this: XofTree, ev: XofTreeEventMap[K]) => void,
      options?: boolean | EventListenerOptions
  ): void;

  public removeEventListener(
      type: string,
      listener: (this: XofTree, ev: Event) => void,
      options?: boolean | EventListenerOptions
  ): void {
    super.removeEventListener(type, listener, options);
  }
}

// TYPES
export declare interface TreeItemData {
  itemdata: HasId;
  children?: TreeItemDataArray;
}
export declare type TreeItemDataArray = Array<TreeItemData>;

export declare interface HasItemData {
  itemdata?: HasId;
}

export declare interface HasId {
  id: Id;
}
export declare type Id = string | number;
/**
 * Fired when an item is expanded or collapsed.
 */
export declare type XofTreeItemExpanded<T> = CustomEvent<{
  old: T[];
  new: T[];
}>;

/**
 * Fired when an item is selected or deselected.
 */
export declare type XofTreeItemSelected<T> = CustomEvent<{
  old: T[];
  new: T[];
}>;

export declare interface XofTreeElementEventMap {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  'item-expanded': XofTreeItemExpanded<any>;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  'item-selected': XofTreeItemSelected<any>;
  /** Intenal events */
  '__item-selected': CustomEvent<{ selected: boolean; item: HasItemData }>;
  '__item-expanded': CustomEvent<{ expanded: boolean; item: HasItemData }>;
}
export declare type TreeItemRenderer<T> = (item: T) => TemplateResult;

export declare interface XofTreeEventMap
    extends HTMLElementEventMap,
        XofTreeElementEventMap {}
