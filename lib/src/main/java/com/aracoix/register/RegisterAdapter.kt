package com.aracoix.register

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

/**
 * @date: 2021/4/9
 * @author: Aracoix
 * @Description: 多类型注册Adapter 使用限制  bean + (viewHolder + ViewBinding) 为一个主键
 * @version: v1.0
 */

/**
 *  使用示例
 *  val registerAdapter = RegisterAdapter()
 *  val recyclerView = RecyclerView(requireContext())
 *   recyclerView.layoutManager = LinearLayoutManager(requireContext())
 *   registerAdapter.apply {
 *        注册itemViewHolder
 *       register(TypeAViewHolder::class.java,object :RegisterClickListener(){
 *               override fun onClick(view: View, position: Int) {
 *                  //DO view click
 *                  item 点击事件
 *               }
 *       })
 *       一对多方式注册
 *      multiRegister(object :MultiRegister<TypeB>(){
 *          override fun pickItem(data: TypeB,position:Int): RegisterItem {
 *              return  when (data.type) {
 *                      1 -> RegisterItem(TypeB1ViewHolder::class.java)
 *                      else -> RegisterItem(TypeB2ViewHolder::class.java)
 *                  }
 *      }
 *      })
 *      registerTo(recyclerView)
 *   }
 *   for (i in 1..10) {
 *      registerAdapter.loadData(TypeA(data = "data $i"))
 *      registerAdapter.loadData(TypeB(type = i%2))
 *   }
 *
 *   ………………………………
 *   class TypeA(val data:String)
 *   class TypeB(val type:Int)
 *
 *
 *   class TypeAViewHolder(override val mBinding: ItemRegisterTestTypeABinding): BaseRegisterViewHolder<TypeA,ItemRegisterTestTypeABinding>(mBinding){
 *      override fun bindData(data: TypeA, payloads: List<Any>) {
 *          mBinding.tvTypea.text = data.data
 *          addOnClickListener(mBinding.root)
 *      }
 *   }
 *
 *   class TypeB1ViewHolder(override val mBinding: ItemRegisterTestTypeB1Binding): BaseRegisterViewHolder<TypeB,ItemRegisterTestTypeB1Binding>(mBinding){
 *      override fun bindData(data: TypeB, payloads: List<Any>) {
 *          mBinding.tvTypeb1.text = this.javaClass.simpleName
 *          addOnClickListener(mBinding.tvTypeb1)
 *      }
 *
 *   }
 *
 *   class TypeB2ViewHolder(override val mBinding: ItemRegisterTestTypeB2Binding): BaseRegisterViewHolder<TypeB,ItemRegisterTestTypeB2Binding>(mBinding){
 *      override fun bindData(data: TypeB, payloads: List<Any>) {
 *          mBinding.tvTypeb2.text = this.javaClass.simpleName
 *      }
 *   }
 */
open class RegisterAdapter : RecyclerView.Adapter<BaseRegisterViewHolder<Any, out ViewBinding>>(),
    IRegister, IRegisterAction, CoroutineScope {
    companion object {
        internal const val TAG = "RegisterAdapter"
    }

    /**
     * 数据集合
     */
    val list: MutableList<Any> = mutableListOf()

    /**
     * 用户注册分发ViewHolder
     */
    private val linkManager: RegisterManager by lazy { RegisterManager() }

    /**
     * 用于存储viewHolder 初始化数据
     */
    private var allValuesInHolder = mapOf<String, Any>()

    /**
     * 设置RecycleView
     */
    var mRecycleView: RecyclerView? = null

    /**
     * 用于存放 存在的LifecycleRegistry
     */
    val lifecycleOwnerList = ConcurrentHashMap<LifecycleRegistry, BaseRegisterViewHolder<*, *>>()

    /**
     * view holder 自动创建
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseRegisterViewHolder<Any, out ViewBinding> {
        val viewHolderClass = getViewHolderByBindingHashCode(viewType)
        var baseRegisterViewHolder: BaseRegisterViewHolder<Any, out ViewBinding>? = null
        try {
            //通过反射 ViewBinding 的 inflate 方法 初始化ViewBinding
            val binding = reflectDataBinding(viewHolderClass)
            val con = viewHolderClass.getConstructor(binding)
            val inflate = binding.getDeclaredMethod(
                "inflate",
                LayoutInflater::class.java,
                ViewGroup::class.java,
                Boolean::class.java
            )
            val viewBinding = inflate.invoke(
                null,
                LayoutInflater.from(parent.context),
                parent,
                false
            ) as ViewBinding
            val map = allValuesInHolder.plus(linkManager.getInitWithHolder(viewType))
            baseRegisterViewHolder =
                con.newInstance(viewBinding) as BaseRegisterViewHolder<Any, out ViewBinding>
            baseRegisterViewHolder.values = map

        } catch (e: Exception) {
            e.printStackTrace()
            if (baseRegisterViewHolder == null) {
                throw HolderInitException(viewHolderClass.simpleName, e)
            }
        }
        return baseRegisterViewHolder!!
    }

    private fun addMap(key: LifecycleRegistry, value: BaseRegisterViewHolder<*, *>) {
        lifecycleOwnerList[key] = value
    }

    private fun removeMap(key: LifecycleRegistry) {
        lifecycleOwnerList.remove(key)
    }

    override fun onBindViewHolder(
        holder: BaseRegisterViewHolder<Any, out ViewBinding>,
        position: Int
    ) {
        val any = list[position]
        holder.clickListener = bindClickWithViewHolder(holder)
        Log.d(
            TAG,
            "onBindViewHolder:${holder.layoutPosition} ${holder.lifeCycleRegister?.currentState}"
        )
        addMap(holder.resetLifeCycle(true), holder)
        holder.lifeCycleRegister?.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        holder.lifeCycleRegister?.handleLifecycleEvent(Lifecycle.Event.ON_START)
        holder.bindData(any)
    }

    override fun onBindViewHolder(
        holder: BaseRegisterViewHolder<Any, out ViewBinding>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            val any = list[position]
            holder.clickListener = bindClickWithViewHolder(holder)
            holder.bindData(any, payloads)
        } else {
            onBindViewHolder(holder, position)
        }

    }

    /**
     * view 回收
     */
    override fun onViewRecycled(holder: BaseRegisterViewHolder<Any, out ViewBinding>) {
        holder.lifeCycleRegister?.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        holder.lifeCycleRegister?.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        holder.lifeCycleRegister?.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        val mBinding = holder.mBinding
        if (mBinding is ViewDataBinding) {
            mBinding.unbind()
        }
        holder.lifeCycleRegister?.apply { removeMap(this) }
        holder.onClear()
        holder.unBindData()
    }

    /**
     * 设置RecycleView 的Adapter
     */
    fun registerTo(view: RecyclerView) {
        var lifecycle = view.findViewTreeLifecycleOwner()?.lifecycle
        if (lifecycle==null&&view.context is LifecycleOwner) {
            lifecycle = (view.context as LifecycleOwner).lifecycle
        }
        if (lifecycle!=null){
            registerTo(view, lifecycle)
        }else{
            view.adapter = this
            mRecycleView = view
            Log.e(
                TAG,
                "registerTo: RecyclerView context not LifecycleOwner it may leak when use holder lifecycle"
            )
        }

    }

    /**
     * 设置RecycleView 的Adapter
     */
    fun registerTo(view: ViewPager2) {
        if (view.childCount!=0) {
            val view1 = view[0]
            if (view1 is RecyclerView) {
                mRecycleView = view1
            }
        }
        if (mRecycleView != null) {
            view.adapter = this
            setLifeCycle(mRecycleView!!, view.findViewTreeLifecycleOwner()?.lifecycle?:(view.context as LifecycleOwner).lifecycle)
        }else{
            view.adapter = this
        }


    }

    /**
     * 设置RecycleView 的Adapter
     */
    fun registerTo(view: RecyclerView, lifecycle: Lifecycle) {
        view.adapter = this
        mRecycleView = view
        setLifeCycle(view, lifecycle)
    }

    internal fun setLifeCycle(view: RecyclerView, lifecycle: Lifecycle){
        view.addOnChildAttachStateChangeListener(object :
            RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewDetachedFromWindow(v: View) {
            }

            override fun onChildViewAttachedToWindow(v: View) {
                val vh = view.findContainingViewHolder(v)
                if (vh is BaseRegisterViewHolder<*, *>) {
                    vh.lifeCycleRegister?.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
                }
            }
        })
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_DESTROY -> {
                        job.cancel()
                        lifecycleOwnerList.forEach {
                            it.key.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                            val mBinding = it.value.mBinding
                            if (mBinding is ViewDataBinding) {
                                mBinding.unbind()
                            }
                            it.value.onClear()
                            it.value.unBindData()
                        }
                        lifecycleOwnerList.clear()
                    }
                    Lifecycle.Event.ON_PAUSE -> {
                        lifecycleOwnerList.forEach {
                            it.key.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                        }
                    }
                    Lifecycle.Event.ON_STOP -> {
                        lifecycleOwnerList.forEach {
                            it.key.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
                        }
                    }
                    Lifecycle.Event.ON_RESUME -> {
                        lifecycleOwnerList.forEach {
                            it.key.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
                        }
                    }
                }
            }

        })
    }
    /**
     * 加载数据 自动向后添加
     */
    fun loadData(data: Any) {
        loadData(itemCount, data)
    }

    /**
     * 加载数据 从index 开始
     */
    fun loadData(index: Int, data: Any) {
        if (data is Iterable<*> || data is Array<*>) {
            val l = if (data is Iterable<*>) {
                data.toList()
            } else {
                (data as Array<*>).asList()
            }.filterNotNull()
            list.addAll(index, l as Collection<Any>)
            notifyItemRangeInserted(index, l.size)
        } else {
            list.add(index, data)
            notifyItemInserted(index)
        }
    }

    /**
     * 刷新item
     */
    fun refreshItem(data: Any) {
        val indexOf = list.indexOf(data)
        if (indexOf != -1) {
            if (mRecycleView?.isComputingLayout == true) {
                Handler(Looper.getMainLooper()).post {
                    notifyItemRangeChanged(indexOf, 1, data)
                }
            } else {
                notifyItemRangeChanged(indexOf, 1, data)
            }
        }
    }

    /**
     * 移除所有数据并刷新列表
     */
    fun removeAllData() {
        if (list.isNotEmpty()) {
            list.clear()
            notifyDataSetChanged()
        }
    }

    /**
     * 移除某个数据
     */
    fun removeData(data: Any) {
        if (list.size == 0) {
            return
        }
        val contains = list.contains(data)
        if (contains) {
            val index = list.indexOf(data)
            removeData(index)
        }
    }

    /**
     * 移除某个数据 按下标
     */
    fun removeData(position: Int) {
        if (list.size == 0) {
            return
        }
        if (position >= 0 && position <= list.size - 1) {
            if (mRecycleView?.isComputingLayout == true) {
                Handler(Looper.getMainLooper()).post {
                    list.removeAt(position)
                    notifyItemRemoved(position)
                }
            } else {
                list.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }

    /**
     * 从下标开始移除数据
     */
    fun removeDataFromIndex(index: Int) {
        val size = list.size
        list.subList(index, list.size).clear()
        notifyItemRangeRemoved(index, size)
    }

    /**
     * 替换某个数据
     */
    fun replaceData(position: Int, data: Any) {
        list.removeAt(position)
        list.add(position, data)
        notifyItemChanged(position)
    }

    /**
     * 替换后面的数据
     */
    fun replaceDataFrom(position: Int, data: List<Any>) {
        list.removeAt(position)
        list.addAll(position, data)
        notifyItemRangeChanged(position, data.size)
//        notifyItemChanged(position)
    }

    override fun getItemCount(): Int = list.size

    /**
     * view类型
     */
    override fun getItemViewType(position: Int): Int {
        val any = list[position]
        return getBindingHashCodeByViewType(position, any)
    }

    /**
     * 注册Item
     */
    override fun register(registerItem: RegisterItem) {
        linkManager.register(
            registerItem.viewHolder,
            registerItem.registerClickListener,
            registerItem.initMap
        )
    }

    /**
     * 注册Item
     */
    override fun register(
        clazz: Class<out BaseRegisterViewHolder<*, out ViewBinding>>,
        clickListener: RegisterClickListener?,
        init: Map<String, Any>?
    ) {
        linkManager.register(clazz, clickListener, init)
    }

    /**
     * 注册不同的Item
     */
    override fun multiRegister(register: MultiRegister<*>) {
        linkManager.multiRegister(register)
    }

    /**
     * 通过类型获取ViewBinding 的 hashCode
     */
    override fun getBindingHashCodeByViewType(position: Int, any: Any): Int =
        linkManager.getBindingHashCodeByViewType(position, any)


    /**
     * 通过ViewBinding 的 hashCode 获取 viewHolder
     */
    override fun getViewHolderByBindingHashCode(type: Int): Class<out BaseRegisterViewHolder<*, out ViewBinding>> =
        linkManager.getViewHolderByBindingHashCode(type)

    /**
     * 设置click 与viewHolder 绑定
     */
    override fun bindClickWithViewHolder(holder: BaseRegisterViewHolder<*, out ViewBinding>): RegisterClickListener? =
        linkManager.bindClickWithViewHolder(holder)


    fun getDataByPosition(position: Int): Any = list[position]
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

}

