package com.aracoix.registeradapter.viewmodel

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @Description: Description
 * @author: Aracoix
 * @date:2021/9/28
 * @version:v1.0
 */
class MvvmViewModel:ViewModel() {
    val countNum = MutableLiveData<String>("0")
    fun setInitCount(int: Int){
        countNum.postValue(int.toString())
    }
    fun add(view:View){
        countNum.postValue(((countNum.value?:"0").toInt()+1).toString())
    }

    fun subtract(view: View){
        countNum.postValue(((countNum.value?:"0").toInt()-1).toString())
    }

}