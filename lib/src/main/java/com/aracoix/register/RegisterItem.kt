package com.aracoix.register

import androidx.viewbinding.ViewBinding

/**
 * @date: 2021/4/9
 * @author: Aracoix
 * @Description: 注册类型存放的bean
 * @version: v1.0
 */
data class RegisterItem @JvmOverloads constructor(
    val viewHolder: Class<out BaseRegisterViewHolder<*, out ViewBinding>>,
    var registerClickListener: RegisterClickListener? = null,
    val initMap: Map<String, Any>? = null
)
