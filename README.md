# RegisterAdapter 使用说明

声明

`val adapter = RegisterAdapter()`

`val recycleView`

`LoadingViewHolder.kt`

```kotlin
class LoadingBean
class LoadingViewHolder(override val mBinding: ItemLoadingViewHolderBinding)
:BaseRegisterViewHolder<LoadingBean, ItemLoadingViewHolderBinding>(mBinding) {
    override fun unBindData() {
        (mBinding.progress.drawable as AnimationDrawable).stop()
    }
    override fun bindData(data: LoadingBean, payloads: List<Any>) {
        (mBinding.progress.drawable as AnimationDrawable).start()
    }
}
```

`item_loading_view_holder.xml`

```xml
 <RelativeLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        tools:background="@color/white">

        <ImageView
            android:id="@+id/progress"
            android:layout_width="72dp"
            android:layout_height="27dp"
            android:layout_centerInParent="true"
            android:src="@drawable/anim_loading" />

    </RelativeLayout>
```

`FooterViewHolder.kt`

```kotlin
data class FooterBean(val desc: Int = R.string.common_list_no_more, val height: Int = 40.dp)
class FooterViewHolder(override val mBinding: ItemFooterViewHolderBinding) :
    BaseRegisterViewHolder<FooterBean, ItemFooterViewHolderBinding>(mBinding) {
    override fun bindData(data: FooterBean, payloads: List<Any>) {
        mBinding.loadingEndText.setText(data.desc)
        val layoutParams = mBinding.llFooter.layoutParams
        layoutParams.height = data.height
        mBinding.llFooter.layoutParams = layoutParams
        addMultiClickListener(mBinding.root)
    }
}
```

- 绑定adapter

```kotlin

recyclerView.layoutManager = LinearLayoutManager(requireContext())
adapter.register(LoadingViewHolder::class.java)
adapter.register(FooterViewHolder::class.java)
//一对多注册
adapter.multiRegister(object : MultiRegister<Recommend>() {
                override fun pickItem(position:Int,data: Recommend): RegisterItem {
                    return when (data.type) {
                        FEED_TYPE_RECOMMEND_CIRCLE -> RegisterItem(ItemFeedRecommendCircleViewHolder::class.java)
                        else -> RegisterItem(ItemFeedRecommendUserViewHolder::class.java)
                    }
                }
            })
adapter.registerTo(recyclerView)
adapter.loadData(LoadingBean())
adapter.loadData(FooterBean())

```

- 删除全部数据
  `adapter.removeAllData()`

- 移除某个数据
  ```kotlin
  
  adapter.removeData(data)
  adapter.removeData(index)
  
  ```

<br/>

- 将viewholder 添加到viewgroup 上

  ```kotlin
  LoadingViewHolder::class.holder2View(viewgroup,LoadingBean())
  ```
