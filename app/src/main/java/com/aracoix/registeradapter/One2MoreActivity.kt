package com.aracoix.registeradapter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.aracoix.register.MultiRegister
import com.aracoix.register.RegisterAdapter
import com.aracoix.register.RegisterItem
import com.aracoix.registeradapter.bean.TypeABean
import com.aracoix.registeradapter.bean.TypeBBean
import com.aracoix.registeradapter.databinding.ActivityDemoBinding
import com.aracoix.registeradapter.viewholder.TypeAViewHolder
import com.aracoix.registeradapter.viewholder.TypeBViewHolderOne
import com.aracoix.registeradapter.viewholder.TypeBViewHolderTwo

/**
 * @Description: Description
 * @author: Aracoix
 * @date:2021/9/28
 * @version:v1.0
 */
class One2MoreActivity:AppCompatActivity() {
    val adapter = RegisterAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mBinding = ActivityDemoBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mBinding.rvList.layoutManager = LinearLayoutManager(this)

        adapter.register(clazz = TypeAViewHolder::class.java)
        adapter.multiRegister(object :MultiRegister<TypeBBean>(){
            override fun pickItem(position: Int, data: TypeBBean): RegisterItem {
                return when(data.type){
                    1->RegisterItem(TypeBViewHolderOne::class.java)
                    else->RegisterItem(TypeBViewHolderTwo::class.java)
                }
            }

        })
        adapter.registerTo(mBinding.rvList,this.lifecycle)
        adapter.loadData(TypeABean("一对多类型注册"))

        val list = (0..100).map {
            TypeBBean(it%2)
        }
        adapter.loadData(list)
    }
}