package com.aracoix.registeradapter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aracoix.register.holder2View
import com.aracoix.registeradapter.bean.TypeABean
import com.aracoix.registeradapter.databinding.ActivityView2HolderBinding
import com.aracoix.registeradapter.viewholder.TypeAViewHolder

/**
 * @Description: 通过反射添加到view
 * @author: Aracoix
 * @date:2021/9/28
 * @version:v1.0
 */
class Holder2ViewActivity :AppCompatActivity(){


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mBinding = ActivityView2HolderBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        val holder2View =
            TypeAViewHolder::class.java.holder2View(
                mBinding.root,
                TypeABean("通过反射添加到view")
            )

    }
}