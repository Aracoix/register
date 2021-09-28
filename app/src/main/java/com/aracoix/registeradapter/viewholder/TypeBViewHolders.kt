package com.aracoix.registeradapter.viewholder

import android.animation.ObjectAnimator
import android.widget.Toast
import com.aracoix.register.BaseRegisterViewHolder
import com.aracoix.registeradapter.bean.TypeBBean
import com.aracoix.registeradapter.databinding.ItemTypeBOneBinding
import com.aracoix.registeradapter.databinding.ItemTypeBTwoBinding

/**
 * @Description: 一对多viewholder
 * @author: Aracoix
 * @date:2021/9/28
 * @version:v1.0
 */

class TypeBViewHolderOne(override val mBinding: ItemTypeBOneBinding)
    : BaseRegisterViewHolder<TypeBBean, ItemTypeBOneBinding>(mBinding){
    override fun bindData(data: TypeBBean, payloads: List<Any>) {
        mBinding.root.multiClickListener {
           val ofFloat = ObjectAnimator.ofFloat(it, "alpha", 1f, 0f, 1f)
            ofFloat.duration = 800L
            ofFloat.start()
        }
    }
}
class TypeBViewHolderTwo(override val mBinding: ItemTypeBTwoBinding)
    : BaseRegisterViewHolder<TypeBBean, ItemTypeBTwoBinding>(mBinding){
    override fun bindData(data: TypeBBean, payloads: List<Any>) {
        mBinding.root.multiClickListener {
            Toast.makeText(it.context,"TypeBViewHolderTwo",Toast.LENGTH_SHORT).show()
        }
    }
}