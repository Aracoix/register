package com.aracoix.registeradapter.viewholder

import android.annotation.SuppressLint
import com.aracoix.register.BaseRegisterViewHolder
import com.aracoix.registeradapter.bean.TypeABean
import com.aracoix.registeradapter.databinding.ItemTypeABinding

/**
 * 基础文字类型
 */
class TypeAViewHolder(override val mBinding: ItemTypeABinding)
    : BaseRegisterViewHolder<TypeABean, ItemTypeABinding>(mBinding){
    @SuppressLint("SetTextI18n")
    override fun bindData(data: TypeABean, payloads: List<Any>) {
        val initPair = values["initPair"] // "1"
        mBinding.tvTypeA.text = initPair.toString() + data.str
        //将点击事件交给外部处理，适用于同view不同页面不同点击事件
        addOnClickListener(mBinding.tvTypeA)

        //同addOnClickListener 增加了防抖策略
//        addMultiClickListener(mBinding.tvTypeA)
        //长按事件
        addOnLongClickListener(mBinding.tvTypeA)
        // touch 事件
//        addOnTouchListener(mBinding.tvTypeA)

        // 适用于只传递数据，例如滑动进度条将进度传递给外部
        clickListener?.onDataSet("任意类型",layoutPosition)

        // holder 内部处理点击事件，适用于view相同点击事件相同
//        mBinding.tvTypeA.setOnClickListener {  }
        // 同点击事件， 增加了防抖策略 默认800ms
//        mBinding.tvTypeA.multiClickListener {  }
//        mBinding.tvTypeA.multiClickListener(3000L) {  }
    }
}