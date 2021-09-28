package com.aracoix.register

import android.util.Log
import androidx.collection.SparseArrayCompat
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * 获取DataBinding
 */
internal interface ViewBindingLayoutContainer<T : ViewBinding> {
    public val mBinding: T?
}

/**
 * holder 数据处理
 */
internal interface IRegisterAction {
    /**
     * 获取viewType 通过类型
     */
    fun getBindingHashCodeByViewType(position: Int, any: Any): Int

    /**
     * 通过ViewBinding 的 hashCode 获取 viewHolder
     */
    fun getViewHolderByBindingHashCode(type: Int): Class<out BaseRegisterViewHolder<*, out ViewBinding>>

    /**
     * 设置click 与viewHolder 绑定
     */
    fun bindClickWithViewHolder(holder: BaseRegisterViewHolder<*, out ViewBinding>): RegisterClickListener?

}

/**
 * 反射失败
 */
internal class HolderInitException(viewHolderName: String, cause: Throwable? = null) : RuntimeException("$viewHolderName 反射初始化失败", cause)

/**
 * 未注册viewholder 时触发
 */
internal class NotRegisterException(className: String, cause: Throwable? = null) : RuntimeException("$className 类型未注册", cause)

/**
 * 注册数据管理
 */
internal class RegisterManager : IRegister, IRegisterAction {

    private var holderIdMap = ConcurrentHashMap<String, Int>()

    /**
     * 用于存放 ViewBinding 的 holderid 和 viewholder
     */
    private var viewHolderMap = SparseArrayCompat<Class<out BaseRegisterViewHolder<*, out ViewBinding>>>()

    /**
     * 用于存放 Bean 的 holderid 和 bean
     */
    private var modelTypeMap = SparseArrayCompat<Class<*>>()


    /**
     * 用于存放 ViewBinding 的 hashCode 和 click
     */
    private var clickListenerMap = SparseArrayCompat<RegisterClickListener?>()

    /**
     * 用于存放初始化数据
     */
    private var initMap = SparseArrayCompat<Map<String, Any>>()

    /**
     * 用户存放bean viewholder 一对多
     */
    private var multiModelMap = mutableMapOf<Class<*>, MultiRegister<Any>>()

    /**
     * 用户存放ViewBinding 的 holderid viewholder 一对多
     */
    private var multiViewHolderMap = SparseArrayCompat<Class<out BaseRegisterViewHolder<*, out ViewBinding>>>()

    val increasor: AtomicInteger = AtomicInteger(0)

    fun genHolderId(viewHolder: Class<out BaseRegisterViewHolder<*, out ViewBinding>>): Int {
        val holderInfoStr = evalHolderIdKey(viewHolder)
        var holderId = holderIdMap[holderInfoStr]
        if (holderId == null) {
            val id = increasor.getAndIncrement()
            holderIdMap[holderInfoStr] = id
            holderId = id
        }
        return holderId
    }

    private fun evalHolderIdKey(viewHolder: Class<out BaseRegisterViewHolder<*, out ViewBinding>>): String {
        val holderInfoStr = reflectDataBinding(viewHolder).toString() + "-" + reflectClass(viewHolder).toString()
        return holderInfoStr
    }

    /**
     * 数据存储
     */
    override fun register(registerItem: RegisterItem) {
        val model = reflectClass(registerItem.viewHolder)
        // 如果有就删除
        if (modelTypeMap.indexOfValue(model) != -1) {
            val index = modelTypeMap.indexOfValue(model)
            holderIdMap.remove(evalHolderIdKey(registerItem.viewHolder))
            viewHolderMap.removeAt(index)
            modelTypeMap.removeAt(index)
            clickListenerMap.removeAt(index)
            initMap.removeAt(index)
        }

        val holderId: Int = genHolderId(registerItem.viewHolder)// reflectDataBinding(registerItem.viewHolder).hashCode() + model.hashCode()
        viewHolderMap.put(holderId, registerItem.viewHolder)
        modelTypeMap.put(holderId, model)
        clickListenerMap.put(holderId, registerItem.registerClickListener)
        initMap.put(holderId, registerItem.initMap)
    }

    override fun register(
        clazz: Class<out BaseRegisterViewHolder<*, out ViewBinding>>,
        clickListener: RegisterClickListener?,
        init: Map<String, Any>?
    ) {
        register(RegisterItem(clazz, clickListener, init))
    }

    /**
     * 注册同bean不同item
     */
    override fun multiRegister(register: MultiRegister<*>) {
        multiModelMap[reflectClass(register::class.java)] = register as MultiRegister<Any>
    }

    /**
     * 通过类型获取ViewBinding 的 hashCode
     */
    override fun getBindingHashCodeByViewType(position: Int, any: Any): Int {
        val clazz = any.javaClass
        val index = modelTypeMap.indexOfValue(clazz)

        if (index == -1) {
            val multiClazz = multiModelMap.containsKey(clazz)
            if (multiClazz) {
                val multiLink = multiModelMap[clazz]

                multiLink?.let {
                    val link = it.pickItem(position, any)
                    val holderId = holderIdMap[evalHolderIdKey(link.viewHolder)]
                        ?: genHolderId(link.viewHolder)// reflectDataBinding(link.viewHolder).hashCode() + clazz.hashCode()
                    if (multiViewHolderMap.get(holderId) == null) {
                        multiViewHolderMap.put(holderId, link.viewHolder)
                        clickListenerMap.put(holderId, link.registerClickListener)
                        initMap.put(holderId, link.initMap)
                    }
                    return holderId
                }
            } else {
                Log.e("类型未注册", modelTypeMap.toString())
                throw NotRegisterException(clazz.simpleName as String)

            }
        }
        return modelTypeMap.keyAt(index)
    }

    /**
     * 通过ViewBinding 的 hashCode 获取viewholder
     */
    override fun getViewHolderByBindingHashCode(type: Int): Class<out BaseRegisterViewHolder<*, out ViewBinding>> {
        if (type == -1) {
            throw  NullPointerException("no such type!")
        }

        val viewHolder = viewHolderMap[type]
        val multiViewHolder = multiViewHolderMap[type]

        if (viewHolder != null) {
            return viewHolder
        }

        if (multiViewHolder != null) {
            return multiViewHolder
        }

        throw  NullPointerException("no such type!")
    }

    override fun bindClickWithViewHolder(holder: BaseRegisterViewHolder<*, out ViewBinding>): RegisterClickListener? {
        val clazz = holder.javaClass

        val viewIndex = viewHolderMap.indexOfValue(clazz)
        val multiIndex = multiViewHolderMap.indexOfValue(clazz)

        if (viewIndex != -1) {
            val viewLayout = viewHolderMap.keyAt(viewIndex)
            return clickListenerMap[viewLayout]
        }

        if (multiIndex != -1) {
            val multiLayout = multiViewHolderMap.keyAt(multiIndex)
            return clickListenerMap[multiLayout]
        }

        return null
    }


    fun getInitWithHolder(type: Int): Map<String, Any> {
        val valueMap = initMap[type] ?: return mutableMapOf()
        if (valueMap.isEmpty()) return mutableMapOf()
        return valueMap
    }

}

/**
 * 注册相关
 */
internal interface IRegister {

    /**
     * 注册item
     */
    fun register(registerItem: RegisterItem)

    /**
     * 注册item
     */
    fun register(
        clazz: Class<out BaseRegisterViewHolder<*, out ViewBinding>>,
        clickListener: RegisterClickListener? = null,
        init: Map<String, Any>? = null
    )

    /**
     * 注册不同的item 依赖于同一个Bean类型的
     */
    fun multiRegister(register: MultiRegister<*>)

}

internal val parameterTypeCache = HashMap<Class<*>, Array<Class<Any>>>()

internal fun getParameterizedType(clazz: Class<*>): Array<Class<Any>> {
    var arr = parameterTypeCache[clazz]
    if (arr == null) {
        var genericSuperclass = clazz.genericSuperclass
        var theClazz: Class<*>? = clazz
        while (genericSuperclass !is ParameterizedType && theClazz != null) {
            theClazz = theClazz.superclass
            genericSuperclass = theClazz?.genericSuperclass
        }
        if (genericSuperclass !is ParameterizedType) {
            throw RuntimeException("no ParameterizedType in ViewHolder")
        }
        val type = genericSuperclass
        val p = type.actualTypeArguments
        arr = p.map { it as Class<Any> }.toTypedArray()
        if (arr.size == 2) {
            parameterTypeCache[clazz] = arr
        }
    }
    return arr
}

internal fun reflectClass(clazz: Class<*>): Class<Any> = getParameterizedType(clazz)[0]

internal fun reflectDataBinding(clazz: Class<*>): Class<Any> = getParameterizedType(clazz)[1]