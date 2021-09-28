package com.aracoix.registeradapter.viewholder

import androidx.lifecycle.ViewModelProvider
import com.aracoix.register.BaseRegisterViewHolder
import com.aracoix.registeradapter.bean.MvvmBean
import com.aracoix.registeradapter.databinding.ItemMvvmBinding
import com.aracoix.registeradapter.viewmodel.MvvmViewModel

/**
 * @Description: Description
 * @author: Aracoix
 * @date:2021/9/28
 * @version:v1.0
 */
class MvvmViewHolder(override val mBinding: ItemMvvmBinding):
    BaseRegisterViewHolder<MvvmBean,ItemMvvmBinding>(mBinding) {
    override fun bindData(data: MvvmBean, payloads: List<Any>) {
        val model = ViewModelProvider(this)[MvvmViewModel::class.java]
        model.setInitCount(data.num)
        mBinding.model = model
        mBinding.lifecycleOwner = this
    }
}