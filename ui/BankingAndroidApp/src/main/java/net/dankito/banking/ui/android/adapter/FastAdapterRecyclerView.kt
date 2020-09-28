package net.dankito.banking.ui.android.adapter

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericItem
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter.Companion.items
import com.mikepenz.fastadapter.drag.ItemTouchCallback
import com.mikepenz.fastadapter.drag.SimpleDragCallback
import com.mikepenz.fastadapter.select.getSelectExtension
import com.mikepenz.fastadapter.utils.DragDropUtil


open class FastAdapterRecyclerView<Item : GenericItem>(
    recyclerView: RecyclerView,
    items: List<Item> = listOf(),
    enableDragAndDrop: Boolean = false,
    open var itemDropped: ((oldPosition: Int, oldItem: Item, newPosition: Int, newItem: Item) -> Unit)? = null,
    open var onClickListener: ((Item) -> Unit)? = null
) : ItemTouchCallback {


    protected val fastAdapter: FastAdapter<Item>
    protected val itemAdapter: ItemAdapter<Item>

    private lateinit var touchCallback: SimpleDragCallback
    private lateinit var touchHelper: ItemTouchHelper


    init {
        itemAdapter = items()

        fastAdapter = FastAdapter.with(itemAdapter)

        init(recyclerView, items, enableDragAndDrop)
    }


    protected open fun init(recyclerView: RecyclerView, items: List<Item>, enableDragAndDrop: Boolean = true) {
        val selectExtension = fastAdapter.getSelectExtension()
        selectExtension.isSelectable = true

        fastAdapter.onClickListener = { _, _, item, _ ->
            onClickListener?.invoke(item)
            false
        }

        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = fastAdapter

        itemAdapter.set(items)


        if (enableDragAndDrop) {
            touchCallback = SimpleDragCallback(this)
            touchHelper = ItemTouchHelper(touchCallback)
            touchHelper.attachToRecyclerView(recyclerView)
        }
    }


    override fun itemTouchStartDrag(viewHolder: RecyclerView.ViewHolder) {
        // add visual highlight to dragged item
    }

    override fun itemTouchOnMove(oldPosition: Int, newPosition: Int): Boolean {
        DragDropUtil.onMove(itemAdapter, oldPosition, newPosition)  // change position
        return true
    }

    override fun itemTouchDropped(oldPosition: Int, newPosition: Int) {
        // remove visual highlight to dropped item

        itemDropped?.invoke(oldPosition, itemAdapter.getAdapterItem(oldPosition), newPosition, itemAdapter.getAdapterItem(newPosition))
    }

}