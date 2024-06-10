package pt.ipca.keystore.fragments.loginRegister

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import pt.ipca.keystore.R
import pt.ipca.keystore.databinding.FragmentIntroductionBinding

class IntroductionFragment: Fragment(R.layout.fragment_account_options) {
    private lateinit var bindind: FragmentIntroductionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
        return bindind.root
    }
}