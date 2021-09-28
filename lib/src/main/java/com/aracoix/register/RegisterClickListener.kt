package com.aracoix.register

import android.view.MotionEvent
import android.view.View

/**
 * @date: 2021/4/9
 * @author: Aracoix
 * @Description: 多类型注册View 点击事件
 * @version: v1.0
 */
public abstract class RegisterClickListener {

    open fun onClick(view: View, position: Int) {}
    open fun onDataSet(data:Any, position: Int) {}

    open fun onTouch(view: View, event: MotionEvent, position: Int): Boolean {
        return false
    }

    open fun onLongClick(view: View, position: Int): Boolean {
        return false
    }
}


fun RegisterOnClick(click:(view: View, position: Int)->Unit) = object : RegisterClickListener(){
    override fun onClick(view: View, position: Int) {
        click.invoke(view, position)
    }
}
fun RegisterOnDataSet(dataSet:(data: Any, position: Int)->Unit) = object : RegisterClickListener(){
    override fun onDataSet(data: Any, position: Int) {
        dataSet.invoke(data, position)
    }
}

fun RegisterOnLongClick(click:(view: View, position: Int)->Boolean) = object :
    RegisterClickListener(){
    override fun onLongClick(view: View, position: Int):Boolean {
        return click.invoke(view, position)
    }
}
fun RegisterOnTouch(touch:(view: View, event: MotionEvent, position: Int)->Boolean) = object :
    RegisterClickListener(){
    override fun onTouch(view: View, event: MotionEvent, position: Int): Boolean {
        return touch.invoke(view,event, position)
    }
}