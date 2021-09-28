package com.aracoix.registeradapter

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.aracoix.register.RegisterAdapter
import com.aracoix.register.RegisterClickListener
import com.aracoix.registeradapter.bean.TypeABean
import com.aracoix.registeradapter.databinding.ActivityDemoBinding
import com.aracoix.registeradapter.viewholder.TypeAViewHolder

/**
 * @Description: 一对一
 * @author: Aracoix
 * @date:2021/9/28
 * @version:v1.0
 */
class One2OneActivity :AppCompatActivity(){
    val adapter = RegisterAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mBinding = ActivityDemoBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mBinding.rvList.layoutManager = LinearLayoutManager(this)
        adapter.registerTo(mBinding.rvList,this.lifecycle)
//        clazz = viewholder 的class
//        clickListener = 事件处理，如果已经在viewholder内部处理可不写
//        init = 初始化参数，适用于参数传递
//        adapter.register(clazz = TypeAViewHolder::class.java)
//        adapter.register(clazz = TypeAViewHolder::class.java,init = hashMapOf("xxx" to "xxx"))
//        adapter.register(clazz = TypeAViewHolder::class.java,clickListener = RegisterOnClick{ view, position ->
//
//        })
        adapter.register(clazz = TypeAViewHolder::class.java,clickListener = object :RegisterClickListener(){
            override fun onClick(view: View, position: Int) {
//                一个holder 有多个点击
//                when (view.id) {
//                    R.id.tvTypeA -> {
//                        //处理
//                    }
//                    else -> {
//                    }
//                }
//                一个holder 只有有一个点击 可以不考虑id
                val any = adapter.list[position]
                if (any is TypeABean) {
                    Toast.makeText(this@One2OneActivity,"点击更新文字",Toast.LENGTH_SHORT).show()
                    any.str += "+"
//                    adapter.refreshItem(any)
                    adapter.notifyItemRangeChanged(position, 1, any)
                }
            }

            override fun onLongClick(view: View, position: Int): Boolean {
                Toast.makeText(this@One2OneActivity,"长按删除",Toast.LENGTH_SHORT).show()
                adapter.removeData(adapter.list[position])
                return true
            }
        },init = hashMapOf("initPair" to "1"))

        val list = (0..100).map { TypeABean(str = "typeA $it") }
        adapter.loadData(list)
    }
}


