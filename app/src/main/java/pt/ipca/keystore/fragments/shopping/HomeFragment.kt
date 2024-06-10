package pt.ipca.keystore.fragments.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import pt.ipca.keystore.R
import pt.ipca.keystore.adpters.HomeViewpagerAdapter
import pt.ipca.keystore.databinding.FragmentHomeBinding
import pt.ipca.keystore.fragments.categories.MainCategoryFragment
import pt.ipca.keystore.fragments.categories.NintendoFragment
import pt.ipca.keystore.fragments.categories.PcFragment
import pt.ipca.keystore.fragments.categories.PsFragment
import pt.ipca.keystore.fragments.categories.XboxFragment

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using the correct inflate method
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoriesFragments = arrayListOf<Fragment>(
            MainCategoryFragment(),
            PcFragment(),
            PsFragment(),
            NintendoFragment(),
            XboxFragment()
        )

        val viewpagerAdapter =
            HomeViewpagerAdapter(categoriesFragments,childFragmentManager,lifecycle)
        binding.viewpagerHome.adapter = viewpagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewpagerHome) { tab, position ->
            when(position) {
                0 -> tab.text = "Main"
                1 -> tab.text = "Pc"
                2 -> tab.text = "Ps"
                3 -> tab.text = "Xbox"
                4 -> tab.text = "Nintendo"

            }
        }.attach()

    }
}
