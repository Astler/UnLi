package dev.astler.unlib.ui.fragment

import android.content.Context
import androidx.fragment.app.Fragment
import dev.astler.unli.interfaces.CoreFragmentInterface
import dev.astler.unlib.ui.interfaces.ActivityInterface
import dev.astler.unlib.utils.getStringResource

abstract class UnLibFragment(pLayoutId: Int = 0): Fragment(pLayoutId), CoreFragmentInterface {

    lateinit var coreListener: ActivityInterface

    override fun onAttach(context: Context) {
        super.onAttach(context)
        coreListener = context as ActivityInterface
    }

    override fun backPressed() {
        coreListener.backPressed()
    }

    override fun onResume() {

        super.onResume()
        coreListener.setCurrentFragment(this)
    }

    fun getStringByName(pName: String): String {
        return requireContext().getStringResource(pName)
    }
}
