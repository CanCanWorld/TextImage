package com.zrq.textimage

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import com.zrq.textimage.databinding.DialogLoadingBinding
import java.util.*

class LoadingDialog(context: Context) : Dialog(context, R.style.loading_dialog) {

    private lateinit var mBinding: DialogLoadingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DialogLoadingBinding.inflate(LayoutInflater.from(context))
        setContentView(mBinding.root)
        initData()
        Timer().schedule(MyTimeTask(), 30000)
    }

    private fun initData() {
        mBinding.apply {
            // 加载动画
            val hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
                context, R.anim.loading_animation
            )
            // 使用ImageView显示动画
            ivLoading.startAnimation(hyperspaceJumpAnimation)
            setCanceledOnTouchOutside(false)

            window!!.attributes.gravity = Gravity.CENTER//居中显示
            window!!.attributes.dimAmount = 0.5f//背景透明度  取值范围 0 ~ 1
        }
    }

    private inner class MyTimeTask : TimerTask() {
        override fun run() {
            dismiss()
        }
    }
}