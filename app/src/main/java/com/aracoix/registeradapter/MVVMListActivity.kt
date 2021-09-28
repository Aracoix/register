package com.aracoix.registeradapter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.aracoix.register.RegisterAdapter
import com.aracoix.registeradapter.bean.MvvmBean
import com.aracoix.registeradapter.bean.TypeABean
import com.aracoix.registeradapter.databinding.ActivityDemoBinding
import com.aracoix.registeradapter.viewholder.MvvmViewHolder

/**
 * @Description: 一对一
 * @author: Aracoix
 * @date:2021/9/28
 * @version:v1.0
 */
class MVVMListActivity :AppCompatActivity(){
    val adapter = RegisterAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mBinding = ActivityDemoBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mBinding.rvList.layoutManager = LinearLayoutManager(this)
        adapter.register(MvvmViewHolder::class.java)
        val list = (0..100).map { MvvmBean(it) }
        adapter.registerTo(mBinding.rvList,this.lifecycle)
        adapter.loadData(list)
    }
}


