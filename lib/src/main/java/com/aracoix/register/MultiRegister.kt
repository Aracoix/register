package com.aracoix.register

/**
 * @date: 2021/4/9
 * @author: Aracoix
 * @Description: 通过data 来获取不同的ItemViewHolder
 * @version: v1.0
 */
public abstract class MultiRegister<T : Any> {

    abstract fun pickItem(position:Int,data: T): RegisterItem

}