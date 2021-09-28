package com.aracoix.register

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

/**
 * @date: 2021/4/9
 * @author: Aracoix
 * @Description: 多类型注册基础ViewHolder
 * @version: v2.0 新增了生命周期  LifecycleOwner ,CoroutineScope,ViewModelStoreOwner
 */
@Keep
abstract class BaseRegisterViewHolder<T : Any, VB : ViewBinding>(override val mBinding: VB) :
    RecyclerView.ViewHolder(mBinding.root),
    ViewBindingLayoutContainer<VB>,
    LifecycleOwner,
    CoroutineScope,
    ViewModelStoreOwner {

    var clickListener: RegisterClickListener? = null

    /**
     * 参数map
     */
    lateinit var values: Map<String, Any>

    /**
     * 添加touch 事件
     */
    @SuppressLint("ClickableViewAccessibility")
    fun addOnTouchListener(viewId: Int) {
        clickListener?.let {
            itemView.findViewById<View>(viewId).setOnTouchListener { v, event ->
                it.onTouch(v, event, layoutPosition)
            }
        }
    }

    /**
     * 添加touch 事件
     */
    @SuppressLint("ClickableViewAccessibility")
    fun addOnTouchListener(view: View) {
        clickListener?.let {
            view.setOnTouchListener { v, event ->
                it.onTouch(v, event, layoutPosition)
            }
        }
    }

    /**
     * 添加点击事件
     */
    fun addOnClickListener(viewId: Int) {
        itemView.findViewById<View>(viewId)
            .setOnClickListener { clickListener?.onClick(it, layoutPosition) }
    }

    /**
     * 添加点击事件
     */
    fun addOnClickListener(view: View) {
        view.setOnClickListener { clickListener?.onClick(it, layoutPosition) }
    }

    /**
     * 添加点击事件 防止重复点击
     */
    fun addMultiClickListener(viewId: Int) {
        itemView.findViewById<View>(viewId)
            .multiClickListener { clickListener?.onClick(it, layoutPosition) }
    }

    /**
     * 添加点击事件 防止重复点击
     */
    fun addMultiClickListener(view: View) {
        view.multiClickListener { clickListener?.onClick(it, layoutPosition) }
    }

    /**
     * 空点击事件 防止重复点击
     */
    fun removeMultiClickListener(view: View) {
        view.multiClickListener {}
    }

    /**
     * 添加长按事件
     */
    fun addOnLongClickListener(viewId: Int) {
        clickListener?.let {
            itemView.findViewById<View>(viewId).setOnLongClickListener { v ->
                it.onLongClick(v, layoutPosition)
            }
        }
    }

    /**
     * 添加长按事件
     */
    fun addOnLongClickListener(view: View) {
        clickListener?.let { it ->
            view.setOnLongClickListener { v ->
                it.onLongClick(v, layoutPosition)
            }
        }
    }

    /**
     * 绑定事件
     */
    abstract fun bindData(data: T, payloads: List<Any> = arrayListOf())

    /**
     * 用于接触绑定事件并释放资源
     */
    open fun unBindData() {}

    fun onClear() {
        job.cancel()
        store.clear()
        lifeCycleRegister = null
    }

    internal var lifeCycleRegister: LifecycleRegistry? = null
    internal fun resetLifeCycle(clear: Boolean): LifecycleRegistry {
        if (clear) {
            lifeCycleRegister?.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            lifeCycleRegister?.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            lifeCycleRegister?.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            lifeCycleRegister = null
            job.cancel()
            store.clear()


            job = Job()
            store = ViewModelStore()
        }
        lifeCycleRegister = LifecycleRegistry(this)
        return lifeCycleRegister!!
    }

    override fun getLifecycle(): Lifecycle {
        if (lifeCycleRegister == null) {
            lifeCycleRegister = LifecycleRegistry(this)
        }
        return lifeCycleRegister!!
    }

    protected var job: Job = Job()
    protected var store = ViewModelStore()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    /**
     * 点击事件阻止重复点击 可能高版面没这个 actor 方法了Q……Q
     */
    fun View.multiClickListener(time: Long = 800, action: suspend (View) -> Unit) {
        val eventActor =
            GlobalScope.actor<View>(context = Dispatchers.Main, capacity = Channel.RENDEZVOUS) {
                for (event in channel) {
                    action(event)
                    delay(time)
                }
            }
        setOnClickListener {
            eventActor.offer(this)
        }
    }

    override fun getViewModelStore() = store
}

/**
 *holder add to viewGroup
 */
inline fun <T : Any, reified VB : ViewBinding>
        KClass<out BaseRegisterViewHolder<T, out VB>>.holder2View(
    parent: ViewGroup,
    data: T, map: HashMap<String, Any> = hashMapOf(),
    clickListener: RegisterClickListener? = null
): BaseRegisterViewHolder<T, VB> =
    this.java.holder2View(parent, data, map, clickListener)


inline fun <T : Any, reified VB : ViewBinding>
        Class<out BaseRegisterViewHolder<T, out VB>>.holder2View(
    parent: ViewGroup,
    data: T,
    map: HashMap<String, Any> = hashMapOf(),
    clickListener: RegisterClickListener? = null
): BaseRegisterViewHolder<T, VB> {
    val binding = VB::class.java
    val con = this.getConstructor(binding)
    val inflate = binding.getDeclaredMethod(
        "inflate",
        LayoutInflater::class.java,
        ViewGroup::class.java,
        Boolean::class.java
    )
    val viewBinding =
        inflate.invoke(null, LayoutInflater.from(parent.context), parent, true) as ViewBinding
    val baseRegisterViewHolder = con.newInstance(viewBinding) as BaseRegisterViewHolder<T, VB>
    baseRegisterViewHolder.values = map
    baseRegisterViewHolder.clickListener = clickListener
    baseRegisterViewHolder.bindData(data, arrayListOf())
    val lifecycleOwner =
        parent.findViewTreeLifecycleOwner() ?: if (parent.context is LifecycleOwner) parent.context as LifecycleOwner else null
    lifecycleOwner?.lifecycle?.observerEvent(Lifecycle.Event.ON_DESTROY) {
        val lifecycle = baseRegisterViewHolder.lifecycle
        if (lifecycle is LifecycleRegistry) {
            lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        }
        baseRegisterViewHolder.onClear()
    }
    return baseRegisterViewHolder
}

fun Lifecycle.observerEvent(vararg events: Lifecycle.Event, func: (Lifecycle.Event) -> Unit): LifecycleEventObserver {
    return LifecycleEventObserver { _, event ->
        if (event in events) {
            func.invoke(event)
        }
    }.apply {
        addObserver(this)
    }
}